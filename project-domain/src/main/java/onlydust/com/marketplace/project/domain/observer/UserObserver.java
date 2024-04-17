package onlydust.com.marketplace.project.domain.observer;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.model.notification.UserSignedUp;
import onlydust.com.marketplace.project.domain.port.input.UserObserverPort;

import java.util.Date;

@AllArgsConstructor
public class UserObserver implements UserObserverPort {

    private final OutboxPort indexerOutbox;
    private final OutboxPort trackingOutbox;

    @Override
    public void onUserSignedUp(User user) {
        final var event = new UserSignedUp(user.getId(), user.getGithubUserId(), user.getGithubLogin(), new Date());
        indexerOutbox.push(event);
        trackingOutbox.push(event);
    }
}
