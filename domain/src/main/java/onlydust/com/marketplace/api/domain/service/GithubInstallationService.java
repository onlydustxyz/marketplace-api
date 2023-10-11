package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.GithubAccount;
import onlydust.com.marketplace.api.domain.port.input.GithubInstallationFacadePort;
import onlydust.com.marketplace.api.domain.port.output.GithubStoragePort;

import java.util.Optional;

@AllArgsConstructor
public class GithubInstallationService implements GithubInstallationFacadePort {

    private final GithubStoragePort githubStoragePort;

    @Override
    public Optional<GithubAccount> getAccountByInstallationId(Long installationId) {
        return githubStoragePort.findAccountByInstallationId(installationId);
    }
}
