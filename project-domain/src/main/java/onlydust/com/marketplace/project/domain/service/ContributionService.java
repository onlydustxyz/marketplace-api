package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.port.input.ContributionFacadePort;
import onlydust.com.marketplace.project.domain.port.input.ContributionObserverPort;
import onlydust.com.marketplace.project.domain.port.input.ProjectObserverPort;
import onlydust.com.marketplace.project.domain.port.output.ContributionStoragePort;
import onlydust.com.marketplace.project.domain.view.ContributionDetailsView;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class ContributionService implements ContributionFacadePort, ContributionObserverPort, ProjectObserverPort {
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
    public void onContributionsChanged(Long repoId) {
        contributionStoragePort.refreshIgnoredContributions(List.of(repoId));
    }

    @Override
    public void onLinkedReposChanged(UUID projectId, Set<Long> linkedRepoIds, Set<Long> unlinkedRepoIds) {
        contributionStoragePort.refreshIgnoredContributions(projectId);
    }

    @Override
    public void onRewardSettingsChanged(UUID projectId) {
        contributionStoragePort.refreshIgnoredContributions(projectId);
    }

    @Override
    public void onUserApplied(UUID projectId, UUID userId, Application.Id applicationId) {
    }

    @Override
    public void onProjectCreated(UUID projectId, UUID projectLeadId) {
    }

    @Override
    public void onProjectCategorySuggested(String categoryName, UUID userId) {
    }
}
