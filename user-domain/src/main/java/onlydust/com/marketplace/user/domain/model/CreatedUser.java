package onlydust.com.marketplace.user.domain.model;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;

public record CreatedUser(@NonNull AuthenticatedUser user, boolean isNew) {
}
