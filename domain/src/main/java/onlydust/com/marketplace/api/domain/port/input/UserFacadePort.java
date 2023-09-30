package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.UserProfile;

import java.util.UUID;

public interface UserFacadePort {

    UserProfile getProfileById(UUID userId);
}
