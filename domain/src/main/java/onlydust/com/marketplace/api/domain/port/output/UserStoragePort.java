package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.UserProfile;

import java.util.UUID;

public interface UserStoragePort {
    UserProfile getProfileById(UUID userId);
}
