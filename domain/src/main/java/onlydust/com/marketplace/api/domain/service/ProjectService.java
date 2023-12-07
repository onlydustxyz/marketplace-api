package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.gateway.DateProvider;
import onlydust.com.marketplace.api.domain.model.*;
import onlydust.com.marketplace.api.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.api.domain.port.output.*;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@AllArgsConstructor
public class ProjectService implements ProjectFacadePort {

    private static final Pattern ISSUE_URL_REGEX = Pattern.compile(
            "https://github\\.com/([^/]+)/([^/]+)/issues/([0-9]+)/?");
    private static final Pattern PULL_REQUEST_URL_REGEX = Pattern.compile(
            "https://github\\.com/([^/]+)/([^/]+)/pull/([0-9]+)/?");

    private final ProjectStoragePort projectStoragePort;
    private final ImageStoragePort imageStoragePort;
    private final UUIDGeneratorPort uuidGeneratorPort;
    private final PermissionService permissionService;
    private final IndexerPort indexerPort;
    private final DateProvider dateProvider;
    private final EventStoragePort eventStoragePort;
    private final ContributionStoragePort contributionStoragePort;
    private final DustyBotStoragePort dustyBotStoragePort;
    private final GithubStoragePort githubStoragePort;

    @Override
    public ProjectDetailsView getById(UUID projectId, UUID userId) {
        if (!permissionService.hasUserAccessToProject(projectId, userId)) {
            throw OnlyDustException.forbidden("Project %s is private and user %s cannot access it".formatted(projectId,
                    userId));
        }
        return projectStoragePort.getById(projectId);
    }

    @Override
    public ProjectDetailsView getBySlug(String slug, UUID userId) {
        if (!permissionService.hasUserAccessToProject(slug, userId)) {
            throw OnlyDustException.forbidden("Project %s is private and user %s cannot access it".formatted(slug,
                    userId));
        }
        return projectStoragePort.getBySlug(slug);
    }


    @Override
    public Page<ProjectCardView> getByTechnologiesSponsorsUserIdSearchSortBy(List<String> technologies,
                                                                             List<UUID> sponsorIds, String search,
                                                                             ProjectCardView.SortBy sort, UUID userId
            , Boolean mine, Integer pageIndex, Integer pageSize) {
        return projectStoragePort.findByTechnologiesSponsorsUserIdSearchSortBy(technologies, sponsorIds, userId, search,
                sort, mine, pageIndex, pageSize);
    }

    @Override
    public Page<ProjectCardView> getByTechnologiesSponsorsSearchSortBy(List<String> technologies, List<UUID> sponsorIds,
                                                                       String search, ProjectCardView.SortBy sort,
                                                                       Integer pageIndex, Integer pageSize) {
        return projectStoragePort.findByTechnologiesSponsorsSearchSortBy(technologies, sponsorIds, search, sort,
                pageIndex, pageSize);
    }

    @Override
    public Pair<UUID, String> createProject(CreateProjectCommand command) {
        if (command.getGithubUserIdsAsProjectLeadersToInvite() != null) {
            indexerPort.indexUsers(command.getGithubUserIdsAsProjectLeadersToInvite());
        }

        indexerPort.onRepoLinkChanged(command.getGithubRepoIds().stream().collect(Collectors.toUnmodifiableSet()),
                Set.of());

        final UUID projectId = uuidGeneratorPort.generate();
        final String projectSlug = this.projectStoragePort.createProject(projectId, command.getName(),
                command.getShortDescription(), command.getLongDescription(),
                command.getIsLookingForContributors(), command.getMoreInfos(),
                command.getGithubRepoIds(),
                command.getFirstProjectLeaderId(),
                command.getGithubUserIdsAsProjectLeadersToInvite(),
                ProjectVisibility.PUBLIC,
                command.getImageUrl(),
                ProjectRewardSettings.defaultSettings(dateProvider.now()));


        eventStoragePort.saveEvent(new ProjectCreatedEvent(projectId));
        return Pair.of(projectId, projectSlug);
    }

