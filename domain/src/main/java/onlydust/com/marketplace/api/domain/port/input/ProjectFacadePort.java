package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.*;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.UUID;

public interface ProjectFacadePort {
    ProjectDetailsView getById(UUID projectId, User caller);

    ProjectDetailsView getBySlug(String slug, User caller);

    Page<ProjectCardView> getByTagsTechnologiesEcosystemsUserIdSearchSortBy(List<Project.Tag> tags, List<String> technologies, List<UUID> ecosystemIds,
                                                                            String search, ProjectCardView.SortBy sort,
                                                                            UUID userId, Boolean mine,
                                                                            Integer pageIndex, Integer pageSize);

    Page<ProjectCardView> getByTagsTechnologiesEcosystemsSearchSortBy(List<Project.Tag> tags, List<String> technologies, List<UUID> ecosystemIds,
                                                                      String search, ProjectCardView.SortBy sort,
                                                                      Integer pageIndex, Integer pageSize);


    Pair<UUID, String> createProject(CreateProjectCommand createProjectCommand);

    Pair<UUID, String> updateProject(UUID projectLeadId, UpdateProjectCommand updateProjectCommand);

    URL saveLogoImage(InputStream imageInputStream);

    ProjectContributorsLinkViewPage getContributors(UUID projectId, String login,
                                                    ProjectContributorsLinkView.SortBy sortBy,
                                                    SortDirection sortDirection,
                                                    Integer pageIndex,
                                                    Integer pageSize);

    ProjectContributorsLinkViewPage getContributorsForProjectLeadId(UUID projectId, String login,
                                                                    UUID projectLeadId, Boolean showHidden,
                                                                    ProjectContributorsLinkView.SortBy sortBy,
                                                                    SortDirection sortDirection,
                                                                    Integer pageIndex,
                                                                    Integer pageSize);

    ProjectRewardsPageView getRewards(UUID projectId, UUID projectLeadId,
                                      ProjectRewardView.Filters filters,
                                      Integer pageIndex, Integer pageSize,
                                      ProjectRewardView.SortBy sortBy, SortDirection sortDirection);

    ProjectBudgetsView getBudgets(UUID projectId, UUID projectLeadId);

    RewardView getRewardByIdForProjectLead(UUID projectId, UUID rewardId, UUID userId);

    Page<RewardItemView> getRewardItemsPageByIdForProjectLead(UUID projectId, UUID rewardId, UUID projectLead,
                                                              int pageIndex, int pageSize);

    Page<RewardableItemView> getRewardableItemsPageByTypeForProjectLeadAndContributorId(UUID projectId,
                                                                                        ContributionType contributionType,
                                                                                        ContributionStatus contributionStatus,
                                                                                        UUID projectLeadId,
                                                                                        Long githubUserid,
                                                                                        int pageIndex, int pageSize,
                                                                                        String search,
                                                                                        Boolean includeIgnoredItems);

    List<RewardableItemView> getAllCompletedRewardableItemsForProjectLeadAndContributorId(UUID projectId,
                                                                                          UUID projectLeadId,
                                                                                          Long githubUserId);

    RewardableItemView createAndCloseIssueForProjectIdAndRepositoryId(CreateAndCloseIssueCommand createAndCloseIssueCommand);

    RewardableItemView addRewardableIssue(UUID projectId, UUID projectLeadId, String issueUrl);

    RewardableItemView addRewardablePullRequest(UUID projectId, UUID projectLeadId, String pullRequestUrl);

    Page<ContributionView> contributions(UUID projectId, User caller, ContributionView.Filters filters,
                                         ContributionView.Sort sort, SortDirection direction,
                                         Integer page, Integer pageSize);

    void updateProjectsRanking();

    Page<ContributionView> staledContributions(UUID projectId, User caller, Integer page, Integer pageSize);

    Page<ChurnedContributorView> churnedContributors(UUID projectId, User caller, Integer page, Integer pageSize);

    Page<NewcomerView> newcomers(UUID projectId, User caller, Integer page, Integer pageSize);

    Page<ContributorActivityView> mostActives(UUID projectId, User caller, Integer page, Integer pageSize);

    void hideContributorForProjectLead(UUID projectId, UUID projectLeadId, Long contributorGithubUserId);

    void showContributorForProjectLead(UUID projectId, UUID projectLeadId, Long contributorGithubUserId);

    void updateProjectsTags();
}
