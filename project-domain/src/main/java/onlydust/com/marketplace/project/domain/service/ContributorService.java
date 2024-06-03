package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.project.domain.model.Contributor;
import onlydust.com.marketplace.project.domain.model.GithubRepo;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.port.input.ContributorFacadePort;
import onlydust.com.marketplace.project.domain.port.output.*;
import onlydust.com.marketplace.project.domain.view.ContributionView;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

@AllArgsConstructor
public class ContributorService implements ContributorFacadePort {
    private static final int MAX_INTERNAL_CONTRIBUTOR_COUNT_TO_RETURN = 200;
    private final ProjectStoragePort projectStorage;
    private final GithubSearchPort githubSearchPort;
    private final UserStoragePort userStoragePort;
    private final ContributionStoragePort contributionStoragePort;
    private final RewardStoragePort rewardStoragePort;

    @Override
    public Pair<List<Contributor>, List<Contributor>> searchContributors(final UUID projectId, final Set<Long> repoIds,
                                                                         final String login,
                                                                         int maxInternalContributorCountToTriggerExternalSearch,
                                                                         int maxInternalContributorCountToReturn,
                                                                         boolean externalSearchOnly) {

        maxInternalContributorCountToReturn = Math.min(maxInternalContributorCountToReturn, MAX_INTERNAL_CONTRIBUTOR_COUNT_TO_RETURN);

        final List<Contributor> internalContributors = externalSearchOnly ? List.of() :
                searchInternalContributors(projectId, repoIds, login, maxInternalContributorCountToReturn);

        final List<Contributor> externalContributors =
                (externalSearchOnly || internalContributors.size() < maxInternalContributorCountToTriggerExternalSearch) &&
                        login != null && !login.isEmpty() ?
                        getExternalContributors(login) : List.of();

        return Pair.of(internalContributors, externalContributors);
    }

    @Override
    public Page<ContributionView> contributions(Optional<Long> callerGithubUserId,
                                                ContributionView.Filters filters,
                                                ContributionView.Sort sort,
                                                SortDirection direction,
                                                Integer page,
                                                Integer pageSize) {
        return contributionStoragePort.findContributions(
                callerGithubUserId, filters, sort, direction, page, pageSize);
    }

    @Override
    public List<Project> contributedProjects(Long contributorId, ContributionView.Filters filters) {
        return contributionStoragePort.listProjectsByContributor(contributorId, filters);
    }

    @Override
    public List<GithubRepo> contributedRepos(Long contributorId, ContributionView.Filters filters) {
        return contributionStoragePort.listReposByContributor(contributorId, filters);
    }

    @Override
    public List<CurrencyView> getRewardCurrencies(Long githubUserId, List<UUID> administratedBillingProfileIds) {
        return userStoragePort.listRewardCurrencies(githubUserId, administratedBillingProfileIds);
    }

    @Override
    public List<Project> rewardingProjects(Long githubUserId) {
        return rewardStoragePort.listProjectsByRecipient(githubUserId);
    }

    private List<Contributor> searchInternalContributors(UUID projectId, Set<Long> repoIds, String login,
                                                         int maxInternalContributorCountToReturn) {
        final Set<Long> searchInRepoIds = repoIds != null ? new HashSet<>(repoIds) : new HashSet<>();
        if (projectId != null) {
            searchInRepoIds.addAll(projectStorage.getProjectRepoIds(projectId));
        }

        return userStoragePort.searchContributorsByLogin(searchInRepoIds, login, maxInternalContributorCountToReturn);
    }

    private List<Contributor> getExternalContributors(String login) {
        return githubSearchPort.searchUsersByLogin(login).stream().map(
                identity -> Contributor.builder()
                        .id(identity)
                        .isRegistered(userStoragePort.getUserByGithubId(identity.getGithubUserId()).isPresent())
                        .build()
        ).toList();
    }
}
