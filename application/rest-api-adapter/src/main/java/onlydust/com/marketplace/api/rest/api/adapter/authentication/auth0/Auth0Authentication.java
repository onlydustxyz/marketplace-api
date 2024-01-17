package onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0;

import lombok.Builder;
import lombok.Value;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.OnlyDustAuthentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Value
@Builder
public class Auth0Authentication implements OnlyDustAuthentication {
    String credentials;
    User user;
    String principal;
    Collection<? extends GrantedAuthority> authorities;
    @Builder.Default
    Boolean isAuthenticated = false;

    User impersonator;
    boolean impersonating;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getDetails() {
        return this.user;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public boolean isAuthenticated() {
        return this.isAuthenticated;
    }

    @Override
    public void setAuthenticated(boolean b) throws IllegalArgumentException {

    }

    @Override
    public String getName() {
        return this.principal;
    }

    @Override
    public boolean isImpersonating() {
        return impersonating;
    }

    @Override
    public User getImpersonator() {
        return impersonator;
    }
}
