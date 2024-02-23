package onlydust.com.marketplace.api.rest.api.adapter.authentication.token;

import onlydust.com.marketplace.api.rest.api.adapter.authentication.OnlyDustAuthentication;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.OnlyDustGrantedAuthority;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.model.UserRole;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public record QueryParamTokenAuthentication(String token) implements OnlyDustAuthentication {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new OnlyDustGrantedAuthority(UserRole.UNSAFE_INTERNAL_SERVICE));
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
    public User getUser() {
        return null;
    }

    @Override
    public boolean isImpersonating() {
        return false;
    }

    @Override
    public User getImpersonator() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}
