package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.model.*;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;

import java.time.ZonedDateTime;
import java.util.*;

public interface UserStoragePort {
    UserProfileView getProfileById(UUID userId);

    UserProfileView getProfileById(Long githubUserId);

    UserProfileView getProfileByLogin(String githubLogin);

    void saveProfile(UUID userId, UserProfile userProfile);

    Optional<User> getUserByGithubId(Long githubId);

    Optional<User> getUserById(UUID userId);

    User createUser(User user);

    void updateUserLastSeenAt(UUID userId, Date lastSeenAt);

    UserPayoutSettings getPayoutSettingsById(UUID id);

    void updateOnboardingWizardDisplayDate(UUID userId, Date date);

    void updateTermsAndConditionsAcceptanceDate(UUID userId, Date date);

    UUID acceptProjectLeaderInvitation(Long githubUserId, UUID projectId);

    UUID createApplicationOnProject(UUID userId, UUID projectId);

    UserPayoutSettings savePayoutSettingsForUserId(UUID userId, UserPayoutSettings userPayoutSettings);

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

    List<User> getUsersLastSeenSince(ZonedDateTime since);

    void saveUsers(List<User> users);

    void saveUser(User user);
}
