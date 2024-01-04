package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import java.util.Optional;

public interface JwtService {

  Optional<OnlyDustAuthentication> getAuthenticationFromJwt(final String jwt, final String impersonationHeader);
}
