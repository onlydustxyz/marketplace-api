package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import onlydust.com.marketplace.project.domain.model.UserRole;
import org.springframework.security.core.GrantedAuthority;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class OnlyDustGrantedAuthority implements GrantedAuthority {

    private final UserRole role;

    @Override
    public String getAuthority() {
        return role.name();
    }
}
