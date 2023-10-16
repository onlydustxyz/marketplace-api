package onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura;

import lombok.Builder;
import lombok.Value;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.OnlyDustAuthentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.stream.Collectors;

@Value
@Builder
public class HasuraAuthentication implements OnlyDustAuthentication {
    HasuraJwtPayload credentials;
    HasuraJwtPayload.HasuraClaims claims;
    User user;
    String principal;
    @Builder.Default
    Boolean isAuthenticated = false;

    User impersonator;
    boolean impersonating;

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
