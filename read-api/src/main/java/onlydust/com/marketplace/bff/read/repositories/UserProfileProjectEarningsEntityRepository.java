package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.UserProfileProjectEarningsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface UserProfileProjectEarningsEntityRepository extends Repository<UserProfileProjectEarningsEntity, String> {
    @Query(value = """
            SELECT
                project_id                     as project_id,
                sum(rsd.amount_usd_equivalent) as total_earned_usd
            FROM
                rewards r
                JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
            WHERE
                r.recipient_id = :githubUserId
            GROUP BY
                r.project_id
            """, nativeQuery = true)
    List<UserProfileProjectEarningsEntity> findByContributorId(Long githubUserId);
}
