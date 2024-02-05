package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.notification.Event;

public interface UserVerificationFacadePort {

    void consumeUserVerificationEvent(final Event event);

}
