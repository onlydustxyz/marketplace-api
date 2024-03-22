package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ShortBillingProfileViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ShortBillingProfileViewRepository extends JpaRepository<ShortBillingProfileViewEntity, UUID> {

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
                count(rs.*) invoiceable_reward_count
            from accounting.billing_profiles bp
                join accounting.billing_profiles_users bpu on bp.id = bpu.billing_profile_id and bpu.user_id = :userId
                left join rewards r on r.billing_profile_id = bp.id
                left join accounting.reward_statuses rs on rs.reward_id = r.id and rs.status = 'PENDING_REQUEST'
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
                count(rs.*) invoiceable_reward_count
            from accounting.billing_profiles bp
                join accounting.billing_profiles_users bpu on bp.id = bpu.billing_profile_id and bpu.user_id = :userId
                left join rewards r on r.billing_profile_id = bp.id
                left join accounting.reward_statuses rs on rs.reward_id = r.id and rs.status = 'PENDING_REQUEST'
            group by bp.id,
                bp.name,
                bp.type,
                bp.invoice_mandate_accepted_at,
                bp.verification_status,
                bp.enabled,
                bpu.role
                
            union
                        
            select bp2.id,
                bp2.name,
                bp2.type,
                bp2.invoice_mandate_accepted_at,
                bp2.verification_status,
                bp2.enabled,
                bpui.role,
                true pending_invitation,
                count(r2.*) reward_count,
                count(rs2.*) invoiceable_reward_count
            from iam.users u
                join accounting.billing_profiles_user_invitations bpui on bpui.github_user_id = u.github_user_id and bpui.accepted = false
                join accounting.billing_profiles bp2 on bp2.id = bpui.billing_profile_id
                left join rewards r2 on r2.billing_profile_id = bp2.id
                left join accounting.reward_statuses rs2 on rs2.reward_id = r2.id and rs2.status = 'PENDING_REQUEST'
            where u.id = :userId
            group by bp2.id,
                bp2.name,
                bp2.type,
                bp2.invoice_mandate_accepted_at,
                bp2.verification_status,
                bp2.enabled,
                bpui.role
            """, nativeQuery = true)
    List<ShortBillingProfileViewEntity> findBillingProfilesForUserId(@Param("userId") UUID userId);

}
