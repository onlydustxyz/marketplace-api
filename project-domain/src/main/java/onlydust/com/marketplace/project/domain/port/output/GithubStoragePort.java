package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.GithubAccount;
import onlydust.com.marketplace.project.domain.model.GithubRepo;
import onlydust.com.marketplace.project.domain.view.GithubIssueView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GithubStoragePort {
    Optional<GithubAccount> findAccountByInstallationId(Long installationId);

    Optional<GithubRepo> findRepoById(Long repoId);

    List<GithubAccount> findInstalledAccountsByIds(List<Long> userGithubAccountIds);

    Page<GithubIssueView> findGoodFirstIssuesForProject(UUID projectId, Integer page, Integer pageSize);
}
