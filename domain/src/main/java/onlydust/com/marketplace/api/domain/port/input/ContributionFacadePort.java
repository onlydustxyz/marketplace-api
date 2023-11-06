package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.view.MyContributionDetailsView;

import java.util.Optional;
import java.util.UUID;

public interface ContributionFacadePort {
    Optional<MyContributionDetailsView> getContribution(UUID projectId, String contributionId);
}
