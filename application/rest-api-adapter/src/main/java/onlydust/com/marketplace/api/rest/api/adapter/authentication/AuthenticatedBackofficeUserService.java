package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.backoffice.OnlyDustBackofficeAuthentication;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static java.lang.String.format;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.unauthorized;

@AllArgsConstructor
@Slf4j
public class AuthenticatedBackofficeUserService {

    private final AuthenticationContext authenticationContext;

    /**
     * @return the authenticated user
     * @throws OnlyDustException if the user is not authenticated
     */
    public BackofficeUser getAuthenticatedBackofficeUser() {
        final Authentication authentication = authenticationContext.getAuthenticationFromContext();
        if (authentication instanceof AnonymousAuthenticationToken) {
            final OnlyDustException unauthorized = unauthorized(format("Unauthorized anonymous user %s", authentication));
            LOGGER.warn(unauthorized.toString());
            throw unauthorized;
        } else if (!authentication.isAuthenticated()) {
            final OnlyDustException unauthorized = unauthorized("Unauthorized non-authenticated user");
            LOGGER.warn(unauthorized.toString());
            throw unauthorized;
        } else if (!(authentication instanceof OnlyDustBackofficeAuthentication)) {
            final OnlyDustException internalError = unauthorized(format("Expected an OnlyDustBackofficeAuthentication, got %s", authentication.getClass()));
            LOGGER.error(internalError.toString());
            throw internalError;
        }

        return ((OnlyDustBackofficeAuthentication) authentication).getUser();
    }

    /**
     * @return the authenticated user if present, empty otherwise.
     * Does not throw any exception when the user is not authenticated.
     */
    public Optional<BackofficeUser> tryGetAuthenticatedUser() {
        final Authentication authentication = authenticationContext.getAuthenticationFromContext();
        if (authentication.isAuthenticated() && authentication instanceof OnlyDustBackofficeAuthentication) {
            return Optional.of(((OnlyDustBackofficeAuthentication) authentication).getUser());
        }
        return Optional.empty();
    }
}
