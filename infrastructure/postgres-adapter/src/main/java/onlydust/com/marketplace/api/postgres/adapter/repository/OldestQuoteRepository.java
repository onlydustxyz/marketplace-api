package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.OldestQuoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OldestQuoteRepository extends JpaRepository<OldestQuoteEntity, OldestQuoteEntity.PrimaryKey> {
}
