package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.GithubAccount;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.GithubRepo;

import java.util.List;
import java.util.Optional;

public interface GithubStoragePort {
    Optional<GithubAccount> findAccountByInstallationId(Long installationId);

    Optional<GithubRepo> findRepoById(Long repoId);

    List<GithubAccount> findInstalledAccountsByIds(List<Long> userGithubAccountIds);

    Optional<GithubIssue> findIssueById(GithubIssue.Id issueId);
}
