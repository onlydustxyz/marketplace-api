package onlydust.com.marketplace.api.domain.port.input;

import java.util.Optional;
import onlydust.com.marketplace.api.domain.model.GithubAccount;

public interface GithubInstallationFacadePort {

  Optional<GithubAccount> getAccountByInstallationId(Long installationId);
}
