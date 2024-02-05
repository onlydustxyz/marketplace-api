package onlydust.com.marketplace.api.domain.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.model.notification.Event;
import onlydust.com.marketplace.api.domain.port.output.OutboxPort;

import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class OutboxConsumerJob implements Runnable {

    private final OutboxPort outbox;
    private final OutboxConsumer consumer;

    @Override
    public void run() {
        Optional<Event> event;
        while ((event = outbox.peek()).isPresent()) {
            try {
                consumer.process(event.get());
                outbox.ack();
            } catch (Exception e) {
                if (e instanceof OutboxSkippingException) {
                    LOGGER.warn("Skipping event", e);
                    outbox.skip(e.getMessage());
                } else {
                    LOGGER.error("Error while processing event", e);
                    outbox.nack(e.getMessage());
                }
            }
        }
    }
}
