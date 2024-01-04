package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserRole;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0Authentication;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthenticationServiceTest {

    private static final Faker faker = new Faker();

    @Test
    void should_return_authenticated_user() {
        // Given
        final AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        final AuthenticationService authenticationService = new AuthenticationService(authenticationContext);
        final UUID userId = UUID.randomUUID();
        final long githubUserId = faker.number().randomNumber();
        final List<UserRole> allowedRoles = List.of(UserRole.USER);
        final User user = User.builder()
                .githubUserId(githubUserId)
                .id(userId)
                .githubLogin(faker.name().username())
                .roles(List.of(UserRole.USER))
                .build();

        // When
        when(authenticationContext.getAuthenticationFromContext())
                .thenReturn(Auth0Authentication.builder()
                        .isAuthenticated(true)
                        .user(user)
                        .authorities(allowedRoles.stream().map(OnlyDustGrantedAuthority::new).collect(Collectors.toList()))
                        .build());
        final User authenticatedUser = authenticationService.getAuthenticatedUser();

        // Then
        assertEquals(userId, authenticatedUser.getId());
        assertEquals(githubUserId, authenticatedUser.getGithubUserId());
        assertEquals(allowedRoles, authenticatedUser.getRoles());
    }

    @Test
    void should_throw_exception_for_unauthenticated_user() {
        // Given
        final AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        final AuthenticationService authenticationService = new AuthenticationService(authenticationContext);

        // When
        when(authenticationContext.getAuthenticationFromContext())
                .thenReturn(mock(AnonymousAuthenticationToken.class));
        OnlyDustException onlydustException = null;
        try {
            authenticationService.getAuthenticatedUser();
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
        final AuthenticationService authenticationService = new AuthenticationService(authenticationContext);

        // When
        when(authenticationContext.getAuthenticationFromContext())
                .thenReturn(Auth0Authentication.builder().build());
        OnlyDustException onlydustException = null;
        try {
            authenticationService.getAuthenticatedUser();
        } catch (OnlyDustException e) {
            onlydustException = e;
        }

        // Then
        assertNotNull(onlydustException);
        assertEquals(401, onlydustException.getStatus());
        assertEquals("Unauthorized non-authenticated user", onlydustException.getMessage());
    }
}
