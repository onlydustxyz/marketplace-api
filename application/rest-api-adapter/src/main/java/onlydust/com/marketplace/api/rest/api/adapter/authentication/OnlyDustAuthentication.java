package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import onlydust.com.marketplace.api.domain.model.User;
import org.springframework.security.core.Authentication;

public interface OnlyDustAuthentication extends Authentication {

  User getUser();

  boolean isImpersonating();

  User getImpersonator();
}
