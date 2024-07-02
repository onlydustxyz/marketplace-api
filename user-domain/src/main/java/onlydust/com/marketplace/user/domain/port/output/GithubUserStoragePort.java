package onlydust.com.marketplace.user.domain.port.output;

import onlydust.com.marketplace.kernel.model.github.GithubUserIdentity;

import java.util.List;

public interface GithubUserStoragePort {

    List<GithubUserIdentity> searchUsers(String login);
}
