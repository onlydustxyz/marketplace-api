package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.exception.OnlydustException;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraAuthentication;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraJwtPayload;
import onlydust.com.marketplace.api.rest.api.adapter.exception.RestApiExceptionCode;
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
                    .message("Unauthorized")
                    .code(RestApiExceptionCode.UNAUTHORIZED)
                    .build();
            LOGGER.warn(unauthorized.toString());
            throw unauthorized;
        } else if (!authentication.isAuthenticated()) {
            final OnlydustException unauthorized = OnlydustException.builder()
                    .message("Unauthorized")
                    .code(RestApiExceptionCode.UNAUTHORIZED)
                    .rootException(((HasuraAuthentication) authentication).getOnlydustException())
                    .build();
            LOGGER.warn(unauthorized.toString());
            throw unauthorized;
        }
        final HasuraJwtPayload.HasuraClaims claims = (HasuraJwtPayload.HasuraClaims) authentication.getDetails();
        return User.builder()
                .id(claims.getUserId())
                .permissions(claims.getAllowedRoles())
                .githubUserId(claims.getGithubUserId())
                .build();
    }

}
