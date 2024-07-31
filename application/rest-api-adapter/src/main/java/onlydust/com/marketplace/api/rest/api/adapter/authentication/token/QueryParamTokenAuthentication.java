package onlydust.com.marketplace.api.rest.api.adapter.authentication.token;

import onlydust.com.marketplace.api.rest.api.adapter.authentication.app.OnlyDustAppAuthentication;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.app.OnlyDustAppGrantedAuthority;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public record QueryParamTokenAuthentication(String token) implements OnlyDustAppAuthentication {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new OnlyDustAppGrantedAuthority(AuthenticatedUser.Role.UNSAFE_INTERNAL_SERVICE));
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

    }

    @Override
    public AuthenticatedUser getUser() {
        return null;
    }

    @Override
    public boolean isImpersonating() {
        return false;
    }

    @Override
    public AuthenticatedUser getImpersonator() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}
