package onlydust.com.marketplace.api.rest.api.adapter.authentication.app;

import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import org.springframework.security.core.Authentication;

public interface OnlyDustAppAuthentication extends Authentication {
    AuthenticatedUser getUser();

    boolean isImpersonating();

    AuthenticatedUser getImpersonator();
}
