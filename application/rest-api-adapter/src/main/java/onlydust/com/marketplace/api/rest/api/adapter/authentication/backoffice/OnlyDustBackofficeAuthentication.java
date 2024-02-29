package onlydust.com.marketplace.api.rest.api.adapter.authentication.backoffice;

import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import org.springframework.security.core.Authentication;

public interface OnlyDustBackofficeAuthentication extends Authentication {
    BackofficeUser getUser();
}
