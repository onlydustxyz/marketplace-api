package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.exception.OnlydustException;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

@AllArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationContext authenticationContext;

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
        }
        final UserClaims claims = (UserClaims) authentication.getDetails();
        return User.builder()
                .id(claims.getUserId())
                //TODO: .permissions(claims.getAllowedRoles())
                .githubUserId(claims.getGithubUserId())
                .build();
    }

}
