package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
import onlydust.com.marketplace.kernel.port.output.IndexerPort;
import onlydust.com.marketplace.project.domain.gateway.DateProvider;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.project.domain.port.input.ProjectObserverPort;
import onlydust.com.marketplace.project.domain.port.output.*;
import onlydust.com.marketplace.project.domain.view.*;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@AllArgsConstructor
public class ProjectService implements ProjectFacadePort {

    private static final Pattern ISSUE_URL_REGEX = Pattern.compile(
            "https://github\\.com/([^/]+)/([^/]+)/issues/([0-9]+)/?");
    private static final Pattern PULL_REQUEST_URL_REGEX = Pattern.compile(
            "https://github\\.com/([^/]+)/([^/]+)/pull/([0-9]+)/?");
    private static final int STALE_CONTRIBUTION_THRESHOLD_IN_DAYS = 10;
    private static final int NEWCOMER_THRESHOLD_IN_MONTHS = 1;

    private final ProjectObserverPort projectObserverPort;
    private final ProjectStoragePort projectStoragePort;
    private final ImageStoragePort imageStoragePort;
    private final UUIDGeneratorPort uuidGeneratorPort;
    private final PermissionService permissionService;
    private final IndexerPort indexerPort;
    private final DateProvider dateProvider;
    private final ContributionStoragePort contributionStoragePort;
    private final DustyBotStoragePort dustyBotStoragePort;
    private final GithubStoragePort githubStoragePort;

    @Override
    public ProjectDetailsView getById(UUID projectId, User caller) {
        final var userId = caller == null ? null : caller.getId();

        final ProjectDetailsView projectById = projectStoragePort.getById(projectId, caller);
        if (!permissionService.hasUserAccessToProject(projectId, userId)) {
            throw OnlyDustException.forbidden("Project %s is private and user %s cannot access it".formatted(projectId, userId));
        }
        return projectById;
    }

    @Override
    public ProjectDetailsView getBySlug(String slug, User caller) {
        final var userId = caller == null ? null : caller.getId();

        final ProjectDetailsView projectBySlug = projectStoragePort.getBySlug(slug, caller);
        if (!permissionService.hasUserAccessToProject(slug, userId)) {
            throw OnlyDustException.forbidden("Project %s is private and user %s cannot access it".formatted(slug,
                    userId));
        }
        return projectBySlug;
    }

    @Override
    public Page<ProjectCardView> getByTagsTechnologiesEcosystemsUserIdSearchSortBy(List<Project.Tag> tags, List<String> technologies,
                                                                                   List<UUID> ecosystemIds, String search,
                                                                                   ProjectCardView.SortBy sort, UUID userId
            , Boolean mine, Integer pageIndex, Integer pageSize) {
        return projectStoragePort.findByTagsTechnologiesEcosystemsUserIdSearchSortBy(tags, technologies, ecosystemIds, userId, search,
                sort, mine, pageIndex, pageSize);
    }

    @Override
    public Page<ProjectCardView> getByTagsTechnologiesEcosystemsSearchSortBy(List<Project.Tag> tags, List<String> technologies, List<UUID> ecosystemIds,
                                                                             String search, ProjectCardView.SortBy sort, Integer pageIndex, Integer pageSize) {
        return projectStoragePort.findByTagsTechnologiesEcosystemsSearchSortBy(tags, technologies, ecosystemIds, search, sort,
                pageIndex, pageSize);
    }

    @Override
    @Transactional
    public Pair<UUID, String> createProject(final UUID projectLeadId, final CreateProjectCommand command) {
        final var slug = Project.slugOf(command.getName());
        if (projectStoragePort.getProjectIdBySlug(slug).isPresent()) {
            throw OnlyDustException.badRequest("Project with slug '%s' already exists".formatted(slug));
        }
        if (command.getGithubUserIdsAsProjectLeadersToInvite() != null) {
            indexerPort.indexUsers(command.getGithubUserIdsAsProjectLeadersToInvite());
        }

        final UUID projectId = uuidGeneratorPort.generate();

        this.projectStoragePort.createProject(projectId,
                slug,
                command.getName(),
                command.getShortDescription(), command.getLongDescription(),
                command.getIsLookingForContributors(), command.getMoreInfos(),
                command.getGithubRepoIds(),
                command.getFirstProjectLeaderId(),
                command.getGithubUserIdsAsProjectLeadersToInvite(),
                ProjectVisibility.PUBLIC,
                command.getImageUrl(),
                ProjectRewardSettings.defaultSettings(dateProvider.now()), command.getEcosystemIds());

        projectObserverPort.onProjectCreated(projectId, projectLeadId);
        if (nonNull(command.getGithubRepoIds())) {
            projectObserverPort.onLinkedReposChanged(projectId, Set.copyOf(command.getGithubRepoIds()), Set.of());
        }
        return Pair.of(projectId, slug);
    }

