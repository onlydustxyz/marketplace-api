package onlydust.com.marketplace.api.bootstrap.helper;

import lombok.Setter;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationContext;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

public class ITAuthenticationContext implements AuthenticationContext {

    @Setter
    private Authentication authentication = new AnonymousAuthenticationToken("anonymous", null, null);

    @Override
    public Authentication getAuthenticationFromContext() {
        return authentication;
    }
}
