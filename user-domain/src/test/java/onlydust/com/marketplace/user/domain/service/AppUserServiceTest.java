package onlydust.com.marketplace.user.domain.service;


import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.model.github.GithubUserIdentity;
import onlydust.com.marketplace.kernel.port.output.IndexerPort;
import onlydust.com.marketplace.user.domain.model.CreatedUser;
import onlydust.com.marketplace.user.domain.port.input.UserObserverPort;
import onlydust.com.marketplace.user.domain.port.output.AppUserStoragePort;
import onlydust.com.marketplace.user.domain.port.output.GithubOAuthAppPort;
import onlydust.com.marketplace.user.domain.port.output.GithubUserStoragePort;
import onlydust.com.marketplace.user.domain.port.output.IdentityProviderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AppUserServiceTest {
    private final Faker faker = new Faker();
    private AppUserStoragePort userStoragePort;
    private UserObserverPort userObserverPort;
    private AppUserService userService;

    @BeforeEach
    void setUp() {
        userObserverPort = mock(UserObserverPort.class);
        userStoragePort = mock(AppUserStoragePort.class);

        userService = new AppUserService(userStoragePort,
                mock(GithubOAuthAppPort.class),
                mock(IdentityProviderPort.class),
                mock(GithubUserStoragePort.class),
                mock(IndexerPort.class),
                userObserverPort);
    }

    @Test
    void should_find_user_given_a_github_id_and_update_it() {
        // Given
        final GithubUserIdentity githubUserIdentity =
                GithubUserIdentity.builder().githubUserId(faker.number().randomNumber()).avatarUrl(faker.internet().avatar()).login(faker.hacker().verb()).email(faker.internet().emailAddress()).build();

        final AuthenticatedUser user =
                AuthenticatedUser.builder().id(UserId.random()).avatarUrl(githubUserIdentity.avatarUrl()).githubUserId(githubUserIdentity.githubUserId()).login(githubUserIdentity.login()).email(githubUserIdentity.email())
                        .lastSeenAt(ZonedDateTime.now().minusDays(5)).build();

        // When
        when(userStoragePort.getRegisteredUserByGithubId(githubUserIdentity.githubUserId())).thenReturn(Optional.of(user));
        final AuthenticatedUser userByGithubIdentity = userService.getUserByGithubIdentity(githubUserIdentity, false);

        // Then
        verify(userStoragePort, times(1)).updateUserLastSeenAt(eq(user.id()));
        verify(userObserverPort, never()).onUserSignedUp(any());
        assertEquals(user, userByGithubIdentity);
        assertEquals(0, userByGithubIdentity.billingProfiles().size());
    }

    @Test
    void should_find_user_given_a_github_id_and_update_it_with_a_billing_profile() {
        // Given
        final GithubUserIdentity githubUserIdentity =
                GithubUserIdentity.builder().githubUserId(faker.number().randomNumber()).avatarUrl(faker.internet().avatar()).login(faker.hacker().verb()).email(faker.internet().emailAddress()).build();

        final AuthenticatedUser user =
                AuthenticatedUser.builder().id(UserId.random()).avatarUrl(githubUserIdentity.avatarUrl()).githubUserId(githubUserIdentity.githubUserId()).login(githubUserIdentity.login()).email(githubUserIdentity.email())
                        .lastSeenAt(ZonedDateTime.now().minusDays(5)).build();

        // When
        when(userStoragePort.getRegisteredUserByGithubId(githubUserIdentity.githubUserId())).thenReturn(Optional.of(user));
        final AuthenticatedUser userByGithubIdentity = userService.getUserByGithubIdentity(githubUserIdentity, false);

        // Then
        verify(userStoragePort, times(1)).updateUserLastSeenAt(eq(user.id()));
        verify(userObserverPort, never()).onUserSignedUp(any());
        assertEquals(user, userByGithubIdentity);
        assertEquals(0, userByGithubIdentity.billingProfiles().size());
    }

    @Test
    void should_find_user_given_a_github_id_but_not_update_it_when_read_only_is_true() {
        // Given
        final GithubUserIdentity githubUserIdentity =
                GithubUserIdentity.builder().githubUserId(faker.number().randomNumber()).avatarUrl(faker.internet().avatar()).login(faker.hacker().verb()).build();
        final AuthenticatedUser user =
                AuthenticatedUser.builder().id(UserId.random()).avatarUrl(githubUserIdentity.avatarUrl()).githubUserId(githubUserIdentity.githubUserId()).login(githubUserIdentity.login()).build();

        // When
        when(userStoragePort.getRegisteredUserByGithubId(githubUserIdentity.githubUserId())).thenReturn(Optional.of(user));
        final AuthenticatedUser userByGithubIdentity = userService.getUserByGithubIdentity(githubUserIdentity, true);

        // Then
        verify(userStoragePort, never()).updateUserLastSeenAt(any());
        verify(userObserverPort, never()).onUserSignedUp(any());
        assertEquals(user, userByGithubIdentity);
        assertEquals(0, userByGithubIdentity.billingProfiles().size());
    }

    @Test
    void should_find_user_given_a_github_id_but_not_update_it_when_lastSeenAt_is_in_last_24_hours() {
        // Given
        final GithubUserIdentity githubUserIdentity =
                GithubUserIdentity.builder().githubUserId(faker.number().randomNumber()).avatarUrl(faker.internet().avatar()).login(faker.hacker().verb()).build();
        final AuthenticatedUser user =
                AuthenticatedUser.builder().id(UserId.random()).avatarUrl(githubUserIdentity.avatarUrl()).githubUserId(githubUserIdentity.githubUserId()).login(githubUserIdentity.login())
                        .lastSeenAt(ZonedDateTime.now().minusHours(5)).build();

        // When
        when(userStoragePort.getRegisteredUserByGithubId(githubUserIdentity.githubUserId())).thenReturn(Optional.of(user));
        final AuthenticatedUser userByGithubIdentity = userService.getUserByGithubIdentity(githubUserIdentity, false);

        // Then
        verify(userStoragePort, never()).updateUserLastSeenAt(any());
        verify(userObserverPort, never()).onUserSignedUp(any());
        assertEquals(user, userByGithubIdentity);
        assertEquals(0, userByGithubIdentity.billingProfiles().size());
    }

    @Test
    void should_create_user_on_the_fly_when_user_with_github_id_doesnt_exist() {
        // Given
        final GithubUserIdentity githubUserIdentity =
                GithubUserIdentity.builder().githubUserId(faker.number().randomNumber()).avatarUrl(faker.internet().avatar()).login(faker.hacker().verb()).build();

        // When
        when(userStoragePort.getRegisteredUserByGithubId(githubUserIdentity.githubUserId())).thenReturn(Optional.empty());
        when(userStoragePort.tryCreateUser(any())).thenReturn(
                new CreatedUser(AuthenticatedUser.builder()
                        .id(UserId.random())
                        .avatarUrl(githubUserIdentity.avatarUrl())
                        .githubUserId(githubUserIdentity.githubUserId())
                        .login(githubUserIdentity.login())
                        .roles(List.of(AuthenticatedUser.Role.USER))
                        .build(), true));
        final AuthenticatedUser userByGithubIdentity = userService.getUserByGithubIdentity(githubUserIdentity, false);

        // Then
        verify(userStoragePort, never()).updateUserLastSeenAt(any());
        verify(userObserverPort, times(1)).onUserSignedUp(any());
        assertThat(userByGithubIdentity.id()).isNotNull();
        assertEquals(AuthenticatedUser.builder().id(userByGithubIdentity.id()).avatarUrl(githubUserIdentity.avatarUrl()).githubUserId(githubUserIdentity.githubUserId()).login(githubUserIdentity.login()).roles(List.of(AuthenticatedUser.Role.USER)).build(), userByGithubIdentity);
    }

    @Test
    void should_throw_exception_when_user_with_github_id_doesnt_exist_and_read_only_is_true() {
        // Given
        final GithubUserIdentity githubUserIdentity =
                GithubUserIdentity.builder().githubUserId(faker.number().randomNumber()).avatarUrl(faker.internet().avatar()).login(faker.hacker().verb()).build();

        // When
        when(userStoragePort.getRegisteredUserByGithubId(githubUserIdentity.githubUserId())).thenReturn(Optional.empty());

        // Then
        verify(userStoragePort, never()).updateUserLastSeenAt(any());
        verify(userObserverPort, never()).onUserSignedUp(any());
        assertThatThrownBy(() -> userService.getUserByGithubIdentity(githubUserIdentity, true))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage(("User %d not found").formatted(githubUserIdentity.githubUserId()));
    }
}