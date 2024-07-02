package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.model.github.GithubUserIdentity;
import onlydust.com.marketplace.project.domain.model.GithubAccount;
import onlydust.com.marketplace.project.domain.model.GithubMembership;

import java.util.List;
import java.util.Optional;

public interface GithubSearchPort {
    List<GithubUserIdentity> searchUsersByLogin(String login);

    List<GithubAccount> searchOrganizationsByGithubUserId(Long githubUserId);

    GithubMembership getGithubUserMembershipForOrganization(Long githubUserId, String userLogin,
                                                            String organizationLogin);

    Optional<GithubUserIdentity> getUserProfile(Long githubUserId);
}
