package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.view.*;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

public interface ProjectFacadePort {

    Pair<ProjectId, String> createProject(UserId projectLeadId, CreateProjectCommand createProjectCommand);

    Pair<ProjectId, String> updateProject(UserId projectLeadId, UpdateProjectCommand updateProjectCommand);

    URL saveLogoImage(InputStream imageInputStream);

    Page<RewardableItemView> getRewardableItemsPageByTypeForProjectLeadAndContributorId(ProjectId projectId,
                                                                                        ContributionType contributionType,
                                                                                        ContributionStatus contributionStatus,
                                                                                        UserId projectLeadId,
                                                                                        Long githubUserid,
                                                                                        int pageIndex, int pageSize,
                                                                                        String search,
                                                                                        Boolean includeIgnoredItems);

    List<RewardableItemView> getAllCompletedRewardableItemsForProjectLeadAndContributorId(ProjectId projectId,
                                                                                          UserId projectLeadId,
                                                                                          Long githubUserId);

    RewardableItemView createAndCloseIssueForProjectIdAndRepositoryId(CreateAndCloseIssueCommand createAndCloseIssueCommand);

    RewardableItemView addRewardableIssue(ProjectId projectId, UserId projectLeadId, String issueUrl);

    RewardableItemView addRewardablePullRequest(ProjectId projectId, UserId projectLeadId, String pullRequestUrl);

    Page<ContributionView> contributions(ProjectId projectId, AuthenticatedUser caller, ContributionView.Filters filters,
                                         ContributionView.Sort sort, SortDirection direction,
                                         Integer page, Integer pageSize);

    void updateProjectsRanking();

    Page<ContributionView> staledContributions(ProjectId projectId, AuthenticatedUser caller, Integer page, Integer pageSize);

    Page<ChurnedContributorView> churnedContributors(ProjectId projectId, AuthenticatedUser caller, Integer page, Integer pageSize);

    Page<NewcomerView> newcomers(ProjectId projectId, AuthenticatedUser caller, Integer page, Integer pageSize);

    Page<ContributorActivityView> mostActives(ProjectId projectId, AuthenticatedUser caller, Integer page, Integer pageSize);

    void hideContributorForProjectLead(ProjectId projectId, UserId projectLeadId, Long contributorGithubUserId);

    void showContributorForProjectLead(ProjectId projectId, UserId projectLeadId, Long contributorGithubUserId);

    void updateProjectsTags();

    void refreshRecommendations();

    void refreshStats();
}
