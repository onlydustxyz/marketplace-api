package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.user.UserProfileProjectEarningsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface UserProfileProjectEarningsEntityRepository extends Repository<UserProfileProjectEarningsEntity, String> {
    @Query(value = """
            SELECT stats.project_id as project_id,
                   stats.usd_total  as total_earned_usd
            FROM received_rewards_stats_per_project_per_user stats
            WHERE stats.recipient_id = :githubUserId
              AND (:ecosystemId IS NULL OR :ecosystemId = ANY (stats.ecosystem_ids))
            """, nativeQuery = true)
    List<UserProfileProjectEarningsEntity> findByContributorIdAndEcosystem(Long githubUserId, UUID ecosystemId);
}
