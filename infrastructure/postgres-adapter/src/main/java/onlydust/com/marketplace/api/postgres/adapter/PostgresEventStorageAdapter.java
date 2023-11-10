package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.Event;
import onlydust.com.marketplace.api.domain.port.output.EventStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.EventEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.EventRepository;

@AllArgsConstructor
public class PostgresEventStorageAdapter implements EventStoragePort {
    final EventRepository eventRepository;

    @Override
    public void saveEvent(Event event) {
        eventRepository.save(EventEntity.of(event));
    }
}
