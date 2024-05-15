package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.PayoutInfoQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface PayoutInfoViewRepository extends JpaRepository<PayoutInfoQueryEntity, UUID> {

    @Query(value = """
                SELECT bp.id as billing_profile_id,
                    ARRAY_AGG(distinct rsd.networks) as networks
                FROM accounting.billing_profiles bp
                    LEFT JOIN rewards r ON r.billing_profile_id = bp.id
                    LEFT JOIN accounting.reward_statuses rs ON rs.reward_id = r.id AND rs.status = 'PAYOUT_INFO_MISSING'
                    LEFT JOIN (SELECT rsd.reward_id, unnest(rsd.networks) as networks FROM accounting.reward_status_data rsd) rsd ON rsd.reward_id = rs.reward_id
                WHERE bp.id = :billingProfileId
                GROUP BY bp.id
            """, nativeQuery = true)
    Optional<PayoutInfoQueryEntity> findByBillingProfileId(UUID billingProfileId);
}
