package onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0;

import lombok.Builder;
import lombok.Value;
import onlydust.com.marketplace.api.domain.exception.OnlydustException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.stream.Collectors;

@Value
@Builder
public class Auth0Authentication implements Authentication {
    Auth0JwtPayload credentials;
    Auth0JwtPayload.Claims claims;
    String principal;
    @Builder.Default
    Boolean isAuthenticated = false;
    OnlydustException onlydustException;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.claims.allowedRoles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getDetails() {
        return this.claims;
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
