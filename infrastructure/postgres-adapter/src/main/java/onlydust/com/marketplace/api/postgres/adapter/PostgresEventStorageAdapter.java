package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.OldEvent;
import onlydust.com.marketplace.api.domain.port.output.EventStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.OldEventEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.EventRepository;

@AllArgsConstructor
public class PostgresEventStorageAdapter implements EventStoragePort {

  final EventRepository eventRepository;

  @Override
  public void saveEvent(OldEvent oldEvent) {
    eventRepository.save(OldEventEntity.of(oldEvent));
  }
}
