package onlydust.com.marketplace.api.postgres.adapter.repository.old;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<WalletEntity, WalletEntity.PrimaryKey> {
    List<WalletEntity> findAllByUserId(UUID userId);

    @Modifying
    @Query(value = "delete from wallets where user_id = :userId", nativeQuery = true)
    void deleteByUserId(final @Param("userId") UUID userId);
}
