package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.view.*;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.UUID;

public interface ProjectFacadePort {

    Pair<UUID, String> createProject(UUID projectLeadId, CreateProjectCommand createProjectCommand);

    Pair<UUID, String> updateProject(UUID projectLeadId, UpdateProjectCommand updateProjectCommand);

    URL saveLogoImage(InputStream imageInputStream);

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
