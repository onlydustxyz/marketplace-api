package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.view.UserProfileView;

import java.util.Optional;
import java.util.UUID;

public interface UserStoragePort {
    UserProfileView getProfileById(UUID userId);

    Optional<User> getUserByGithubId(Long githubId);

    void createUser(User user);
}
