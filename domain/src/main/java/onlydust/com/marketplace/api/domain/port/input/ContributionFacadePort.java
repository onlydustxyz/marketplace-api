package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.view.MyContributionDetailsView;

import java.util.Optional;

public interface ContributionFacadePort {
    Optional<MyContributionDetailsView> getContribution(String id);
}
