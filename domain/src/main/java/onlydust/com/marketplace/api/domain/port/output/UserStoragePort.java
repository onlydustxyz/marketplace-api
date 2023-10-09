package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.view.UserProfileView;

import java.util.UUID;

public interface UserStoragePort {
    UserProfileView getProfileById(UUID userId);
}