    @Override
    public Pair<UUID, String> updateProject(UUID projectLeadId, UpdateProjectCommand command) {
        if (!permissionService.isUserProjectLead(command.getId(), projectLeadId)) {
            throw OnlyDustException.forbidden("Only project leads can update their projects");
        }

        if (command.getGithubUserIdsAsProjectLeadersToInvite() != null) {
            indexerPort.indexUsers(command.getGithubUserIdsAsProjectLeadersToInvite());
        }

        if (command.getGithubRepoIds() != null) {
            final var previousRepos = projectStoragePort.getProjectRepoIds(command.getId());
            final var removedRepos = previousRepos.stream()
                    .filter(repoId -> !command.getGithubRepoIds().contains(repoId))
                    .collect(Collectors.toSet());

            final var newRepos = command.getGithubRepoIds().stream()
                    .filter(repoId -> !previousRepos.contains(repoId))
                    .collect(Collectors.toSet());

            indexerPort.onRepoLinkChanged(newRepos, this.projectStoragePort.removeUsedRepos(removedRepos));
        }

        this.projectStoragePort.updateProject(command.getId(),
                command.getName(),
                command.getShortDescription(), command.getLongDescription(),
                command.getIsLookingForContributors(), command.getMoreInfos(),
                command.getGithubRepoIds(),
                command.getGithubUserIdsAsProjectLeadersToInvite(),
                command.getProjectLeadersToKeep(), command.getImageUrl(),
                command.getRewardSettings());

        if (!isNull(command.getRewardSettings()) || !isNull(command.getGithubRepoIds())) {
            contributionStoragePort.refreshIgnoredContributions(command.getId());
        }
        final String slug = this.projectStoragePort.getProjectSlugById(command.getId());
        return Pair.of(command.getId(), slug);
    }

    @Override
    public URL saveLogoImage(InputStream imageInputStream) {
        return this.imageStoragePort.storeImage(imageInputStream);
    }

    @Override
    public Page<ProjectContributorsLinkView> getContributors(UUID projectId, String login,
                                                             ProjectContributorsLinkView.SortBy sortBy,
                                                             SortDirection sortDirection,
                                                             Integer pageIndex, Integer pageSize) {
        return projectStoragePort.findContributors(projectId, login, sortBy, sortDirection, pageIndex, pageSize);
    }

    @Override
    public Page<ProjectContributorsLinkView> getContributorsForProjectLeadId(UUID projectId, String login,
                                                                             UUID projectLeadId,
                                                                             ProjectContributorsLinkView.SortBy sortBy,
                                                                             SortDirection sortDirection,
                                                                             Integer pageIndex,
                                                                             Integer pageSize) {
        if (permissionService.isUserProjectLead(projectId, projectLeadId)) {
            return projectStoragePort.findContributorsForProjectLead(projectId, login, sortBy, sortDirection, pageIndex,
                    pageSize);
        } else {
            return projectStoragePort.findContributors(projectId, login, sortBy, sortDirection, pageIndex, pageSize);
        }
    }

    @Override
    public ProjectRewardsPageView getRewards(UUID projectId, UUID projectLeadId, Integer pageIndex, Integer pageSize
            , ProjectRewardView.SortBy sortBy, SortDirection sortDirection) {
        if (permissionService.isUserProjectLead(projectId, projectLeadId)) {
            return projectStoragePort.findRewards(projectId, sortBy, sortDirection, pageIndex, pageSize);
        } else {
            throw OnlyDustException.forbidden("Only project leads can read rewards on their projects");
        }
    }

    @Override
    public ProjectBudgetsView getBudgets(UUID projectId, UUID projectLeadId) {
        if (permissionService.isUserProjectLead(projectId, projectLeadId)) {
            return projectStoragePort.findBudgets(projectId);
        } else {
            throw OnlyDustException.forbidden("Only project leads can read budgets on their projects");
        }
    }

    @Override
    public RewardView getRewardByIdForProjectLead(UUID projectId, UUID rewardId, UUID projectLeadId) {
        if (permissionService.isUserProjectLead(projectId, projectLeadId)) {
            return projectStoragePort.getProjectReward(rewardId);
        } else {
            throw OnlyDustException.forbidden("Only project leads can read reward on their projects");
        }
    }

    @Override
    public Page<RewardItemView> getRewardItemsPageByIdForProjectLead(UUID projectId, UUID rewardId,
                                                                     UUID projectLeadId, int pageIndex, int pageSize) {
        if (permissionService.isUserProjectLead(projectId, projectLeadId)) {
            return projectStoragePort.getProjectRewardItems(rewardId, pageIndex, pageSize);
        } else {
            throw OnlyDustException.forbidden("Only project leads can read reward items on their projects");
        }
    }

    @Override
    public Page<RewardableItemView> getRewardableItemsPageByTypeForProjectLeadAndContributorId(UUID projectId,
                                                                                               ContributionType contributionType,
                                                                                               ContributionStatus contributionStatus,
                                                                                               UUID projectLeadId,
                                                                                               Long githubUserid,
                                                                                               int pageIndex,
                                                                                               int pageSize,
                                                                                               String search,
                                                                                               Boolean includeIgnoredItems) {
        if (permissionService.isUserProjectLead(projectId, projectLeadId)) {
            return projectStoragePort.getProjectRewardableItemsByTypeForProjectLeadAndContributorId(projectId,
                    contributionType, contributionStatus, githubUserid, pageIndex, pageSize, search,
                    includeIgnoredItems);
        } else {
            throw OnlyDustException.forbidden("Only project leads can read rewardable items on their projects");
        }
    }

