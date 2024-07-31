package onlydust.com.marketplace.user.domain.observer;


import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.user.domain.port.input.UserObserverPort;

import java.util.List;

public class UserObserverComposite implements UserObserverPort {
    private final List<UserObserverPort> observers;

    public UserObserverComposite(UserObserverPort... observers) {
        this.observers = List.of(observers);
    }

    @Override
    public void onUserSignedUp(AuthenticatedUser user) {
        observers.forEach(observer -> observer.onUserSignedUp(user));
    }
}
