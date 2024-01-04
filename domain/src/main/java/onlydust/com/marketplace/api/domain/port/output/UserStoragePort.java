package onlydust.com.marketplace.api.domain.port.output;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import onlydust.com.marketplace.api.domain.model.Contributor;
import onlydust.com.marketplace.api.domain.model.Currency;
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

public interface UserStoragePort {

  UserProfileView getProfileById(UUID userId);

  UserProfileView getProfileById(Long githubUserId);

  UserProfileView getProfileByLogin(String githubLogin);

  void saveProfile(UUID userId, UserProfile userProfile);

  Optional<User> getUserByGithubId(Long githubId);

  void createUser(User user);

  void updateUserIdentity(UUID userId, String githubLogin, String githubAvatarUrl, String emailFromGithub,
      Date lastSeenAt);

  UserPayoutInformation getPayoutInformationById(UUID id);

  void updateOnboardingWizardDisplayDate(UUID userId, Date date);

  void updateTermsAndConditionsAcceptanceDate(UUID userId, Date date);

  UUID acceptProjectLeaderInvitation(Long githubUserId, UUID projectId);

  UUID createApplicationOnProject(UUID userId, UUID projectId);

  UserPayoutInformation savePayoutInformationForUserId(UUID userId, UserPayoutInformation userPayoutInformation);

  UserRewardsPageView findRewardsForUserId(UUID userId, UserRewardView.Filters filters,
      int pageIndex, int pageSize,
      UserRewardView.SortBy sortBy, SortDirection sortDirection);

  UserRewardTotalAmountsView findRewardTotalAmountsForUserId(UUID userId);

  RewardView findRewardById(UUID rewardId);

  Page<RewardItemView> findRewardItemsPageById(UUID rewardId, int pageIndex, int pageSize);

  List<UserRewardView> findPendingInvoiceRewardsForRecipientId(Long githubUserId);

  List<Contributor> searchContributorsByLogin(Set<Long> reposIds, String login, int maxContributorCountToReturn);

  void saveProjectLead(UUID userId, UUID projectId);

  List<Currency> listRewardCurrencies(Long githubUserId);
}
