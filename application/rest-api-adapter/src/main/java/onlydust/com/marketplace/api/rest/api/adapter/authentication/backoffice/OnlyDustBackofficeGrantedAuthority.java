package onlydust.com.marketplace.api.rest.api.adapter.authentication.backoffice;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import org.springframework.security.core.GrantedAuthority;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class OnlyDustBackofficeGrantedAuthority implements GrantedAuthority {

    private final BackofficeUser.Role role;

    @Override
    public String getAuthority() {
        return role.name();
    }
}
