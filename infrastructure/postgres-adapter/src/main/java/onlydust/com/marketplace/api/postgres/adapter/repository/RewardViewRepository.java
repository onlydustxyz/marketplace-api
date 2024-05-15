package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardViewEntity;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RewardViewRepository extends JpaRepository<RewardViewEntity, UUID> {
    List<RewardViewEntity> findByBillingProfileIdAndStatusStatus(UUID billingProfileId, RewardStatus.Input status);

    @Query(value = """
            SELECT EXISTS(
                SELECT 1
                FROM rewards r
                         JOIN accounting.reward_statuses rs ON rs.reward_id = r.id
                WHERE r.recipient_id = :githubUserId
                  AND rs.status = 'PENDING_BILLING_PROFILE'
            )
            """, nativeQuery = true)
    boolean existsPendingBillingProfileByRecipientId(Long githubUserId);
}
