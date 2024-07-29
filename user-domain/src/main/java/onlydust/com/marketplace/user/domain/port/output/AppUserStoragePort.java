package onlydust.com.marketplace.user.domain.port.output;

import onlydust.com.marketplace.user.domain.model.User;
import onlydust.com.marketplace.user.domain.model.UserId;

import java.util.Optional;
import java.util.UUID;

public interface AppUserStoragePort {

    void replaceUser(UUID userId, Long currentGithubUserId, Long newGithubUserId, String githubLogin, String githubAvatarUrl);

    Optional<Long> getGithubUserId(UUID userId);

    Optional<User> findById(UserId userId);
}
