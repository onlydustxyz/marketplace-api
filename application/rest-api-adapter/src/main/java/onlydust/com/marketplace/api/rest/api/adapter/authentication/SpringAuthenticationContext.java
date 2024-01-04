package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SpringAuthenticationContext implements AuthenticationContext {

  @Override
  public Authentication getAuthenticationFromContext() {
    return SecurityContextHolder.getContext().getAuthentication();
  }
}
