package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import org.springframework.security.core.Authentication;

public interface AuthenticationContext {

  Authentication getAuthenticationFromContext();
}
