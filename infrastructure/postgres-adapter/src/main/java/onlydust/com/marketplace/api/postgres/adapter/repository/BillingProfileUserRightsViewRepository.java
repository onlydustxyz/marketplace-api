package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.BillingProfileUserRightsViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface BillingProfileUserRightsViewRepository extends JpaRepository<BillingProfileUserRightsViewEntity, UUID> {

    @Query(nativeQuery = true, value = """
            select u.id user_id,
                   bpu.role                        user_role,
                   (select count(*)
                    from accounting.invoices i
                    where i.billing_profile_id = :billingProfileId
                      and i.status != 'DRAFT') > 0 has_bp_some_invoices,
                   (select count(*)
                    from accounting.invoices i
                             join rewards r on r.invoice_id = i.id and r.recipient_id = u.github_user_id
                    where i.billing_profile_id = :billingProfileId
                      and i.status != 'DRAFT') > 0 has_user_some_linked_invoices,
                   bpui.role                       invited_role,
                   bpui.invited_at,
                   u_by.github_login               invited_by_github_login,
                   u_by.github_user_id             invited_by_github_user_id,
                   u_by.github_avatar_url          invited_by_github_avatar_url,
                   (select count(*) from accounting.billing_profiles_user_invitations bpui2
                    where bpui2.billing_profile_id = :billingProfileId ) > 0 has_more_than_one_coworker
            from iam.users u
            left join accounting.billing_profiles_user_invitations bpui on bpui.github_user_id = u.github_user_id and bpui.billing_profile_id = :billingProfileId
            left join accounting.billing_profiles_users bpu on bpu.user_id = u.id and bpu.billing_profile_id = :billingProfileId
            left join iam.users u_by on u_by.id = bpui.invited_by
            where u.id = :userId
            """)
    Optional<BillingProfileUserRightsViewEntity> findForUserIdAndBillingProfileId(UUID userId, UUID billingProfileId);

}
