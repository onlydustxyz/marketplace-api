package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.project.domain.model.GithubAccount;
import onlydust.com.marketplace.project.domain.model.GithubAppInstallationStatus;
import onlydust.com.marketplace.project.domain.model.GithubMembership;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.port.input.GithubInstallationFacadePort;
import onlydust.com.marketplace.project.domain.port.input.GithubOrganizationFacadePort;
import onlydust.com.marketplace.project.domain.port.output.GithubSearchPort;
import onlydust.com.marketplace.project.domain.port.output.GithubStoragePort;

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

    @Override
    public List<GithubAccount> getOrganizationsForAuthenticatedUser(final User authenticatedUser) {
        final List<GithubAccount> userGithubAccounts =
                new ArrayList<>(githubSearchPort.searchOrganizationsByGithubUserId(authenticatedUser.getGithubUserId()))
                        .stream()
                        .map(githubAccount -> githubAccount.toBuilder()
                                .isCurrentUserAdmin(githubSearchPort.getGithubUserMembershipForOrganization(
                                        authenticatedUser.getGithubUserId(),
                                        authenticatedUser.getGithubLogin(),
                                        githubAccount.getLogin()
                                ).equals(GithubMembership.ADMIN))
                                .build()
                        )
                        .collect(Collectors.toList());
        userGithubAccounts.add(GithubAccount.builder()
                .id(authenticatedUser.getGithubUserId())
                .login(authenticatedUser.getGithubLogin())
                .avatarUrl(authenticatedUser.getGithubAvatarUrl())
                .isPersonal(true)
                .isCurrentUserAdmin(true)
                .installationStatus(GithubAppInstallationStatus.NOT_INSTALLED)
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
                                .installationStatus(githubAccount.getInstallationStatus()).build())
                        .orElse(userGithubAccount));
            }
            return updatedUserGithubAccounts;
        }
    }
}
