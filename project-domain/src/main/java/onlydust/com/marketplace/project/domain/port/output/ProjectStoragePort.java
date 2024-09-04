package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.view.*;

import java.time.ZonedDateTime;
import java.util.*;

public interface ProjectStoragePort {

    Optional<Project> getById(UUID projectId);

    void createProject(UUID projectId, String slug, String name, String shortDescription, String longDescription,
                       Boolean isLookingForContributors, List<NamedLink> moreInfos,
                       List<Long> githubRepoIds, UUID firstProjectLeaderId, List<Long> githubUserIdsAsProjectLeads,
                       ProjectVisibility visibility
            , String imageUrl, ProjectRewardSettings rewardSettings, List<UUID> ecosystemIds, List<UUID> categoryIds, List<String> categorySuggestions,
                       boolean botNotifyExternalApplications);

    void updateProject(UUID id, String slug, String name, String shortDescription, String longDescription,
                       Boolean isLookingForContributors, List<NamedLink> moreInfos,
                       List<Long> githubRepoIds, List<Long> githubUserIdsAsProjectLeadersToInvite,
                       List<UUID> projectLeadersToKeep, String imageUrl, ProjectRewardSettings rewardSettings, List<UUID> ecosystemIds,
                       List<UUID> categoryIds, List<String> categorySuggestions);

    List<UUID> getProjectLeadIds(UUID projectId);

    Set<Long> getProjectInvitedLeadIds(UUID projectId);

    Set<Long> getProjectRepoIds(UUID projectId);

    List<ProjectOrganizationView> getProjectOrganizations(UUID projectId);

    Page<RewardableItemView> getProjectRewardableItemsByTypeForProjectLeadAndContributorId(UUID projectId,
                                                                                           ContributionType contributionType,
                                                                                           ContributionStatus contributionStatus,
                                                                                           Long githubUserid,
                                                                                           int pageIndex, int pageSize,
                                                                                           String search,
                                                                                           Boolean includeIgnoredItems);

    Optional<UUID> getProjectIdBySlug(String slug);

    RewardableItemView getRewardableIssue(String repoOwner, String repoName, long issueNumber);

    RewardableItemView getRewardablePullRequest(String repoOwner, String repoName, long pullRequestNumber);

    boolean hasUserAccessToProject(UUID projectId, UUID userId);

    boolean hasUserAccessToProject(String projectSlug, UUID userId);

    void updateProjectsRanking();

    Page<ChurnedContributorView> getChurnedContributors(UUID projectId, Integer pageIndex, Integer pageSize);

    Page<NewcomerView> getNewcomers(UUID projectId, ZonedDateTime since, Integer pageIndex, Integer pageSize);

    Page<ContributorActivityView> getMostActivesContributors(UUID projectId, Integer pageIndex, Integer pageSize);

    void hideContributorForProjectLead(UUID projectId, UUID projectLeadId, Long contributorGithubUserId);

    void showContributorForProjectLead(UUID projectId, UUID projectLeadId, Long contributorGithubUserId);

    void updateProjectsTags(Date now);

    boolean exists(UUID projectId);

    ProjectInfosView getProjectInfos(UUID projectId);

    List<UUID> getProjectLedIdsForUser(UUID userId);

    List<UUID> getProjectContributedOnIdsForUser(UUID userId);

    List<UUID> findProjectIdsByRepoId(Long repoId);

    List<ProjectCategorySuggestion> getProjectCategorySuggestions(UUID projectId);

    void refreshRecommendations();

    void refreshStats();
}
