package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import onlydust.com.marketplace.api.domain.view.UserProfileView;

import java.util.UUID;

public interface UserFacadePort {

    User getUserByGithubIdentity(GithubUserIdentity githubUserIdentity);

    UserProfileView getProfileById(UUID userId);

    UserPayoutInformation getPayoutInformationForUserId(UUID id);
}
