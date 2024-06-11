package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.OutboxRepository;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.IndexingEventEntity;
import onlydust.com.marketplace.kernel.model.Event;

public interface IndexingEventRepository extends OutboxRepository<IndexingEventEntity> {

    @Override
    default void saveEvent(Event event) {
        save(new IndexingEventEntity(event));
    }

}
