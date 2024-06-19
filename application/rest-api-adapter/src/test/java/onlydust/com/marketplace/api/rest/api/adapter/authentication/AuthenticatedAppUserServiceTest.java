package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.app.Auth0OnlyDustAppAuthentication;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.app.OnlyDustAppGrantedAuthority;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.port.output.GithubAuthenticationPort;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class AuthenticatedAppUserServiceTest {

    private static final Faker faker = new Faker();

    final AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
    final GithubAuthenticationPort githubAuthenticationPort = mock(GithubAuthenticationPort.class);
    final AuthenticatedAppUserService authenticatedAppUserService = new AuthenticatedAppUserService(authenticationContext, githubAuthenticationPort);

    final UUID userId = UUID.randomUUID();
    final long githubUserId = faker.number().randomNumber();
    final User user = User.builder()
            .githubUserId(githubUserId)
            .id(userId)
            .githubLogin(faker.name().username())
            .roles(List.of(AuthenticatedUser.Role.USER))
            .build();

    @Test
    void should_return_authenticated_user() {
        // Given
        final List<AuthenticatedUser.Role> allowedRoles = List.of(AuthenticatedUser.Role.USER);

        // When
        when(authenticationContext.getAuthenticationFromContext())
                .thenReturn(Auth0OnlyDustAppAuthentication.builder()
                        .isAuthenticated(true)
                        .user(user)
                        .authorities(allowedRoles.stream().map(OnlyDustAppGrantedAuthority::new).collect(Collectors.toList()))
                        .build());
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        // Then
        assertEquals(userId, authenticatedUser.getId());
        assertEquals(githubUserId, authenticatedUser.getGithubUserId());
        assertEquals(allowedRoles, authenticatedUser.getRoles());
    }

    @Test
    void should_throw_exception_for_unauthenticated_user() {
        // When
        when(authenticationContext.getAuthenticationFromContext())
                .thenReturn(mock(AnonymousAuthenticationToken.class));
        OnlyDustException onlydustException = null;
        try {
            authenticatedAppUserService.getAuthenticatedUser();
        } catch (OnlyDustException e) {
            onlydustException = e;
        }

        // Then
        assertNotNull(onlydustException);
        assertEquals(401, onlydustException.getStatus());
    }

    @Test
    void should_throw_exception_for_invalid_jwt() {
        // When
        when(authenticationContext.getAuthenticationFromContext())
                .thenReturn(Auth0OnlyDustAppAuthentication.builder().build());
        OnlyDustException onlydustException = null;
        try {
            authenticatedAppUserService.getAuthenticatedUser();
        } catch (OnlyDustException e) {
            onlydustException = e;
        }

        // Then
        assertNotNull(onlydustException);
        assertEquals(401, onlydustException.getStatus());
        assertEquals("Unauthorized non-authenticated user", onlydustException.getMessage());
    }

    @Test
    void should_logout_from_github() {
        // Given
        when(authenticationContext.getAuthenticationFromContext())
                .thenReturn(Auth0OnlyDustAppAuthentication.builder()
                        .isAuthenticated(true)
                        .user(user)
                        .authorities(List.of(new OnlyDustAppGrantedAuthority(AuthenticatedUser.Role.USER)))
                        .build());

        // When
        authenticatedAppUserService.logout();

        // Then
        verify(githubAuthenticationPort).logout(githubUserId);
    }
}
