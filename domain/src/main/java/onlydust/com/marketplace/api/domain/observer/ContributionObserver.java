package onlydust.com.marketplace.api.domain.observer;

import java.util.List;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.port.input.ContributionObserverPort;
import onlydust.com.marketplace.api.domain.port.output.ContributionStoragePort;

@AllArgsConstructor
public class ContributionObserver implements ContributionObserverPort {

  final ContributionStoragePort contributionStoragePort;

  @Override
  public void onContributionsChanged(List<Long> repoIds) {
    contributionStoragePort.refreshIgnoredContributions(repoIds);
  }
}
