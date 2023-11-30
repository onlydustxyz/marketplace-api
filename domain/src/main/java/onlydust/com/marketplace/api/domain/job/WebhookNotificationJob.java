package onlydust.com.marketplace.api.domain.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.model.notification.Notification;
import onlydust.com.marketplace.api.domain.port.output.NotificationPort;
import onlydust.com.marketplace.api.domain.port.output.WebhookPort;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class WebhookNotificationJob implements NotificationJob {

    private final NotificationPort notificationPort;
    private final WebhookPort webhookPort;

    @Override
    public void run() {
        try {
            Optional<Notification> notification;
            while ((notification = notificationPort.peek()).isPresent()) {
                sendNotification(notification.get());
                notificationPort.ack();
            }
        } catch (Exception e) {
            LOGGER.error("Error while sending notifications", e);
            notificationPort.nack(e.getMessage());
        }
    }

    @Retryable(maxAttempts = 6, backoff = @Backoff(delay = 500, multiplier = 2))
    private void sendNotification(Notification notification) {
        webhookPort.send(notification);
    }
}
