package onlydust.com.marketplace.api.rest.api.adapter.authentication.app;

import onlydust.com.marketplace.project.domain.model.User;
import org.springframework.security.core.Authentication;

public interface OnlyDustAppAuthentication extends Authentication {
    User getUser();

    boolean isImpersonating();

    User getImpersonator();
}
