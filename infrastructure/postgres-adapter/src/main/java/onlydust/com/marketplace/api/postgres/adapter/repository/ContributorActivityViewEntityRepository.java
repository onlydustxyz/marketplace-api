package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ContributorActivityViewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ContributorActivityViewEntityRepository extends JpaRepository<ContributorActivityViewEntity, Long> {
    @Query(value = """
            WITH stats AS (SELECT c.contributor_id                 AS id,
                   c.contributor_login                             AS login,
                   c.contributor_html_url                          AS html_url,
                   c.contributor_avatar_url                        AS avatar_url,
                   u.id IS NOT NULL                                AS is_registered,
                   DATE_PART('isoyear', c.created_at)              AS year,
                   DATE_PART('week', c.created_at)                 AS week,
                   MAX(c.created_at)                               AS created_at,
                   COUNT(*) FILTER (WHERE c.type = 'PULL_REQUEST') AS pull_request_count,
                   COUNT(*) FILTER (WHERE c.type = 'ISSUE')        AS issue_count,
                   COUNT(*) FILTER (WHERE c.type = 'CODE_REVIEW')  AS code_review_count,
                   COUNT(*)                                        AS total_count
            FROM indexer_exp.contributions c
                 JOIN project_github_repos pgr ON pgr.github_repo_id = c.repo_id
                 LEFT JOIN iam.users u ON u.github_user_id = c.contributor_id
            WHERE pgr.project_id = :projectId
              AND c.status = 'COMPLETED'
            GROUP BY c.contributor_id,
                     c.contributor_login,
                     c.contributor_html_url,
                     c.contributor_avatar_url,
                     u.id,
                     year,
                     week
            )
            SELECT
                s.id,
                s.login,
                s.html_url,
                s.avatar_url,
                s.is_registered,
                SUM(s.pull_request_count)  AS completed_pull_request_count,
                SUM(s.issue_count)         AS completed_issue_count,
                SUM(s.code_review_count)   AS completed_code_review_count,
                SUM(s.total_count)         AS completed_total_count,
                COALESCE(jsonb_agg(JSONB_BUILD_OBJECT(
                        'year', s.year,
                        'week', s.week,
                        'pull_request_count', s.pull_request_count,
                        'issue_count', s.issue_count,
                        'code_review_count', s.code_review_count
                )) FILTER ( WHERE s.created_at >= to_date(cast(:fromDate as text), 'YYYY-MM-DD')), '[]') AS counts
            FROM stats s
            GROUP BY s.id, s.login, s.html_url, s.avatar_url, s.is_registered
            """,
            countQuery = """
                        SELECT COUNT(DISTINCT c.contributor_id)
                        FROM indexer_exp.contributions c
                        JOIN project_github_repos pgr ON pgr.github_repo_id = c.repo_id
                        WHERE pgr.project_id = :projectId
                          AND c.status = 'COMPLETED'
                    """, nativeQuery = true)
    Page<ContributorActivityViewEntity> findAllByProjectId(UUID projectId, String fromDate, Pageable pageable);
}
