package onlydust.com.marketplace.api.github_api.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.model.GithubAccount;
import onlydust.com.marketplace.api.domain.model.GithubMembership;
import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.api.domain.port.output.GithubSearchPort;
import onlydust.com.marketplace.api.github_api.GithubHttpClient;
import onlydust.com.marketplace.api.github_api.dto.GetOrgaMembershipsResponseDTO;
import onlydust.com.marketplace.api.github_api.dto.GithubOrgaSearchResponseDTO;
import onlydust.com.marketplace.api.github_api.dto.GithubUserSearchResponse;
import onlydust.com.marketplace.api.github_api.properties.GithubPaginationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Objects.nonNull;

@AllArgsConstructor
public class GithubSearchApiAdapter implements GithubSearchPort {
    private final GithubHttpClient client;
    private final GithubPaginationProperties githubPaginationProperties;

    private static final Pattern ORG_API_URL_REGEX = Pattern.compile(
            "https://api\\.github\\.com/orgs/([^/]+)");

    @Override
    public List<GithubUserIdentity> searchUsersByLogin(final String login) {
        return client.get("/search/users?per_page=5&q=" + login, GithubUserSearchResponse.class)
                .map(GithubUserSearchResponse::getItems)
                .orElse(List.of())
                .stream().map(
                        githubUser -> GithubUserIdentity.builder()
                                .githubUserId(githubUser.getId())
                                .githubLogin(githubUser.getLogin())
                                .githubAvatarUrl(githubUser.getAvatarUrl())
                                .build()
                ).toList();
    }

    @Override
    public List<GithubAccount> searchOrganizationsByGithubPersonalToken(final String githubPersonalToken) {
        final int pageSize = githubPaginationProperties.getPageSize();
        Integer pageIndex = 1;
        List<GithubAccount> githubAccountsForPageIndex = getGithubAccountsForPageIndex(githubPersonalToken, pageSize,
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
            githubAccountsForPageIndex = getGithubAccountsForPageIndex(githubPersonalToken, pageSize, pageIndex);
            githubAccounts.addAll(githubAccountsForPageIndex);
        }
        return githubAccounts;
    }

    private List<GithubAccount> getGithubAccountsForPageIndex(String githubPersonalToken, final int pageSize,
                                                              Integer pageIndex) {
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
    public GithubMembership getGithubUserMembershipForOrganization(String githubPersonalToken, String userLogin,
                                                                   String organizationLogin) {
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
}
