package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.GithubRepo;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.port.input.TechnologiesPort;
import onlydust.com.marketplace.api.domain.port.output.GithubStoragePort;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.domain.port.output.TrackingIssuePort;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class TechnologiesService implements TechnologiesPort {
    private final TrackingIssuePort trackingIssuePort;
    private final GithubStoragePort githubStoragePort;
    private final ProjectStoragePort projectStoragePort;

    @Override
    public void suggest(String name, User requester) {
        trackingIssuePort.createIssueForTechTeam("New technology suggestion: " + name,
                "Suggested by: " + requester.getLogin());
    }

    @Override
    public void refreshTechnologies(UUID projectId) {
        final var aggregatedTechnologies = githubStoragePort.findPublicReposByProjectId(projectId).stream()
                .map(GithubRepo::getTechnologies)
                .filter(Objects::nonNull)
                .reduce((technologies1, technologies2) -> {
                    technologies2.forEach((technology, value) -> technologies1.merge(technology, value, Long::sum));
                    return technologies1;
                });
        aggregatedTechnologies.ifPresent(technologies -> projectStoragePort.updateProjectTechnologies(projectId,
                technologies));
    }

    @Override
    public void refreshTechnologies(List<Long> repoIds) {
        final var projectIds = projectStoragePort.getProjectIdsLinkedToRepoIds(Set.copyOf(repoIds));
        projectIds.forEach(this::refreshTechnologies);
    }
}
