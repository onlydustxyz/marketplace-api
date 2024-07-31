package onlydust.com.marketplace.api.github_api.adapters;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.github_api.GithubHttpClient;
import onlydust.com.marketplace.api.github_api.dto.*;
import onlydust.com.marketplace.api.github_api.properties.GithubPaginationProperties;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.github.GithubUserIdentity;
import onlydust.com.marketplace.project.domain.model.GithubAccount;
import onlydust.com.marketplace.project.domain.model.GithubMembership;
import onlydust.com.marketplace.project.domain.port.output.GithubAuthenticationPort;
import onlydust.com.marketplace.project.domain.port.output.GithubSearchPort;
import onlydust.com.marketplace.user.domain.port.output.GithubUserStoragePort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;

@AllArgsConstructor
@Slf4j
public class GithubSearchApiAdapter implements GithubSearchPort, GithubUserStoragePort {
    private final GithubHttpClient client;
    private final GithubPaginationProperties githubPaginationProperties;
    private final GithubAuthenticationPort githubAuthenticationPort;

    private static final Pattern ORG_API_URL_REGEX = Pattern.compile(
            "https://api\\.github\\.com/orgs/([^/]+)");

    @Override
    public List<GithubUserIdentity> searchUsersByLogin(final String login) {
        return searchGithubUsersByLogin(login);
    }

    private List<GithubUserIdentity> searchGithubUsersByLogin(String login) {
        final var encodedLogin = encode(login.trim(), UTF_8);
        return client.get("/search/users?per_page=5&q=" + encodedLogin, GithubUserSearchResponse.class)
                .map(GithubUserSearchResponse::getItems)
                .orElse(List.of())
                .stream()
                .map(githubUser -> GithubUserIdentity.builder()
                        .githubUserId(githubUser.getId())
                        .login(githubUser.getLogin())
                        .avatarUrl(githubUser.getAvatarUrl())
                        .build()
                )
                .map(GithubUserIdentity.class::cast)
                .toList();
    }


    @Override
    public List<GithubUserIdentity> searchUsers(String login) {
        return searchGithubUsersByLogin(login);
    }

    @Override
    public List<GithubAccount> searchOrganizationsByGithubUserId(final Long githubUserId) {
        final int pageSize = githubPaginationProperties.getPageSize();
        int pageIndex = 1;
        List<GithubAccount> githubAccountsForPageIndex = getGithubAccountsForPageIndex(githubUserId, pageSize,
                pageIndex);
        if (githubAccountsForPageIndex.isEmpty()) {
            return githubAccountsForPageIndex;
        }
        if (githubAccountsForPageIndex.size() < pageSize) {
            return githubAccountsForPageIndex;
        }

        final List<GithubAccount> githubAccounts = new ArrayList<>(githubAccountsForPageIndex);
        while (githubAccountsForPageIndex.size() == pageSize) {
            pageIndex += 1;
            githubAccountsForPageIndex = getGithubAccountsForPageIndex(githubUserId, pageSize, pageIndex);
            githubAccounts.addAll(githubAccountsForPageIndex);
        }
        return githubAccounts;
    }

    private List<GithubAccount> getGithubAccountsForPageIndex(final Long githubUserId, final int pageSize,
                                                              Integer pageIndex) {
        final String githubPersonalToken = githubAuthenticationPort.getGithubPersonalToken(githubUserId);

        final Optional<GithubOrgaSearchResponseDTO[]> githubOrgaSearchResponseDTOS =
                client.get(String.format("/user/orgs?per_page=%s&page=%s", pageSize, pageIndex),
                        GithubOrgaSearchResponseDTO[].class, githubPersonalToken);
        if (githubOrgaSearchResponseDTOS.isEmpty() || githubOrgaSearchResponseDTOS.get().length == 0) {
            return List.of();
        }
        return Arrays.stream(githubOrgaSearchResponseDTOS.get()).map(githubOrgaSearchResponseDTO -> GithubAccount.builder()
                        .id(githubOrgaSearchResponseDTO.getId())
                        .login(githubOrgaSearchResponseDTO.getLogin())
                        .avatarUrl(githubOrgaSearchResponseDTO.getAvatarUrl())
                        .htmlUrl(getHtmlUrlFromApiUrl(githubOrgaSearchResponseDTO.getUrl()))
                        .build())
                .toList();
    }

    private static String getHtmlUrlFromApiUrl(String apiUrl) {
        final var matcher = ORG_API_URL_REGEX.matcher(apiUrl);
        if (!matcher.matches()) {
            throw OnlyDustException.internalServerError("Github API URL for organization could not be parsed: '%s'".formatted(apiUrl));
        }
        return "https://github.com/%s".formatted(matcher.group(1));
    }

    @Override
    public GithubMembership getGithubUserMembershipForOrganization(Long githubUserId, String userLogin,
                                                                   String organizationLogin) {
        final String githubPersonalToken = githubAuthenticationPort.getGithubPersonalToken(githubUserId);

        return client.get(String.format("/orgs/%s/memberships/%s", organizationLogin, userLogin),
                        GetOrgaMembershipsResponseDTO.class, githubPersonalToken)
                .filter(dto -> nonNull(dto.getRole()) && nonNull(dto.getState()))
                .map(dto -> {
                    if (dto.getRole().equals("admin") && dto.getState().equals("active")) {
                        return GithubMembership.ADMIN;
                    }
                    if (dto.getRole().equals("member") && dto.getState().equals("active")) {
                        return GithubMembership.MEMBER;
                    }
                    return GithubMembership.EXTERNAL;
                })
                .orElse(GithubMembership.EXTERNAL);
    }

    @Override
    public Optional<GithubUserIdentity> getUserProfile(Long githubUserId) {
        try {
            final var githubPersonalToken = githubAuthenticationPort.getGithubPersonalToken(githubUserId);

            return client.get("/user", GithubUser.class, githubPersonalToken)
                    .flatMap(userProfile -> client.get("/user/emails", GetUserEmailsResponseDTO[].class, githubPersonalToken)
                            .flatMap(githubUserEmails -> Arrays.stream(githubUserEmails).filter(GetUserEmailsResponseDTO::primary).findFirst())
                            .map(primaryEmail -> GithubUserIdentity.builder()
                                    .githubUserId(userProfile.getId())
                                    .login(userProfile.getLogin())
                                    .avatarUrl(userProfile.getAvatarUrl())
                                    .email(primaryEmail.email())
                                    .build())
                    );
        } catch (Exception e) {
            LOGGER.warn("Could not retrieve user profile for github user id {}", githubUserId, e);
            return Optional.empty();
        }
    }
}
