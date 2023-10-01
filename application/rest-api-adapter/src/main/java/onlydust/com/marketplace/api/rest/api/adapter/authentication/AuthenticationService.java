package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraJwtPayload;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@AllArgsConstructor
public class AuthenticationService {

    public User getAuthenticatedUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final HasuraJwtPayload.HasuraClaims claims = (HasuraJwtPayload.HasuraClaims) authentication.getDetails();
        return User.builder()
                .id(claims.getUserId())
                .permissions(claims.getAllowedRoles())
                .githubUserId(claims.getGithubUserId())
                .build();
    }

}
