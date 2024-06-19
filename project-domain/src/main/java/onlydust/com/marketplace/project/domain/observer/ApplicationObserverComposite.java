package onlydust.com.marketplace.project.domain.observer;

import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.port.output.ApplicationObserverPort;

import java.util.List;

public class ApplicationObserverComposite implements ApplicationObserverPort {
    private final List<ApplicationObserverPort> observers;

    public ApplicationObserverComposite(ApplicationObserverPort... observers) {
        this.observers = List.of(observers);
    }

    @Override
    public void onApplicationCreated(Application application) {
        observers.forEach(observer -> observer.onApplicationCreated(application));
    }
}
