package onlydust.com.marketplace.project.domain.observer;

import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.port.input.HackathonObserverPort;

import java.util.List;
import java.util.UUID;

public class HackathonObserverComposite implements HackathonObserverPort {

    private final List<HackathonObserverPort> observers;

    public HackathonObserverComposite(HackathonObserverPort... observers) {
        this.observers = List.of(observers);
    }

    @Override
    public void onUserRegistration(Hackathon.Id hackathonId, UUID userId) {
        observers.forEach(observer -> observer.onUserRegistration(hackathonId, userId));
    }
}
