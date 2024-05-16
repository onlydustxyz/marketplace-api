package onlydust.com.marketplace.project.domain.observer;

import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.port.input.UserObserverPort;

import java.util.List;

public class UserObserverComposite implements UserObserverPort {
    private final List<UserObserverPort> observers;

    public UserObserverComposite(UserObserverPort... observers) {
        this.observers = List.of(observers);
    }

    @Override
    public void onUserSignedUp(User user) {
        observers.forEach(observer -> observer.onUserSignedUp(user));
    }
}
