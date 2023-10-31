package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.model.*;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLeadViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectIdEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeaderInvitationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectRepoEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectIdRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeaderInvitationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectRepoRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;

@AllArgsConstructor
public class PostgresProjectAdapter implements ProjectStoragePort {

    private static final int TOP_CONTRIBUTOR_COUNT = 3;
    private final ProjectRepository projectRepository;
    private final ProjectViewRepository projectViewRepository;
    private final ProjectIdRepository projectIdRepository;
    private final ProjectLeaderInvitationRepository projectLeaderInvitationRepository;
    private final ProjectRepoRepository projectRepoRepository;
    private final CustomProjectRepository customProjectRepository;
    private final CustomContributorRepository customContributorRepository;
    private final CustomProjectListRepository customProjectListRepository;
    private final CustomProjectRewardRepository customProjectRewardRepository;
    private final CustomProjectBudgetRepository customProjectBudgetRepository;
    private final ProjectLeadViewRepository projectLeadViewRepository;
    private final CustomRewardRepository customRewardRepository;

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

    private ProjectDetailsView getProjectDetails(ProjectViewEntity projectView) {
        final var topContributors = customContributorRepository.findProjectTopContributors(projectView.getId(),
                TOP_CONTRIBUTOR_COUNT);
        final var contributorCount = customContributorRepository.getProjectContributorCount(projectView.getId());
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
                                                                              List<String> sponsors, UUID userId,
                                                                              String search,
                                                                              ProjectCardView.SortBy sort,
                                                                              Boolean mine) {
        return customProjectListRepository.findByTechnologiesSponsorsUserIdSearchSortBy(technologies, sponsors,
                search, sort, userId, mine);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectCardView> findByTechnologiesSponsorsSearchSortBy(List<String> technologies,
                                                                        List<String> sponsors, String search,
                                                                        ProjectCardView.SortBy sort) {
        return customProjectListRepository.findByTechnologiesSponsorsSearchSortBy(technologies, sponsors, search, sort);
    }

    @Override
    @Transactional
    public void createProject(UUID projectId, String name, String shortDescription, String longDescription,
                              Boolean isLookingForContributors, List<ProjectMoreInfoLink> moreInfos,
                              List<Long> githubRepoIds, List<Long> githubUserIdsAsProjectLeads,
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
        moreInfos.stream().findFirst().ifPresent(moreInfo -> projectEntity.setTelegramLink(moreInfo.getUrl()));

        this.projectIdRepository.save(new ProjectIdEntity(projectId));
        this.projectRepository.save(projectEntity);

        this.projectRepoRepository.saveAll(githubRepoIds.stream().map(repoId -> new ProjectRepoEntity(projectId,
                repoId)).toList());

        this.projectLeaderInvitationRepository.saveAll(githubUserIdsAsProjectLeads.stream()
                .map(githubUserId -> new ProjectLeaderInvitationEntity(UUID.randomUUID(), projectId, githubUserId)).toList());
    }

    @Override
    public void updateProject(UUID id, String name, String shortDescription, String longDescription,
                              Boolean isLookingForContributors, List<ProjectMoreInfoLink> moreInfos,
                              List<Long> githubRepoIds, List<Long> githubUserIdsAsProjectLeadersToInvite,
                              List<UUID> projectLeadersToKeep, String imageUrl, ProjectRewardSettings rewardSettings) {

    }

    @Override
    @Transactional(readOnly = true)
    public List<Contributor> searchContributorsByLogin(UUID projectId, String login) {
        return customProjectRepository.findProjectContributorsByLogin(projectId, login)
                .stream().map(entity -> Contributor.builder().id(GithubUserIdentity.builder().githubUserId(entity.getGithubUserId()).githubLogin(entity.getLogin()).githubAvatarUrl(entity.getAvatarUrl()).build()).isRegistered(entity.getIsRegistered()).build()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectContributorsLinkView> findContributors(UUID projectId,
                                                              ProjectContributorsLinkView.SortBy sortBy,
                                                              SortDirection sortDirection,
                                                              int pageIndex, int pageSize) {
        final Integer count = customContributorRepository.getProjectContributorCount(projectId);
        final List<ProjectContributorsLinkView> projectContributorsLinkViews =
                customContributorRepository.getProjectContributorViewEntity(projectId, sortBy, sortDirection,
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
    public Page<ProjectContributorsLinkView> findContributorsForProjectLead(UUID projectId,
                                                                            ProjectContributorsLinkView.SortBy sortBy,
                                                                            SortDirection sortDirection,
                                                                            int pageIndex, int pageSize) {
        final Integer count = customContributorRepository.getProjectContributorCount(projectId);
        final List<ProjectContributorsLinkView> projectContributorsLinkViews =
                customContributorRepository.getProjectContributorViewEntity(projectId, sortBy, sortDirection,
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
}
