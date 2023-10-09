package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.view.UserProfileView;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.api.domain.port.output.UserStoragePort;

import java.util.UUID;

@AllArgsConstructor
public class UserService implements UserFacadePort {

    private final UserStoragePort userStoragePort;

    @Override
    public UserProfileView getProfileById(UUID userId) {
        return userStoragePort.getProfileById(userId);
    }

}
