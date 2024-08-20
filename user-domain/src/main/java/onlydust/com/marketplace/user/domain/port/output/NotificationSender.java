package onlydust.com.marketplace.user.domain.port.output;

import onlydust.com.marketplace.user.domain.model.NotificationRecipient;
import onlydust.com.marketplace.user.domain.model.SendableNotification;

import java.util.List;

public interface NotificationSender {

    void send(SendableNotification notification);

    void sendAllForRecipient(NotificationRecipient notificationRecipient, List<SendableNotification> sendableNotifications);
}
