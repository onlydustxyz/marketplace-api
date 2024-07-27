package onlydust.com.marketplace.user.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.model.notification.Notification;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.kernel.model.notification.NotificationData;
import onlydust.com.marketplace.kernel.model.notification.NotificationRecipient;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.user.domain.port.output.NotificationSettingsStoragePort;
import onlydust.com.marketplace.user.domain.port.output.NotificationStoragePort;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
public class NotificationService implements NotificationPort {
    private final NotificationSettingsStoragePort notificationSettingsStoragePort;
    private final NotificationStoragePort notificationStoragePort;

    @Override
    public Notification push(UUID recipientId, NotificationData notificationData) {
        final var channels = notificationSettingsStoragePort.getNotificationChannels(recipientId, notificationData.category());
        final var notification = Notification.of(recipientId, notificationData, new HashSet<>(channels));
        notificationStoragePort.save(notification);
        return notification;
    }

    @Override
    public Map<NotificationRecipient, List<Notification>> getPendingNotificationsPerRecipient(NotificationChannel notificationChannel) {
        return notificationStoragePort.getPendingNotificationsPerRecipient(notificationChannel);
    }
}
