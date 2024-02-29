package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import org.springframework.security.core.Authentication;

import java.util.Optional;

public interface JwtService {
    Optional<Authentication> getAuthenticationFromJwt(final String jwt, final String impersonationHeader);
}
