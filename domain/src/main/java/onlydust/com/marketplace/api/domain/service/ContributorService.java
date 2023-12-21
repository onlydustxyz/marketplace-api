package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.Contributor;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.model.GithubRepo;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.port.input.ContributorFacadePort;
import onlydust.com.marketplace.api.domain.port.output.*;
import onlydust.com.marketplace.api.domain.view.ContributionView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class ContributorService implements ContributorFacadePort {
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

        final List<Contributor> internalContributors = externalSearchOnly ? List.of() :
                searchInternalContributors(projectId, repoIds, login, maxInternalContributorCountToReturn);

        final List<Contributor> externalContributors =
                (externalSearchOnly || internalContributors.size() < maxInternalContributorCountToTriggerExternalSearch) &&
                login != null && !login.isEmpty() ?
                        getExternalContributors(login) : List.of();

        return Pair.of(internalContributors, externalContributors);
    }

    @Override
    public Page<ContributionView> contributions(Long contributorId,
                                                ContributionView.Filters filters,
                                                ContributionView.Sort sort,
                                                SortDirection direction,
                                                Integer page,
                                                Integer pageSize) {
        return contributionStoragePort.findContributions(
                contributorId, filters, sort, direction, page, pageSize);
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
    public List<Currency> getRewardCurrencies(Long githubUserId) {
        return userStoragePort.listRewardCurrencies(githubUserId);
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

        return searchInRepoIds.isEmpty() ? List.of() :
                userStoragePort.searchContributorsByLogin(searchInRepoIds, login, maxInternalContributorCountToReturn);
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
