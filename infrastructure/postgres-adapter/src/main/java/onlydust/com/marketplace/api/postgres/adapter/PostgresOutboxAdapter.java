package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.project.domain.model.notification.Event;
import onlydust.com.marketplace.project.domain.port.output.OutboxPort;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.EventEntity;

import java.util.Optional;

@AllArgsConstructor
public class PostgresOutboxAdapter<E extends EventEntity> implements OutboxPort {

    private final OutboxRepository<E> outboxRepository;

    @Override
    public void push(Event event) {
        outboxRepository.saveEvent(event);
    }

    @Override
    public Optional<Event> peek() {
        return outboxRepository.findNextToProcess().map(EventEntity::getEvent);
    }

    @Override
    public void ack() {
        outboxRepository.findNextToProcess().ifPresent(entity -> {
            entity.setStatus(EventEntity.Status.PROCESSED);
            entity.setError(null);
            outboxRepository.save(entity);
        });
    }

    @Override
    public void nack(String message) {
        outboxRepository.findNextToProcess().ifPresent(entity -> {
            entity.setStatus(EventEntity.Status.FAILED);
            entity.setError(message);
            outboxRepository.save(entity);
        });
    }

    @Override
    public void skip(String message) {
        outboxRepository.findNextToProcess().ifPresent(entity -> {
            entity.setStatus(EventEntity.Status.SKIPPED);
            entity.setError(message);
            outboxRepository.save(entity);
        });
    }
}
