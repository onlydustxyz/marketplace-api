package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillingProfileUserRepository extends JpaRepository<BillingProfileUserEntity, BillingProfileUserEntity.PrimaryKey> {

    Optional<BillingProfileUserEntity> findByBillingProfileIdAndUserId(UUID billingProfileId, UUID userId);

    Boolean existsByBillingProfileIdAndUserId(UUID billingProfileId, UUID userId);

    List<BillingProfileUserEntity> findByUserId(UUID userId);

}
