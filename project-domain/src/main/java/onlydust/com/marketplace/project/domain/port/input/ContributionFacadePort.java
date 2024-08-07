package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.project.domain.view.ContributionDetailsView;

import java.util.List;
import java.util.UUID;

public interface ContributionFacadePort {
    ContributionDetailsView getContribution(UUID projectId, String contributionId, AuthenticatedUser caller);

    void ignoreContributions(UUID projectId, UUID projectLeadId, List<String> contributionIds);

    void unignoreContributions(UUID projectId, UUID projectLeadId, List<String> contributionIds);

    void unassign(UUID projectId, UUID projectLeadId, String contributionId);
}
