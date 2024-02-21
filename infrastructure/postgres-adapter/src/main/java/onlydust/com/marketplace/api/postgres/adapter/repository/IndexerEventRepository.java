package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.project.domain.model.notification.Event;
import onlydust.com.marketplace.api.postgres.adapter.OutboxRepository;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.IndexerEventEntity;

public interface IndexerEventRepository extends OutboxRepository<IndexerEventEntity> {

    @Override
    default void saveEvent(Event event) {
        save(new IndexerEventEntity(event));
    }

}
