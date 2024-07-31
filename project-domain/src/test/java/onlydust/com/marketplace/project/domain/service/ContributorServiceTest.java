package onlydust.com.marketplace.project.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.project.domain.mocks.ContributorFaker;
import onlydust.com.marketplace.project.domain.model.Contributor;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.port.output.*;
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
    final RewardStoragePort rewardStoragePort = mock(RewardStoragePort.class);
    final ContributorService contributorService = new ContributorService(projectStoragePort, githubSearchPort,
            userStoragePort, contributionStoragePort, rewardStoragePort);
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
        final var contributors = contributorService.searchContributors(projectId, repoIds, login, 5, 100,
                false);

        // Then
        verify(githubSearchPort, never()).searchUsersByLogin(anyString());
        verify(userStoragePort, never()).getRegisteredUserByGithubId(anyLong());
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
                contributor -> when(userStoragePort.getRegisteredUserByGithubId(contributor.getId().getGithubUserId()))
                        .thenReturn(contributor.getIsRegistered() ? Optional.of(User.builder()
                                .githubUserId(contributor.getId().getGithubUserId())
                                .githubLogin(contributor.getId().getGithubLogin())
                                .githubAvatarUrl(contributor.getId().getGithubAvatarUrl())
                                .email(contributor.getId().getEmail())
                                .build()) :
                                Optional.empty()));
        final var contributors = contributorService.searchContributors(projectId, repoIds, login, 5, 100,
                false);

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
        final var contributors = contributorService.searchContributors(null, repoIds, login, 5, 100,
                false);

        // Then
        verify(githubSearchPort, never()).searchUsersByLogin(anyString());
        verify(userStoragePort, never()).getRegisteredUserByGithubId(anyLong());
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
        final var contributors = contributorService.searchContributors(projectId, null, login, 5, 100,
                false);

        // Then
        verify(githubSearchPort, never()).searchUsersByLogin(anyString());
        verify(userStoragePort, never()).getRegisteredUserByGithubId(anyLong());
        assertThat(contributors.getLeft()).containsExactlyElementsOf(internalContributors);
        assertThat(contributors.getRight()).isEmpty();
    }

    @Test
    void should_work_without_project_id_and_without_repo_ids() {
        // Given
        final List<Contributor> internalContributors = List.of(
                contributorFaker.contributor(),
                contributorFaker.contributor(),
                contributorFaker.contributor(),
                contributorFaker.contributor(),
                contributorFaker.contributor()
        );

        // When
        when(userStoragePort.searchContributorsByLogin(eq(Set.of()), eq(null), eq(100))).thenReturn(internalContributors);
        final var contributors = contributorService.searchContributors(null, null, null, 5, 100,
                false);

        // Then
        verify(githubSearchPort, never()).searchUsersByLogin(anyString());
        verify(userStoragePort, never()).getRegisteredUserByGithubId(anyLong());
        assertThat(contributors.getLeft()).containsExactlyElementsOf(internalContributors);
        assertThat(contributors.getRight()).isEmpty();
    }

    @Test
    void should_work_with_externalSearchOnly() {
        // Given
        final String login = faker.name().username();
        final List<Contributor> externalContributors = List.of(
                contributorFaker.contributor(),
                contributorFaker.contributor(),
                contributorFaker.contributor(),
                contributorFaker.contributor(),
                contributorFaker.contributor()
        );

        // When
        when(githubSearchPort.searchUsersByLogin(login)).thenReturn(externalContributors.stream().map(Contributor::getId).toList());
        externalContributors.forEach(
                contributor -> when(userStoragePort.getRegisteredUserByGithubId(contributor.getId().getGithubUserId()))
                        .thenReturn(contributor.getIsRegistered() ? Optional.of(User.builder()
                                .githubUserId(contributor.getId().getGithubUserId())
                                .githubLogin(contributor.getId().getGithubLogin())
                                .githubAvatarUrl(contributor.getId().getGithubAvatarUrl())
                                .email(contributor.getId().getEmail())
                                .build()) :
                                Optional.empty()));
        final var contributors = contributorService.searchContributors(null, null, login, 0, 0,
                true);

        // Then
        verify(userStoragePort, never()).searchContributorsByLogin(anySet(), anyString(), anyInt());
        verify(projectStoragePort, never()).getProjectRepoIds(any(UUID.class));
        assertThat(contributors.getLeft()).isEmpty();
        assertThat(contributors.getRight()).containsExactlyElementsOf(externalContributors);
    }
}
