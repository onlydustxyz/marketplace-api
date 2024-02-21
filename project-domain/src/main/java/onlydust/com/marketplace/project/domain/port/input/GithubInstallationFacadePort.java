package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.project.domain.model.GithubAccount;

import java.util.Optional;

public interface GithubInstallationFacadePort {
    Optional<GithubAccount> getAccountByInstallationId(Long installationId);
}
