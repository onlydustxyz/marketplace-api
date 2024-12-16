package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.IndexingEventEntity;
import onlydust.com.marketplace.kernel.infrastructure.postgres.OutboxRepository;
import onlydust.com.marketplace.kernel.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndexingEventRepository extends OutboxRepository<IndexingEventEntity>, JpaRepository<IndexingEventEntity, Long> {

    @Override
    default void saveEvent(@NonNull Event event) {
        save(new IndexingEventEntity(event));
    }

}
