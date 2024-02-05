package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.IndividualBillingProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IndividualBillingProfileRepository extends JpaRepository<IndividualBillingProfileEntity, UUID> {

    Optional<IndividualBillingProfileEntity> findByUserId(final UUID userId);
}
