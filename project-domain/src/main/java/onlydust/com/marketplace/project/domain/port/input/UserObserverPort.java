package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.project.domain.model.User;

public interface UserObserverPort {
    void onUserSignedUp(User user);
}
