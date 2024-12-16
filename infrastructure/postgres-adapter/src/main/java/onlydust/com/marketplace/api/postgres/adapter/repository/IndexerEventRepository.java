package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.IndexerEventEntity;
import onlydust.com.marketplace.kernel.infrastructure.postgres.OutboxRepository;
import onlydust.com.marketplace.kernel.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndexerEventRepository extends OutboxRepository<IndexerEventEntity>, JpaRepository<IndexerEventEntity, Long> {

    @Override
    default void saveEvent(@NonNull Event event) {
        save(new IndexerEventEntity(event));
    }

}
