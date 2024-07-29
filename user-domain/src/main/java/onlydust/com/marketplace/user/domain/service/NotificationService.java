package onlydust.com.marketplace.user.domain.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.model.notification.Notification;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.kernel.model.notification.NotificationData;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.user.domain.model.SendableNotification;
import onlydust.com.marketplace.user.domain.model.User;
import onlydust.com.marketplace.user.domain.port.output.AppUserStoragePort;
import onlydust.com.marketplace.user.domain.port.output.NotificationSettingsStoragePort;
import onlydust.com.marketplace.user.domain.port.output.NotificationStoragePort;

import java.util.HashSet;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
public class NotificationService implements NotificationPort {
    private final NotificationSettingsStoragePort notificationSettingsStoragePort;
    private final NotificationStoragePort notificationStoragePort;
    private final AppUserStoragePort userStoragePort;
    private final AsyncNotificationEmailProcessor asyncNotificationEmailProcessor;

    @Override
    public Notification push(UUID recipientId, NotificationData notificationData) {
        final var channels = notificationSettingsStoragePort.getNotificationChannels(recipientId, notificationData.category());
        final var notification = Notification.of(recipientId, notificationData, new HashSet<>(channels));
        notificationStoragePort.save(notification);

        if (channels.contains(NotificationChannel.EMAIL)) {
            sendEmail(recipientId, notification);
        }
        return notification;
    }

    private void sendEmail(UUID recipientId, Notification notification) {
        userStoragePort.findById(User.Id.of(recipientId))
                .ifPresent(user -> asyncNotificationEmailProcessor.send(SendableNotification.of(user, notification)));
    }
}
