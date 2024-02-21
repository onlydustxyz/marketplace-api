package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.project.domain.model.GithubAccount;
import onlydust.com.marketplace.project.domain.model.User;

import java.util.List;

public interface GithubOrganizationFacadePort {
    List<GithubAccount> getOrganizationsForAuthenticatedUser(final User authenticatedUser);
}
