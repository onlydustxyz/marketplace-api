package onlydust.com.marketplace.user.domain.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.notification.Notification;


@Value
@Accessors(fluent = true, chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
public class SendableNotification extends Notification {
    NotificationRecipient recipient;

    public static SendableNotification of(NotificationRecipient recipient, Notification notification) {
        return SendableNotification.builder()
                .recipient(recipient)
                .id(notification.id())
                .recipientId(notification.recipientId())
                .data(notification.data())
                .createdAt(notification.createdAt())
                .channels(notification.channels())
                .build();
    }
}
