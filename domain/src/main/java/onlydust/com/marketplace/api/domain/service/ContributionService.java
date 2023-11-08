package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.port.input.ContributionFacadePort;
import onlydust.com.marketplace.api.domain.port.output.ContributionStoragePort;
import onlydust.com.marketplace.api.domain.view.ContributionDetailsView;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class ContributionService implements ContributionFacadePort {
    final ContributionStoragePort contributionStoragePort;
    final PermissionService permissionService;

    @Override
    public ContributionDetailsView getContribution(UUID projectId, String contributionId, Long githubUserId) {
        if (!permissionService.isUserContributor(contributionId, githubUserId))
            throw OnlyDustException.forbidden("User is not a contributor of this contribution");
        return contributionStoragePort.findContributionById(projectId, contributionId);
    }

    @Override
    public void setIgnoredContributions(UUID projectId, UUID projectLeadId, List<String> ignoredContributionIds) {
        if (!permissionService.isUserProjectLead(projectId, projectLeadId))
            throw OnlyDustException.forbidden("Only project leaders can edit the list of ignored contributions");
        contributionStoragePort.setIgnoredContributions(projectId, ignoredContributionIds);
    }
}
