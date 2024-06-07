package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.project.domain.model.Contributor;
import onlydust.com.marketplace.project.domain.model.Reward;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.model.UserProfile;
import onlydust.com.marketplace.project.domain.view.*;

import java.time.ZonedDateTime;
import java.util.*;

public interface UserStoragePort {
    UserProfileView getProfileById(UUID userId);

    void saveProfile(UUID userId, UserProfile userProfile);

    Optional<User> getUserByGithubId(Long githubId);

    Optional<User> getUserById(UUID userId);

    User createUser(User user);

    void updateUserLastSeenAt(UUID userId, Date lastSeenAt);

    void updateOnboardingWizardDisplayDate(UUID userId, Date date);

    void updateTermsAndConditionsAcceptanceDate(UUID userId, Date date);

    UUID acceptProjectLeaderInvitation(Long githubUserId, UUID projectId);

    UUID createApplicationOnProject(UUID userId, UUID projectId);

    RewardDetailsView findRewardById(UUID rewardId);

    Page<RewardItemView> findRewardItemsPageById(UUID rewardId, int pageIndex, int pageSize);

    List<Contributor> searchContributorsByLogin(Set<Long> reposIds, String login, int maxContributorCountToReturn);

    void saveProjectLead(UUID userId, UUID projectId);

    List<CurrencyView> listRewardCurrencies(Long githubUserId, List<UUID> administratedBillingProfileIds);

    List<User> getUsersLastSeenSince(ZonedDateTime since);

    void saveUsers(List<User> users);

    void saveUser(User user);

    void refreshUserRanksAndStats();

    void historizeUserRanks();
}
