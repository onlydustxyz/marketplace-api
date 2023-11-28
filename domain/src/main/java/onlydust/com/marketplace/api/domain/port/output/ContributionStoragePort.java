package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.GithubRepo;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.view.ContributionDetailsView;
import onlydust.com.marketplace.api.domain.view.ContributionView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;

import java.util.List;
import java.util.UUID;

public interface ContributionStoragePort {
    Page<ContributionView> findContributionsForUser(Long contributorId,
                                                    ContributionView.Filters filters,
                                                    ContributionView.Sort sort,
                                                    SortDirection direction,
                                                    Integer page,
                                                    Integer pageSize);

    ContributionDetailsView findContributionById(UUID projectId, String contributionId, Long githubUserId);

    List<Project> listProjectsByContributor(Long contributorId, ContributionView.Filters filters);

    List<GithubRepo> listReposByContributor(Long contributorId, ContributionView.Filters filters);

    Long getContributorId(String contributionId);

    void ignoreContributions(UUID projectId, List<String> contributionIds);

    void unignoreContributions(UUID projectId, List<String> contributionIds);

    void refreshIgnoredContributions(UUID projectId);

    void refreshIgnoredContributions(List<Long> repoIds);
}
