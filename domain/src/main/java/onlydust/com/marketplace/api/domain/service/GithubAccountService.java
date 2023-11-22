package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.GithubAccount;
import onlydust.com.marketplace.api.domain.model.GithubMembership;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.port.input.GithubInstallationFacadePort;
import onlydust.com.marketplace.api.domain.port.input.GithubOrganizationFacadePort;
import onlydust.com.marketplace.api.domain.port.output.GithubSearchPort;
import onlydust.com.marketplace.api.domain.port.output.GithubStoragePort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
public class GithubAccountService implements GithubInstallationFacadePort, GithubOrganizationFacadePort {

    private final GithubStoragePort githubStoragePort;
    private final GithubSearchPort githubSearchPort;

    @Override
    public Optional<GithubAccount> getAccountByInstallationId(Long installationId) {
        return githubStoragePort.findAccountByInstallationId(installationId);
    }

    public List<GithubAccount> getOrganizationsForAuthenticatedUserAndGithubPersonalToken(final String githubPersonalToken,
                                                                                          final User authenticatedUser) {
        final List<GithubAccount> userGithubAccounts =
                new ArrayList<>(githubSearchPort.searchOrganizationsByGithubPersonalToken(githubPersonalToken))
                        .stream()
                        .map(githubAccount -> githubAccount.toBuilder()
                                .isCurrentUserAdmin(githubSearchPort.getGithubUserMembershipForOrganization(githubPersonalToken
                                        , authenticatedUser.getLogin(), githubAccount.getLogin()).equals(GithubMembership.ADMIN))
                                .build()
                        )
                        .collect(Collectors.toList());
        userGithubAccounts.add(GithubAccount.builder()
                .id(authenticatedUser.getGithubUserId())
                .login(authenticatedUser.getLogin())
                .avatarUrl(authenticatedUser.getAvatarUrl())
                .isPersonal(true)
                .isCurrentUserAdmin(true)
                .installed(false)
                .build());

        final List<Long> userGithubAccountIds =
                userGithubAccounts.stream().map(GithubAccount::getId).collect(Collectors.toList());
        final List<GithubAccount> installedGithubAccounts =
                githubStoragePort.findInstalledAccountsByIds(userGithubAccountIds);
        if (installedGithubAccounts.isEmpty()) {
            return userGithubAccounts;
        } else {
            final List<GithubAccount> updatedUserGithubAccounts = new ArrayList<>();
            for (GithubAccount userGithubAccount : userGithubAccounts) {
                updatedUserGithubAccounts.add(installedGithubAccounts.stream()
                        .filter(githubAccount -> githubAccount.getId().equals(userGithubAccount.getId()))
                        .findFirst()
                        .map(githubAccount -> githubAccount.toBuilder()
                                .isPersonal(userGithubAccount.getIsPersonal())
                                .isCurrentUserAdmin(userGithubAccount.getIsCurrentUserAdmin())
                                .installed(true).build())
                        .orElse(userGithubAccount));
            }
            return updatedUserGithubAccounts;
        }
    }
}
