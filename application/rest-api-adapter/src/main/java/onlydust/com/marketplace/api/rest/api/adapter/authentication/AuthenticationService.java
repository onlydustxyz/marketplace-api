package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.exception.OnlydustException;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

@AllArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationContext authenticationContext;

    /**
     * @return the authenticated user
     * @throws OnlydustException if the user is not authenticated
     */
    public User getAuthenticatedUser() {
        final Authentication authentication = authenticationContext.getAuthenticationFromContext();
        if (authentication instanceof AnonymousAuthenticationToken) {
            final OnlydustException unauthorized = OnlydustException.builder()
                    .message(String.format("Unauthorized anonymous user %s", authentication))
                    .status(401)
                    .build();
            LOGGER.warn(unauthorized.toString());
            throw unauthorized;
        } else if (!authentication.isAuthenticated()) {
            final OnlydustException unauthorized = OnlydustException.builder()
                    .message("Unauthorized")
                    .status(401)
                    .rootException(((Auth0Authentication) authentication).getOnlydustException())
                    .build();
            LOGGER.warn(unauthorized.toString());
            throw unauthorized;
        } else if (!(authentication instanceof OnlyDustAuthentication)) {
            final OnlydustException internalError = OnlydustException.builder()
                    .message(String.format("Expected an OnlyDustAuthentication, got %s", authentication.getClass()))
                    .status(500)
                    .build();
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
}
