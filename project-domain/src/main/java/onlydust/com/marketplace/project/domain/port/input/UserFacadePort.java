package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.view.RewardDetailsView;
import onlydust.com.marketplace.project.domain.view.RewardItemView;
import onlydust.com.marketplace.project.domain.view.UserProfileView;

import java.io.InputStream;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface UserFacadePort {

    User getUserByGithubIdentity(GithubUserIdentity githubUserIdentity, boolean readOnly);

    UserProfileView getProfileById(UUID userId);

    UserProfileView updateProfile(UUID userId, UserProfile userProfile);

    void refreshActiveUserProfiles(ZonedDateTime since);

    void markUserAsOnboarded(UUID userId);

    void updateTermsAndConditionsAcceptanceDate(UUID userId);

    void acceptInvitationToLeadProject(Long githubUserId, UUID projectId);

    Application applyOnProject(@NonNull UUID userId,
                               @NonNull Long githubUserId,
                               @NonNull UUID projectId,
                               @NonNull GithubIssue.Id issueId,
                               @NonNull String motivation,
                               String problemSolvingApproach);

    Application updateApplication(@NonNull Application.Id applicationId,
                                  @NonNull UUID userId,
                                  @NonNull String motivation,
                                  String problemSolvingApproach);

    RewardDetailsView getRewardByIdForRecipientIdAndAdministratedBillingProfileIds(UUID rewardId, Long recipientId, List<UUID> companyAdminBillingProfileIds);

    Page<RewardItemView> getRewardItemsPageByIdForRecipientIdAndAdministratedBillingProfileIds(UUID rewardId, Long recipientId, int pageIndex,
                                                                                               int pageSize, List<UUID> companyAdminBillingProfileIds);

    void claimProjectForAuthenticatedUser(UUID projectId, User user);

    URL saveAvatarImage(InputStream imageInputStream);

    void updateGithubProfile(User authenticatedUser);

    void refreshUserRanksAndStats();

    void historizeUserRanks();
}
