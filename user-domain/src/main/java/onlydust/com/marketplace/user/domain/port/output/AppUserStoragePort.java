package onlydust.com.marketplace.user.domain.port.output;

import java.util.Optional;
import java.util.UUID;

public interface AppUserStoragePort {

    void replaceUser(UUID userId, Long currentGithubUserId, Long newGithubUserId, String githubLogin, String githubAvatarUrl);

    Optional<Long> getGithubUserId(UUID userId);
}
