package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.notification.Notification;

import java.util.Optional;

public interface NotificationPort {
    void push(Notification notification);

    Optional<Notification> peek();

    void ack();

    void nack(String message);
}
