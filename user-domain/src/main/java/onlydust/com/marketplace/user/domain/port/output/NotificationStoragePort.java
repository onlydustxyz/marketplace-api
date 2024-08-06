package onlydust.com.marketplace.user.domain.port.output;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.notification.Notification;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.user.domain.model.SendableNotification;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface NotificationStoragePort {
    void save(@NonNull Notification notification);

    List<SendableNotification> getPendingNotifications(@NonNull NotificationChannel notificationChannel);

    void markAsSent(NotificationChannel channel, Collection<Notification.Id> notificationIds);

    void markAllInAppUnreadAsRead(UUID userId);

    void markInAppNotificationsAsUnreadForUser(UUID userId, List<Notification.Id> notificationIds);

    void markInAppNotificationsAsReadForUser(UUID userId, List<Notification.Id> notificationIds);
}
