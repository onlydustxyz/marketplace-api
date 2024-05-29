package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.project.domain.model.Contributor;
import onlydust.com.marketplace.project.domain.model.GithubRepo;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.view.ContributionView;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ContributorFacadePort {
    Pair<List<Contributor>, List<Contributor>> searchContributors(UUID projectId, Set<Long> repoIds, String login,
                                                                  int maxInternalContributorCountToTriggerExternalSearch,
                                                                  int maxInternalContributorCountToReturn,
                                                                  boolean externalSearchOnly);

    Page<ContributionView> contributions(Optional<Long> callerGithubUserId,
                                         ContributionView.Filters filters,
                                         ContributionView.Sort sort,
                                         SortDirection direction,
                                         Integer page, Integer pageSize);

    List<Project> contributedProjects(Long contributorId, ContributionView.Filters filters);

    List<GithubRepo> contributedRepos(Long contributorId, ContributionView.Filters filters);

    List<Project> rewardingProjects(Long githubUserId);

    List<CurrencyView> getRewardCurrencies(Long githubUserId, List<UUID> administratedBillingProfileIds);
}
