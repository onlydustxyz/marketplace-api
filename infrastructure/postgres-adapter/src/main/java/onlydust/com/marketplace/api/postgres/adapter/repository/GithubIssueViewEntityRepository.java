package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubIssueViewEntity;
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
            JOIN project_github_repos pgr ON pgr.github_repo_id = r.id
            LEFT JOIN indexer_exp.github_issues_assignees gia ON gia.issue_id = i.id
            JOIN LATERAL (
                SELECT issue_id
                FROM indexer_exp.github_issues_labels gil
                JOIN indexer_exp.github_labels gl ON gil.label_id = gl.id
                WHERE
                    gil.issue_id = i.id AND
                    gl.name ilike '%good%first%issue%'
                LIMIT 1
            ) gfi ON gfi.issue_id = i.id
            WHERE
                i.status = 'OPEN' AND
                pgr.project_id = :projectId AND
                gia.user_id IS NULL
            """, nativeQuery = true)
    Page<GithubIssueViewEntity> findProjectGoodFirstIssues(UUID projectId, Pageable pageable);
}
