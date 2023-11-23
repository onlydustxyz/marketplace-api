package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.Contributor;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import onlydust.com.marketplace.api.domain.model.UserProfile;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;

import java.util.*;

public interface UserStoragePort {
    UserProfileView getProfileById(UUID userId);

    UserProfileView getProfileById(Long githubUserId);

    UserProfileView getProfileByLogin(String githubLogin);

    void saveProfile(UUID userId, UserProfile userProfile);

    Optional<User> getUserByGithubId(Long githubId);

    void createUser(User user);

    UserPayoutInformation getPayoutInformationById(UUID id);

    void updateOnboardingWizardDisplayDate(UUID userId, Date date);

    void updateTermsAndConditionsAcceptanceDate(UUID userId, Date date);

    void acceptProjectLeaderInvitation(Long githubUserId, UUID projectId);

    void createApplicationOnProject(UUID userId, UUID projectId);

    UserPayoutInformation savePayoutInformationForUserId(UUID userId, UserPayoutInformation userPayoutInformation);

    Page<UserRewardView> findRewardsForUserId(UUID userId, int pageIndex, int pageSize, UserRewardView.SortBy sortBy,
                                              SortDirection sortDirection);

    UserRewardTotalAmountsView findRewardTotalAmountsForUserId(UUID userId);

    RewardView findRewardById(UUID rewardId);

    Page<RewardItemView> findRewardItemsPageById(UUID rewardId, int pageIndex, int pageSize);

    List<UserRewardView> findPendingInvoiceRewardsForRecipientId(Long githubUserId);

    List<Contributor> searchContributorsByLogin(Set<Long> reposIds, String login, int maxContributorCountToReturn);

    void saveProjectLead(UUID userId, UUID projectId);
}
