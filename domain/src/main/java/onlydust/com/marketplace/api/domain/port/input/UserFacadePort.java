package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.*;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;

import java.io.InputStream;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface UserFacadePort {

    User getUserByGithubIdentity(GithubUserIdentity githubUserIdentity, boolean readOnly);

    UserProfileView getProfileById(UUID userId);

    UserProfileView getProfileById(Long githubUserId);

    UserProfileView getProfileByLogin(String githubLogin);

    UserProfileView updateProfile(UUID userId, UserProfile userProfile);

    UserPayoutSettings getPayoutSettingsForUserId(UUID id);

    void refreshActiveUserProfiles(ZonedDateTime since);

    void markUserAsOnboarded(UUID userId);

    void updateTermsAndConditionsAcceptanceDate(UUID userId);

    void acceptInvitationToLeadProject(Long githubUserId, UUID projectId);

    void applyOnProject(UUID id, UUID projectId);

    UserRewardsPageView getRewardsForUserId(UUID userId, UserRewardView.Filters filters,
                                            int pageIndex, int sanitizedPageSize,
                                            UserRewardView.SortBy sortBy, SortDirection sortDirection);

    UserRewardTotalAmountsView getRewardTotalAmountsForUserId(UUID userId);

    UserPayoutSettings updatePayoutSettings(UUID userId, UserPayoutSettings userPayoutSettings);

    RewardView getRewardByIdForRecipientId(UUID rewardId, Long recipientId);

    Page<RewardItemView> getRewardItemsPageByIdForRecipientId(UUID rewardId, Long recipientId, int pageIndex,
                                                              int pageSize);

    List<UserRewardView> getPendingInvoiceRewardsForRecipientId(Long githubUserId);

    void claimProjectForAuthenticatedUser(UUID projectId, User user);

    URL saveAvatarImage(InputStream imageInputStream);

    CompanyBillingProfile getCompanyBillingProfile(UUID userId);

    IndividualBillingProfile getIndividualBillingProfile(UUID userId);

    void updateBillingProfileType(UUID userId, BillingProfileType billingProfileType);

    void updateGithubProfile(User authenticatedUser);

    List<BillingProfile> getBillingProfiles(UUID id, Long githubUserId);
}
