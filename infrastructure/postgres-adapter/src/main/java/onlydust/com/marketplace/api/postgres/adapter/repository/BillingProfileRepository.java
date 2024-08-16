package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.ZonedDateTime;
import java.util.List;
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

    @Query(value = """
            select exists(
                select 1
                from accounting.billing_profiles bp
                    join accounting.billing_profiles_users bpu on bp.id = bpu.billing_profile_id and bpu.user_id = :userId
                where bp.type = 'INDIVIDUAL'
            )
            """, nativeQuery = true)
    boolean individualBillingProfileExistsByUserId(UUID userId);

    @Query(value = """
            select distinct bpe
            from BillingProfileEntity bpe
            join fetch bpe.users
            where date_trunc('day', cast(bpe.createdAt as timestamp)) = date_trunc('day', cast(:creationDate as timestamp ))
            """)
    List<BillingProfileEntity> findAllByCreationDate(ZonedDateTime creationDate);
}
