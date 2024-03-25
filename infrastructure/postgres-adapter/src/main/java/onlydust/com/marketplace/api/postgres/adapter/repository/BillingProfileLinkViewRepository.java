package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.BillingProfileLinkViewEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillingProfileLinkViewRepository extends JpaRepository<BillingProfileLinkViewEntity, UUID> {

    @Query(nativeQuery = true, value = """
            select bp.id,
                   bp.type,
                   bp.verification_status,
                   bpu.role,
                   count(rs.reward_id) filter ( where rs.status = 'PAYOUT_INFO_MISSING' ) > 0 as missing_payout_info,
                   count(rs.reward_id) filter ( where rs.status = 'PENDING_VERIFICATION' ) > 0 as missing_verification
            from accounting.billing_profiles bp
            join accounting.billing_profiles_users bpu on bpu.billing_profile_id = bp.id and bpu.user_id = :userId
            left join rewards r on r.billing_profile_id = bp.id
            left join accounting.reward_statuses rs on rs.reward_id = r.id
            group by bp.id, bp.type, bp.verification_status, bpu.role
            """)
    List<BillingProfileLinkViewEntity> findByUserId(final UUID userId);

    @Query(nativeQuery = true, value = """
            select bp.id,
                   bp.type,
                   bp.verification_status,
                   bpu.role,
                   count(rs.reward_id) filter ( where rs.status = 'PAYOUT_INFO_MISSING' ) > 0 as missing_payout_info,
                   count(rs.reward_id) filter ( where rs.status = 'PENDING_VERIFICATION' ) > 0 as missing_verification
            from accounting.billing_profiles bp
            join accounting.billing_profiles_users bpu on bpu.billing_profile_id = bp.id
            left join rewards r on r.billing_profile_id = bp.id
            left join accounting.reward_statuses rs on rs.reward_id = r.id
            where bp.id = :billingProfileId
            group by bp.id, bp.type, bp.verification_status, bpu.role
            """)
    @NotNull
    Optional<BillingProfileLinkViewEntity> findById(final @NotNull UUID billingProfileId);
}
