package onlydust.com.marketplace.user.domain.port.output;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.notification.Notification;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.kernel.model.notification.NotificationRecipient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface NotificationStoragePort {
    void save(@NonNull UUID recipientId, @NonNull Notification notification, @NonNull List<NotificationChannel> channels);

    Map<NotificationRecipient, List<Notification>> getPendingNotificationsPerRecipient(@NonNull NotificationChannel notificationChannel);
}
