package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.KycEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface KycRepository extends JpaRepository<KycEntity, UUID> {

    Optional<KycEntity> findByBillingProfileId(UUID billingProfileId);
}
