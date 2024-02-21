package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.notification.Event;

public interface WebhookPort {
    void send(Event event);
}
