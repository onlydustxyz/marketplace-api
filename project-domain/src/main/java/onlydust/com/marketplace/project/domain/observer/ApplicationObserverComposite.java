package onlydust.com.marketplace.project.domain.observer;

import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.port.output.ApplicationObserverPort;

import java.util.List;
import java.util.UUID;

public class ApplicationObserverComposite implements ApplicationObserverPort {
    private final List<ApplicationObserverPort> observers;

    public ApplicationObserverComposite(ApplicationObserverPort... observers) {
        this.observers = List.of(observers);
    }

    @Override
    public void onApplicationCreated(Application application) {
        observers.forEach(observer -> observer.onApplicationCreated(application));
    }

    @Override
    public void onApplicationAccepted(Application application, UUID projectLeadId) {
        observers.forEach(observer -> observer.onApplicationAccepted(application, projectLeadId));
    }

    @Override
    public void onHackathonExternalApplicationDetected(GithubIssue issue, Long applicantId, Hackathon hackathon) {
        observers.forEach(observer -> observer.onHackathonExternalApplicationDetected(issue, applicantId, hackathon));
    }

    @Override
    public void onApplicationRefused(Application application) {
        observers.forEach(observer -> observer.onApplicationRefused(application));
    }
}
