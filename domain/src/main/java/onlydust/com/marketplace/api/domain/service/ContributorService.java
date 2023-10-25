package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.Contributor;
import onlydust.com.marketplace.api.domain.model.GithubRepo;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.port.input.ContributorFacadePort;
import onlydust.com.marketplace.api.domain.port.output.ContributionStoragePort;
import onlydust.com.marketplace.api.domain.port.output.GithubSearchPort;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.api.domain.view.ContributionView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class ContributorService implements ContributorFacadePort {
    private final ProjectStoragePort projectStorage;
    private final GithubSearchPort githubSearchPort;
    private final UserStoragePort userStoragePort;
    private final ContributionStoragePort contributionStoragePort;

    @Override
    public Pair<List<Contributor>, List<Contributor>> searchContributors(UUID projectId, String login) {
        final List<Contributor> internalContributors = projectStorage.searchContributorsByLogin(projectId, login);
        final List<Contributor> externalContributors = internalContributors.size() < 5 ?
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
        return contributionStoragePort.findContributionsForUser(
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

    private List<Contributor> getExternalContributors(String login) {
        return githubSearchPort.searchUsersByLogin(login).stream().map(
                identity -> Contributor.builder()
                        .id(identity)
                        .isRegistered(userStoragePort.getUserByGithubId(identity.getGithubUserId()).isPresent())
                        .build()
        ).toList();
    }
}
