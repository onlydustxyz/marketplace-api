package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.project.domain.model.ContributionStatus;
import onlydust.com.marketplace.project.domain.model.ContributionType;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.port.input.ContributionFacadePort;
import onlydust.com.marketplace.project.domain.port.input.ContributionObserverPort;
import onlydust.com.marketplace.project.domain.port.input.ProjectObserverPort;
import onlydust.com.marketplace.project.domain.port.output.ContributionStoragePort;
import onlydust.com.marketplace.project.domain.port.output.GithubApiPort;
import onlydust.com.marketplace.project.domain.view.ContributionDetailsView;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.*;

@AllArgsConstructor
public class ContributionService implements ContributionFacadePort, ContributionObserverPort, ProjectObserverPort {
    final ContributionStoragePort contributionStoragePort;
    final PermissionService permissionService;
    final GithubAppService githubAppService;
    final GithubApiPort githubApiPort;

    @Override
    public ContributionDetailsView getContribution(UUID projectId, String contributionId, User caller) {
        if (!permissionService.isUserContributor(contributionId, caller.getGithubUserId()) &&
            !permissionService.isUserProjectLead(projectId, caller.getId()))
            throw forbidden("User is not the contributor of this contribution, nor a project leader" +
                            " of this project");
        return contributionStoragePort.findContributionById(projectId, contributionId);
    }

    @Override
    public void ignoreContributions(UUID projectId, UUID projectLeadId, List<String> contributionIds) {
        if (!permissionService.isUserProjectLead(projectId, projectLeadId))
            throw forbidden("Only project leaders can edit the list of ignored contributions");
        contributionStoragePort.ignoreContributions(projectId, contributionIds);
    }

    @Override
    public void unignoreContributions(UUID projectId, UUID projectLeadId, List<String> contributionIds) {
        if (!permissionService.isUserProjectLead(projectId, projectLeadId))
            throw forbidden("Only project leaders can edit the list of ignored contributions");
        contributionStoragePort.unignoreContributions(projectId, contributionIds);
    }

    @Override
    public void unassign(UUID projectId, UUID projectLeadId, String contributionId) {
        if (!permissionService.isUserProjectLead(projectId, projectLeadId))
            throw forbidden("Only project leaders can unassign contributions");

        final var contribution = contributionStoragePort.findContributionById(projectId, contributionId);
        if (contribution.getType() != ContributionType.ISSUE)
            throw badRequest("Only issues can be unassigned");

        if (contribution.getStatus() != ContributionStatus.IN_PROGRESS)
            throw badRequest("Only in progress contributions can be unassigned");

        final var githubToken = githubAppService.getInstallationTokenFor(contribution.getGithubRepo().getId())
                .orElseThrow(() -> internalServerError("Could not to generate installation token for GitHub repo %d".formatted(contribution.getGithubRepo().getId())));

        githubApiPort.unassign(githubToken, contribution.getGithubRepo().getId(), contribution.getGithubNumber(), contribution.getContributor().getLogin());
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
    public void onProjectCreated(UUID projectId, UUID projectLeadId) {
    }

    @Override
    public void onProjectCategorySuggested(String categoryName, UUID userId) {
    }
}
