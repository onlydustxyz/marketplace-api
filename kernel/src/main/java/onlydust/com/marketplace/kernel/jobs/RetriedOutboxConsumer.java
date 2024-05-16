package onlydust.com.marketplace.kernel.jobs;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@AllArgsConstructor
public class RetriedOutboxConsumer implements OutboxConsumer {
    private final OutboxConsumer consumer;

    @Override
    public void process(Event event) {
        notify(event);
    }

    @Retryable(maxAttempts = 6, backoff = @Backoff(delay = 500, multiplier = 2))
    private void notify(Event event) {
        consumer.process(event);
    }
}
