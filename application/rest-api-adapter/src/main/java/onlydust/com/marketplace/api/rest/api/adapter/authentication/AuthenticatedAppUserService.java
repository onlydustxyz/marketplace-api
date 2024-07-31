package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.app.OnlyDustAppAuthentication;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.project.domain.port.input.GithubUserPermissionsFacadePort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static java.lang.String.format;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.unauthorized;

@AllArgsConstructor
@Slf4j
public class AuthenticatedAppUserService {

    private final AuthenticationContext authenticationContext;
    private final GithubUserPermissionsFacadePort githubUserPermissionsFacadePort;

    /**
     * @return the authenticated user
     * @throws OnlyDustException if the user is not authenticated
     */
    public AuthenticatedUser getAuthenticatedUser() {
        final Authentication authentication = authenticationContext.getAuthenticationFromContext();
        if (authentication instanceof AnonymousAuthenticationToken) {
            final OnlyDustException unauthorized = unauthorized(format("Unauthorized anonymous user %s", authentication));
            LOGGER.warn(unauthorized.toString());
            throw unauthorized;
        } else if (!authentication.isAuthenticated()) {
            final OnlyDustException unauthorized = unauthorized("Unauthorized non-authenticated user");
            LOGGER.warn(unauthorized.toString());
            throw unauthorized;
        } else if (!(authentication instanceof OnlyDustAppAuthentication)) {
            final OnlyDustException internalError = unauthorized(format("Expected an OnlyDustAppAuthentication, got %s", authentication.getClass()));
            LOGGER.error(internalError.toString());
            throw internalError;
        }

        return ((OnlyDustAppAuthentication) authentication).getUser();
    }

    /**
     * @return the authenticated user if present, empty otherwise.
     * Does not throw any exception when the user is not authenticated.
     */
    public Optional<AuthenticatedUser> tryGetAuthenticatedUser() {
        final Authentication authentication = authenticationContext.getAuthenticationFromContext();
        if (authentication.isAuthenticated() && authentication instanceof OnlyDustAppAuthentication) {
            return Optional.of(((OnlyDustAppAuthentication) authentication).getUser());
        }
        return Optional.empty();
    }

    public void logout() {
        githubUserPermissionsFacadePort.logout(getAuthenticatedUser().githubUserId());
    }
}
