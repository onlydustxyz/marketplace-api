package onlydust.com.marketplace.user.domain.port.input;

import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.github.GithubUserIdentity;

import java.util.UUID;

public interface AppUserFacadePort {

    AuthenticatedUser getUserByGithubIdentity(GithubUserIdentity githubUserIdentity, boolean readOnly);

    void resetAndReplaceUser(UUID appUserId, String newGithubLogin, String githubOAuthAppId, String githubOAuthAppSecret);
}
