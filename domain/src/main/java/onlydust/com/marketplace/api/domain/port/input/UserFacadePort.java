package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import onlydust.com.marketplace.api.domain.model.UserProfile;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.UUID;

public interface UserFacadePort {

    User getUserByGithubIdentity(GithubUserIdentity githubUserIdentity);

    UserProfileView getProfileById(UUID userId);

    UserProfileView getProfileById(Long githubUserId);

    UserProfileView getProfileByLogin(String githubLogin);

    UserProfileView updateProfile(UUID userId, UserProfile userProfile);

    UserPayoutInformation getPayoutInformationForUserId(UUID id);

    void markUserAsOnboarded(UUID userId);

    void updateTermsAndConditionsAcceptanceDate(UUID userId);

    void acceptInvitationToLeadProject(Long githubUserId, UUID projectId);

    void applyOnProject(UUID id, UUID projectId);

    UserRewardsPageView getRewardsForUserId(UUID userId, int pageIndex, int sanitizedPageSize,
                                             UserRewardView.SortBy sortBy,
                                             SortDirection sortDirection);

    UserRewardTotalAmountsView getRewardTotalAmountsForUserId(UUID userId);

    UserPayoutInformation updatePayoutInformation(UUID userId, UserPayoutInformation userPayoutInformation);

    RewardView getRewardByIdForRecipientId(UUID rewardId, Long recipientId);

    Page<RewardItemView> getRewardItemsPageByIdForRecipientId(UUID rewardId, Long recipientId, int pageIndex,
                                                              int pageSize);

    List<UserRewardView> getPendingInvoiceRewardsForRecipientId(Long githubUserId);

    void claimProjectForAuthenticatedUserAndGithubPersonalToken(UUID projectId, User user, String githubAccessToken);

    URL saveAvatarImage(InputStream imageInputStream);
}
