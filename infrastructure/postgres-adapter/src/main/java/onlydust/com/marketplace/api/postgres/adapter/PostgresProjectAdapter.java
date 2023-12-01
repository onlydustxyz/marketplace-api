package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.model.*;
import onlydust.com.marketplace.api.domain.model.notification.ProjectLeaderAssigned;
import onlydust.com.marketplace.api.domain.model.notification.ProjectLeaderInvitationCancelled;
import onlydust.com.marketplace.api.domain.model.notification.ProjectLeaderInvited;
import onlydust.com.marketplace.api.domain.model.notification.ProjectLeaderUnassigned;
import onlydust.com.marketplace.api.domain.port.output.NotificationPort;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.*;
import onlydust.com.marketplace.api.postgres.adapter.mapper.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectIdRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeadRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeaderInvitationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectRepoRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@AllArgsConstructor
public class PostgresProjectAdapter implements ProjectStoragePort {

    private static final int TOP_CONTRIBUTOR_COUNT = 3;
    private final NotificationPort notificationPort;
    private final ProjectRepository projectRepository;
    private final ProjectViewRepository projectViewRepository;
    private final ProjectIdRepository projectIdRepository;
    private final ProjectLeaderInvitationRepository projectLeaderInvitationRepository;
    private final ProjectLeadRepository projectLeadRepository;
    private final ProjectRepoRepository projectRepoRepository;
    private final CustomProjectRepository customProjectRepository;
    private final CustomContributorRepository customContributorRepository;
    private final CustomProjectRewardRepository customProjectRewardRepository;
    private final CustomProjectBudgetRepository customProjectBudgetRepository;
    private final ProjectLeadViewRepository projectLeadViewRepository;
    private final CustomRewardRepository customRewardRepository;
    private final ProjectsPageRepository projectsPageRepository;
    private final ProjectsPageFiltersRepository projectsPageFiltersRepository;
    private final RewardableItemRepository rewardableItemRepository;
    private final ProjectMoreInfoRepository projectMoreInfoRepository;

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailsView getById(UUID projectId) {
        final var projectEntity = projectViewRepository.findById(projectId)
                .orElseThrow(() -> OnlyDustException.notFound(format("Project %s not found", projectId)));
        return getProjectDetails(projectEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailsView getBySlug(String slug) {
        final var projectEntity = projectViewRepository.findByKey(slug)
                .orElseThrow(() -> OnlyDustException.notFound(format("Project '%s' not found", slug)));
        return getProjectDetails(projectEntity);
    }

    @Override
    public String getProjectSlugById(UUID projectId) {
        return projectViewRepository.findById(projectId)
                .orElseThrow(() -> OnlyDustException.notFound(format("Project %s not found", projectId)))
                .getKey();
    }

    @Override
    public RewardableItemView getRewardableIssue(String repoOwner, String repoName, long issueNumber) {
        return rewardableItemRepository.findRewardableIssue(repoOwner, repoName, issueNumber)
                .map(RewardableItemMapper::itemToDomain)
                .orElseThrow(() -> OnlyDustException.notFound(format("Issue %s/%s#%d not found", repoOwner, repoName,
                        issueNumber)));
    }

    @Override
    public RewardableItemView getRewardablePullRequest(String repoOwner, String repoName, long pullRequestNumber) {
        return rewardableItemRepository.findRewardablePullRequest(repoOwner, repoName, pullRequestNumber)
                .map(RewardableItemMapper::itemToDomain)
                .orElseThrow(() -> OnlyDustException.notFound(format("Pull request %s/%s#%d not found", repoOwner,
                        repoName,
                        pullRequestNumber)));
    }

    @Override
    public Set<Long> removeUsedRepos(Collection<Long> repoIds) {
        final var usedRepos = projectRepoRepository.findAllByRepoId(repoIds).stream()
                .map(ProjectRepoEntity::getPrimaryKey)
                .map(ProjectRepoEntity.PrimaryKey::getRepoId)
                .collect(Collectors.toUnmodifiableSet());

        return repoIds.stream()
                .filter(repoId -> !usedRepos.contains(repoId))
                .collect(Collectors.toSet());
    }

    private ProjectDetailsView getProjectDetails(ProjectViewEntity projectView) {
        final var topContributors = customContributorRepository.findProjectTopContributors(projectView.getId(),
                TOP_CONTRIBUTOR_COUNT);
        final var contributorCount = customContributorRepository.getProjectContributorCount(projectView.getId(), null);
        final var leaders = projectLeadViewRepository.findProjectLeadersAndInvitedLeaders(projectView.getId());
        final var sponsors = customProjectRepository.getProjectSponsors(projectView.getId());
        // TODO : migrate to multi-token
        final BigDecimal remainingUsdBudget = customProjectRepository.getUSDBudget(projectView.getId());
        return ProjectMapper.mapToProjectDetailsView(projectView, topContributors, contributorCount, leaders
                , sponsors, remainingUsdBudget);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectCardView> findByTechnologiesSponsorsUserIdSearchSortBy(List<String> technologies,
                                                                              List<UUID> sponsorIds, UUID userId,
                                                                              String search,
                                                                              ProjectCardView.SortBy sort,
                                                                              Boolean mine, Integer pageIndex,
                                                                              Integer pageSize) {
        final String sponsorsJsonPath = ProjectPageItemViewEntity.getSponsorsJsonPath(sponsorIds);
        final String technologiesJsonPath = ProjectPageItemViewEntity.getTechnologiesJsonPath(technologies);
        final Long count = projectsPageRepository.countProjectsForUserId(userId, mine, technologiesJsonPath,
                sponsorsJsonPath, search);
        final List<ProjectPageItemViewEntity> projectsForUserId =
                projectsPageRepository.findProjectsForUserId(userId, mine,
                        technologiesJsonPath, sponsorsJsonPath, search, isNull(sort) ?
                                ProjectCardView.SortBy.NAME.name() : sort.name(),
                        PaginationMapper.getPostgresOffsetFromPagination(pageSize, pageIndex), pageSize);
        final Map<String, Set<Object>> filters = ProjectPageItemFiltersViewEntity.entitiesToFilters(
                projectsPageFiltersRepository.findFiltersForUser(userId, mine, technologiesJsonPath, sponsorsJsonPath,
                        search));
        return Page.<ProjectCardView>builder()
                .content(projectsForUserId.stream().map(p -> p.toView(userId)).toList())
                .totalItemNumber(count.intValue())
                .totalPageNumber(PaginationHelper.calculateTotalNumberOfPage(pageSize, count.intValue()))
                .filters(filters)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectCardView> findByTechnologiesSponsorsSearchSortBy(List<String> technologies,
                                                                        List<UUID> sponsorIds, String search,
                                                                        ProjectCardView.SortBy sort,
                                                                        Integer pageIndex, Integer pageSize) {

        final String sponsorsJsonPath = ProjectPageItemViewEntity.getSponsorsJsonPath(sponsorIds);
        final String technologiesJsonPath = ProjectPageItemViewEntity.getTechnologiesJsonPath(technologies);
        final List<ProjectPageItemViewEntity> projectsForAnonymousUser =
                projectsPageRepository.findProjectsForAnonymousUser(technologiesJsonPath, sponsorsJsonPath, search,
                        isNull(sort) ?
                                ProjectCardView.SortBy.NAME.name() : sort.name(),
                        PaginationMapper.getPostgresOffsetFromPagination(pageSize, pageIndex), pageSize);
        final Long count = projectsPageRepository.countProjectsForAnonymousUser(technologiesJsonPath,
                sponsorsJsonPath, search);
        final Map<String, Set<Object>> filters = ProjectPageItemFiltersViewEntity.entitiesToFilters(
                projectsPageFiltersRepository.findFiltersForAnonymousUser(technologiesJsonPath, sponsorsJsonPath,
                        search));
        return Page.<ProjectCardView>builder()
                .content(projectsForAnonymousUser.stream().map(p -> p.toView(null)).toList())
                .totalItemNumber(count.intValue())
                .totalPageNumber(PaginationHelper.calculateTotalNumberOfPage(pageSize, count.intValue()))
                .filters(filters)
                .build();
    }

    @Override
    @Transactional
    public String createProject(UUID projectId, String name, String shortDescription, String longDescription,
                                Boolean isLookingForContributors, List<MoreInfoLink> moreInfos,
                                List<Long> githubRepoIds, UUID firstProjectLeaderId,
                                List<Long> githubUserIdsAsProjectLeads,
                                ProjectVisibility visibility, String imageUrl, ProjectRewardSettings rewardSettings) {
        final ProjectEntity projectEntity =
                ProjectEntity.builder()
                        .id(projectId)
                        .name(name)
                        .shortDescription(shortDescription)
                        .longDescription(longDescription)
                        .hiring(isLookingForContributors)
                        .logoUrl(imageUrl)
                        .visibility(ProjectMapper.projectVisibilityToEntity(visibility))
                        .ignorePullRequests(rewardSettings.getIgnorePullRequests())
                        .ignoreIssues(rewardSettings.getIgnoreIssues())
                        .ignoreCodeReviews(rewardSettings.getIgnoreCodeReviews())
                        .ignoreContributionsBefore(rewardSettings.getIgnoreContributionsBefore())
                        .rank(0)
                        .build();

        this.projectIdRepository.save(new ProjectIdEntity(projectId));
        this.projectRepository.save(projectEntity);
        this.projectLeadRepository.save(new ProjectLeadEntity(projectId, firstProjectLeaderId));
        notificationPort.push(new ProjectLeaderAssigned(projectId, firstProjectLeaderId, new Date()));

        if (nonNull(githubUserIdsAsProjectLeads)) {
            githubUserIdsAsProjectLeads.forEach(githubUserId ->
                    notificationPort.push(new ProjectLeaderInvited(projectId, githubUserId, new Date())));

            projectLeaderInvitationRepository.saveAll(githubUserIdsAsProjectLeads.stream()
                    .map(githubUserId -> new ProjectLeaderInvitationEntity(
                            UUID.randomUUID(),
                            projectId,
                            githubUserId))
                    .collect(Collectors.toSet()));
        }

        if (nonNull(githubRepoIds)) {
            projectRepoRepository.saveAll(githubRepoIds.stream()
                    .map(repoId -> new ProjectRepoEntity(projectId, repoId))
                    .collect(Collectors.toSet()));
        }
        if (nonNull(moreInfos)) {
            projectMoreInfoRepository.saveAll(moreInfos.stream()
                    .map(moreInfoLink -> ProjectMoreInfoEntity.builder()
                            .id(ProjectMoreInfoEntity.Id.builder().url(moreInfoLink.getUrl()).projectId(projectId).build())
                            .name(moreInfoLink.getValue()).build()).collect(Collectors.toList()));
        }

        return projectRepository.getKeyById(projectId);
    }

    @Override
    @Transactional
    public void updateProject(UUID projectId, String name, String shortDescription,
                              String longDescription,
                              Boolean isLookingForContributors, List<MoreInfoLink> moreInfos,
                              List<Long> githubRepoIds, List<Long> githubUserIdsAsProjectLeadersToInvite,
                              List<UUID> projectLeadersToKeep, String imageUrl,
                              ProjectRewardSettings rewardSettings) {
        final var project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> OnlyDustException.notFound(format("Project %s not found", projectId)));
        project.setName(name);
        project.setShortDescription(shortDescription);
        project.setLongDescription(longDescription);
        project.setHiring(isLookingForContributors);
        project.setLogoUrl(imageUrl);

        if (!isNull(rewardSettings)) {
            project.setIgnorePullRequests(rewardSettings.getIgnorePullRequests());
            project.setIgnoreIssues(rewardSettings.getIgnoreIssues());
            project.setIgnoreCodeReviews(rewardSettings.getIgnoreCodeReviews());
            project.setIgnoreContributionsBefore(rewardSettings.getIgnoreContributionsBefore());
        }

        if (nonNull(project.getMoreInfos())) {
            project.getMoreInfos().stream()
                    .filter(existingMoreInfo -> moreInfos.stream().noneMatch(moreInfoLink -> moreInfoLink.getUrl().equals(existingMoreInfo.getId().getUrl())))
                    .forEach(projectMoreInfoRepository::delete);

            moreInfos.stream()
                    .map(moreInfoLink -> ProjectMoreInfoEntity.builder()
                            .id(ProjectMoreInfoEntity.Id.builder().url(moreInfoLink.getUrl()).projectId(projectId).build())
                            .name(moreInfoLink.getValue()).build())
                    .forEach(projectMoreInfoRepository::save);
        }

        final var projectLeaderInvitations = project.getProjectLeaderInvitations();
        if (!isNull(githubUserIdsAsProjectLeadersToInvite)) {
            projectLeaderInvitations.stream()
                    .filter(invitation -> !githubUserIdsAsProjectLeadersToInvite.contains(invitation.getGithubUserId()))
                    .forEach(invitation -> {
                        projectLeaderInvitationRepository.delete(invitation);
                        notificationPort.push(new ProjectLeaderInvitationCancelled(projectId,
                                invitation.getGithubUserId(), new Date()));
                    });

            githubUserIdsAsProjectLeadersToInvite.stream()
                    .filter(githubUserId -> projectLeaderInvitations.stream()
                            .noneMatch(invitation -> invitation.getGithubUserId().equals(githubUserId)))
                    .forEach(githubUserId -> {
                        projectLeaderInvitationRepository.save(
                                new ProjectLeaderInvitationEntity(UUID.randomUUID(), projectId, githubUserId));
                        notificationPort.push(new ProjectLeaderInvited(projectId, githubUserId, new Date()));
                    });
        }

        final var projectLeaders = project.getProjectLeaders();
        if (!isNull(projectLeadersToKeep)) {
            projectLeaders.stream()
                    .filter(projectLeader -> !projectLeadersToKeep.contains(projectLeader.getPrimaryKey().getUserId()))
                    .forEach(entity -> {
                        projectLeadRepository.delete(entity);
                        notificationPort.push(new ProjectLeaderUnassigned(projectId,
                                entity.getPrimaryKey().getUserId(), new Date()));
                    });

            if (projectLeadersToKeep.stream()
                    .anyMatch(userId -> projectLeaders.stream()
                            .noneMatch(projectLeader -> projectLeader.getPrimaryKey().getUserId().equals(userId)))) {
                throw OnlyDustException.badRequest("Project leaders to keep must be a subset of current project " +
                                                   "leaders");
            }
        }

        final var repos = project.getRepos();
        if (!isNull(githubRepoIds)) {
            repos.stream()
                    .filter(repo -> !githubRepoIds.contains(repo.getPrimaryKey().getRepoId()))
                    .forEach(projectRepoRepository::delete);

            githubRepoIds.stream()
                    .filter(repoId -> repos.stream()
                            .noneMatch(repo -> repo.getPrimaryKey().getRepoId().equals(repoId)))
                    .forEach(repoId -> projectRepoRepository.save(new ProjectRepoEntity(
                            projectId,
                            repoId)));
        }

        this.projectRepository.save(project);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<ProjectContributorsLinkView> findContributors(UUID projectId, String login,
                                                              ProjectContributorsLinkView.SortBy sortBy,
                                                              SortDirection sortDirection,
                                                              int pageIndex, int pageSize) {
        final Integer count = customContributorRepository.getProjectContributorCount(projectId, login);
        final List<ProjectContributorsLinkView> projectContributorsLinkViews =
                customContributorRepository.getProjectContributorViewEntity(projectId, login, sortBy, sortDirection,
                                pageIndex, pageSize)
                        .stream().map(ProjectContributorsMapper::mapToDomainWithoutProjectLeadData)
                        .toList();
        return Page.<ProjectContributorsLinkView>builder()
                .content(projectContributorsLinkViews)
                .totalItemNumber(count)
                .totalPageNumber(PaginationHelper.calculateTotalNumberOfPage(pageSize, count))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectContributorsLinkView> findContributorsForProjectLead(UUID projectId, String login,
                                                                            ProjectContributorsLinkView.SortBy sortBy,
                                                                            SortDirection sortDirection,
                                                                            int pageIndex, int pageSize) {
        final Integer count = customContributorRepository.getProjectContributorCount(projectId, login);
        final List<ProjectContributorsLinkView> projectContributorsLinkViews =
                customContributorRepository.getProjectContributorViewEntity(projectId, login, sortBy, sortDirection,
                                pageIndex, pageSize)
                        .stream().map(ProjectContributorsMapper::mapToDomainWithProjectLeadData)
                        .toList();
        return Page.<ProjectContributorsLinkView>builder()
                .content(projectContributorsLinkViews)
                .totalItemNumber(count)
                .totalPageNumber(PaginationHelper.calculateTotalNumberOfPage(pageSize, count))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> getProjectLeadIds(UUID projectId) {
        return projectLeadViewRepository.findProjectLeaders(projectId)
                .stream()
                .map(ProjectLeadViewEntity::getId)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectRewardView> findRewards(UUID projectId, ProjectRewardView.SortBy sortBy,
                                               SortDirection sortDirection, int pageIndex,
                                               int pageSize) {
        final Integer count = customProjectRewardRepository.getCount(projectId);
        final List<ProjectRewardView> projectRewardViews = customProjectRewardRepository.getViewEntities(projectId,
                        sortBy, sortDirection, pageIndex, pageSize)
                .stream().map(ProjectRewardMapper::mapEntityToDomain)
                .toList();
        return Page.<ProjectRewardView>builder()
                .content(projectRewardViews)
                .totalItemNumber(count)
                .totalPageNumber(PaginationHelper.calculateTotalNumberOfPage(pageSize, count))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectBudgetsView findBudgets(UUID projectId) {
        return ProjectBudgetsView.builder().budgets(customProjectBudgetRepository.findProjectBudgetByProjectId(projectId)
                        .stream().map(BudgetMapper::entityToDomain)
                        .toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RewardView getProjectReward(UUID rewardId) {
        return RewardMapper.rewardToDomain(customRewardRepository.findProjectRewardViewEntityByd(rewardId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RewardItemView> getProjectRewardItems(UUID rewardId, int pageIndex, int pageSize) {
        final Integer count = customRewardRepository.countRewardItemsForRewardId(rewardId);
        final List<RewardItemView> rewardItemViews =
                customRewardRepository.findRewardItemsByRewardId(rewardId, pageIndex, pageSize)
                        .stream()
                        .map(RewardMapper::itemToDomain)
                        .toList();
        return Page.<RewardItemView>builder()
                .content(rewardItemViews)
                .totalItemNumber(count)
                .totalPageNumber(PaginationHelper.calculateTotalNumberOfPage(pageSize, count))
                .build();
    }

    @Override
    public Page<RewardableItemView> getProjectRewardableItemsByTypeForProjectLeadAndContributorId(UUID projectId,
                                                                                                  ContributionType contributionType,
                                                                                                  ContributionStatus contributionStatus,
                                                                                                  Long githubUserid,
                                                                                                  int pageIndex,
                                                                                                  int pageSize,
                                                                                                  String search,
                                                                                                  Boolean includeIgnoredItems) {

        final List<RewardableItemView> rewardableItemViews =
                rewardableItemRepository.findByProjectIdAndGithubUserId(projectId, githubUserid,
                                ContributionViewEntity.Type.fromViewToString(contributionType),
                                ContributionViewEntity.Status.fromViewToString(contributionStatus),
                                search,
                                PaginationMapper.getPostgresOffsetFromPagination(pageSize, pageIndex),
                                pageSize, includeIgnoredItems)
                        .stream()
                        .map(RewardableItemMapper::itemToDomain)
                        .toList();
        final Long count = rewardableItemRepository.countByProjectIdAndGithubUserId(projectId, githubUserid,
                ContributionViewEntity.Type.fromViewToString(contributionType),
                ContributionViewEntity.Status.fromViewToString(contributionStatus),
                search, includeIgnoredItems);
        return Page.<RewardableItemView>builder()
                .content(rewardableItemViews)
                .totalItemNumber(count.intValue())
                .totalPageNumber(PaginationHelper.calculateTotalNumberOfPage(pageSize, count.intValue()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getProjectRepoIds(UUID projectId) {
        final var project = projectRepository.getById(projectId);
        return project.getRepos() == null ? Set.of() : project.getRepos().stream()
                .map(repo -> repo.getPrimaryKey().getRepoId())
                .collect(Collectors.toSet());
    }
}
