package onlydust.com.marketplace.user.domain.port.output;

import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.user.domain.model.NotificationRecipient;

import java.util.Date;
import java.util.Optional;

public interface AppUserStoragePort {

    void replaceUser(UserId userId, Long currentGithubUserId, Long newGithubUserId, String githubLogin, String githubAvatarUrl);

    Optional<Long> getGithubUserId(UserId userId);

    Optional<NotificationRecipient> findById(UserId userId);

    Optional<AuthenticatedUser> getRegisteredUserByGithubId(Long githubId);

    void updateUserLastSeenAt(UserId userId, Date lastSeenAt);

    AuthenticatedUser createUser(AuthenticatedUser user);
}
