package onlydust.com.marketplace.api.postgres.adapter.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.WalletEntity;

public interface WalletRepository extends JpaRepository<WalletEntity, WalletEntity.PrimaryKey> {

    @Modifying
    @Query(value = "delete from accounting.wallets where billing_profile_id = :billingProfileId", nativeQuery = true)
    void deleteByBillingProfileId(UUID billingProfileId);
}
