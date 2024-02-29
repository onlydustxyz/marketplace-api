package onlydust.com.marketplace.api.bootstrap.helper;

import lombok.Setter;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationContext;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.app.Auth0OnlyDustAppAuthentication;
import org.springframework.security.core.Authentication;

public class ITAuthenticationContext implements AuthenticationContext {

    @Setter
    private Authentication authentication = Auth0OnlyDustAppAuthentication.builder().isAuthenticated(false).build();

    @Override
    public Authentication getAuthenticationFromContext() {
        return authentication;
    }
}
