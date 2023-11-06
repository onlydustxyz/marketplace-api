package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.port.input.ContributionFacadePort;
import onlydust.com.marketplace.api.domain.view.MyContributionDetailsView;

import java.util.Optional;

@AllArgsConstructor
public class ContributionService implements ContributionFacadePort {

    @Override
    public Optional<MyContributionDetailsView> getContribution(String id) {
        return Optional.empty();
    }
}
