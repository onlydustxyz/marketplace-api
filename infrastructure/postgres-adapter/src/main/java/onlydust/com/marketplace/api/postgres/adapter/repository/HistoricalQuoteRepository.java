package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.HistoricalQuoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface HistoricalQuoteRepository extends JpaRepository<HistoricalQuoteEntity, HistoricalQuoteEntity.PrimaryKey> {

    Optional<HistoricalQuoteEntity> findFirstByCurrencyIdAndBaseIdAndTimestampLessThanEqualOrderByTimestampDesc(UUID currencyId,
                                                                                                                UUID baseId,
                                                                                                                Instant timestamp);
}
