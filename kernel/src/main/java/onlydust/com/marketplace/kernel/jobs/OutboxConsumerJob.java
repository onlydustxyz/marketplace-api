package onlydust.com.marketplace.kernel.jobs;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;

@Slf4j
@AllArgsConstructor
public class OutboxConsumerJob {

    private final OutboxPort outbox;
    private final OutboxConsumer consumer;

    public void run() {
        outbox.peek().forEach(this::processEvent);
    }

    private void processEvent(OutboxPort.IdentifiableEvent identifiableEvent) {
        final var eventId = identifiableEvent.id();
        try {
            consumer.process(identifiableEvent.event());
            outbox.ack(eventId);
        } catch (Exception e) {
            if (e instanceof OutboxSkippingException) {
                LOGGER.warn("Skipping event %d".formatted(eventId), e);
                outbox.skip(eventId, e.getMessage());
            } else {
                LOGGER.error("Error while processing event %d".formatted(eventId), e);
                outbox.nack(eventId, e.getMessage());
            }
        }
    }
}
