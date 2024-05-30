package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.HiddenContributorEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectCategoryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectEcosystemEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeadEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeaderInvitationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectRepoEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.PaginationMapper;
import onlydust.com.marketplace.api.postgres.adapter.mapper.ProjectContributorsMapper;
import onlydust.com.marketplace.api.postgres.adapter.mapper.ProjectMapper;
import onlydust.com.marketplace.api.postgres.adapter.mapper.RewardableItemMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ApplicationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeaderInvitationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectRepoRepository;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.view.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static onlydust.com.marketplace.api.postgres.adapter.mapper.ProjectMapper.moreInfosToEntities;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PostgresProjectAdapter implements ProjectStoragePort {
    private static final int CHURNED_CONTRIBUTOR_THRESHOLD_IN_DAYS = 10;
    private static final int CONTRIBUTOR_ACTIVITY_COUNTS_THRESHOLD_IN_WEEKS = 5;
    private static final int TOP_CONTRIBUTOR_COUNT = 3;
    private final ProjectRepository projectRepository;
    private final ProjectViewRepository projectViewRepository;
    private final ProjectLeaderInvitationRepository projectLeaderInvitationRepository;
    private final ProjectRepoRepository projectRepoRepository;
    private final CustomProjectRepository customProjectRepository;
    private final CustomContributorRepository customContributorRepository;
    private final ProjectLeadViewRepository projectLeadViewRepository;
    private final ProjectsPageRepository projectsPageRepository;
    private final ProjectsPageFiltersRepository projectsPageFiltersRepository;
    private final RewardableItemRepository rewardableItemRepository;
    private final CustomProjectRankingRepository customProjectRankingRepository;
    private final ChurnedContributorViewEntityRepository churnedContributorViewEntityRepository;
    private final NewcomerViewEntityRepository newcomerViewEntityRepository;
    private final ContributorActivityViewEntityRepository contributorActivityViewEntityRepository;
    private final ApplicationRepository applicationRepository;
    private final ContributionViewEntityRepository contributionViewEntityRepository;
    private final HiddenContributorRepository hiddenContributorRepository;
    private final ProjectTagRepository projectTagRepository;
    private final ProjectCategoryRepository projectCategoryRepository;
    private final ProjectInfosViewRepository projectInfosViewRepository;

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailsView getById(UUID projectId, User caller) {
        final var projectEntity = projectViewRepository.findById(projectId)
                .orElseThrow(() -> notFound(format("Project %s not found", projectId)));
        return getProjectDetails(projectEntity, caller);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailsView getBySlug(String slug, User caller) {
        final var projectEntity = projectViewRepository.findBySlug(slug)
                .orElseThrow(() -> notFound(format("Project '%s' not found", slug)));
        return getProjectDetails(projectEntity, caller);
    }

    @Override
    public Optional<UUID> getProjectIdBySlug(String slug) {
        return projectViewRepository.findBySlug(slug).map(ProjectViewEntity::getId);
    }

    @Override
    public RewardableItemView getRewardableIssue(String repoOwner, String repoName, long issueNumber) {
        return rewardableItemRepository.findRewardableIssue(repoOwner, repoName, issueNumber)
                .map(RewardableItemMapper::itemToDomain)
                .orElseThrow(() -> notFound(format("Issue %s/%s#%d not found", repoOwner, repoName,
                        issueNumber)));
    }

    @Override
    public RewardableItemView getRewardablePullRequest(String repoOwner, String repoName, long pullRequestNumber) {
        return rewardableItemRepository.findRewardablePullRequest(repoOwner, repoName, pullRequestNumber)
                .map(RewardableItemMapper::itemToDomain)
                .orElseThrow(() -> notFound(format("Pull request %s/%s#%d not found", repoOwner,
                        repoName,
                        pullRequestNumber)));
    }

    @Override
    public Set<Long> removeUsedRepos(Collection<Long> repoIds) {
        final var usedRepos = projectRepoRepository.findAllByRepoId(repoIds).stream()
                .map(ProjectRepoEntity::getRepoId)
                .collect(Collectors.toUnmodifiableSet());

        return repoIds.stream()
                .filter(repoId -> !usedRepos.contains(repoId))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean hasUserAccessToProject(UUID projectId, UUID userId) {
        return customProjectRepository.isProjectPublic(projectId) ||
               (userId != null && customProjectRepository.hasUserAccessToProject(projectId, userId));
    }

    @Override
    public boolean hasUserAccessToProject(String projectSlug, UUID userId) {
        return customProjectRepository.isProjectPublic(projectSlug) ||
               (userId != null && customProjectRepository.hasUserAccessToProject(projectSlug, userId));
    }

    private ProjectDetailsView getProjectDetails(ProjectViewEntity projectView, User caller) {
        final var topContributors = customContributorRepository.findProjectTopContributors(projectView.getId(),
                TOP_CONTRIBUTOR_COUNT);
        final var contributorCount = customContributorRepository.getProjectContributorCount(projectView.getId(), null);
        final var leaders = projectLeadViewRepository.findProjectLeadersAndInvitedLeaders(projectView.getId());
        final var ecosystems = customProjectRepository.getProjectEcosystems(projectView.getId());
        final var hasRemainingBudget = customProjectRepository.hasRemainingBudget(projectView.getId());
        final var me = isNull(caller) ? null : new ProjectDetailsView.Me(
                leaders.stream().anyMatch(l -> l.getGithubId().equals(caller.getGithubUserId()) && l.getHasAcceptedInvitation()),
                leaders.stream().anyMatch(l -> l.getGithubId().equals(caller.getGithubUserId()) && !l.getHasAcceptedInvitation()),
                !contributionViewEntityRepository.findContributions(caller.getGithubUserId(),
                        List.of(caller.getGithubUserId()),
                        List.of(projectView.getId()),
                        List.of(),
                        List.of(),
                        List.of(),
                        new UUID[0],
                        new UUID[0],
                        null,
                        null,
                        Pageable.ofSize(1)).isEmpty(),
                applicationRepository.findByProjectIdAndApplicantId(projectView.getId(), caller.getId()).isPresent()
        );
        return ProjectMapper.mapToProjectDetailsView(projectView, topContributors, contributorCount, leaders, ecosystems, hasRemainingBudget, me);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectCardView> findByTagsTechnologiesEcosystemsUserIdSearchSortBy(List<Project.Tag> tags,
                                                                                    List<String> technologies,
                                                                                    List<UUID> sponsorIds, UUID userId,
                                                                                    String search,
                                                                                    ProjectCardView.SortBy sort,
                                                                                    Boolean mine, Integer pageIndex,
                                                                                    Integer pageSize) {
        final String ecosystemsJsonPath = ProjectPageItemQueryEntity.getEcosystemsJsonPath(sponsorIds);
        final String technologiesJsonPath = ProjectPageItemQueryEntity.getTechnologiesJsonPath(technologies);
        final String tagsJsonPath = ProjectPageItemQueryEntity.getTagsJsonPath(tags);
        final Long count = projectsPageRepository.countProjectsForUserId(userId, mine, tagsJsonPath, technologiesJsonPath,
                ecosystemsJsonPath, search);
        final List<ProjectPageItemQueryEntity> projectsForUserId =
                projectsPageRepository.findProjectsForUserId(userId, mine, tagsJsonPath,
                        technologiesJsonPath, ecosystemsJsonPath, search, isNull(sort) ?
                                ProjectCardView.SortBy.NAME.name() : sort.name(),
                        PaginationMapper.getPostgresOffsetFromPagination(pageSize, pageIndex), pageSize);
        final Map<String, Set<Object>> filters = ProjectPageItemFiltersQueryEntity.entitiesToFilters(
                projectsPageFiltersRepository.findFiltersForUser(userId, mine));
        return Page.<ProjectCardView>builder()
                .content(projectsForUserId.stream().map(p -> p.toView(userId)).toList())
                .totalItemNumber(count.intValue())
                .totalPageNumber(PaginationHelper.calculateTotalNumberOfPage(pageSize, count.intValue()))
                .filters(filters)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectCardView> findByTagsTechnologiesEcosystemsSearchSortBy(List<Project.Tag> tags,
                                                                              List<String> technologies,
                                                                              List<UUID> sponsorIds, String search,
                                                                              ProjectCardView.SortBy sort,
                                                                              Integer pageIndex, Integer pageSize) {

        final String ecosystemsJsonPath = ProjectPageItemQueryEntity.getEcosystemsJsonPath(sponsorIds);
        final String technologiesJsonPath = ProjectPageItemQueryEntity.getTechnologiesJsonPath(technologies);
        final String tagsJsonPath = ProjectPageItemQueryEntity.getTagsJsonPath(tags);
        final List<ProjectPageItemQueryEntity> projectsForAnonymousUser =
                projectsPageRepository.findProjectsForAnonymousUser(tagsJsonPath, technologiesJsonPath, ecosystemsJsonPath, search,
                        isNull(sort) ?
                                ProjectCardView.SortBy.NAME.name() : sort.name(),
                        PaginationMapper.getPostgresOffsetFromPagination(pageSize, pageIndex), pageSize);
        final Long count = projectsPageRepository.countProjectsForAnonymousUser(tagsJsonPath, technologiesJsonPath,
                ecosystemsJsonPath, search);
        final Map<String, Set<Object>> filters = ProjectPageItemFiltersQueryEntity.entitiesToFilters(
                projectsPageFiltersRepository.findFiltersForAnonymousUser());
        return Page.<ProjectCardView>builder()
                .content(projectsForAnonymousUser.stream().map(p -> p.toView(null)).toList())
                .totalItemNumber(count.intValue())
                .totalPageNumber(PaginationHelper.calculateTotalNumberOfPage(pageSize, count.intValue()))
                .filters(filters)
                .build();
    }

    @Override
    @Transactional
    public void createProject(UUID projectId, String slug, String name, String shortDescription, String longDescription,
                              Boolean isLookingForContributors, List<NamedLink> moreInfos,
                              List<Long> githubRepoIds, UUID firstProjectLeaderId,
                              List<Long> githubUserIdsAsProjectLeads,
                              ProjectVisibility visibility, String imageUrl, ProjectRewardSettings rewardSettings, List<UUID> ecosystemIds) {
        final ProjectEntity projectEntity =
                ProjectEntity.builder()
                        .id(projectId)
                        .slug(slug)
                        .name(name)
                        .shortDescription(shortDescription)
                        .longDescription(longDescription)
                        .hiring(isLookingForContributors)
                        .logoUrl(imageUrl)
                        .visibility(visibility)
                        .ignorePullRequests(rewardSettings.getIgnorePullRequests())
                        .ignoreIssues(rewardSettings.getIgnoreIssues())
                        .ignoreCodeReviews(rewardSettings.getIgnoreCodeReviews())
                        .ignoreContributionsBefore(rewardSettings.getIgnoreContributionsBefore())
                        .repos(githubRepoIds == null ? null : githubRepoIds.stream()
                                .map(repoId -> new ProjectRepoEntity(projectId, repoId))
                                .collect(Collectors.toSet()))
                        .moreInfos(moreInfos == null ? null : moreInfosToEntities(moreInfos, projectId))
                        .projectLeaders(Set.of(new ProjectLeadEntity(projectId, firstProjectLeaderId)))
                        .projectLeaderInvitations(githubUserIdsAsProjectLeads == null ? null :
                                githubUserIdsAsProjectLeads.stream()
                                        .map(githubUserId -> new ProjectLeaderInvitationEntity(UUID.randomUUID(),
                                                projectId, githubUserId))
                                        .collect(Collectors.toSet()))
                        .ecosystems(ecosystemIds == null ? null :
                                ecosystemIds.stream().map(ecosystemId -> new ProjectEcosystemEntity(projectId, ecosystemId)).collect(Collectors.toSet()))
                        .rank(0)
                        .build();

        this.projectRepository.saveAndFlush(projectEntity);
    }

    @Override
    @Transactional
    public void updateProject(UUID projectId, String slug, String name, String shortDescription,
                              String longDescription,
                              Boolean isLookingForContributors, List<NamedLink> moreInfos,
                              List<Long> githubRepoIds, List<Long> githubUserIdsAsProjectLeadersToInvite,
                              List<UUID> projectLeadersToKeep, String imageUrl,
                              ProjectRewardSettings rewardSettings, List<UUID> ecosystemIds) {
        final var project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> notFound(format("Project %s not found", projectId)));
        project.setSlug(slug);
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

        if (nonNull(moreInfos)) {
            if (nonNull(project.getMoreInfos())) {
                project.getMoreInfos().clear();
                if (!moreInfos.isEmpty()) {
                    project.getMoreInfos().addAll(moreInfosToEntities(moreInfos, projectId));
                }
            } else {
                project.setMoreInfos(moreInfosToEntities(moreInfos, projectId));
            }
        }

        final var projectLeaderInvitations = project.getProjectLeaderInvitations();
        if (!isNull(githubUserIdsAsProjectLeadersToInvite)) {
            if (nonNull(projectLeaderInvitations)) {
                projectLeaderInvitations.removeIf(invitation -> githubUserIdsAsProjectLeadersToInvite.stream()
                        .noneMatch(githubUserId -> invitation.getGithubUserId().equals(githubUserId) &&
                                                   invitation.getProjectId().equals(projectId)));

                projectLeaderInvitations.addAll(githubUserIdsAsProjectLeadersToInvite.stream()
                        .filter(githubUserId -> projectLeaderInvitations.stream()
                                .noneMatch(invitation -> invitation.getGithubUserId().equals(githubUserId) &&
                                                         invitation.getProjectId().equals(projectId)))
                        .map(githubUserId -> new ProjectLeaderInvitationEntity(UUID.randomUUID(), projectId,
                                githubUserId)).toList());
            } else {
                project.setProjectLeaderInvitations(githubUserIdsAsProjectLeadersToInvite.stream()
                        .map(githubUserId -> new ProjectLeaderInvitationEntity(UUID.randomUUID(), projectId,
                                githubUserId))
                        .collect(Collectors.toSet()));
            }
        }

        final var projectLeaders = project.getProjectLeaders();
        if (!isNull(projectLeadersToKeep)) {
            projectLeaders.clear();
            projectLeaders.addAll(projectLeadersToKeep.stream()
                    .map(userId -> new ProjectLeadEntity(projectId, userId))
                    .collect(Collectors.toUnmodifiableSet()));
        }

        if (!isNull(githubRepoIds)) {
            if (nonNull(project.getRepos())) {
                project.getRepos().clear();
                project.getRepos().addAll(githubRepoIds.stream()
                        .map(repoId -> new ProjectRepoEntity(projectId, repoId))
                        .collect(Collectors.toSet()));
            } else {
                project.setRepos(githubRepoIds.stream()
                        .map(repoId -> new ProjectRepoEntity(projectId, repoId))
                        .collect(Collectors.toSet()));
            }
        }

        if (nonNull(ecosystemIds)) {
            if (nonNull(project.getEcosystems())) {
                project.getEcosystems().clear();
                project.getEcosystems().addAll(ecosystemIds.stream()
                        .map(ecosystemId -> new ProjectEcosystemEntity(projectId, ecosystemId))
                        .collect(Collectors.toSet()));
            } else {
                project.setEcosystems(ecosystemIds.stream()
                        .map(ecosystemId -> new ProjectEcosystemEntity(projectId, ecosystemId))
                        .collect(Collectors.toSet()));
            }
        }

        this.projectRepository.saveAndFlush(project);
    }


    @Override
    @Transactional(readOnly = true)
    public ProjectContributorsLinkViewPage findContributors(UUID projectId, String login,
                                                            ProjectContributorsLinkView.SortBy sortBy,
                                                            SortDirection sortDirection,
                                                            int pageIndex, int pageSize) {
        final Integer count = customContributorRepository.getProjectContributorCount(projectId, login);
        final List<ProjectContributorsLinkView> projectContributorsLinkViews =
                customContributorRepository.getProjectContributorViewEntity(projectId, login, null, true, sortBy, sortDirection,
                                pageIndex, pageSize)
                        .stream().map(ProjectContributorsMapper::mapToDomainWithoutProjectLeadData)
                        .toList();
        return ProjectContributorsLinkViewPage.builder()
                .content(projectContributorsLinkViews)
                .totalItemNumber(count)
                .totalPageNumber(PaginationHelper.calculateTotalNumberOfPage(pageSize, count))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectContributorsLinkViewPage findContributorsForProjectLead(UUID projectId, UUID projectLeadId, String login, Boolean showHidden,
                                                                          ProjectContributorsLinkView.SortBy sortBy,
                                                                          SortDirection sortDirection,
                                                                          int pageIndex, int pageSize) {
        final Integer count = customContributorRepository.getProjectContributorCount(projectId, login);
        final List<ProjectContributorsLinkView> projectContributorsLinkViews =
                customContributorRepository.getProjectContributorViewEntity(projectId, login, projectLeadId, showHidden, sortBy, sortDirection,
                                pageIndex, pageSize)
                        .stream().map(ProjectContributorsMapper::mapToDomainWithProjectLeadData)
                        .toList();
        return ProjectContributorsLinkViewPage.builder()
                .content(projectContributorsLinkViews)
                .totalItemNumber(count)
                .totalPageNumber(PaginationHelper.calculateTotalNumberOfPage(pageSize, count))
                .hasHiddenContributors(hiddenContributorRepository.existsByProjectIdAndProjectLeadId(projectId, projectLeadId))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> getProjectLeadIds(UUID projectId) {
        return projectLeadViewRepository.findProjectLeaders(projectId)
                .stream()
                .map(ProjectLeadQueryEntity::getId)
                .toList();
    }

    @Override
    public Set<Long> getProjectInvitedLeadIds(UUID projectId) {
        return projectLeaderInvitationRepository.findAllByProjectId(projectId)
                .stream()
                .map(ProjectLeaderInvitationEntity::getGithubUserId)
                .collect(Collectors.toSet());
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
                                isNull(contributionType) ? null : contributionType.name(),
                                isNull(contributionStatus) ? null : contributionStatus.name(),
                                search,
                                PaginationMapper.getPostgresOffsetFromPagination(pageSize, pageIndex),
                                pageSize, includeIgnoredItems)
                        .stream()
                        .map(RewardableItemMapper::itemToDomain)
                        .toList();
        final Long count = rewardableItemRepository.countByProjectIdAndGithubUserId(projectId, githubUserid,
                isNull(contributionType) ? null : contributionType.name(),
                isNull(contributionStatus) ? null : contributionStatus.name(),
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
                .map(ProjectRepoEntity::getRepoId)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void updateProjectsRanking() {
        customProjectRankingRepository.updateProjectsRanking();
    }

    @Override
    public Page<ChurnedContributorView> getChurnedContributors(UUID projectId, Integer pageIndex, Integer pageSize) {
        final var page = churnedContributorViewEntityRepository.findAllByProjectId(
                projectId, CHURNED_CONTRIBUTOR_THRESHOLD_IN_DAYS,
                PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "last_contribution_completed_at")));
        return Page.<ChurnedContributorView>builder()
                .content(page.getContent().stream().map(ChurnedContributorQueryEntity::toDomain).toList())
                .totalItemNumber(page.getNumberOfElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    public Page<NewcomerView> getNewcomers(UUID projectId, ZonedDateTime since, Integer pageIndex, Integer pageSize) {
        final var page = newcomerViewEntityRepository.findAllByProjectId(
                projectId, since, PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC,
                        "first_contribution_created_at")));
        return Page.<NewcomerView>builder()
                .content(page.getContent().stream().map(NewcomerQueryEntity::toDomain).toList())
                .totalItemNumber(page.getNumberOfElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    public Page<ContributorActivityView> getMostActivesContributors(UUID projectId, Integer pageIndex,
                                                                    Integer pageSize) {
        final var format = new SimpleDateFormat("yyyy-MM-dd");
        final var fromDate =
                Date.from(ZonedDateTime.now().minusWeeks(CONTRIBUTOR_ACTIVITY_COUNTS_THRESHOLD_IN_WEEKS).toInstant());

        final var page = contributorActivityViewEntityRepository.findAllByProjectId(
                projectId, format.format(fromDate),
                PageRequest.of(pageIndex, pageSize, JpaSort.unsafe(Sort.Direction.DESC, "completed_total_count")));
        return Page.<ContributorActivityView>builder()
                .content(page.getContent().stream().map(ContributorActivityQueryEntity::toDomain).toList())
                .totalItemNumber(page.getNumberOfElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    public void hideContributorForProjectLead(UUID projectId, UUID projectLeadId, Long contributorGithubUserId) {
        hiddenContributorRepository.saveAndFlush(HiddenContributorEntity.builder()
                .projectId(projectId)
                .projectLeadId(projectLeadId)
                .contributorGithubUserId(contributorGithubUserId)
                .build());
    }

    @Override
    public void showContributorForProjectLead(UUID projectId, UUID projectLeadId, Long contributorGithubUserId) {
        hiddenContributorRepository.deleteById(HiddenContributorEntity.PrimaryKey.builder()
                .projectId(projectId)
                .projectLeadId(projectLeadId)
                .contributorGithubUserId(contributorGithubUserId)
                .build());
    }

    @Override
    @Transactional
    public void updateProjectsTags(final Date now) {
        projectTagRepository.deleteAll();
        projectTagRepository.updateHotCommunityTag(now);
        projectTagRepository.updateNewbiesWelcome(now);
        projectTagRepository.updateLikelyToReward(now);
        projectTagRepository.updateWorkInProgress(now);
        projectTagRepository.updateFastAndFurious(now);
        projectTagRepository.updateBigWhale(now);
    }

    @Override
    @Transactional
    public void createCategory(ProjectCategory projectCategory) {
        projectCategoryRepository.save(ProjectCategoryEntity.fromDomain(projectCategory));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID projectId) {
        return projectRepository.existsById(projectId);
    }

    @Override
    public ProjectInfosView getProjectInfos(UUID projectId) {
        return projectInfosViewRepository.findByProjectId(projectId)
                .orElseThrow(() -> notFound(format("Project %s not found", projectId)))
                .toView();
    }

    @Override
    public List<UUID> getProjectLedIdsForUser(UUID userId) {
        return projectRepository.getProjectLedIdsForUser(userId);
    }

    @Override
    public List<UUID> getProjectContributedOnIdsForUser(UUID userId) {
        return projectRepository.getProjectContributedOnIdsForUser(userId);
    }
}
