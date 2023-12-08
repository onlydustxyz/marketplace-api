package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.OldEvent;

public interface EventStoragePort {
    void saveEvent(OldEvent oldEvent);
}
