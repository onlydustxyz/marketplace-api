package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.view.ContributionDetailsView;

import java.util.List;
import java.util.UUID;

public interface ContributionFacadePort {
    ContributionDetailsView getContribution(UUID projectId, String contributionId, Long githubUserId);

    void setIgnoredContributions(UUID projectId, UUID projectLeadId, List<String> ignoredContributionIds);
}
