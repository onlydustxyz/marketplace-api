package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.model.OrSlug;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.view.*;

import java.time.ZonedDateTime;
import java.util.*;

public interface ProjectStoragePort {

    Optional<Project> getById(ProjectId projectId);

    void createProject(ProjectId projectId, String slug, String name, String shortDescription, String longDescription,
                       Boolean isLookingForContributors, List<NamedLink> moreInfos,
                       List<Long> githubRepoIds, UserId firstProjectLeaderId, List<Long> githubUserIdsAsProjectLeads,
                       ProjectVisibility visibility
            , String imageUrl, ProjectRewardSettings rewardSettings, List<UUID> ecosystemIds, List<UUID> categoryIds, List<String> categorySuggestions,
                       boolean botNotifyExternalApplications);

    void updateProject(ProjectId id, String slug, String name, String shortDescription, String longDescription,
                       Boolean isLookingForContributors, List<NamedLink> moreInfos,
                       List<Long> githubRepoIds, List<Long> githubUserIdsAsProjectLeadersToInvite,
                       List<UserId> projectLeadersToKeep, String imageUrl, ProjectRewardSettings rewardSettings, List<UUID> ecosystemIds,
                       List<UUID> categoryIds, List<String> categorySuggestions);

    List<UserId> getProjectLeadIds(ProjectId projectId);

    List<UserId> getProjectLeadIds(OrSlug<ProjectId> projectIdOrSlug);

    Set<Long> getProjectInvitedLeadIds(ProjectId projectId);

    Set<Long> getProjectRepoIds(ProjectId projectId);

    List<ProjectOrganizationView> getProjectOrganizations(ProjectId projectId);

    Page<RewardableItemView> getProjectRewardableItemsByTypeForProjectLeadAndContributorId(ProjectId projectId,
                                                                                           ContributionType contributionType,
                                                                                           ContributionStatus contributionStatus,
                                                                                           Long githubUserid,
                                                                                           int pageIndex, int pageSize,
                                                                                           String search,
                                                                                           Boolean includeIgnoredItems);

    Optional<ProjectId> getProjectIdBySlug(String slug);

    RewardableItemView getRewardableIssue(String repoOwner, String repoName, long issueNumber);

    RewardableItemView getRewardablePullRequest(String repoOwner, String repoName, long pullRequestNumber);

    boolean hasUserAccessToProject(ProjectId projectId, UserId userId);

    boolean hasUserAccessToProject(String projectSlug, UserId userId);

    void updateProjectsRanking();

    Page<ChurnedContributorView> getChurnedContributors(ProjectId projectId, Integer pageIndex, Integer pageSize);

    Page<NewcomerView> getNewcomers(ProjectId projectId, ZonedDateTime since, Integer pageIndex, Integer pageSize);

    Page<ContributorActivityView> getMostActivesContributors(ProjectId projectId, Integer pageIndex, Integer pageSize);

    void hideContributorForProjectLead(ProjectId projectId, UserId projectLeadId, Long contributorGithubUserId);

    void showContributorForProjectLead(ProjectId projectId, UserId projectLeadId, Long contributorGithubUserId);

    void updateProjectsTags(Date now);

    boolean exists(ProjectId projectId);

    ProjectInfosView getProjectInfos(ProjectId projectId);

    List<ProjectId> getProjectLedIdsForUser(UserId userId);

    List<ProjectId> getProjectContributedOnIdsForUser(UserId userId);

    List<ProjectId> findProjectIdsByRepoId(Long repoId);

    List<ProjectCategorySuggestion> getProjectCategorySuggestions(ProjectId projectId);

    void refreshRecommendations();

    void refreshStats();
}
