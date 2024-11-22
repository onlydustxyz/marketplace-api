package onlydust.com.marketplace.user.domain.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.model.notification.Notification;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.user.domain.model.NotificationRecipient;
import onlydust.com.marketplace.user.domain.model.SendableNotification;
import onlydust.com.marketplace.user.domain.port.output.NotificationSender;
import onlydust.com.marketplace.user.domain.port.output.NotificationStoragePort;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@AllArgsConstructor
public class AsyncNotificationEmailProcessor implements NotificationSender {

    private final NotificationSender notificationSender;
    private final NotificationStoragePort notificationStoragePort;

    @Override
    public void send(SendableNotification notification) {
        CompletableFuture.runAsync(() -> {
            try {
                notificationSender.send(notification);
                notificationStoragePort.markAsSent(NotificationChannel.EMAIL, List.of(notification.id()));
            } catch (Exception e) {
                LOGGER.error("Error while sending email for notification %s".formatted(notification.id()), e);
            }
        });
    }

    @Override
    public void sendAllForRecipient(NotificationRecipient notificationRecipient, List<SendableNotification> sendableNotifications) {
        CompletableFuture.runAsync(() -> {
            try {
                notificationSender.sendAllForRecipient(notificationRecipient, sendableNotifications);
                notificationStoragePort.markAsSent(NotificationChannel.SUMMARY_EMAIL, sendableNotifications.stream().map(Notification::id).toList());
            } catch (Exception e) {
                LOGGER.error("Error while sending summary email for recipient %s and %s notification(s)".formatted(notificationRecipient,
                        sendableNotifications.size()), e);
            }
        });
    }
}
