package onlydust.com.marketplace.user.domain.port.output;

import lombok.NonNull;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;

import java.util.Optional;

public interface BackofficeUserStoragePort {
    Optional<BackofficeUser> getUserByEmail(@NonNull final String email);

    void save(@NonNull final BackofficeUser user);
}
