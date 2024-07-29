package onlydust.com.marketplace.user.domain.port.output;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.notification.Notification;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.user.domain.model.SendableNotification;

import java.util.List;

public interface NotificationStoragePort {
    void save(@NonNull Notification notification);

    List<SendableNotification> getPendingNotifications(@NonNull NotificationChannel notificationChannel);
}
