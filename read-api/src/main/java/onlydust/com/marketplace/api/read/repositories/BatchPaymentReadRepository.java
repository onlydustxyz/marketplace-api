package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.billing_profile.BatchPaymentReadEntity;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface BatchPaymentReadRepository extends Repository<BatchPaymentReadEntity, UUID> {

    Optional<BatchPaymentReadEntity> findById(UUID id);
}
