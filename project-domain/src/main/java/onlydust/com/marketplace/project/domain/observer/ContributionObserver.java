package onlydust.com.marketplace.project.domain.observer;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.project.domain.port.input.ContributionObserverPort;
import onlydust.com.marketplace.project.domain.port.output.ContributionStoragePort;

import java.util.List;

@AllArgsConstructor
public class ContributionObserver implements ContributionObserverPort {

    final ContributionStoragePort contributionStoragePort;

    @Override
    public void onContributionsChanged(List<Long> repoIds) {
        contributionStoragePort.refreshIgnoredContributions(repoIds);
    }
}
