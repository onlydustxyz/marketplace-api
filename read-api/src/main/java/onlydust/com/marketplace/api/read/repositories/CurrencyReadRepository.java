package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface CurrencyReadRepository extends Repository<CurrencyReadEntity, UUID> {
    Optional<CurrencyReadEntity> findById(UUID currencyId);
}
