package onlydust.com.marketplace.project.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.project.domain.port.output.GithubAuthenticationInfoPort;
import onlydust.com.marketplace.project.domain.port.output.GithubAuthenticationPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GithubUserPermissionsServiceTest {
    final GithubAuthenticationPort githubAuthenticationPort = mock(GithubAuthenticationPort.class);
    final GithubAuthenticationInfoPort githubAuthenticationInfoPort = mock(GithubAuthenticationInfoPort.class);
    final GithubUserPermissionsService authenticatedAppUserService = new GithubUserPermissionsService(githubAuthenticationPort, githubAuthenticationInfoPort);

    final Faker faker = new Faker();
    final long githubUserId = faker.number().randomNumber();

    @ParameterizedTest
    @ValueSource(strings = {"repo", "public_repo"})
    void should_know_if_user_is_authorized_to_apply_on_github_issue(String scope) {
        // Given
        final var token = faker.internet().password();
        final var scopes = new HashSet<>(Set.of("read:org", "read:packages"));
        scopes.add(scope);

        when(githubAuthenticationPort.getGithubPersonalToken(githubUserId)).thenReturn(token);
        when(githubAuthenticationInfoPort.getAuthorizedScopes(token)).thenReturn(scopes);

        // When
        final var isAllowed = authenticatedAppUserService.isUserAuthorizedToApplyOnProject(githubUserId);

        // Then
        assertTrue(isAllowed);
    }

    @Test
    void should_know_if_user_is_not_authorized_to_apply_on_github_issue() {
        // Given
        final var token = faker.internet().password();
        final var scopes = new HashSet<>(faker.lorem().words());

        when(githubAuthenticationPort.getGithubPersonalToken(githubUserId)).thenReturn(token);
        when(githubAuthenticationInfoPort.getAuthorizedScopes(token)).thenReturn(scopes);

        // When
        final var isAllowed = authenticatedAppUserService.isUserAuthorizedToApplyOnProject(githubUserId);

        // Then
        assertFalse(isAllowed);
    }
}