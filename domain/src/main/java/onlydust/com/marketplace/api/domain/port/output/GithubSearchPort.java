package onlydust.com.marketplace.api.domain.port.output;

import java.util.List;
import onlydust.com.marketplace.api.domain.model.GithubAccount;
import onlydust.com.marketplace.api.domain.model.GithubMembership;
import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;

public interface GithubSearchPort {

  List<GithubUserIdentity> searchUsersByLogin(String login);

  List<GithubAccount> searchOrganizationsByGithubUserId(Long githubUserId);

  GithubMembership getGithubUserMembershipForOrganization(Long githubUserId, String userLogin,
      String organizationLogin);
}
