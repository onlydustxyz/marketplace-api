package onlydust.com.marketplace.project.domain.job;

import onlydust.com.marketplace.project.domain.model.notification.Event;

public interface OutboxConsumer {
    void process(Event event);
}
