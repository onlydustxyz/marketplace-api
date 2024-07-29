package onlydust.com.marketplace.user.domain.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.user.domain.model.SendableNotification;
import onlydust.com.marketplace.user.domain.port.output.NotificationSender;
import onlydust.com.marketplace.user.domain.port.output.NotificationStoragePort;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

@Slf4j
@AllArgsConstructor
public class AsyncNotificationEmailProcessor {

    private final NotificationSender notificationSender;
    private final NotificationStoragePort notificationStoragePort;

    @Async
    public void send(SendableNotification notification) {
        try {
            notificationSender.send(notification);
            notificationStoragePort.markAsSent(NotificationChannel.EMAIL, List.of(notification.id()));
        } catch (Exception e) {
            LOGGER.error("Error while sending email for notification %s".formatted(notification.id()), e);
        }
    }
}
