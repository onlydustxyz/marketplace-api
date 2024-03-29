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
                false pending_invitation
            from accounting.billing_profiles bp
            where bp.id = :billingProfileId
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
                false pending_invitation
            from accounting.billing_profiles bp
                join accounting.billing_profiles_users bpu on bp.id = bpu.billing_profile_id and bpu.user_id = :userId
            where coalesce(:billingProfileTypes) is null or cast(bp.type as text) in (:billingProfileTypes)
            """, nativeQuery = true)
    List<ShortBillingProfileViewEntity> findBillingProfilesForUserId(@Param("userId") UUID userId, List<String> billingProfileTypes);

    @Query(value = """
            select bp.id,
                bp.name,
                bp.type,
                bp.invoice_mandate_accepted_at,
                bp.verification_status,
                bp.enabled,
                bpui.role,
                true pending_invitation
            from iam.users u
                join accounting.billing_profiles_user_invitations bpui on bpui.github_user_id = u.github_user_id and bpui.accepted = false
                join accounting.billing_profiles bp on bp.id = bpui.billing_profile_id
            where u.id = :userId
            """, nativeQuery = true)
    List<ShortBillingProfileViewEntity> findBillingProfilesForUserIdInvited(@Param("userId") UUID userId);

}