    @Override
    @Transactional
    public Pair<UUID, String> updateProject(UUID projectLeadId, UpdateProjectCommand command) {
        if (!permissionService.isUserProjectLead(command.getId(), projectLeadId)) {
            throw OnlyDustException.forbidden("Only project leads can update their projects");
        }
        final var slug = Project.slugOf(command.getName());
        if (projectStoragePort.getProjectIdBySlug(slug).stream().anyMatch(id -> !id.equals(command.getId()))) {
            throw OnlyDustException.badRequest("Project with slug '%s' already exists".formatted(slug));
        }
        checkProjectLeadersToKeep(command);

        if (command.getGithubUserIdsAsProjectLeadersToInvite() != null) {
            indexerPort.indexUsers(command.getGithubUserIdsAsProjectLeadersToInvite());
        }

        final Set<Long> invitedLeaderGithubIds = new HashSet<>();
        final Set<Long> invitationCancelledLeaderGithubIds = new HashSet<>();
        getLeaderInvitationsChanges(command, invitationCancelledLeaderGithubIds, invitedLeaderGithubIds);

        final Set<Long> linkedRepoIds = new HashSet<>();
        final Set<Long> unlinkedRepoIds = new HashSet<>();
        getLinkedReposChanges(command, linkedRepoIds, unlinkedRepoIds);

        this.projectStoragePort.updateProject(command.getId(),
                slug,
                command.getName(),
                command.getShortDescription(), command.getLongDescription(),
                command.getIsLookingForContributors(), command.getMoreInfos(),
                command.getGithubRepoIds(),
                command.getGithubUserIdsAsProjectLeadersToInvite(),
                command.getProjectLeadersToKeep(), command.getImageUrl(),
                command.getRewardSettings(), command.getEcosystemIds());

        if (!isNull(command.getGithubRepoIds())) {
            projectObserverPort.onLinkedReposChanged(command.getId(), linkedRepoIds, unlinkedRepoIds);
        }
        if (!isNull(command.getRewardSettings())) {
            projectObserverPort.onRewardSettingsChanged(command.getId());
        }
        return Pair.of(command.getId(), slug);
    }

    private void checkProjectLeadersToKeep(UpdateProjectCommand command) {
        if (command.getProjectLeadersToKeep() == null) {
            return;
        }

        final var projectLeadIds = projectStoragePort.getProjectLeadIds(command.getId());
        if (command.getProjectLeadersToKeep().stream()
                .anyMatch(userId -> projectLeadIds.stream()
                        .noneMatch(projectLeaderId -> projectLeaderId.equals(userId)))) {
            throw OnlyDustException.badRequest("Project leaders to keep must be a subset of current project " +
                                               "leaders");
        }
    }

    private void getLinkedReposChanges(UpdateProjectCommand command, Set<Long> linkedRepoIds,
                                       Set<Long> unlinkedRepoIds) {
        if (command.getGithubRepoIds() != null) {
            final var previousRepos = projectStoragePort.getProjectRepoIds(command.getId());
            unlinkedRepoIds.addAll(previousRepos.stream()
                    .filter(repoId -> !command.getGithubRepoIds().contains(repoId))
                    .collect(Collectors.toSet()));

            linkedRepoIds.addAll(command.getGithubRepoIds().stream()
                    .filter(repoId -> !previousRepos.contains(repoId))
                    .collect(Collectors.toSet()));
        }
    }

    private void getLeaderInvitationsChanges(UpdateProjectCommand command,
                                             Set<Long> invitationCancelledLeaderGithubIds,
                                             Set<Long> invitedLeaderGithubIds) {
        if (command.getGithubUserIdsAsProjectLeadersToInvite() != null) {
            final var projectInvitedLeadIds = projectStoragePort.getProjectInvitedLeadIds(command.getId());
            invitationCancelledLeaderGithubIds.addAll(projectInvitedLeadIds.stream()
                    .filter(leaderId -> !command.getGithubUserIdsAsProjectLeadersToInvite().contains(leaderId))
                    .toList());
            invitedLeaderGithubIds.addAll(command.getGithubUserIdsAsProjectLeadersToInvite().stream()
                    .filter(leaderId -> !projectInvitedLeadIds.contains(leaderId)).toList());
        }
    }

