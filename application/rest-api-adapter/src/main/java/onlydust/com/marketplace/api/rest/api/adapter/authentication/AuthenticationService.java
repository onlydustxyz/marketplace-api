package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.model.User;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

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
            final OnlyDustException unauthorized = OnlyDustException.builder()
                    .message(String.format("Unauthorized anonymous user %s", authentication))
                    .status(401)
                    .build();
            LOGGER.warn(unauthorized.toString());
            throw unauthorized;
        } else if (!authentication.isAuthenticated()) {
            final OnlyDustException unauthorized = OnlyDustException.builder()
                    .message("Unauthorized")
                    .status(401)
                    .build();
            LOGGER.warn(unauthorized.toString());
            throw unauthorized;
        } else if (!(authentication instanceof OnlyDustAuthentication)) {
            final OnlyDustException internalError = OnlyDustException.builder()
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
