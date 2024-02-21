package onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key;

import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.model.UserRole;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.OnlyDustAuthentication;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.OnlyDustGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public record ApiKeyAuthentication(String apiKey) implements OnlyDustAuthentication {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new OnlyDustGrantedAuthority(UserRole.INTERNAL_SERVICE));
    }

    @Override
    public Object getCredentials() {
        return apiKey;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return apiKey;
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
