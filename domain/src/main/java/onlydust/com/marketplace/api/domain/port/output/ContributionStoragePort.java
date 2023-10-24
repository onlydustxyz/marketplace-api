package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.GithubRepo;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.view.ContributionView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;

import java.util.List;

public interface ContributionStoragePort {
    Page<ContributionView> findContributionsForUser(Long contributorId, ContributionView.Filters filters,
                                                    SortDirection direction, Integer page, Integer pageSize);

    List<Project> listProjectsByContributor(Long contributorId, ContributionView.Filters filters);

    List<GithubRepo> listReposByContributor(Long contributorId, ContributionView.Filters filters);
}
