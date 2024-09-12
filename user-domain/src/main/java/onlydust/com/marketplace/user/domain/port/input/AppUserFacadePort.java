package onlydust.com.marketplace.user.domain.port.input;

import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.model.github.GithubUserIdentity;

public interface AppUserFacadePort {

    AuthenticatedUser getUserByGithubIdentity(GithubUserIdentity githubUserIdentity, boolean readOnly);

    void resetAndReplaceUser(UserId appUserId, String newGithubLogin, String githubOAuthAppId, String githubOAuthAppSecret);
}
