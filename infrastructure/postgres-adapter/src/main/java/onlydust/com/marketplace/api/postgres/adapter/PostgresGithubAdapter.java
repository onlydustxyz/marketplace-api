package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubIssueViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.GithubAccountMapper;
import onlydust.com.marketplace.api.postgres.adapter.mapper.GithubRepoMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.GithubAppInstallationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.GithubIssueViewEntityRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.GithubRepoViewEntityRepository;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.GithubAccount;
import onlydust.com.marketplace.project.domain.model.GithubRepo;
import onlydust.com.marketplace.project.domain.port.output.GithubStoragePort;
import onlydust.com.marketplace.project.domain.view.GithubIssueView;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class PostgresGithubAdapter implements GithubStoragePort {

    private final GithubAppInstallationRepository githubAppInstallationRepository;
    private final GithubRepoViewEntityRepository githubRepoViewEntityRepository;
    private final GithubIssueViewEntityRepository githubIssueViewEntityRepository;

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
    @Transactional
    public Page<GithubIssueView> findGoodFirstIssuesForProject(UUID projectId, Integer pageIndex, Integer pageSize) {
        final var page = githubIssueViewEntityRepository.findProjectGoodFirstIssues(projectId, PageRequest.of(pageIndex, pageSize,
                Sort.by(Sort.Direction.DESC, "created_at")));

        return Page.<GithubIssueView>builder()
                .content(page.getContent().stream().map(GithubIssueViewEntity::toView).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }
}
