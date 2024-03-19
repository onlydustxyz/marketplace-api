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
                where project_id = :projectId and recipient_id in (select github_user_id from iam.users where id = :recipientUserId)
                and invoice_id is null
            """)
    void updateBillingProfileForRecipientUserIdAndProjectId(UUID billingProfileId, UUID recipientUserId, UUID projectId);


    @Modifying
    @Query(nativeQuery = true, value = """
            with payout_pref as (select pp.billing_profile_id, pp.project_id, u.github_user_id
                                 from accounting.payout_preferences pp
                                          join iam.users u on u.id = pp.user_id)
            update rewards r
            set billing_profile_id = :billingProfileId
            from payout_pref
            where payout_pref.github_user_id = r.recipient_id
              and payout_pref.project_id = r.project_id
              and payout_pref.billing_profile_id = :billingProfileId
              and r.invoice_id is null
            """)
    void addBillingProfileId(UUID billingProfileId);

    @Modifying
    @Query(nativeQuery = true, value = """
                update public.rewards
                set billing_profile_id = null
                where billing_profile_id = :billingProfileId and invoice_id is null
            """)
    void removeBillingProfileId(UUID billingProfileId);
}
