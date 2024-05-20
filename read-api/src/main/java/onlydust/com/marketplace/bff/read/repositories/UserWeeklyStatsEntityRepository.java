package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.UserWeeklyStatsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface UserWeeklyStatsEntityRepository extends Repository<UserWeeklyStatsEntity, String> {
    @Query(value = """
            SELECT stats.contributor_id,
                   stats.created_at_week as created_at,
                   stats.code_review_count,
                   stats.issue_count,
                   stats.pull_request_count,
                   coalesce(rewards_stats.reward_count, 0) as reward_count
            FROM contributions_stats_per_ecosystem_per_user_per_week stats
                LEFT JOIN received_rewards_stats_per_ecosystem_per_user_per_week rewards_stats ON
                    rewards_stats.recipient_id = stats.contributor_id AND
                    rewards_stats.ecosystem_id = stats.ecosystem_id AND
                    rewards_stats.requested_at_week = stats.created_at_week
            WHERE stats.contributor_id = :githubUserId
              AND stats.ecosystem_id = :ecosystemId
            """, nativeQuery = true)
    List<UserWeeklyStatsEntity> findByContributorIdAndEcosystem(Long githubUserId, UUID ecosystemId);

    @Query(value = """
            SELECT stats.contributor_id,
                   stats.created_at_week as created_at,
                   stats.code_review_count,
                   stats.issue_count,
                   stats.pull_request_count,
                   coalesce(rewards_stats.reward_count, 0) as reward_count
            FROM contributions_stats_per_user_per_week stats
                LEFT JOIN received_rewards_stats_per_user_per_week rewards_stats ON
                    rewards_stats.recipient_id = stats.contributor_id AND
                    rewards_stats.requested_at_week = stats.created_at_week
            WHERE stats.contributor_id = :githubUserId
            """, nativeQuery = true)
    List<UserWeeklyStatsEntity> findByContributorId(Long githubUserId);
}
