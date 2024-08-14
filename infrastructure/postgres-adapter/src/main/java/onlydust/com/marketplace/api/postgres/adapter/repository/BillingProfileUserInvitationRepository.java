package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileUserInvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface BillingProfileUserInvitationRepository extends JpaRepository<BillingProfileUserInvitationEntity,
        BillingProfileUserInvitationEntity.PrimaryKey> {

    @Modifying
    @Query(value = """
                delete from accounting.billing_profiles_user_invitations where billing_profile_id = :billingProfileId
            """, nativeQuery = true)
    void deleteAllByBillingProfileId(UUID billingProfileId);

    @Modifying
    @Query(nativeQuery = true, value = """
                update accounting.billing_profiles_user_invitations
                set accepted = true
                where billing_profile_id = :billingProfileId and github_user_id = :githubUserId
            """)
    void acceptInvitation(UUID billingProfileId, Long githubUserId);
}