    @Override
    public List<RewardableItemView> getAllCompletedRewardableItemsForProjectLeadAndContributorId(UUID projectId,
                                                                                                 UUID projectLeadId,
                                                                                                 Long githubUserId) {
        if (permissionService.isUserProjectLead(projectId, projectLeadId)) {
            final var allCompletedRewardableItems =
                    projectStoragePort.getProjectRewardableItemsByTypeForProjectLeadAndContributorId(projectId,
                            null, ContributionStatus.COMPLETED, githubUserId, 0, 1_000_000, null, false);
            return allCompletedRewardableItems != null ? allCompletedRewardableItems.getContent() : List.of();
        } else {
            throw OnlyDustException.forbidden("Only project leads can read rewardable items on their projects");
        }
    }

    @Override
    public RewardableItemView createAndCloseIssueForProjectIdAndRepositoryId(CreateAndCloseIssueCommand command) {
        if (permissionService.isUserProjectLead(command.getProjectId(), command.getProjectLeadId())) {
            if (permissionService.isRepoLinkedToProject(command.getProjectId(), command.getGithubRepoId())) {
                final var repo = githubStoragePort.findRepoById(command.getGithubRepoId()).orElseThrow(() ->
                        OnlyDustException.notFound("Repo not found"));
                final var openedIssue = dustyBotStoragePort.createIssue(repo, command.getTitle(),
                        command.getDescription());
                final RewardableItemView closedIssue = dustyBotStoragePort.closeIssue(repo, openedIssue.getNumber());
                indexerPort.indexIssue(repo.getOwner(), repo.getName(), closedIssue.getNumber());
                return closedIssue;
            } else {
                throw OnlyDustException.forbidden("Rewardable issue can only be created on repos linked to this " +
                                                  "project");
            }
        } else {
            throw OnlyDustException.forbidden("Only project leads can create rewardable issue on their projects");
        }
    }

    @Override
    public RewardableItemView addRewardableIssue(UUID projectId, UUID projectLeadId, String issueUrl) {
        if (!permissionService.isUserProjectLead(projectId, projectLeadId)) {
            throw OnlyDustException.forbidden("Only project leads can add other issues as rewardable items");
        }
        final var matcher = ISSUE_URL_REGEX.matcher(issueUrl);
        if (!matcher.matches()) {
            throw OnlyDustException.badRequest("Invalid issue url '%s'".formatted(issueUrl));
        }
        final var repoOwner = matcher.group(1);
        final var repoName = matcher.group(2);
        final var issueNumber = Long.parseLong(matcher.group(3));

        indexerPort.indexIssue(repoOwner, repoName, issueNumber);
        return projectStoragePort.getRewardableIssue(repoOwner, repoName, issueNumber);
    }

    @Override
    public RewardableItemView addRewardablePullRequest(UUID projectId, UUID projectLeadId, String pullRequestUrl) {
        if (!permissionService.isUserProjectLead(projectId, projectLeadId)) {
            throw OnlyDustException.forbidden("Only project leads can add other pull requests as rewardable items");
        }
        final var matcher = PULL_REQUEST_URL_REGEX.matcher(pullRequestUrl);
        if (!matcher.matches()) {
            throw OnlyDustException.badRequest("Invalid pull request url '%s'".formatted(pullRequestUrl));
        }
        final var repoOwner = matcher.group(1);
        final var repoName = matcher.group(2);
        final var pullRequestNumber = Long.parseLong(matcher.group(3));

        indexerPort.indexPullRequest(repoOwner, repoName, pullRequestNumber);
        return projectStoragePort.getRewardablePullRequest(repoOwner, repoName, pullRequestNumber);
    }

    @Override
    public Page<ContributionView> contributions(UUID projectId, User caller, ContributionView.Filters filters,
                                                ContributionView.Sort sort, SortDirection direction,
                                                Integer page, Integer pageSize) {
        if (!permissionService.isUserProjectLead(projectId, caller.getId())) {
            throw OnlyDustException.forbidden("Only project leads can list project contributions");
        }
        return contributionStoragePort.findContributions(caller.getGithubUserId(), filters, sort, direction, page,
                pageSize);
    }

    @Override
    public void updateProjectsRanking() {
        projectStoragePort.updateProjectsRanking();
    }
}
