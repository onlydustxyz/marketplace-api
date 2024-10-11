package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RewardRepository extends JpaRepository<RewardEntity, UUID> {

    @Query(value = """
            select count(r.*) from rewards r
            join iam.users u on u.github_user_id = r.recipient_id and u.id = :userId
            where r.project_id = :projectId
            """, nativeQuery = true)
    Long countAllByRecipientIdAndProjectId(UUID userId, UUID projectId);

    List<RewardEntity> findAllByInvoiceId(UUID invoiceId);

    @Modifying
    @Query(nativeQuery = true, value = """
                update public.rewards
                set billing_profile_id = :billingProfileId
                from accounting.reward_statuses rs
                where
                    rs.reward_id = id and
                    rs.project_id = :projectId and
                    rs.recipient_user_id = :recipientUserId and
                    rs.status < 'PROCESSING'
            """)
    void updateBillingProfileForRecipientUserIdAndProjectId(UUID billingProfileId, UUID recipientUserId, UUID projectId);

    @Modifying
    @Query(nativeQuery = true, value = """
                update public.rewards
                set billing_profile_id = null
                where id in (:rewardIds)
            """)
    void removeBillingProfileIdOf(List<UUID> rewardIds);

    @Query(value = """
                select r from RewardEntity r
                join RewardStatusEntity rs on rs.rewardId = r.id and rs.status < 'PROCESSING'
                where r.billingProfileId = :billingProfileId
            """)
    List<RewardEntity> getRewardIdsToBeRemovedFromBillingProfile(UUID billingProfileId);

    @Query(value = """
                select r from RewardEntity r
                join RewardStatusEntity rs on rs.rewardId = r.id and rs.status < 'PROCESSING'
                where r.billingProfileId = :billingProfileId
                and r.recipientId = (select u.githubUserId from UserEntity u where u.id = :recipientUserId)
            """)
    List<RewardEntity> getRewardIdsToBeRemovedFromBillingProfileForUser(UUID billingProfileId, UUID recipientUserId);

    @Modifying
    @Query(nativeQuery = true, value = """
            with s as (select r.id reward_id, pp.billing_profile_id
                       from rewards r
                                join iam.users u on u.github_user_id = r.recipient_id
                                join accounting.payout_preferences pp on pp.user_id = u.id and pp.project_id = r.project_id
                       where r.id = :rewardId)
            update rewards rr
            set billing_profile_id = s.billing_profile_id
            from s
            where rr.id = s.reward_id
            """)
    void updateBillingProfileFromRecipientPayoutPreferences(UUID rewardId);

    @Query(value = """
            select r
            from RewardEntity r
            join fetch r.status
            join fetch r.currency
            join fetch r.receipts
            where r.status.status = 'COMPLETE'
            """)
    List<RewardEntity> findAllComplete();


    @Modifying
    @Query(nativeQuery = true, value = """
            update rewards
            set payment_notified_at = now()
            where id in (:rewardIds)
            """)
    void markRewardAsPaymentNotified(List<UUID> rewardIds);
}
