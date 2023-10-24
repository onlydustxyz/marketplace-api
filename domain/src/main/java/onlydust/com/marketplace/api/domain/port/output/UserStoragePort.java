package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import onlydust.com.marketplace.api.domain.model.UserProfile;
import onlydust.com.marketplace.api.domain.view.UserProfileView;
import onlydust.com.marketplace.api.domain.view.UserRewardTotalAmountsView;
import onlydust.com.marketplace.api.domain.view.UserRewardView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public interface UserStoragePort {
    UserProfileView getProfileById(UUID userId);

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
}
