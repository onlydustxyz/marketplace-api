package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.notification.Notification;

public interface WebhookPort {
    void send(Notification notification);
}
