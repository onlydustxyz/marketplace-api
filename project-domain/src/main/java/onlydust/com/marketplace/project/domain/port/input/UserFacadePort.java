package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.project.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.model.UserProfile;
import onlydust.com.marketplace.project.domain.view.*;

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

    void refreshActiveUserProfiles(ZonedDateTime since);

    void markUserAsOnboarded(UUID userId);

    void updateTermsAndConditionsAcceptanceDate(UUID userId);

    void acceptInvitationToLeadProject(Long githubUserId, UUID projectId);

    void applyOnProject(UUID id, UUID projectId);

    UserRewardsPageView getRewardsForUserId(UUID userId, UserRewardView.Filters filters,
                                            int pageIndex, int sanitizedPageSize,
                                            UserRewardView.SortBy sortBy, SortDirection sortDirection);

    RewardDetailsView getRewardByIdForRecipientId(UUID rewardId, Long recipientId);

    Page<RewardItemView> getRewardItemsPageByIdForRecipientId(UUID rewardId, Long recipientId, int pageIndex,
                                                              int pageSize);

    List<UserRewardView> getPendingInvoiceRewardsForRecipientId(Long githubUserId);

    void claimProjectForAuthenticatedUser(UUID projectId, User user);

    URL saveAvatarImage(InputStream imageInputStream);

    void updateGithubProfile(User authenticatedUser);

}
