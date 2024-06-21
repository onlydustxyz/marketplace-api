package onlydust.com.marketplace.user.domain.port.input;

import java.util.UUID;

public interface AppUserFacadePort {

    void resetAndReplaceUser(UUID appUserId, String newGithubLogin, String githubOAuthAppId, String githubOAuthAppSecret);
}
