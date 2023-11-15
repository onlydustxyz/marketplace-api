package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.GithubAccount;
import onlydust.com.marketplace.api.domain.model.GithubRepo;

import java.util.Optional;

public interface GithubStoragePort {
    Optional<GithubAccount> findAccountByInstallationId(Long installationId);

    Optional<GithubRepo> findRepoById(Long repoId);
}
