package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.UserProfileProjectEarningsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface UserProfileProjectEarningsEntityRepository extends Repository<UserProfileProjectEarningsEntity, String> {
    @Query(value = """
            SELECT
                r.project_id                   as project_id,
                sum(rsd.amount_usd_equivalent) as total_earned_usd
            FROM
                rewards r
                JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
                LEFT JOIN (
                    SELECT project_id, array_agg(ecosystem_id) as ecosystems 
                    FROM projects_ecosystems 
                    GROUP BY project_id
                ) ecosystems ON ecosystems.project_id = r.project_id
            WHERE
                r.recipient_id = :githubUserId AND
                (:ecosystemId IS NULL OR :ecosystemId = ANY (ecosystems.ecosystems))
            GROUP BY
                r.project_id
            """, nativeQuery = true)
    List<UserProfileProjectEarningsEntity> findByContributorIdAndEcosystem(Long githubUserId, UUID ecosystemId);
}