    @Override
    public URL saveLogoImage(InputStream imageInputStream) {
        return this.imageStoragePort.storeImage(imageInputStream);
    }

    @Override
    public ProjectContributorsLinkViewPage getContributors(UUID projectId, String login,
                                                           ProjectContributorsLinkView.SortBy sortBy,
                                                           SortDirection sortDirection,
                                                           Integer pageIndex, Integer pageSize) {
        return projectStoragePort.findContributors(projectId, login, sortBy, sortDirection, pageIndex, pageSize);
    }

    @Override
    public ProjectContributorsLinkViewPage getContributorsForProjectLeadId(UUID projectId, String login,
                                                                           UUID projectLeadId, Boolean showHidden,
                                                                           ProjectContributorsLinkView.SortBy sortBy,
                                                                           SortDirection sortDirection,
                                                                           Integer pageIndex,
                                                                           Integer pageSize) {
        if (permissionService.isUserProjectLead(projectId, projectLeadId)) {
            return projectStoragePort.findContributorsForProjectLead(projectId, projectLeadId, login, showHidden, sortBy, sortDirection, pageIndex,
                    pageSize);
        } else {
            return projectStoragePort.findContributors(projectId, login, sortBy, sortDirection, pageIndex, pageSize);
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

    @Override
    public Page<ContributionView> staledContributions(UUID projectId, User caller, Integer page, Integer pageSize) {
        final var filters = ContributionView.Filters.builder()
                .projects(List.of(projectId))
                .statuses(List.of(ContributionStatus.IN_PROGRESS))
                .to(Date.from(ZonedDateTime.now().minusDays(STALE_CONTRIBUTION_THRESHOLD_IN_DAYS).toInstant()))
                .build();

        return contributions(projectId, caller, filters, ContributionView.Sort.CREATED_AT, SortDirection.desc, page, pageSize);
    }

    @Override
    public Page<ChurnedContributorView> churnedContributors(UUID projectId, User caller, Integer page,
                                                            Integer pageSize) {
        if (!permissionService.isUserProjectLead(projectId, caller.getId())) {
            throw OnlyDustException.forbidden("Only project leads can view project insights");
        }
        return projectStoragePort.getChurnedContributors(projectId, page, pageSize);
    }

    @Override
    public Page<NewcomerView> newcomers(UUID projectId, User caller, Integer page, Integer pageSize) {
        if (!permissionService.isUserProjectLead(projectId, caller.getId())) {
            throw OnlyDustException.forbidden("Only project leads can view project insights");
        }
        return projectStoragePort.getNewcomers(projectId, ZonedDateTime.now().minusMonths(NEWCOMER_THRESHOLD_IN_MONTHS), page, pageSize);
    }

    @Override
    public Page<ContributorActivityView> mostActives(UUID projectId, User caller, Integer page, Integer pageSize) {
        if (!permissionService.isUserProjectLead(projectId, caller.getId())) {
            throw OnlyDustException.forbidden("Only project leads can view project insights");
        }
        return projectStoragePort.getMostActivesContributors(projectId, page, pageSize);
    }

    @Override
    public void hideContributorForProjectLead(UUID projectId, UUID projectLeadId, Long contributorGithubUserId) {
        if (permissionService.isUserProjectLead(projectId, projectLeadId)) {
            projectStoragePort.hideContributorForProjectLead(projectId, projectLeadId, contributorGithubUserId);
        } else {
            throw OnlyDustException.forbidden("Only project leads can hide contributors on their projects");
        }
    }

    @Override
    public void showContributorForProjectLead(UUID projectId, UUID projectLeadId, Long contributorGithubUserId) {
        if (permissionService.isUserProjectLead(projectId, projectLeadId)) {
            projectStoragePort.showContributorForProjectLead(projectId, projectLeadId, contributorGithubUserId);
        } else {
            throw OnlyDustException.forbidden("Only project leads can show contributors on their projects");
        }
    }

    @Override
    public void updateProjectsTags() {
        final Date now = new Date();
        projectStoragePort.updateProjectsTags(now);
    }

    @Override
    public Page<GithubIssueView> findGoodFirstIssues(UUID projectId, Integer page, Integer pageSize) {
        return githubStoragePort.findGoodFirstIssuesForProject(projectId, page, pageSize);
    }

    @Override
    public void suggestCategory(String categoryName, UUID userId) {
        projectStoragePort.createCategory(ProjectCategory.suggest(categoryName));
        projectObserverPort.onProjectCategorySuggested(categoryName, userId);
    }
}
