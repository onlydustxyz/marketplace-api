package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.GithubAccount;

import java.util.Optional;

public interface GithubInstallationFacadePort {
    Optional<GithubAccount> getAccountByInstallationId(Long installationId);
}
