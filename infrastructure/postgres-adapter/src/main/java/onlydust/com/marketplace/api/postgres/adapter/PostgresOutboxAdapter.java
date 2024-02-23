package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.EventEntity;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;

import java.util.List;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@AllArgsConstructor
public class PostgresOutboxAdapter<E extends EventEntity> implements OutboxPort {

    private final OutboxRepository<E> outboxRepository;

    @Override
    public void push(Event event) {
        outboxRepository.saveEvent(event);
    }

    @Override
    public List<IdentifiableEvent> peek() {
        return outboxRepository.findNextToProcess().stream().map(EventEntity::toIdentifiableEvent).toList();
    }

    @Override
    public void ack(Long eventId) {
        final var entity = outboxRepository.findById(eventId)
                .orElseThrow(() -> internalServerError("Event %d not found".formatted(eventId)));

        entity.setStatus(EventEntity.Status.PROCESSED);
        entity.setError(null);
        outboxRepository.save(entity);
    }

    @Override
    public void nack(Long eventId, String message) {
        final var entity = outboxRepository.findById(eventId)
                .orElseThrow(() -> internalServerError("Event %d not found".formatted(eventId)));

        entity.setStatus(EventEntity.Status.FAILED);
        entity.setError(message);
        outboxRepository.save(entity);
    }

    @Override
    public void skip(Long eventId, String message) {
        final var entity = outboxRepository.findById(eventId)
                .orElseThrow(() -> internalServerError("Event %d not found".formatted(eventId)));

        entity.setStatus(EventEntity.Status.SKIPPED);
        entity.setError(message);
        outboxRepository.save(entity);
    }
}
