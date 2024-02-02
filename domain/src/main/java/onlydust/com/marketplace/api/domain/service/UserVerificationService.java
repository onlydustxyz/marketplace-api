package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.notification.Event;
import onlydust.com.marketplace.api.domain.port.input.UserVerificationFacadePort;
import onlydust.com.marketplace.api.domain.port.output.OutboxPort;

@AllArgsConstructor
public class UserVerificationService implements UserVerificationFacadePort {

    private final OutboxPort outboxPort;

    @Override
    public void consumeUserVerificationEvent(Event event) {
        outboxPort.push(event);
    }
}
