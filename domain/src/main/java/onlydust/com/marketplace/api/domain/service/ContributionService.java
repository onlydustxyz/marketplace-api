package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.model.User;
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
    public ContributionDetailsView getContribution(UUID projectId, String contributionId, User caller) {
        if (!permissionService.isUserContributor(contributionId, caller.getGithubUserId()) &&
            !permissionService.isUserProjectLead(projectId, caller.getId()))
            throw OnlyDustException.forbidden("User is not the contributor of this contribution, nor a project leader" +
                                              " of this project");
        return contributionStoragePort.findContributionById(projectId, contributionId);
    }

    @Override
    public void ignoreContributions(UUID projectId, UUID projectLeadId, List<String> contributionIds) {
        if (!permissionService.isUserProjectLead(projectId, projectLeadId))
            throw OnlyDustException.forbidden("Only project leaders can edit the list of ignored contributions");
        contributionStoragePort.ignoreContributions(projectId, contributionIds);
    }

    @Override
    public void unignoreContributions(UUID projectId, UUID projectLeadId, List<String> contributionIds) {
        if (!permissionService.isUserProjectLead(projectId, projectLeadId))
            throw OnlyDustException.forbidden("Only project leaders can edit the list of ignored contributions");
        contributionStoragePort.unignoreContributions(projectId, contributionIds);
    }

    @Override
    public void refreshIgnoredContributions(List<Long> repoIds) {
        contributionStoragePort.refreshIgnoredContributions(repoIds);
    }
}
