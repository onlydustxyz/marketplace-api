package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.billing_profile.AllBillingProfileUserReadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AllBillingProfileUserReadRepository extends JpaRepository<AllBillingProfileUserReadEntity, AllBillingProfileUserReadEntity.PrimaryKey> {
    @Query(value = """
            SELECT coalesce(bpu.billing_profile_id, bpui.billing_profile_id) as billing_profile_id,
                   coalesce(bpu.user_id, u.id)                               as user_id,
                   coalesce(bpu.role, bpui.role)                             as role,
                   coalesce(bpui.accepted, true)                             as invitation_accepted
            FROM accounting.billing_profiles_user_invitations bpui
                     JOIN iam.users u on bpui.github_user_id = u.github_user_id
                     FULL OUTER JOIN accounting.billing_profiles_users bpu on bpu.billing_profile_id = bpui.billing_profile_id
            WHERE bpu.user_id = :userId
               OR u.id = :userId
            """, nativeQuery = true)
    List<AllBillingProfileUserReadEntity> findAllByUserId(UUID userId);
}
