package onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key;

import onlydust.com.marketplace.api.rest.api.adapter.authentication.app.OnlyDustAppAuthentication;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.app.OnlyDustAppGrantedAuthority;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.project.domain.model.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public record ApiKeyAuthentication(String apiKey) implements OnlyDustAppAuthentication {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new OnlyDustAppGrantedAuthority(AuthenticatedUser.Role.INTERNAL_SERVICE));
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
