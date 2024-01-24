package onlydust.com.marketplace.api.domain.observer;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.notification.UserSignedUp;
import onlydust.com.marketplace.api.domain.port.input.UserObserverPort;
import onlydust.com.marketplace.api.domain.port.output.OutboxPort;

import java.util.Date;

@AllArgsConstructor
public class UserObserver implements UserObserverPort {

    private final OutboxPort indexerOutbox;
    private final OutboxPort notificationOutbox;
    private final OutboxPort trackingOutbox;

    @Override
    public void onUserSignedUp(User user) {
        final var event = new UserSignedUp(user.getId(), user.getGithubUserId(), user.getGithubLogin(), new Date());
        indexerOutbox.push(event);
        notificationOutbox.push(event);
        trackingOutbox.push(event);
    }
}
