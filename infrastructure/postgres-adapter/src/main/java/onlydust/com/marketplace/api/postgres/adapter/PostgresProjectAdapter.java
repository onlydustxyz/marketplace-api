package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeadEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeaderInvitationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectRepoEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.PaginationMapper;
import onlydust.com.marketplace.api.postgres.adapter.mapper.RewardableItemMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.bi.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeaderInvitationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectRepoRepository;
import onlydust.com.marketplace.kernel.model.OrSlug;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.view.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;
import static onlydust.com.marketplace.api.postgres.adapter.mapper.ProjectMapper.moreInfosToEntities;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PostgresProjectAdapter implements ProjectStoragePort {
    private static final int CHURNED_CONTRIBUTOR_THRESHOLD_IN_DAYS = 10;
    private static final int CONTRIBUTOR_ACTIVITY_COUNTS_THRESHOLD_IN_WEEKS = 5;
    private final ProjectRepository projectRepository;
    private final ProjectViewRepository projectViewRepository;
    private final ProjectLeaderInvitationRepository projectLeaderInvitationRepository;
    private final ProjectRepoRepository projectRepoRepository;
    private final CustomProjectRepository customProjectRepository;
    private final ProjectLeadViewRepository projectLeadViewRepository;
    private final RewardableItemRepository rewardableItemRepository;
    private final CustomProjectRankingRepository customProjectRankingRepository;
    private final ChurnedContributorViewEntityRepository churnedContributorViewEntityRepository;
    private final NewcomerViewEntityRepository newcomerViewEntityRepository;
    private final ContributorActivityViewEntityRepository contributorActivityViewEntityRepository;
    private final HiddenContributorRepository hiddenContributorRepository;
    private final ProjectTagRepository projectTagRepository;
    private final ProjectInfosViewRepository projectInfosViewRepository;
    private final ProjectCategorySuggestionRepository projectCategorySuggestionRepository;
    private final BiRewardDataRepository biRewardDataRepository;
    private final BiContributionDataRepository biContributionDataRepository;
    private final BiProjectGrantsDataRepository biProjectGrantsDataRepository;
    private final BiProjectGlobalDataRepository biProjectGlobalDataRepository;
    private final BiContributorGlobalDataRepository biContributorGlobalDataRepository;

    @Override
    public Optional<ProjectId> getProjectIdBySlug(String slug) {
        return projectViewRepository.findBySlug(slug).map(ProjectViewEntity::getId).map(ProjectId::of);
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
    public boolean hasUserAccessToProject(ProjectId projectId, UserId userId) {
        return customProjectRepository.isProjectPublic(projectId.value()) ||
               (userId != null && customProjectRepository.hasUserAccessToProject(projectId.value(), userId.value()));
    }

    @Override
    public boolean hasUserAccessToProject(String projectSlug, UserId userId) {
        return customProjectRepository.isProjectPublic(projectSlug) ||
               (userId != null && customProjectRepository.hasUserAccessToProject(projectSlug, userId.value()));
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<Project> getById(ProjectId projectId) {
        return projectRepository.findById(projectId.value()).map(ProjectEntity::toDomain);
    }

    @Override
    @Transactional
    public void createProject(ProjectId projectId, String slug, String name, String shortDescription, String longDescription,
                              Boolean isLookingForContributors, List<NamedLink> moreInfos,
                              List<Long> githubRepoIds, UserId firstProjectLeaderId,
                              List<Long> githubUserIdsAsProjectLeads,
                              ProjectVisibility visibility, String imageUrl, ProjectRewardSettings rewardSettings,
                              List<UUID> ecosystemIds, List<UUID> categoryIds, List<String> categorySuggestions,
                              boolean botNotifyExternalApplications, List<ProjectContributorLabel> contributorLabels) {
        final ProjectEntity projectEntity =
                ProjectEntity.builder()
                        .id(projectId.value())
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
                                .map(repoId -> new ProjectRepoEntity(projectId.value(), repoId))
                                .collect(toSet()))
                        .moreInfos(moreInfos == null ? null : moreInfosToEntities(moreInfos, projectId))
                        .projectLeaders(Set.of(new ProjectLeadEntity(projectId.value(), firstProjectLeaderId.value())))
                        .projectLeaderInvitations(githubUserIdsAsProjectLeads == null ? null :
                                githubUserIdsAsProjectLeads.stream()
                                        .map(githubUserId -> new ProjectLeaderInvitationEntity(UUID.randomUUID(),
                                                projectId.value(), githubUserId))
                                        .collect(toSet()))
                        .ecosystems(ecosystemIds == null ? null :
                                ecosystemIds.stream().map(ecosystemId -> new ProjectEcosystemEntity(projectId.value(), ecosystemId)).collect(toSet()))
                        .categories(categoryIds == null ? null :
                                categoryIds.stream().map(categoryId -> new ProjectProjectCategoryEntity(projectId.value(), categoryId)).collect(toSet()))
                        .categorySuggestions(categorySuggestions == null ? Set.of() :
                                categorySuggestions.stream().map(categorySuggestion -> new ProjectCategorySuggestionEntity(UUID.randomUUID(),
                                        categorySuggestion, projectId.value())).collect(toSet())
                        )
                        .rank(0)
                        .botNotifyExternalApplications(botNotifyExternalApplications)
                        .contributorLabels(contributorLabels.stream().map(ProjectContributorLabelEntity::fromDomain).collect(toSet()))
                        .build();

        this.projectRepository.saveAndFlush(projectEntity);
    }

    @Override
    @Transactional
    public void updateProject(ProjectId projectId, String slug, String name, String shortDescription,
                              String longDescription,
                              Boolean isLookingForContributors, List<NamedLink> moreInfos,
                              List<Long> githubRepoIds, List<Long> githubUserIdsAsProjectLeadersToInvite,
                              List<UserId> projectLeadersToKeep, String imageUrl,
                              ProjectRewardSettings rewardSettings,
                              List<UUID> ecosystemIds, List<UUID> categoryIds, List<String> categorySuggestions,
                              List<ProjectContributorLabel> contributorLabels) {
        final var project = this.projectRepository.findById(projectId.value())
                .orElseThrow(() -> notFound(format("Project %s not found", projectId.value())));
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
                                                   invitation.getProjectId().equals(projectId.value())));

                projectLeaderInvitations.addAll(githubUserIdsAsProjectLeadersToInvite.stream()
                        .filter(githubUserId -> projectLeaderInvitations.stream()
                                .noneMatch(invitation -> invitation.getGithubUserId().equals(githubUserId) &&
                                                         invitation.getProjectId().equals(projectId.value())))
                        .map(githubUserId -> new ProjectLeaderInvitationEntity(UUID.randomUUID(), projectId.value(),
                                githubUserId)).toList());
            } else {
                project.setProjectLeaderInvitations(githubUserIdsAsProjectLeadersToInvite.stream()
                        .map(githubUserId -> new ProjectLeaderInvitationEntity(UUID.randomUUID(), projectId.value(),
                                githubUserId))
                        .collect(toSet()));
            }
        }

        if (!isNull(projectLeadersToKeep))
            project.getProjectLeaders().removeIf(lead -> !projectLeadersToKeep.contains(UserId.of(lead.getUserId())));

        if (!isNull(githubRepoIds)) {
            if (nonNull(project.getRepos())) {
                project.getRepos().clear();
                project.getRepos().addAll(githubRepoIds.stream()
                        .map(repoId -> new ProjectRepoEntity(projectId.value(), repoId))
                        .collect(toSet()));
            } else {
                project.setRepos(githubRepoIds.stream()
                        .map(repoId -> new ProjectRepoEntity(projectId.value(), repoId))
                        .collect(toSet()));
            }
        }

        if (nonNull(ecosystemIds)) {
            if (nonNull(project.getEcosystems())) {
                project.getEcosystems().clear();
                project.getEcosystems().addAll(ecosystemIds.stream()
                        .map(ecosystemId -> new ProjectEcosystemEntity(projectId.value(), ecosystemId))
                        .collect(toSet()));
            } else {
                project.setEcosystems(ecosystemIds.stream()
                        .map(ecosystemId -> new ProjectEcosystemEntity(projectId.value(), ecosystemId))
                        .collect(toSet()));
            }
        }

        if (nonNull(categoryIds)) {
            if (nonNull(project.getCategories())) {
                project.getCategories().clear();
                project.getCategories().addAll(categoryIds.stream()
                        .map(categoryId -> new ProjectProjectCategoryEntity(projectId.value(), categoryId))
                        .collect(toSet()));
            } else {
                project.setCategories(categoryIds.stream()
                        .map(categoryId -> new ProjectProjectCategoryEntity(projectId.value(), categoryId))
                        .collect(toSet()));
            }
        }

        if (nonNull(categorySuggestions)) {
            project.getCategorySuggestions().removeIf(categorySuggestion -> !categorySuggestions.contains(categorySuggestion.getName()));
            project.getCategorySuggestions().addAll(categorySuggestions.stream()
                    .filter(categorySuggestion -> project.getCategorySuggestions().stream()
                            .noneMatch(suggestion -> suggestion.getName().equals(categorySuggestion)))
                    .map(categorySuggestion -> new ProjectCategorySuggestionEntity(UUID.randomUUID(), categorySuggestion, projectId.value()))
                    .collect(toSet()));
        }

        if (nonNull(contributorLabels)) {
            project.getContributorLabels().clear();
            project.getContributorLabels().addAll(contributorLabels.stream()
                    .map(ProjectContributorLabelEntity::fromDomain)
                    .collect(toSet()));
        }

        this.projectRepository.saveAndFlush(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserId> getProjectLeadIds(ProjectId projectId) {
        return getProjectLeadIds(OrSlug.of(projectId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserId> getProjectLeadIds(OrSlug<ProjectId> projectIdOrSlug) {
        return projectLeadViewRepository.findProjectLeaders(
                        projectIdOrSlug.uuid().orElse(null),
                        projectIdOrSlug.slug().orElse(null))
                .stream()
                .map(ProjectLeadQueryEntity::getId)
                .map(UserId::of)
                .toList();
    }

    @Override
    public Set<Long> getProjectInvitedLeadIds(ProjectId projectId) {
        return projectLeaderInvitationRepository.findAllByProjectId(projectId.value())
                .stream()
                .map(ProjectLeaderInvitationEntity::getGithubUserId)
                .collect(toSet());
    }

    @Override
    public Page<RewardableItemView> getProjectRewardableItemsByTypeForProjectLeadAndContributorId(ProjectId projectId,
                                                                                                  ContributionType contributionType,
                                                                                                  ContributionStatus contributionStatus,
                                                                                                  Long githubUserid,
                                                                                                  int pageIndex,
                                                                                                  int pageSize,
                                                                                                  String search,
                                                                                                  Boolean includeIgnoredItems) {

        final List<RewardableItemView> rewardableItemViews =
                rewardableItemRepository.findByProjectIdAndGithubUserId(projectId.value(), githubUserid,
                                isNull(contributionType) ? null : contributionType.name(),
                                isNull(contributionStatus) ? null : contributionStatus.name(),
                                search,
                                PaginationMapper.getPostgresOffsetFromPagination(pageSize, pageIndex),
                                pageSize, includeIgnoredItems)
                        .stream()
                        .map(RewardableItemMapper::itemToDomain)
                        .toList();
        final Long count = rewardableItemRepository.countByProjectIdAndGithubUserId(projectId.value(), githubUserid,
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
    public Set<Long> getProjectRepoIds(ProjectId projectId) {
        final var project = projectRepository.findById(projectId.value())
                .orElseThrow(() -> notFound("Project %s not found".formatted(projectId.value())));

        return project.getRepos() == null ? Set.of() : project.getRepos().stream()
                .map(ProjectRepoEntity::getRepoId)
                .collect(toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectOrganizationView> getProjectOrganizations(ProjectId projectId) {
        final var project = projectViewRepository.findById(projectId.value())
                .orElseThrow(() -> notFound(format("Project %s not found", projectId.value())));
        return project.organizations();
    }

    @Override
    @Transactional
    public void updateProjectsRanking() {
        customProjectRankingRepository.updateProjectsRanking();
    }

    @Override
    public Page<ChurnedContributorView> getChurnedContributors(ProjectId projectId, Integer pageIndex, Integer pageSize) {
        final var page = churnedContributorViewEntityRepository.findAllByProjectId(
                projectId.value(), CHURNED_CONTRIBUTOR_THRESHOLD_IN_DAYS,
                PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "last_contribution_completed_at")));
        return Page.<ChurnedContributorView>builder()
                .content(page.getContent().stream().map(ChurnedContributorQueryEntity::toDomain).toList())
                .totalItemNumber(page.getNumberOfElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    public Page<NewcomerView> getNewcomers(ProjectId projectId, ZonedDateTime since, Integer pageIndex, Integer pageSize) {
        final var page = newcomerViewEntityRepository.findAllByProjectId(
                projectId.value(), since, PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC,
                        "first_contribution_created_at")));
        return Page.<NewcomerView>builder()
                .content(page.getContent().stream().map(NewcomerQueryEntity::toDomain).toList())
                .totalItemNumber(page.getNumberOfElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    public Page<ContributorActivityView> getMostActivesContributors(ProjectId projectId, Integer pageIndex,
                                                                    Integer pageSize) {
        final var format = new SimpleDateFormat("yyyy-MM-dd");
        final var fromDate =
                Date.from(ZonedDateTime.now().minusWeeks(CONTRIBUTOR_ACTIVITY_COUNTS_THRESHOLD_IN_WEEKS).toInstant());

        final var page = contributorActivityViewEntityRepository.findAllByProjectId(
                projectId.value(), format.format(fromDate),
                PageRequest.of(pageIndex, pageSize, JpaSort.unsafe(Sort.Direction.DESC, "completed_total_count")));
        return Page.<ContributorActivityView>builder()
                .content(page.getContent().stream().map(ContributorActivityQueryEntity::toDomain).toList())
                .totalItemNumber(page.getNumberOfElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    public void hideContributorForProjectLead(ProjectId projectId, UserId projectLeadId, Long contributorGithubUserId) {
        hiddenContributorRepository.saveAndFlush(HiddenContributorEntity.builder()
                .projectId(projectId.value())
                .projectLeadId(projectLeadId.value())
                .contributorGithubUserId(contributorGithubUserId)
                .build());
    }

    @Override
    public void showContributorForProjectLead(ProjectId projectId, UserId projectLeadId, Long contributorGithubUserId) {
        hiddenContributorRepository.deleteById(HiddenContributorEntity.PrimaryKey.builder()
                .projectId(projectId.value())
                .projectLeadId(projectLeadId.value())
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
        projectTagRepository.updateHasGoodFirstIssues();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(ProjectId projectId) {
        return projectRepository.existsById(projectId.value());
    }

    @Override
    public ProjectInfosView getProjectInfos(ProjectId projectId) {
        return projectInfosViewRepository.findByProjectId(projectId.value())
                .orElseThrow(() -> notFound(format("Project %s not found", projectId.value())))
                .toView();
    }

    @Override
    public List<ProjectId> getProjectLedIdsForUser(UserId userId) {
        return projectRepository.getProjectLedIdsForUser(userId.value()).stream().map(ProjectId::of).toList();
    }

    @Override
    public List<ProjectId> getProjectContributedOnIdsForUser(UserId userId) {
        return projectRepository.getProjectContributedOnIdsForUser(userId.value()).stream().map(ProjectId::of).toList();
    }

    @Override
    public List<ProjectId> findProjectIdsByRepoId(Long repoId) {
        return projectRepoRepository.findAllByRepoId(repoId)
                .stream()
                .map(ProjectRepoEntity::getProjectId)
                .map(ProjectId::of)
                .toList();
    }

    @Override
    public List<ProjectCategorySuggestion> getProjectCategorySuggestions(ProjectId projectId) {
        return projectCategorySuggestionRepository.findAllByProjectId(projectId.value())
                .stream()
                .map(ProjectCategorySuggestionEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void refreshRecommendations() {
        projectRepository.refreshRecommendations();
    }

    @Override
    @Transactional
    public void refreshStats() {
        projectRepository.refreshStats();
        biRewardDataRepository.refresh();
        biContributionDataRepository.refresh();
        biProjectGrantsDataRepository.refresh();
        biProjectGlobalDataRepository.refresh();
        biContributorGlobalDataRepository.refresh();
    }
}
