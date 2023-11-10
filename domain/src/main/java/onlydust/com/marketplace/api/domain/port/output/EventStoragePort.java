package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.Event;

public interface EventStoragePort {
    void saveEvent(Event event);
}
