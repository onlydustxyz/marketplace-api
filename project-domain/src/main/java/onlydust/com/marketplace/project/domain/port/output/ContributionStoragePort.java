package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.project.domain.model.GithubRepo;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.view.ContributionDetailsView;
import onlydust.com.marketplace.project.domain.view.ContributionView;

import java.util.List;
import java.util.UUID;

public interface ContributionStoragePort {
    Page<ContributionView> findContributions(Long contributorId,
                                             ContributionView.Filters filters,
                                             ContributionView.Sort sort,
                                             SortDirection direction,
                                             Integer page,
                                             Integer pageSize);

    ContributionDetailsView findContributionById(UUID projectId, String contributionId);

    List<Project> listProjectsByContributor(Long contributorId, ContributionView.Filters filters);

    List<GithubRepo> listReposByContributor(Long contributorId, ContributionView.Filters filters);

    Long getContributorId(String contributionId);

    void ignoreContributions(UUID projectId, List<String> contributionIds);

    void unignoreContributions(UUID projectId, List<String> contributionIds);

    void refreshIgnoredContributions(UUID projectId);

    void refreshIgnoredContributions(List<Long> repoIds);
}
