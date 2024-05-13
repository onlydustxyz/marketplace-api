package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.UserWeeklyStatsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface UserWeeklyStatsEntityRepository extends Repository<UserWeeklyStatsEntity, String> {
    @Query(value = """
            SELECT
                coalesce(contributor_id, recipient_id) as contributor_id,
                coalesce(created_at, requested_at)     as created_at,
                coalesce(sum(code_review_count), 0)    as code_review_count,
                coalesce(sum(issue_count), 0)          as issue_count,
                coalesce(sum(pull_request_count), 0)   as pull_request_count,
                coalesce(sum(reward_count), 0)         as reward_count
            FROM (
                SELECT
                    c.contributor_id                                     as contributor_id,
                    pgr.project_id                                       as project_id,
                    date_trunc('week', c.created_at)                     as created_at,
                    count(c.id) FILTER ( WHERE c.type = 'CODE_REVIEW' )  as code_review_count,
                    count(c.id) FILTER ( WHERE c.type = 'ISSUE' )        as issue_count,
                    count(c.id) FILTER ( WHERE c.type = 'PULL_REQUEST' ) as pull_request_count
                FROM
                    indexer_exp.contributions c
                    JOIN indexer_exp.github_repos gr on gr.id = c.repo_id and gr.visibility = 'PUBLIC'
                    JOIN project_github_repos pgr on pgr.github_repo_id = c.repo_id
                    JOIN projects p on p.id = pgr.project_id and p.visibility = 'PUBLIC'
                GROUP BY 
                    c.contributor_id, 
                    pgr.project_id,
                    date_trunc('week', c.created_at)
            ) as contribution_stats
            FULL OUTER JOIN (
                SELECT
                    r.recipient_id                     as recipient_id,
                    r.project_id                       as project_id,
                    date_trunc('week', r.requested_at) as requested_at,
                    count(r.id)                        as reward_count
                FROM
                    rewards r
                    JOIN projects p on p.id = r.project_id and p.visibility = 'PUBLIC'
                GROUP BY
                    r.recipient_id, 
                    r.project_id,
                    date_trunc('week', r.requested_at)
            ) as rewards_stats ON 
                    rewards_stats.recipient_id = contribution_stats.contributor_id AND 
                    rewards_stats.requested_at = contribution_stats.created_at AND
                    rewards_stats.project_id = contribution_stats.project_id
            LEFT JOIN (
                    SELECT project_id, array_agg(ecosystem_id) as ecosystems 
                    FROM projects_ecosystems 
                    GROUP BY project_id
                ) ecosystems ON ecosystems.project_id = coalesce(contribution_stats.project_id, rewards_stats.project_id)
            WHERE
                (:ecosystemId IS NULL OR :ecosystemId = ANY (ecosystems.ecosystems)) AND
                (contributor_id = :githubUserId OR recipient_id = :githubUserId)
            GROUP BY
                coalesce(contributor_id, recipient_id),
                coalesce(created_at, requested_at)    
            ORDER BY
                coalesce(created_at, requested_at)
            """, nativeQuery = true)
    List<UserWeeklyStatsEntity> findByContributorIdAndEcosystem(Long githubUserId, UUID ecosystemId);
}
