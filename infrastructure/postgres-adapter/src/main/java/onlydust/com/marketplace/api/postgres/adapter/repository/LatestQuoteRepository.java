package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.LatestQuoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LatestQuoteRepository extends JpaRepository<LatestQuoteEntity, LatestQuoteEntity.PrimaryKey> {
}
