package onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0;

import lombok.NonNull;
import org.springframework.security.core.Authentication;

import java.util.Optional;

public interface AuthenticationService {
    Optional<Authentication> getAuthentication(@NonNull final Auth0JwtClaims userClaims,
                                               @NonNull final String credentials,
                                               final String impersonationHeader);
}
