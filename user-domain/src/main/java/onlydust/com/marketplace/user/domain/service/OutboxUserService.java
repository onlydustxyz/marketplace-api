package onlydust.com.marketplace.user.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
import onlydust.com.marketplace.user.domain.event.UserSignedUp;
import onlydust.com.marketplace.user.domain.port.input.UserObserverPort;

import java.util.Date;

@AllArgsConstructor
public class OutboxUserService implements UserObserverPort {
    private final OutboxPort indexerOutbox;
    private final OutboxPort trackingOutbox;

    @Override
    public void onUserSignedUp(AuthenticatedUser user) {
        final var event = new UserSignedUp(user.id(), user.githubUserId(), user.login(), new Date());
        indexerOutbox.push(event);
        trackingOutbox.push(event);
    }
}
