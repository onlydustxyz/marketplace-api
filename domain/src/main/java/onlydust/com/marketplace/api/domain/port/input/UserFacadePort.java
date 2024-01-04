package onlydust.com.marketplace.api.domain.port.input;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import onlydust.com.marketplace.api.domain.model.UserProfile;
import onlydust.com.marketplace.api.domain.view.RewardItemView;
import onlydust.com.marketplace.api.domain.view.RewardView;
import onlydust.com.marketplace.api.domain.view.UserProfileView;
import onlydust.com.marketplace.api.domain.view.UserRewardTotalAmountsView;
import onlydust.com.marketplace.api.domain.view.UserRewardView;
import onlydust.com.marketplace.api.domain.view.UserRewardsPageView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;

public interface UserFacadePort {

  User getUserByGithubIdentity(GithubUserIdentity githubUserIdentity, boolean createIfNotExists);

  UserProfileView getProfileById(UUID userId);

  UserProfileView getProfileById(Long githubUserId);

  UserProfileView getProfileByLogin(String githubLogin);

  UserProfileView updateProfile(UUID userId, UserProfile userProfile);

  UserPayoutInformation getPayoutInformationForUserId(UUID id);

  void markUserAsOnboarded(UUID userId);

  void updateTermsAndConditionsAcceptanceDate(UUID userId);

  void acceptInvitationToLeadProject(Long githubUserId, UUID projectId);

  void applyOnProject(UUID id, UUID projectId);

  UserRewardsPageView getRewardsForUserId(UUID userId, UserRewardView.Filters filters,
      int pageIndex, int sanitizedPageSize,
      UserRewardView.SortBy sortBy, SortDirection sortDirection);

  UserRewardTotalAmountsView getRewardTotalAmountsForUserId(UUID userId);

  UserPayoutInformation updatePayoutInformation(UUID userId, UserPayoutInformation userPayoutInformation);

  RewardView getRewardByIdForRecipientId(UUID rewardId, Long recipientId);

  Page<RewardItemView> getRewardItemsPageByIdForRecipientId(UUID rewardId, Long recipientId, int pageIndex,
      int pageSize);

  List<UserRewardView> getPendingInvoiceRewardsForRecipientId(Long githubUserId);

  void claimProjectForAuthenticatedUser(UUID projectId, User user);

  URL saveAvatarImage(InputStream imageInputStream);
}
