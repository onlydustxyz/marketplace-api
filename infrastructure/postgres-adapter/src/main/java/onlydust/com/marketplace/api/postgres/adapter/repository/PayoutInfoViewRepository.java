package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.PayoutInfoViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface PayoutInfoViewRepository extends JpaRepository<PayoutInfoViewEntity, UUID> {

    @Query(value = """
                SELECT p.billing_profile_id,
                    ARRAY_AGG(distinct rsd.networks) as networks
                FROM accounting.payout_infos p
                    LEFT JOIN rewards r ON r.billing_profile_id = p.billing_profile_id
                    LEFT JOIN accounting.reward_statuses rs ON rs.reward_id = r.id
                    LEFT JOIN (SELECT rsd.reward_id, unnest(rsd.networks) as networks FROM accounting.reward_status_data rsd) rsd ON rsd.reward_id = rs.reward_id AND rs.status = 'PAYOUT_INFO_MISSING'
                WHERE p.billing_profile_id = :billingProfileId
                GROUP BY p.billing_profile_id
            """, nativeQuery = true)
    Optional<PayoutInfoViewEntity> findByBillingProfileId(UUID billingProfileId);
}
