package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ShortBillingProfileQueryEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShortBillingProfileViewRepository extends JpaRepository<ShortBillingProfileQueryEntity, UUID> {

    @Query(value = """
            select bp.id,
                bp.name,
                bp.type,
                bp.invoice_mandate_accepted_at,
                bp.verification_status,
                bp.enabled,
                NULL as role,
                false pending_invitation,
                max(rs.status) is not null as individual_limit_reached
            from accounting.billing_profiles bp
                left join rewards r on r.billing_profile_id = bp.id
                left join accounting.reward_statuses rs on rs.reward_id = r.id and rs.status = 'INDIVIDUAL_LIMIT_REACHED'
            where bp.id = :billingProfileId
            group by bp.id
            """, nativeQuery = true)
    @NotNull
    Optional<ShortBillingProfileQueryEntity> findById(@NotNull UUID billingProfileId);

    @Query(value = """
            select bp.id,
                bp.name,
                bp.type,
                bp.invoice_mandate_accepted_at,
                bp.verification_status,
                bp.enabled,
                bpu.role,
                false pending_invitation,
                max(rs.status) is not null as individual_limit_reached
            from accounting.billing_profiles bp
                join accounting.billing_profiles_users bpu on bp.id = bpu.billing_profile_id and bpu.user_id = :userId
                left join rewards r on r.billing_profile_id = bp.id
                left join accounting.reward_statuses rs on rs.reward_id = r.id and rs.status = 'INDIVIDUAL_LIMIT_REACHED'
            where coalesce(:billingProfileTypes) is null or cast(bp.type as text) in (:billingProfileTypes)
            group by bp.id, bpu.role
            """, nativeQuery = true)
    List<ShortBillingProfileQueryEntity> findBillingProfilesForUserId(@Param("userId") UUID userId, List<String> billingProfileTypes);

    @Query(value = """
            select bp.id,
                bp.name,
                bp.type,
                bp.invoice_mandate_accepted_at,
                bp.verification_status,
                bp.enabled,
                bpui.role,
                true pending_invitation,
                max(rs.status) is not null as individual_limit_reached
            from iam.users u
                join accounting.billing_profiles_user_invitations bpui on bpui.github_user_id = u.github_user_id and bpui.accepted = false
                join accounting.billing_profiles bp on bp.id = bpui.billing_profile_id
                left join rewards r on r.billing_profile_id = bp.id
                left join accounting.reward_statuses rs on rs.reward_id = r.id and rs.status = 'INDIVIDUAL_LIMIT_REACHED'
            where u.id = :userId
            group by bp.id, bpui.role
            """, nativeQuery = true)
    List<ShortBillingProfileQueryEntity> findBillingProfilesForUserIdInvited(@Param("userId") UUID userId);

}
