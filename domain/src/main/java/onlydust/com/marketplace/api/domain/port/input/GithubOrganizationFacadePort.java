package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.GithubAccount;

import java.util.List;

public interface GithubOrganizationFacadePort {
    List<GithubAccount> getOrganizationsForGithubPersonalToken(String githubAccessToken);
}
