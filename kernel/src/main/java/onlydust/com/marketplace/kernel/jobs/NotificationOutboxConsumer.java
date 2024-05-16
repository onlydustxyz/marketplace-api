package onlydust.com.marketplace.kernel.jobs;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@AllArgsConstructor
public class NotificationOutboxConsumer implements OutboxConsumer {
    private final NotificationPort notificationPort;

    @Override
    public void process(Event event) {
        notify(event);
    }

    @Retryable(maxAttempts = 6, backoff = @Backoff(delay = 500, multiplier = 2))
    private void notify(Event event) {
        notificationPort.notify(event);
    }
}
