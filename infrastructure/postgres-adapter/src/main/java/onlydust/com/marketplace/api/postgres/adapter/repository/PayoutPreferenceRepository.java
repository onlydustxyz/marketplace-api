package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.PayoutPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface PayoutPreferenceRepository extends JpaRepository<PayoutPreferenceEntity, PayoutPreferenceEntity.PrimaryKey> {

    @Modifying
    @Query(nativeQuery = true, value = """
                delete from accounting.payout_preferences where billing_profile_id = :billingProfileId
            """)
    void deleteAllByBillingProfileId(UUID billingProfileId);
}
