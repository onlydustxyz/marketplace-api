package onlydust.com.marketplace.user.domain.port.output;

import onlydust.com.marketplace.user.domain.model.SendableNotification;

import java.util.Collection;

public interface NotificationSender {
    void send(Collection<SendableNotification> notifications);
}
