package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardStatusDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RewardStatusRepository extends JpaRepository<RewardStatusDataEntity, UUID> {
    List<RewardStatusDataEntity> findByPaidAtIsNull();

    @Query(value = """
            SELECT * FROM accounting.reward_status_data rsd
            JOIN public.rewards r on r.id = rsd.reward_id
            JOIN iam.users u on r.recipient_id = u.github_user_id
            JOIN accounting.payout_preferences pp ON pp.project_id = r.project_id AND pp.user_id = u.id
            JOIN accounting.billing_profiles bp ON bp.id = pp.billing_profile_id
            WHERE bp.id = :billingProfileId AND
            rsd.paid_at IS NULL
            """, nativeQuery = true)
    List<RewardStatusDataEntity> findNotPaidByBillingProfile(UUID billingProfileId);
}
