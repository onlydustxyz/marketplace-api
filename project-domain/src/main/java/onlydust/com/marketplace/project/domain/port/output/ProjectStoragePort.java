package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.view.*;

import java.time.ZonedDateTime;
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

    void createProject(UUID projectId, String slug, String name, String shortDescription, String longDescription,
                       Boolean isLookingForContributors, List<NamedLink> moreInfos,
                       List<Long> githubRepoIds, UUID firstProjectLeaderId, List<Long> githubUserIdsAsProjectLeads,
                       ProjectVisibility visibility
            , String imageUrl, ProjectRewardSettings rewardSettings, List<UUID> ecosystemIds);

    void updateProject(UUID id, String slug, String name, String shortDescription, String longDescription,
                       Boolean isLookingForContributors, List<NamedLink> moreInfos,
                       List<Long> githubRepoIds, List<Long> githubUserIdsAsProjectLeadersToInvite,
                       List<UUID> projectLeadersToKeep, String imageUrl, ProjectRewardSettings rewardSettings, List<UUID> ecosystemIds);

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

    Optional<UUID> getProjectIdBySlug(String slug);

    RewardableItemView getRewardableIssue(String repoOwner, String repoName, long issueNumber);

    RewardableItemView getRewardablePullRequest(String repoOwner, String repoName, long pullRequestNumber);

    Set<Long> removeUsedRepos(Collection<Long> repoIds);

    boolean hasUserAccessToProject(UUID projectId, UUID userId);

    boolean hasUserAccessToProject(String projectSlug, UUID userId);

    void updateProjectsRanking();

    Page<ChurnedContributorView> getChurnedContributors(UUID projectId, Integer pageIndex, Integer pageSize);

    Page<NewcomerView> getNewcomers(UUID projectId, ZonedDateTime since, Integer pageIndex, Integer pageSize);

    Page<ContributorActivityView> getMostActivesContributors(UUID projectId, Integer pageIndex, Integer pageSize);

    void hideContributorForProjectLead(UUID projectId, UUID projectLeadId, Long contributorGithubUserId);

    void showContributorForProjectLead(UUID projectId, UUID projectLeadId, Long contributorGithubUserId);

    void updateProjectsTags(Date now);

}
