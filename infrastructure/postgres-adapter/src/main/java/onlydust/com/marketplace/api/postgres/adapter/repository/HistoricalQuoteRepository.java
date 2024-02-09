package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.HistoricalQuoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricalQuoteRepository extends JpaRepository<HistoricalQuoteEntity, HistoricalQuoteEntity.PrimaryKey> {
}
