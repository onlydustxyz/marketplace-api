package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.AccountBookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountBookRepository extends JpaRepository<AccountBookEntity, UUID> {
    Optional<AccountBookEntity> findByCurrencyId(UUID currencyId);
}
