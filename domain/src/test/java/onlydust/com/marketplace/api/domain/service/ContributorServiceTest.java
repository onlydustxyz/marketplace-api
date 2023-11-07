package onlydust.com.marketplace.api.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.domain.mocks.ContributorFaker;
import onlydust.com.marketplace.api.domain.model.Contributor;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.port.output.ContributionStoragePort;
import onlydust.com.marketplace.api.domain.port.output.GithubSearchPort;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.domain.port.output.UserStoragePort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ContributorServiceTest {

    final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
    final GithubSearchPort githubSearchPort = mock(GithubSearchPort.class);
    final UserStoragePort userStoragePort = mock(UserStoragePort.class);
    final ContributionStoragePort contributionStoragePort = mock(ContributionStoragePort.class);
    final ContributorService contributorService = new ContributorService(projectStoragePort, githubSearchPort,
            userStoragePort, contributionStoragePort);
    private final ContributorFaker contributorFaker = new ContributorFaker();
    private final Faker faker = new Faker();

    private final UUID projectId = UUID.randomUUID();
    private final Set<Long> repoIds = Set.of(10L, 20L);
    private final Set<Long> projectRepoIds = Set.of(100L, 200L, 20L);
    private final Set<Long> allRepoIds = Set.of(10L, 20L, 100L, 200L);

    @Test
    void should_return_internal_contributors_if_enough() {
        // Given
        final String login = faker.name().username();
        final List<Contributor> internalContributors = List.of(
                contributorFaker.contributor(),
                contributorFaker.contributor(),
                contributorFaker.contributor(),
                contributorFaker.contributor(),
                contributorFaker.contributor()
        );

        // When
        when(projectStoragePort.getProjectRepoIds(projectId)).thenReturn(projectRepoIds);
        when(userStoragePort.searchContributorsByLogin(allRepoIds, login, 100)).thenReturn(internalContributors);
        final var contributors = contributorService.searchContributors(projectId, repoIds, login, 5, 100);

        // Then
        verify(githubSearchPort, never()).searchUsersByLogin(anyString());
        verify(userStoragePort, never()).getUserByGithubId(anyLong());
        assertThat(contributors.getLeft()).containsExactlyElementsOf(internalContributors);
        assertThat(contributors.getRight()).isEmpty();
    }


    @Test
    void should_complete_with_external_contributors_if_not_enough() {
        // Given
        final String login = faker.name().username();
        final List<Contributor> internalContributors = List.of(
                contributorFaker.contributor(),
                contributorFaker.contributor(),
                contributorFaker.contributor()
        );

        final List<Contributor> externalContributors = List.of(
                contributorFaker.contributor(),
                contributorFaker.contributor(),
                contributorFaker.contributor(),
                contributorFaker.contributor(),
                contributorFaker.contributor()
        );

        // When
        when(projectStoragePort.getProjectRepoIds(projectId)).thenReturn(projectRepoIds);
        when(userStoragePort.searchContributorsByLogin(allRepoIds, login, 100)).thenReturn(internalContributors);
        when(githubSearchPort.searchUsersByLogin(login)).thenReturn(externalContributors.stream().map(Contributor::getId).toList());
        externalContributors.forEach(
                contributor -> when(userStoragePort.getUserByGithubId(contributor.getId().getGithubUserId()))
                        .thenReturn(contributor.getIsRegistered() ? Optional.of(User.builder().build()) :
                                Optional.empty()));
        final var contributors = contributorService.searchContributors(projectId, repoIds, login, 5, 100);

        // Then
        assertThat(contributors.getLeft()).containsExactlyElementsOf(internalContributors);
        assertThat(contributors.getRight()).containsExactlyElementsOf(externalContributors);
    }

    @Test
    void should_work_without_project_id() {
        // Given
        final String login = faker.name().username();
        final List<Contributor> internalContributors = List.of(
                contributorFaker.contributor(),
                contributorFaker.contributor(),
                contributorFaker.contributor(),
                contributorFaker.contributor(),
                contributorFaker.contributor()
        );

        // When
        when(userStoragePort.searchContributorsByLogin(repoIds, login, 100)).thenReturn(internalContributors);
        final var contributors = contributorService.searchContributors(null, repoIds, login, 5, 100);

        // Then
        verify(githubSearchPort, never()).searchUsersByLogin(anyString());
        verify(userStoragePort, never()).getUserByGithubId(anyLong());
        assertThat(contributors.getLeft()).containsExactlyElementsOf(internalContributors);
        assertThat(contributors.getRight()).isEmpty();
    }

    @Test
    void should_work_without_repo_ids() {
        // Given
        final String login = faker.name().username();
        final List<Contributor> internalContributors = List.of(
                contributorFaker.contributor(),
                contributorFaker.contributor(),
                contributorFaker.contributor(),
                contributorFaker.contributor(),
                contributorFaker.contributor()
        );

        // When
        when(projectStoragePort.getProjectRepoIds(projectId)).thenReturn(projectRepoIds);
        when(userStoragePort.searchContributorsByLogin(projectRepoIds, login, 100)).thenReturn(internalContributors);
        final var contributors = contributorService.searchContributors(projectId, null, login, 5, 100);

        // Then
        verify(githubSearchPort, never()).searchUsersByLogin(anyString());
        verify(userStoragePort, never()).getUserByGithubId(anyLong());
        assertThat(contributors.getLeft()).containsExactlyElementsOf(internalContributors);
        assertThat(contributors.getRight()).isEmpty();
    }
}
