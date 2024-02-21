package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.model.Event;

public interface UserVerificationFacadePort {

    void consumeUserVerificationEvent(final Event event);

}
