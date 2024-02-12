package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RewardStatusRepository extends JpaRepository<RewardStatusEntity, UUID> {
    List<RewardStatusEntity> findByPaidAtIsNull();

    @Query(value = """
            SELECT * FROM accounting.reward_status_data rsd
            JOIN public.rewards r on r.id = rsd.reward_id
            JOIN iam.users u on r.recipient_id = u.github_user_id
            LEFT JOIN individual_billing_profiles ibp on ibp.user_id = u.id AND ibp.id = :billingProfileId
            LEFT JOIN company_billing_profiles cbp on cbp.user_id = u.id AND cbp.id = :billingProfileId
            WHERE COALESCE(ibp.id, cbp.id) IS NOT NULL AND
            rsd.paid_at IS NULL
            """, nativeQuery = true)
    List<RewardStatusEntity> findNotPaidByBillingProfile(UUID billingProfileId);
}
