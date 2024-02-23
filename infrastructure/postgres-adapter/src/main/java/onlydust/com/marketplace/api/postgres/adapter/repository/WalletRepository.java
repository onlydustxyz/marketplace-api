package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface WalletRepository extends JpaRepository<WalletEntity, UUID> {

    @Modifying
    @Query(value = "delete from accounting.wallets where billing_profile_id = :billingProfileId", nativeQuery = true)
    void deleteByBillingProfileId(UUID billingProfileId);
}
