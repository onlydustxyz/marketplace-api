package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.*;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;

import java.util.*;

public interface ProjectStoragePort {
    ProjectDetailsView getById(UUID projectId, User caller);

    ProjectDetailsView getBySlug(String slug, User caller);

    Page<ProjectCardView> findByTagsTechnologiesEcosystemsUserIdSearchSortBy(List<Project.Tag> tags, List<String> technologies, List<UUID> sponsorIds,
                                                                             UUID userId, String search,
                                                                             ProjectCardView.SortBy sort, Boolean mine,
                                                                             Integer pageIndex, Integer pageSize);

    Page<ProjectCardView> findByTagsTechnologiesEcosystemsSearchSortBy(List<Project.Tag> tags, List<String> technologies, List<UUID> sponsorIds,
                                                                       String search, ProjectCardView.SortBy sort,
                                                                       Integer pageIndex, Integer pageSize);

    String createProject(UUID projectId, String name, String shortDescription, String longDescription,
                         Boolean isLookingForContributors, List<MoreInfoLink> moreInfos,
                         List<Long> githubRepoIds, UUID firstProjectLeaderId, List<Long> githubUserIdsAsProjectLeads,
                         ProjectVisibility visibility
            , String imageUrl, ProjectRewardSettings rewardSettings);

    void updateProject(UUID id, String name, String shortDescription, String longDescription,
                       Boolean isLookingForContributors, List<MoreInfoLink> moreInfos,
                       List<Long> githubRepoIds, List<Long> githubUserIdsAsProjectLeadersToInvite,
                       List<UUID> projectLeadersToKeep, String imageUrl, ProjectRewardSettings rewardSettings);

    ProjectContributorsLinkViewPage findContributors(UUID projectId, String login,
                                                     ProjectContributorsLinkView.SortBy sortBy,
                                                     SortDirection sortDirection,
                                                     int pageIndex, int pageSize);

    ProjectContributorsLinkViewPage findContributorsForProjectLead(UUID projectId, UUID projectLeadId, String login, Boolean showHidden,
                                                                   ProjectContributorsLinkView.SortBy sortBy,
                                                                   SortDirection sortDirection,
                                                                   int pageIndex, int pageSize);

    List<UUID> getProjectLeadIds(UUID projectId);

    Set<Long> getProjectInvitedLeadIds(UUID projectId);

    Set<Long> getProjectRepoIds(UUID projectId);

    Page<RewardableItemView> getProjectRewardableItemsByTypeForProjectLeadAndContributorId(UUID projectId,
                                                                                           ContributionType contributionType,
                                                                                           ContributionStatus contributionStatus,
                                                                                           Long githubUserid,
                                                                                           int pageIndex, int pageSize,
                                                                                           String search,
                                                                                           Boolean includeIgnoredItems);

    String getProjectSlugById(UUID projectId);

    RewardableItemView getRewardableIssue(String repoOwner, String repoName, long issueNumber);

    RewardableItemView getRewardablePullRequest(String repoOwner, String repoName, long pullRequestNumber);

    Set<Long> removeUsedRepos(Collection<Long> repoIds);

    boolean hasUserAccessToProject(UUID projectId, UUID userId);

    boolean hasUserAccessToProject(String projectSlug, UUID userId);

    void updateProjectsRanking();

    Page<ChurnedContributorView> getChurnedContributors(UUID projectId, Integer pageIndex, Integer pageSize);

    Page<NewcomerView> getNewcomers(UUID projectId, Integer pageIndex, Integer pageSize);

    Page<ContributorActivityView> getMostActivesContributors(UUID projectId, Integer pageIndex, Integer pageSize);

    void hideContributorForProjectLead(UUID projectId, UUID projectLeadId, Long contributorGithubUserId);

    void showContributorForProjectLead(UUID projectId, UUID projectLeadId, Long contributorGithubUserId);

    void updateProjectsTags(Date now);
}
