package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.view.UserProfileView;

import java.util.UUID;

public interface UserFacadePort {

    UserProfileView getProfileById(UUID userId);

}
