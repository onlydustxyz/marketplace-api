package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.KycEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface KycRepository extends JpaRepository<KycEntity, UUID> {

    Optional<KycEntity> findByBillingProfileId(UUID billingProfileId);

    @Modifying
    @Query(nativeQuery = true, value = """
            delete from accounting.kyc where billing_profile_id = :billingProfileId
            """)
    void deleteByBillingProfileId(UUID billingProfileId);
}
