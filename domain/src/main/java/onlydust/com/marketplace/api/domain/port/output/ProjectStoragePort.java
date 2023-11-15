package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.ContributionType;
import onlydust.com.marketplace.api.domain.model.ProjectMoreInfoLink;
import onlydust.com.marketplace.api.domain.model.ProjectRewardSettings;
import onlydust.com.marketplace.api.domain.model.ProjectVisibility;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ProjectStoragePort {
    ProjectDetailsView getById(UUID projectId);

    ProjectDetailsView getBySlug(String slug);

    Page<ProjectCardView> findByTechnologiesSponsorsUserIdSearchSortBy(List<String> technologies, List<String> sponsors,
                                                                       UUID userId, String search,
                                                                       ProjectCardView.SortBy sort, Boolean mine,
                                                                       Integer pageIndex, Integer pageSize);

    Page<ProjectCardView> findByTechnologiesSponsorsSearchSortBy(List<String> technologies, List<String> sponsors,
                                                                 String search, ProjectCardView.SortBy sort,
                                                                 Integer pageIndex, Integer pageSize);

    String createProject(UUID projectId, String name, String shortDescription, String longDescription,
                         Boolean isLookingForContributors, List<ProjectMoreInfoLink> moreInfos,
                         List<Long> githubRepoIds, UUID firstProjectLeaderId, List<Long> githubUserIdsAsProjectLeads,
                         ProjectVisibility visibility
            , String imageUrl, ProjectRewardSettings rewardSettings);

    void updateProject(UUID id, String name, String shortDescription, String longDescription,
                       Boolean isLookingForContributors, List<ProjectMoreInfoLink> moreInfos,
                       List<Long> githubRepoIds, List<Long> githubUserIdsAsProjectLeadersToInvite,
                       List<UUID> projectLeadersToKeep, String imageUrl, ProjectRewardSettings rewardSettings);

    Page<ProjectContributorsLinkView> findContributors(UUID projectId, String login,
                                                       ProjectContributorsLinkView.SortBy sortBy,
                                                       SortDirection sortDirection,
                                                       int pageIndex, int pageSize);

    Page<ProjectContributorsLinkView> findContributorsForProjectLead(UUID projectId, String login,
                                                                     ProjectContributorsLinkView.SortBy sortBy,
                                                                     SortDirection sortDirection,
                                                                     int pageIndex, int pageSize);

    List<UUID> getProjectLeadIds(UUID projectId);

    Page<ProjectRewardView> findRewards(UUID projectId, ProjectRewardView.SortBy sortBy, SortDirection sortDirection,
                                        int pageIndex, int pageSize);

    ProjectBudgetsView findBudgets(UUID projectId);

    RewardView getProjectReward(UUID rewardId);

    Page<RewardItemView> getProjectRewardItems(UUID rewardId, int pageIndex, int pageSize);

    Set<Long> getProjectRepoIds(UUID projectId);

    Page<RewardableItemView> getProjectRewardableItemsByTypeForProjectLeadAndContributorId(UUID projectId,
                                                                                       ContributionType contributionType,
                                                                                       Long githubUserid,
                                                                                       int pageIndex, int pageSize,
                                                                                       String search,
                                                                                       Boolean includeIgnoredItems);

    String getProjectSlugById(UUID projectId);
}
