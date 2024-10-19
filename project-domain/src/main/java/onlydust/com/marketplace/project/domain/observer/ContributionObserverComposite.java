package onlydust.com.marketplace.project.domain.observer;

import onlydust.com.marketplace.kernel.model.ContributionUUID;
import onlydust.com.marketplace.project.domain.port.input.ContributionObserverPort;

import java.util.List;

public class ContributionObserverComposite implements ContributionObserverPort {

    private final List<ContributionObserverPort> observers;

    public ContributionObserverComposite(ContributionObserverPort... observers) {
        this.observers = List.of(observers);
    }

    @Override
    public void onContributionsChanged(Long repoId, ContributionUUID contributionUUID) {
        observers.forEach(observer -> observer.onContributionsChanged(repoId, contributionUUID));
    }
}
