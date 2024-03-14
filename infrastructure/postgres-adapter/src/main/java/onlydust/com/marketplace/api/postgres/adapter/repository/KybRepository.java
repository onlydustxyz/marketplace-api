package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.KybEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface KybRepository extends JpaRepository<KybEntity, UUID> {

    Optional<KybEntity> findByBillingProfileId(UUID billingProfileId);

    Optional<KybEntity> findByApplicantId(String applicantId);

    @Modifying
    @Query(nativeQuery = true, value = """
            delete from accounting.kyb where billing_profile_id = :billingProfileId
            """)
    void deleteByBillingProfileId(UUID billingProfileId);
}
