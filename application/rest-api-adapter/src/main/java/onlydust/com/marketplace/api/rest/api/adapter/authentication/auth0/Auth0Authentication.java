package onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0;

import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Builder;
import lombok.Value;
import onlydust.com.marketplace.api.domain.exception.OnlydustException;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.OnlyDustAuthentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Value
@Builder
public class Auth0Authentication implements OnlyDustAuthentication {
    DecodedJWT credentials;
    User user;
    String principal;
    Collection<? extends GrantedAuthority> authorities;
    @Builder.Default
    Boolean isAuthenticated = false;
    OnlydustException onlydustException;

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
}
