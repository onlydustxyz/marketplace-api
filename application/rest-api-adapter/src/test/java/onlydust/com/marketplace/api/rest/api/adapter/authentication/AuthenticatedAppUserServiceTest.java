package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.app.Auth0OnlyDustAppAuthentication;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.app.OnlyDustAppGrantedAuthority;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.project.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthenticatedAppUserServiceTest {

    private static final Faker faker = new Faker();

    @Test
    void should_return_authenticated_user() {
        // Given
        final AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        final AuthenticatedAppUserService authenticatedAppUserService = new AuthenticatedAppUserService(authenticationContext);
        final UUID userId = UUID.randomUUID();
        final long githubUserId = faker.number().randomNumber();
        final List<AuthenticatedUser.Role> allowedRoles = List.of(AuthenticatedUser.Role.USER);
        final User user = User.builder()
                .githubUserId(githubUserId)
                .id(userId)
                .githubLogin(faker.name().username())
                .roles(List.of(AuthenticatedUser.Role.USER))
                .build();

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
        // Given
        final AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        final AuthenticatedAppUserService authenticatedAppUserService = new AuthenticatedAppUserService(authenticationContext);

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
        // Given
        final AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        final AuthenticatedAppUserService authenticatedAppUserService = new AuthenticatedAppUserService(authenticationContext);

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
}
