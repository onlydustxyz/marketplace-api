package onlydust.com.marketplace.api.domain.port.output;

import java.util.List;
import java.util.Optional;
import onlydust.com.marketplace.api.domain.model.GithubAccount;
import onlydust.com.marketplace.api.domain.model.GithubRepo;

public interface GithubStoragePort {

  Optional<GithubAccount> findAccountByInstallationId(Long installationId);

  Optional<GithubRepo> findRepoById(Long repoId);

  List<GithubAccount> findInstalledAccountsByIds(List<Long> userGithubAccountIds);
}
