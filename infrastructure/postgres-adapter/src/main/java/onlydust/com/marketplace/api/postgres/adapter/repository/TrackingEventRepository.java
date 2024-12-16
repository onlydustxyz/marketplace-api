package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.TrackingEventEntity;
import onlydust.com.marketplace.kernel.infrastructure.postgres.OutboxRepository;
import onlydust.com.marketplace.kernel.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackingEventRepository extends OutboxRepository<TrackingEventEntity>, JpaRepository<TrackingEventEntity, Long> {

    @Override
    default void saveEvent(@NonNull Event event) {
        save(new TrackingEventEntity(event));
    }

}
