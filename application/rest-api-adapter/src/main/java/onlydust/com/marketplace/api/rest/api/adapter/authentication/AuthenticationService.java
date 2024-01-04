package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraAuthentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static java.lang.String.format;

@AllArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationContext authenticationContext;

    /**
     * @return the authenticated user
     * @throws OnlyDustException if the user is not authenticated
     */
    public User getAuthenticatedUser() {
        final Authentication authentication = authenticationContext.getAuthenticationFromContext();
        if (authentication instanceof AnonymousAuthenticationToken) {
            final OnlyDustException unauthorized = OnlyDustException.unauthorized(format("Unauthorized anonymous user" +
                                                                                         " %s", authentication));
            LOGGER.warn(unauthorized.toString());
            throw unauthorized;
        } else if (!authentication.isAuthenticated()) {
            final OnlyDustException unauthorized = OnlyDustException.unauthorized("Unauthorized non-authenticated " +
                                                                                  "user");
            LOGGER.warn(unauthorized.toString());
            throw unauthorized;
        } else if (!(authentication instanceof OnlyDustAuthentication)) {
            final OnlyDustException internalError = OnlyDustException.internalServerError(format("Expected an " +
                                                                                                 "OnlyDustAuthentication, got %s", authentication.getClass()));
            LOGGER.error(internalError.toString());
            throw internalError;
        }

        return ((OnlyDustAuthentication) authentication).getUser();
    }

    /**
     * @return the authenticated user if present, empty otherwise.
     * Does not throw any exception when the user is not authenticated.
     */
    public Optional<User> tryGetAuthenticatedUser() {
        final Authentication authentication = authenticationContext.getAuthenticationFromContext();
        if (authentication.isAuthenticated() && authentication instanceof OnlyDustAuthentication) {
            return Optional.of(((OnlyDustAuthentication) authentication).getUser());
        }
        return Optional.empty();
    }

    @Deprecated
    public Optional<HasuraAuthentication> tryGetHasuraAuthentication() {
        final Authentication authentication = authenticationContext.getAuthenticationFromContext();
        if (authentication.isAuthenticated() && authentication instanceof HasuraAuthentication) {
            return Optional.of((HasuraAuthentication) authentication);
        }
        return Optional.empty();
    }
}
