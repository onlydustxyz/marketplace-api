package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.*;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.HiddenContributorEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ApplicationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectIdRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeaderInvitationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectRepoRepository;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static onlydust.com.marketplace.api.postgres.adapter.mapper.ProjectMapper.moreInfosToEntities;

@AllArgsConstructor
public class PostgresProjectAdapter implements ProjectStoragePort {
    private static final int CHURNED_CONTRIBUTOR_THRESHOLD_IN_DAYS = 10;
    private static final int CONTRIBUTOR_ACTIVITY_COUNTS_THRESHOLD_IN_WEEKS = 5;
    private static final int TOP_CONTRIBUTOR_COUNT = 3;
    private final ProjectRepository projectRepository;
    private final ProjectViewRepository projectViewRepository;
    private final ProjectIdRepository projectIdRepository;
    private final ProjectLeaderInvitationRepository projectLeaderInvitationRepository;
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
    private final CustomProjectRankingRepository customProjectRankingRepository;
    private final BudgetStatsRepository budgetStatsRepository;
    private final ChurnedContributorViewEntityRepository churnedContributorViewEntityRepository;
    private final NewcomerViewEntityRepository newcomerViewEntityRepository;
    private final ContributorActivityViewEntityRepository contributorActivityViewEntityRepository;
    private final ApplicationRepository applicationRepository;
    private final ContributionViewEntityRepository contributionViewEntityRepository;
    private final HiddenContributorRepository hiddenContributorRepository;
    private final ProjectTagRepository projectTagRepository;

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailsView getById(UUID projectId, User caller) {
        final var projectEntity = projectViewRepository.findById(projectId)
                .orElseThrow(() -> OnlyDustException.notFound(format("Project %s not found", projectId)));
        return getProjectDetails(projectEntity, caller);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailsView getBySlug(String slug, User caller) {
        final var projectEntity = projectViewRepository.findByKey(slug)
                .orElseThrow(() -> OnlyDustException.notFound(format("Project '%s' not found", slug)));
        return getProjectDetails(projectEntity, caller);
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
        final var sponsors = customProjectRepository.getProjectSponsors(projectView.getId());
        // TODO : migrate to multi-token
        final Boolean hasRemainingBudget = customProjectRepository.hasRemainingBudget(projectView.getId());
        final var me = isNull(caller) ? null : new ProjectDetailsView.Me(
                leaders.stream().anyMatch(l -> l.getGithubId().equals(caller.getGithubUserId()) && l.getHasAcceptedInvitation()),
                leaders.stream().anyMatch(l -> l.getGithubId().equals(caller.getGithubUserId()) && !l.getHasAcceptedInvitation()),
                !contributionViewEntityRepository.findContributions(caller.getGithubUserId(),
                        List.of(caller.getGithubUserId()),
                        List.of(projectView.getId()),
                        List.of(),
                        List.of(),
                        List.of(),
                        null,
                        null,
                        Pageable.ofSize(1)).isEmpty(),
                applicationRepository.findByProjectIdAndApplicantId(projectView.getId(), caller.getId()).isPresent()
        );
        return ProjectMapper.mapToProjectDetailsView(projectView, topContributors, contributorCount, leaders
                , sponsors, ecosystems, hasRemainingBudget, me);
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
        final String ecosystemsJsonPath = ProjectPageItemViewEntity.getEcosystemsJsonPath(sponsorIds);
        final String technologiesJsonPath = ProjectPageItemViewEntity.getTechnologiesJsonPath(technologies);
        final String tagsJsonPath = ProjectPageItemViewEntity.getTagsJsonPath(tags);
        final Long count = projectsPageRepository.countProjectsForUserId(userId, mine, tagsJsonPath, technologiesJsonPath,
                ecosystemsJsonPath, search);
        final List<ProjectPageItemViewEntity> projectsForUserId =
                projectsPageRepository.findProjectsForUserId(userId, mine, tagsJsonPath,
                        technologiesJsonPath, ecosystemsJsonPath, search, isNull(sort) ?
                                ProjectCardView.SortBy.NAME.name() : sort.name(),
                        PaginationMapper.getPostgresOffsetFromPagination(pageSize, pageIndex), pageSize);
        final Map<String, Set<Object>> filters = ProjectPageItemFiltersViewEntity.entitiesToFilters(
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

        final String ecosystemsJsonPath = ProjectPageItemViewEntity.getEcosystemsJsonPath(sponsorIds);
        final String technologiesJsonPath = ProjectPageItemViewEntity.getTechnologiesJsonPath(technologies);
        final String tagsJsonPath = ProjectPageItemViewEntity.getTagsJsonPath(tags);
        final List<ProjectPageItemViewEntity> projectsForAnonymousUser =
                projectsPageRepository.findProjectsForAnonymousUser(tagsJsonPath, technologiesJsonPath, ecosystemsJsonPath, search,
                        isNull(sort) ?
                                ProjectCardView.SortBy.NAME.name() : sort.name(),
                        PaginationMapper.getPostgresOffsetFromPagination(pageSize, pageIndex), pageSize);
        final Long count = projectsPageRepository.countProjectsForAnonymousUser(tagsJsonPath, technologiesJsonPath,
                ecosystemsJsonPath, search);
        final Map<String, Set<Object>> filters = ProjectPageItemFiltersViewEntity.entitiesToFilters(
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
                        .rank(0)
                        .build();

        this.projectIdRepository.save(new ProjectIdEntity(projectId));
        this.projectRepository.save(projectEntity);

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

        if (nonNull(moreInfos)) {
            if (nonNull(project.getMoreInfos())) {
                project.getMoreInfos().clear();
                if (!moreInfos.isEmpty()){
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

        this.projectRepository.save(project);
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
                .map(ProjectLeadViewEntity::getId)
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
    @Transactional(readOnly = true)
    public ProjectRewardsPageView findRewards(UUID projectId, ProjectRewardView.Filters filters,
                                              ProjectRewardView.SortBy sortBy, SortDirection sortDirection,
                                              int pageIndex, int pageSize) {
        final var currencies = filters.getCurrencies().stream().map(CurrencyEnumEntity::of).map(Enum::name).toList();
        final var format = new SimpleDateFormat("yyyy-MM-dd");
        final var fromDate = isNull(filters.getFrom()) ? null : format.format(filters.getFrom());
        final var toDate = isNull(filters.getTo()) ? null : format.format(filters.getTo());

        final Integer count = customProjectRewardRepository.getCount(projectId, currencies, filters.getContributors(),
                fromDate, toDate);
        final List<ProjectRewardView> projectRewardViews = customProjectRewardRepository.getViewEntities(projectId,
                        currencies, filters.getContributors(),
                        fromDate, toDate,
                        sortBy, sortDirection, pageIndex, pageSize)
                .stream().map(ProjectRewardMapper::mapEntityToDomain)
                .toList();

        final var budgetStats = budgetStatsRepository.findByProject(projectId, currencies, filters.getContributors(),
                fromDate, toDate);

        return ProjectRewardsPageView.builder().
                rewards(Page.<ProjectRewardView>builder()
                        .content(projectRewardViews)
                        .totalItemNumber(count)
                        .totalPageNumber(PaginationHelper.calculateTotalNumberOfPage(pageSize, count))
                        .build())
                .remainingBudget(budgetStats.size() == 1 ?
                        new Money(budgetStats.get(0).getRemainingAmount(),
                                budgetStats.get(0).getCurrency().toDomain(),
                                budgetStats.get(0).getRemainingUsdAmount()) :
                        new Money(null, null,
                                budgetStats.stream().map(BudgetStatsEntity::getRemainingUsdAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO,
                                        BigDecimal::add)))
                .spentAmount(budgetStats.size() == 1 ?
                        new Money(budgetStats.get(0).getSpentAmount(),
                                budgetStats.get(0).getCurrency().toDomain(),
                                budgetStats.get(0).getSpentUsdAmount()) :
                        new Money(null, null,
                                budgetStats.stream().map(BudgetStatsEntity::getSpentUsdAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO,
                                        BigDecimal::add)))
                .sentRewardsCount(budgetStats.stream().map(BudgetStatsEntity::getRewardIds).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet()).size())
                .rewardedContributionsCount(budgetStats.stream().map(BudgetStatsEntity::getRewardItemIds).flatMap(Collection::stream).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet()).size())
                .rewardedContributorsCount(budgetStats.stream().map(BudgetStatsEntity::getRewardRecipientIds).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet()).size())
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
                .content(page.getContent().stream().map(ChurnedContributorViewEntity::toDomain).toList())
                .totalItemNumber(page.getNumberOfElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    public Page<NewcomerView> getNewcomers(UUID projectId, Integer pageIndex, Integer pageSize) {
        final var page = newcomerViewEntityRepository.findAllByProjectId(
                projectId, PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC,
                        "first_contribution_created_at")));
        return Page.<NewcomerView>builder()
                .content(page.getContent().stream().map(NewcomerViewEntity::toDomain).toList())
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
                .content(page.getContent().stream().map(ContributorActivityViewEntity::toDomain).toList())
                .totalItemNumber(page.getNumberOfElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    public void hideContributorForProjectLead(UUID projectId, UUID projectLeadId, Long contributorGithubUserId) {
        hiddenContributorRepository.save(HiddenContributorEntity.builder()
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
}
