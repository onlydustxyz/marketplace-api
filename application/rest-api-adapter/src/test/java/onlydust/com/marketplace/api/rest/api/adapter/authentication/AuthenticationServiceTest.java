package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.domain.exception.OnlydustException;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraAuthentication;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraJwtPayload;
import onlydust.com.marketplace.api.rest.api.adapter.exception.RestApiExceptionCode;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

import java.util.List;
import java.util.UUID;

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
        final AuthenticationService authenticationService = new AuthenticationService(
                authenticationContext);
        final UUID userId = UUID.randomUUID();
        final int githubUserId = faker.number().randomDigit();
        final List<String> allowedRoles = List.of(faker.pokemon().name(), faker.pokemon().location());
        final HasuraJwtPayload.HasuraClaims hasuraClaims = HasuraJwtPayload.HasuraClaims.builder()
                .githubUserId(githubUserId)
                .userId(userId)
                .allowedRoles(allowedRoles)
                .build();

        // When
        when(authenticationContext.getAuthenticationFromContext())
                .thenReturn(HasuraAuthentication.builder()
                        .isAuthenticated(true)
                        .claims(
                                hasuraClaims
                        )
                        .build());
        final User authenticatedUser = authenticationService.getAuthenticatedUser();

        // Then
        assertEquals(userId, authenticatedUser.getId());
        assertEquals(githubUserId, authenticatedUser.getGithubUserId());
        assertEquals(allowedRoles, authenticatedUser.getPermissions());
    }

    @Test
    void should_throw_exception_for_unauthenticated_user() {
        // Given
        final AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        final AuthenticationService authenticationService = new AuthenticationService(
                authenticationContext);

        // When
        when(authenticationContext.getAuthenticationFromContext())
                .thenReturn(mock(AnonymousAuthenticationToken.class));
        OnlydustException onlydustException = null;
        try {
            authenticationService.getAuthenticatedUser();
        } catch (OnlydustException e) {
            onlydustException = e;
        }

        // Then
        assertNotNull(onlydustException);
        assertEquals(RestApiExceptionCode.UNAUTHORIZED, onlydustException.getCode());
        assertEquals("Unauthorized", onlydustException.getMessage());
    }

    @Test
    void should_throw_exception_for_invalid_jwt() {
        // Given
        final AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        final AuthenticationService authenticationService = new AuthenticationService(
                authenticationContext);

        // When
        when(authenticationContext.getAuthenticationFromContext())
                .thenReturn(HasuraAuthentication.builder()
                        .build());
        OnlydustException onlydustException = null;
        try {
            authenticationService.getAuthenticatedUser();
        } catch (OnlydustException e) {
            onlydustException = e;
        }

        // Then
        assertNotNull(onlydustException);
        assertEquals(RestApiExceptionCode.UNAUTHORIZED, onlydustException.getCode());
        assertEquals("Unauthorized", onlydustException.getMessage());
    }


}
