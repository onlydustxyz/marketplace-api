package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAppInstallationViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubIssueViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.GithubAccountMapper;
import onlydust.com.marketplace.api.postgres.adapter.mapper.GithubRepoMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.GithubAppInstallationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.GithubIssueViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.GithubRepoViewEntityRepository;
import onlydust.com.marketplace.project.domain.model.GithubAccount;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.GithubRepo;
import onlydust.com.marketplace.project.domain.port.output.GithubStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

@AllArgsConstructor
public class PostgresGithubAdapter implements GithubStoragePort {

    private final GithubAppInstallationRepository githubAppInstallationRepository;
    private final GithubRepoViewEntityRepository githubRepoViewEntityRepository;
    private final GithubIssueViewRepository githubIssueViewRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<GithubAccount> findAccountByInstallationId(Long installationId) {
        return githubAppInstallationRepository.findById(installationId)
                .map(GithubAccountMapper::map);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GithubRepo> findRepoById(Long repoId) {
        return githubRepoViewEntityRepository.findById(repoId)
                .map(GithubRepoMapper::map);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GithubAccount> findInstalledAccountsByIds(List<Long> userGithubAccountIds) {
        return githubAppInstallationRepository.findAllByAccount_IdIn(userGithubAccountIds)
                .stream()
                .map(GithubAccountMapper::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GithubIssue> findIssueById(GithubIssue.Id issueId) {
        return githubIssueViewRepository.findById(issueId.value())
                .map(GithubIssueViewEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> findInstallationIdByRepoId(Long repoId) {
        return githubAppInstallationRepository.findByAuthorizedReposIdRepoId(repoId)
                .map(GithubAppInstallationViewEntity::getId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GithubIssue> findGoodFirstIssuesCreatedSince5Minutes() {
        return githubIssueViewRepository.findAllGoodFirstIssuesCreatedSince5Minutes(ZonedDateTime.now(), TimeZone.getDefault().getID())
                .stream()
                .map(GithubIssueViewEntity::toDomain)
                .toList();
    }
}
