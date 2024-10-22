package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.model.ContributionUUID;

public interface ContributionObserverPort {
    void onContributionsChanged(ContributionUUID contributionUUID);

    void onContributionsChanged(Long repoId, ContributionUUID contributionUUID);
}
