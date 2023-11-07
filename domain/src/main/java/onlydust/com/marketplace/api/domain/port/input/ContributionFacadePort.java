package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.view.MyContributionDetailsView;

import java.util.UUID;

public interface ContributionFacadePort {
    MyContributionDetailsView getContribution(UUID projectId, String contributionId);
}
