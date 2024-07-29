package onlydust.com.marketplace.user.domain.port.output;

import onlydust.com.marketplace.user.domain.model.SendableNotification;

import java.util.Collection;
import java.util.List;

public interface NotificationSender {
    void send(Collection<SendableNotification> notifications);

    default void send(SendableNotification notification) {
        send(List.of(notification));
    }
}
