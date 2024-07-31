package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.project.domain.model.GithubAccount;

import java.util.List;

public interface GithubOrganizationFacadePort {
    List<GithubAccount> getOrganizationsForAuthenticatedUser(final AuthenticatedUser authenticatedUser);
}
