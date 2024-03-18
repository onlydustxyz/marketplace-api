package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileEntity;
import org.apache.commons.lang3.function.Failable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillingProfileRepository extends JpaRepository<BillingProfileEntity, UUID> {

    @Modifying
    @Query(value = """
                    update accounting.billing_profiles
                    set verification_status = cast(:verificationStatus as accounting.verification_status)
                    where id = :billingProfileId
            """, nativeQuery = true)
    void updateBillingProfileVerificationStatus(UUID billingProfileId, String verificationStatus);

    @Query(value = """
                select count(*) > 0
                from accounting.invoices
                where billing_profile_id = :billingProfileId
            """, nativeQuery = true)
    boolean hasInvoices(UUID billingProfileId);

    @Modifying
    @Query(nativeQuery = true, value = """
                update accounting.billing_profiles
                set enabled = :enabled
                where id = :billingProfileId
            """)
    void updateEnabled(UUID billingProfileId, Boolean enabled);

    @Query(nativeQuery = true, value = """
                select enabled from accounting.billing_profiles where id = :billingProfileId
            """)
    boolean isBillingProfileEnabled(UUID billingProfileId);

    @Query(value = """
            select bp.* from accounting.billing_profiles bp
            join accounting.billing_profiles_users bpu on bp.id = bpu.billing_profile_id and bpu.user_id = :userId
            """, nativeQuery = true)
    List<BillingProfileEntity> findBillingProfilesForUserId(@Param("userId") UUID userId);
}
