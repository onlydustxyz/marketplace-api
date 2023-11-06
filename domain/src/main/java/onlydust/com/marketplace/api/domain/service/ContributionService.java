package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.port.input.ContributionFacadePort;
import onlydust.com.marketplace.api.domain.port.output.ContributionStoragePort;
import onlydust.com.marketplace.api.domain.view.MyContributionDetailsView;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class ContributionService implements ContributionFacadePort {
    final ContributionStoragePort contributionStoragePort;

    @Override
    public Optional<MyContributionDetailsView> getContribution(UUID projectId, String contributionId) {
        return contributionStoragePort.findContributionById(projectId, contributionId);
    }
}
