package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.Contributor;
import onlydust.com.marketplace.api.domain.port.input.ContributorFacadePort;
import onlydust.com.marketplace.api.domain.port.output.GithubSearchPort;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.domain.port.output.UserStoragePort;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class ContributorService implements ContributorFacadePort {
    private final ProjectStoragePort projectStorage;
    private final GithubSearchPort githubSearchPort;
    private final UserStoragePort userStoragePort;

    @Override
    public Pair<List<Contributor>, List<Contributor>> searchContributors(UUID projectId, String login) {
        final List<Contributor> internalContributors = projectStorage.searchContributorsByLogin(projectId, login);
        final List<Contributor> externalContributors = internalContributors.size() < 5 ? getExternalContributors(login) : List.of();

        return Pair.of(internalContributors, externalContributors);
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
