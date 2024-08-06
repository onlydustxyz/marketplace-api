package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.NotificationRepository;
import onlydust.com.marketplace.kernel.model.UuidWrapper;
import onlydust.com.marketplace.kernel.model.notification.Notification;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.user.domain.model.SendableNotification;
import onlydust.com.marketplace.user.domain.port.output.NotificationStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static java.util.Comparator.comparing;

@AllArgsConstructor
public class PostgresNotificationAdapter implements NotificationStoragePort {
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void save(@NonNull Notification notification) {
        notificationRepository.save(NotificationEntity.of(notification));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SendableNotification> getPendingNotifications(@NonNull NotificationChannel notificationChannel) {
        return notificationRepository.findAllPendingByChannel(notificationChannel).stream()
                .map(NotificationEntity::toDomain)
                .sorted(comparing(SendableNotification::createdAt))
                .toList();
    }

    @Override
    @Transactional
    public void markAsSent(NotificationChannel channel, Collection<Notification.Id> notificationIds) {
        notificationRepository.markAsSent(channel, notificationIds.stream().map(Notification.Id::value).toList());
    }

    @Override
    @Transactional
    public void markAllInAppUnreadAsRead(UUID userId) {
        notificationRepository.markAllInAppUnreadAsRead(userId);
    }

    @Override
    public void markInAppNotificationsAsUnreadForUser(UUID userId, List<Notification.Id> notificationIds) {
     notificationRepository.markAllInAppAsUnread(userId, notificationIds.stream().map(UuidWrapper::value).toList());
    }

    @Override
    public void markInAppNotificationsAsReadForUser(UUID userId, List<Notification.Id> notificationIds) {
        notificationRepository.markAllInAppAsRead(userId, notificationIds.stream().map(UuidWrapper::value).toList());
    }
}
