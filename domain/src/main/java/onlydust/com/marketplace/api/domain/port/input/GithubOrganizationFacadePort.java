package onlydust.com.marketplace.api.domain.port.input;

import java.util.List;
import onlydust.com.marketplace.api.domain.model.GithubAccount;
import onlydust.com.marketplace.api.domain.model.User;

public interface GithubOrganizationFacadePort {

  List<GithubAccount> getOrganizationsForAuthenticatedUser(final User authenticatedUser);
}
