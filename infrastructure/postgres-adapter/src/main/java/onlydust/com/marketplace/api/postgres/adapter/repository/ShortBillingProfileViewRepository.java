package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ShortBillingProfileViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ShortBillingProfileViewRepository extends JpaRepository<ShortBillingProfileViewEntity, UUID> {

    @Query(value = """
            select bp.*, false pending_invitation from accounting.billing_profiles bp
            join accounting.billing_profiles_users bpu on bp.id = bpu.billing_profile_id and bpu.user_id = :userId
            where bp.type = 'INDIVIDUAL'
            """, nativeQuery = true)
    List<ShortBillingProfileViewEntity> findIndividualProfilesForUserId(@Param("userId") UUID userId);

    @Query(value = """
            select bp.*, false pending_invitation
            from accounting.billing_profiles bp
                     join accounting.billing_profiles_users bpu
                          on bp.id = bpu.billing_profile_id and bpu.user_id = :userId
            union
            select bp2.*, true pending_invitation
            from iam.users u
                     join accounting.billing_profiles_user_invitations bpui on bpui.github_user_id = u.github_user_id
                     join accounting.billing_profiles bp2 on bp2.id = bpui.billing_profile_id
            where u.id = :userId
            """, nativeQuery = true)
    List<ShortBillingProfileViewEntity> findBillingProfilesForUserId(@Param("userId") UUID userId);

}
