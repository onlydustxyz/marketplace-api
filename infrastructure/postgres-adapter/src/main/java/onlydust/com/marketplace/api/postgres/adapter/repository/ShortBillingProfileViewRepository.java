package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ShortBillingProfileViewEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShortBillingProfileViewRepository extends JpaRepository<ShortBillingProfileViewEntity, UUID> {

    @Query(value = """
            select bp.id,
                bp.name,
                bp.type,
                bp.invoice_mandate_accepted_at,
                bp.verification_status,
                bp.enabled,
                NULL as role,
                false pending_invitation,
                count(r.*) reward_count,
                count(rs.reward_id) filter ( where rs.status = 'PENDING_REQUEST' ) invoiceable_reward_count,
                count(rs.reward_id) filter ( where rs.status = 'PAYOUT_INFO_MISSING' ) > 0 as missing_payout_info,
                count(rs.reward_id) filter ( where rs.status = 'PENDING_VERIFICATION' ) > 0 as missing_verification
            from accounting.billing_profiles bp
                left join rewards r on r.billing_profile_id = bp.id
                left join accounting.reward_statuses rs on rs.reward_id = r.id
            where bp.id = :billingProfileId
            group by bp.id,
                bp.name,
                bp.type,
                bp.invoice_mandate_accepted_at,
                bp.verification_status,
                bp.enabled
            """, nativeQuery = true)
    @NotNull
    Optional<ShortBillingProfileViewEntity> findById(@NotNull UUID billingProfileId);

    @Query(value = """
            select bp.id,
                bp.name,
                bp.type,
                bp.invoice_mandate_accepted_at,
                bp.verification_status,
                bp.enabled,
                bpu.role,
                false pending_invitation,
                count(r.*) reward_count,
                count(rs.reward_id) filter ( where rs.status = 'PENDING_REQUEST' ) invoiceable_reward_count,
                count(rs.reward_id) filter ( where rs.status = 'PAYOUT_INFO_MISSING' ) > 0 as missing_payout_info,
                count(rs.reward_id) filter ( where rs.status = 'PENDING_VERIFICATION' ) > 0 as missing_verification
            from accounting.billing_profiles bp
                join accounting.billing_profiles_users bpu on bp.id = bpu.billing_profile_id and bpu.user_id = :userId
                left join rewards r on r.billing_profile_id = bp.id
                left join accounting.reward_statuses rs on rs.reward_id = r.id
            where bp.type = 'INDIVIDUAL'
            group by bp.id,
                bp.name,
                bp.type,
                bp.invoice_mandate_accepted_at,
                bp.verification_status,
                bp.enabled,
                bpu.role
            """, nativeQuery = true)
    List<ShortBillingProfileViewEntity> findIndividualProfilesForUserId(@Param("userId") UUID userId);

    @Query(value = """
            select bp.id,
                bp.name,
                bp.type,
                bp.invoice_mandate_accepted_at,
                bp.verification_status,
                bp.enabled,
                bpu.role,
                false pending_invitation,
                count(r.*) reward_count,
                count(rs.reward_id) filter ( where rs.status = 'PENDING_REQUEST' ) invoiceable_reward_count,
                count(rs.reward_id) filter ( where rs.status = 'PAYOUT_INFO_MISSING' ) > 0 as missing_payout_info,
                count(rs.reward_id) filter ( where rs.status = 'PENDING_VERIFICATION' ) > 0 as missing_verification
            from accounting.billing_profiles bp
                join accounting.billing_profiles_users bpu on bp.id = bpu.billing_profile_id and bpu.user_id = :userId
                left join rewards r on r.billing_profile_id = bp.id
                left join accounting.reward_statuses rs on rs.reward_id = r.id
            group by bp.id,
                bp.name,
                bp.type,
                bp.invoice_mandate_accepted_at,
                bp.verification_status,
                bp.enabled,
                bpu.role
            """, nativeQuery = true)
    List<ShortBillingProfileViewEntity> findBillingProfilesForUserId(@Param("userId") UUID userId);

    @Query(value = """
            select bp2.id,
                bp2.name,
                bp2.type,
                bp2.invoice_mandate_accepted_at,
                bp2.verification_status,
                bp2.enabled,
                bpui.role,
                true pending_invitation,
                count(r2.*) reward_count,
                count(rs2.reward_id) filter ( where rs2.status = 'PENDING_REQUEST' ) invoiceable_reward_count,
                count(rs2.reward_id) filter ( where rs2.status = 'PAYOUT_INFO_MISSING' ) > 0 as missing_payout_info,
                count(rs2.reward_id) filter ( where rs2.status = 'PENDING_VERIFICATION' ) > 0 as missing_verification
            from iam.users u
                join accounting.billing_profiles_user_invitations bpui on bpui.github_user_id = u.github_user_id and bpui.accepted = false
                join accounting.billing_profiles bp2 on bp2.id = bpui.billing_profile_id
                left join rewards r2 on r2.billing_profile_id = bp2.id
                left join accounting.reward_statuses rs2 on rs2.reward_id = r2.id
            where u.id = :userId
            group by bp2.id,
                bp2.name,
                bp2.type,
                bp2.invoice_mandate_accepted_at,
                bp2.verification_status,
                bp2.enabled,
                bpui.role
            """, nativeQuery = true)
    List<ShortBillingProfileViewEntity> findBillingProfilesForUserIdInvited(@Param("userId") UUID userId);

}
