package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.project.domain.model.notification.Event;
import onlydust.com.marketplace.api.postgres.adapter.OutboxRepository;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.TrackingEventEntity;

public interface TrackingEventRepository extends OutboxRepository<TrackingEventEntity> {

    @Override
    default void saveEvent(Event event) {
        save(new TrackingEventEntity(event));
    }

}
