package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import onlydust.com.marketplace.api.domain.model.UserProfile;
import onlydust.com.marketplace.api.domain.view.UserRewardView;
import onlydust.com.marketplace.api.domain.view.UserProfileView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;

import java.util.UUID;

public interface UserFacadePort {

    User getUserByGithubIdentity(GithubUserIdentity githubUserIdentity);

    UserProfileView getProfileById(UUID userId);

    UserProfileView updateProfile(UUID userId, UserProfile userProfile);

    UserPayoutInformation getPayoutInformationForUserId(UUID id);

    void markUserAsOnboarded(UUID userId);

    void updateTermsAndConditionsAcceptanceDate(UUID userId);

    void acceptInvitationToLeadProject(Long githubUserId, UUID projectId);

    void applyOnProject(UUID id, UUID projectId);

    Page<UserRewardView> getRewardsForUserId(UUID userId, int pageIndex, int sanitizedPageSize,
                                             UserRewardView.SortBy sortBy,
                                             SortDirection sortDirection);
}
