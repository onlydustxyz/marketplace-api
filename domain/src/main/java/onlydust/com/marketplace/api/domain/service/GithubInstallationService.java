package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.GithubAccount;
import onlydust.com.marketplace.api.domain.port.input.GithubInstallationFacadePort;
import onlydust.com.marketplace.api.domain.port.input.GithubOrganizationFacadePort;
import onlydust.com.marketplace.api.domain.port.output.GithubSearchPort;
import onlydust.com.marketplace.api.domain.port.output.GithubStoragePort;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class GithubInstallationService implements GithubInstallationFacadePort, GithubOrganizationFacadePort {

    private final GithubStoragePort githubStoragePort;
    private final GithubSearchPort githubSearchPort;

    @Override
    public Optional<GithubAccount> getAccountByInstallationId(Long installationId) {
        return githubStoragePort.findAccountByInstallationId(installationId);
    }

    @Override
    public List<GithubAccount> getOrganizationsForGithubPersonalToken(String githubPersonalToken) {
        List<GithubAccount> userGithubAccounts =
                githubSearchPort.searchOrganizationsByGithubPersonalToken(githubPersonalToken);
        if (!userGithubAccounts.isEmpty()) {
            final List<GithubAccount> installedGithubAccounts = githubStoragePort.findAccountsByIds(userGithubAccounts);
            for (GithubAccount userGithubAccount : userGithubAccounts) {
                installedGithubAccounts.stream()
                        .filter(githubAccount -> githubAccount.getId() ==u)
            }
        }
        return null;
    }
}
