package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.HiddenContributorEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectEcosystemEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectProjectCategoryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeadEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeaderInvitationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectRepoEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.PaginationMapper;
import onlydust.com.marketplace.api.postgres.adapter.mapper.RewardableItemMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeaderInvitationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectRepoRepository;
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
    public boolean hasUserAccessToProject(UUID projectId, UUID userId) {
        return customProjectRepository.isProjectPublic(projectId) ||
               (userId != null && customProjectRepository.hasUserAccessToProject(projectId, userId));
    }

    @Override
    public boolean hasUserAccessToProject(String projectSlug, UUID userId) {
        return customProjectRepository.isProjectPublic(projectSlug) ||
               (userId != null && customProjectRepository.hasUserAccessToProject(projectSlug, userId));
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<Project> getById(UUID projectId) {
        return projectRepository.findById(projectId).map(ProjectEntity::toDomain);
    }

    @Override
    @Transactional
    public void createProject(UUID projectId, String slug, String name, String shortDescription, String longDescription,
                              Boolean isLookingForContributors, List<NamedLink> moreInfos,
                              List<Long> githubRepoIds, UUID firstProjectLeaderId,
                              List<Long> githubUserIdsAsProjectLeads,
                              ProjectVisibility visibility, String imageUrl, ProjectRewardSettings rewardSettings,
                              List<UUID> ecosystemIds, List<UUID> categoryIds) {
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
                        .categories(categoryIds == null ? null :
                                categoryIds.stream().map(categoryId -> new ProjectProjectCategoryEntity(projectId, categoryId)).collect(Collectors.toSet()))
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
                              ProjectRewardSettings rewardSettings,
                              List<UUID> ecosystemIds, List<UUID> categoryIds) {
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

        if (nonNull(categoryIds)) {
            if (nonNull(project.getCategories())) {
                project.getCategories().clear();
                project.getCategories().addAll(categoryIds.stream()
                        .map(categoryId -> new ProjectProjectCategoryEntity(projectId, categoryId))
                        .collect(Collectors.toSet()));
            } else {
                project.setCategories(categoryIds.stream()
                        .map(categoryId -> new ProjectProjectCategoryEntity(projectId, categoryId))
                        .collect(Collectors.toSet()));
            }
        }

        this.projectRepository.saveAndFlush(project);
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
        final var project = projectRepository.findById(projectId)
                .orElseThrow(() -> notFound("Project %s not found".formatted(projectId)));

        return project.getRepos() == null ? Set.of() : project.getRepos().stream()
                .map(ProjectRepoEntity::getRepoId)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectOrganizationView> getProjectOrganizations(UUID projectId) {
        final var project = projectViewRepository.findById(projectId)
                .orElseThrow(() -> notFound(format("Project %s not found", projectId)));
        return project.organizations();
    }

    @Override
    @Transactional
    public void updateProjectsRanking() {
        customProjectRankingRepository.updateProjectsRanking();
    }

    @Override
    public boolean isLinkedToAProject(Long repoId) {
        return projectRepoRepository.existsByRepoId(repoId);
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

    @Override
    public List<UUID> findProjectIdsByRepoId(Long repoId) {
        return projectRepoRepository.findAllByRepoId(repoId)
                .stream()
                .map(ProjectRepoEntity::getProjectId)
                .toList();
    }
}
