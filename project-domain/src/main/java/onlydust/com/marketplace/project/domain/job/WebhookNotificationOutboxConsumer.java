package onlydust.com.marketplace.project.domain.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.project.domain.model.notification.Event;
import onlydust.com.marketplace.project.domain.port.output.WebhookPort;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Slf4j
@AllArgsConstructor
public class WebhookNotificationOutboxConsumer implements OutboxConsumer {

    private final WebhookPort webhookPort;

    @Override
    public void process(Event event) {
        sendNotification(event);
    }

    @Retryable(maxAttempts = 6, backoff = @Backoff(delay = 500, multiplier = 2))
    private void sendNotification(Event event) {
        webhookPort.send(event);
    }
}
