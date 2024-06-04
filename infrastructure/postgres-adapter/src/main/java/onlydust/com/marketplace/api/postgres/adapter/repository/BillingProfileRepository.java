package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface BillingProfileRepository extends JpaRepository<BillingProfileEntity, UUID> {

    @Modifying
    @Query(value = """
                    update accounting.billing_profiles
                    set verification_status = cast(:verificationStatus as accounting.verification_status)
                    where id = :billingProfileId
            """, nativeQuery = true)
    void updateBillingProfileVerificationStatus(UUID billingProfileId, String verificationStatus);

    @Modifying
    @Query(nativeQuery = true, value = """
                update accounting.billing_profiles
                set enabled = :enabled
                where id = :billingProfileId
            """)
    void updateEnabled(UUID billingProfileId, Boolean enabled);

    @Modifying
    @Query(nativeQuery = true, value = """
                update accounting.billing_profiles
                set type = cast(:type as accounting.billing_profile_type)
                where id = :billingProfileId
            """)
    void updateBillingProfileType(UUID billingProfileId, String type);
}
