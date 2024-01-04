package onlydust.com.marketplace.api.domain.port.input;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import onlydust.com.marketplace.api.domain.model.Contributor;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.model.GithubRepo;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.view.ContributionView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;
import org.apache.commons.lang3.tuple.Pair;

public interface ContributorFacadePort {

  Pair<List<Contributor>, List<Contributor>> searchContributors(UUID projectId, Set<Long> repoIds, String login,
      int maxInternalContributorCountToTriggerExternalSearch,
      int maxInternalContributorCountToReturn,
      boolean externalSearchOnly);

  Page<ContributionView> contributions(Long contributorId,
      ContributionView.Filters filters,
      ContributionView.Sort sort,
      SortDirection direction,
      Integer page, Integer pageSize);

  List<Project> contributedProjects(Long contributorId, ContributionView.Filters filters);

  List<GithubRepo> contributedRepos(Long contributorId, ContributionView.Filters filters);

  List<Project> rewardingProjects(Long githubUserId);

  List<Currency> getRewardCurrencies(Long githubUserId);
}
