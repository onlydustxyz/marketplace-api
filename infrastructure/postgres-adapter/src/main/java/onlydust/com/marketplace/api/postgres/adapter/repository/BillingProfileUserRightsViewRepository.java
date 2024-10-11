package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.BillingProfileUserRightsQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface BillingProfileUserRightsViewRepository extends JpaRepository<BillingProfileUserRightsQueryEntity, UUID> {

    @Query(nativeQuery = true, value = """
            select u.id                                                 as user_id,
                   bpu.role                                             as user_role,
                   bp.type                                              as billing_profile_type,
                   (select count(*)
                    from accounting.reward_statuses r
                    where r.status >= 'PROCESSING'
                      and r.billing_profile_id = :billingProfileId)     as billing_profile_processing_rewards_count,
                   (select count(*)
                    from accounting.reward_statuses r
                    where r.status >= 'PROCESSING'
                      and r.billing_profile_id = :billingProfileId
                      and r.recipient_id = u.github_user_id)            as user_processing_rewards_count,
                   bpui.role                                            as invited_role,
                   bpui.invited_at                                      as invited_at,
                   u_by.github_login                                    as invited_by_github_login,
                   u_by.github_user_id                                  as invited_by_github_user_id,
                   u_by.github_avatar_url                               as invited_by_github_avatar_url,
                   (select count(*)
                    from accounting.billing_profiles_user_invitations bpui2
                    where bpui2.billing_profile_id = :billingProfileId) as billing_profile_coworkers_count
            from iam.users u
                     left join accounting.billing_profiles_user_invitations bpui
                               on bpui.github_user_id = u.github_user_id and bpui.billing_profile_id = :billingProfileId
                     left join accounting.billing_profiles_users bpu
                               on bpu.user_id = u.id and bpu.billing_profile_id = :billingProfileId
                     left join accounting.billing_profiles bp on bpu.billing_profile_id = bp.id
                     left join iam.users u_by on u_by.id = bpui.invited_by
            where u.id = :userId
            """)
    Optional<BillingProfileUserRightsQueryEntity> findForUserIdAndBillingProfileId(UUID userId, UUID billingProfileId);

}
