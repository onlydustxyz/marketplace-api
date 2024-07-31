package onlydust.com.marketplace.user.domain.port.output;

import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.user.domain.model.NotificationRecipient;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public interface AppUserStoragePort {

    void replaceUser(UUID userId, Long currentGithubUserId, Long newGithubUserId, String githubLogin, String githubAvatarUrl);

    Optional<Long> getGithubUserId(UUID userId);

    Optional<NotificationRecipient> findById(NotificationRecipient.Id userId);

    Optional<AuthenticatedUser> getRegisteredUserByGithubId(Long githubId);

    void updateUserLastSeenAt(UUID userId, Date lastSeenAt);

    AuthenticatedUser createUser(AuthenticatedUser user);
}
