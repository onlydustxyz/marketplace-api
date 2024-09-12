package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Contact;
import onlydust.com.marketplace.project.domain.model.UserAllocatedTimeToContribute;
import onlydust.com.marketplace.project.domain.model.UserProfile;
import onlydust.com.marketplace.project.domain.view.RewardDetailsView;
import onlydust.com.marketplace.project.domain.view.RewardItemView;

import java.io.InputStream;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface UserFacadePort {

    void updateProfile(final @NonNull UserId userId,
                       final String avatarUrl,
                       final String location,
                       final String bio,
                       final String website,
                       final String contactEmail,
                       final List<Contact> contacts,
                       final UserAllocatedTimeToContribute allocatedTimeToContribute,
                       final Boolean isLookingForAJob,
                       final String firstName,
                       final String lastName,
                       final UserProfile.JoiningReason joiningReason,
                       final UserProfile.JoiningGoal joiningGoal,
                       final List<UUID> preferredLanguageIds,
                       final List<UUID> preferredCategoryIds);

    void replaceProfile(UserId userId, UserProfile userProfile);

    void refreshActiveUserProfiles(ZonedDateTime since);

    void markUserAsOnboarded(UserId userId);

    void updateTermsAndConditionsAcceptanceDate(UserId userId);

    void acceptInvitationToLeadProject(Long githubUserId, ProjectId projectId);

    RewardDetailsView getRewardByIdForRecipientIdAndAdministratedBillingProfileIds(UUID rewardId, Long recipientId, List<UUID> companyAdminBillingProfileIds);

    Page<RewardItemView> getRewardItemsPageByIdForRecipientIdAndAdministratedBillingProfileIds(UUID rewardId, Long recipientId, int pageIndex,
                                                                                               int pageSize, List<UUID> companyAdminBillingProfileIds);

    void claimProjectForAuthenticatedUser(ProjectId projectId, AuthenticatedUser user);

    URL saveAvatarImage(InputStream imageInputStream);

    void updateGithubProfile(Long githubUserId);

    void refreshUserRanksAndStats();

    void historizeUserRanks();
}
