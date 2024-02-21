package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.project.domain.model.notification.Event;

public interface UserVerificationFacadePort {

    void consumeUserVerificationEvent(final Event event);

}
