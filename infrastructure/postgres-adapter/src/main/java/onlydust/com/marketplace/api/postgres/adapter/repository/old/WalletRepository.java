package onlydust.com.marketplace.api.postgres.adapter.repository.old;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.WalletEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.WalletIdEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface WalletRepository extends JpaRepository<WalletEntity, WalletIdEntity> {

    @Modifying
    @Query(value = "delete from wallets where user_id = :userId", nativeQuery = true)
    void deleteByUserId(final @Param("userId") UUID userId);
}
