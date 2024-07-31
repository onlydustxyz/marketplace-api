package onlydust.com.marketplace.user.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;

import java.util.UUID;

@Builder
@Value
@Accessors(fluent = true)
public class BillingProfileLinkView {
    UUID id;
    Role role;

    public AuthenticatedUser.BillingProfileMembership toBillingProfileMembership() {
        return new AuthenticatedUser.BillingProfileMembership(id, switch (role) {
            case ADMIN -> AuthenticatedUser.BillingProfileMembership.Role.ADMIN;
            case MEMBER -> AuthenticatedUser.BillingProfileMembership.Role.MEMBER;
        });
    }

    public enum Role {
        ADMIN, MEMBER;
    }
}
