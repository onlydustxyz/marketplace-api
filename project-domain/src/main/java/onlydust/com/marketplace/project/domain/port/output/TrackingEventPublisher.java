package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.model.Event;

public interface TrackingEventPublisher {
    void publish(Event event);
}
