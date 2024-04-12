package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.GithubIssueViewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface GithubIssueViewEntityRepository extends JpaRepository<GithubIssueViewEntity, Long> {
    @Query(value = """
            SELECT
                i.*
            FROM
                 indexer_exp.github_issues i
            JOIN indexer_exp.github_repos r ON i.repo_id = r.id
            JOIN project_github_repos pgr ON pgr.github_repo_id = r.id AND pgr.project_id = :projectId
            """, nativeQuery = true)
    Page<GithubIssueViewEntity> findProjectGoodFirstIssues(UUID projectId, Pageable pageable);
}
