package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.notification.Event;

public interface WebhookPort {
    void send(Event event);
}
