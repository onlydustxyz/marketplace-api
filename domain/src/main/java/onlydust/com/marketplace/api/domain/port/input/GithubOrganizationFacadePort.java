package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.GithubAccount;
import onlydust.com.marketplace.api.domain.model.User;

import java.util.List;

public interface GithubOrganizationFacadePort {
    List<GithubAccount> getOrganizationsForAuthenticatedUserAndGithubPersonalToken(String githubAccessToken,
                                                                                   final User authenticatedUser);
}
