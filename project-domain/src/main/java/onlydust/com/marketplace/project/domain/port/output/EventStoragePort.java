package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.OldEvent;

public interface EventStoragePort {
    void saveEvent(OldEvent oldEvent);
}
