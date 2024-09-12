package onlydust.com.marketplace.user.domain.port.output;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.model.notification.Notification;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.user.domain.model.SendableNotification;

import java.util.Collection;
import java.util.List;

public interface NotificationStoragePort {
    void save(@NonNull Notification notification);

    List<SendableNotification> getPendingNotifications(@NonNull NotificationChannel notificationChannel);

    void markAsSent(NotificationChannel channel, Collection<Notification.Id> notificationIds);

    void markAllInAppUnreadAsRead(UserId userId);

    void markInAppNotificationsAsUnreadForUser(UserId userId, List<Notification.Id> notificationIds);

    void markInAppNotificationsAsReadForUser(UserId userId, List<Notification.Id> notificationIds);
}
