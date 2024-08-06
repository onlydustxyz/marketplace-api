package onlydust.com.marketplace.user.domain.model;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.notification.Notification;

public record NotificationStatusUpdateRequest(@NonNull Notification.Id notificationId, @NonNull NotificationStatus notificationStatus) {

    public static enum NotificationStatus {
        READ, UNREAD
    }
}
