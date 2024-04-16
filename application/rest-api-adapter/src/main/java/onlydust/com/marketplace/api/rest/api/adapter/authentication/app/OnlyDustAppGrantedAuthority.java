package onlydust.com.marketplace.api.rest.api.adapter.authentication.app;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import org.springframework.security.core.GrantedAuthority;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class OnlyDustAppGrantedAuthority implements GrantedAuthority {

    private final AuthenticatedUser.Role role;

    @Override
    public String getAuthority() {
        return role.name();
    }
}
