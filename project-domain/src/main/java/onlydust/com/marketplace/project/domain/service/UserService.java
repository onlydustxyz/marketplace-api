package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
import onlydust.com.marketplace.project.domain.gateway.DateProvider;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.port.input.AccountingUserObserverPort;
import onlydust.com.marketplace.project.domain.port.input.ProjectObserverPort;
import onlydust.com.marketplace.project.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.project.domain.port.input.UserObserverPort;
import onlydust.com.marketplace.project.domain.port.output.GithubSearchPort;
import onlydust.com.marketplace.project.domain.port.output.OldBillingProfileStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.project.domain.view.*;

import javax.transaction.Transactional;
import java.io.InputStream;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Slf4j
public class UserService implements UserFacadePort {

    private final ProjectObserverPort projectObserverPort;
    private final UserObserverPort userObserverPort;
    private final UserStoragePort userStoragePort;
    private final DateProvider dateProvider;
    private final ProjectStoragePort projectStoragePort;
    private final GithubSearchPort githubSearchPort;
    private final ImageStoragePort imageStoragePort;
    private final OldBillingProfileStoragePort oldBillingProfileStoragePort;
    private final AccountingUserObserverPort accountingUserObserverPort;

    @Override
    @Transactional
    public User getUserByGithubIdentity(GithubUserIdentity githubUserIdentity, boolean readOnly) {
        return userStoragePort
                .getUserByGithubId(githubUserIdentity.getGithubUserId())
                .map(user -> {
                    final var payoutInformationById = userStoragePort.getPayoutSettingsById(user.getId());
                    user.setHasValidPayoutInfos(payoutInformationById.isValid());
                    user.setOldBillingProfileType(oldBillingProfileStoragePort.getBillingProfileTypeForUser(user.getId()).orElse(OldBillingProfileType.INDIVIDUAL));
                    if (payoutInformationById.hasPendingPayments()) {
                        user.setHasValidBillingProfile(oldBillingProfileStoragePort.hasValidBillingProfileForUserAndType(user.getId(),
                                user.getOldBillingProfileType()));
                    }
                    if (!readOnly)
                        userStoragePort.updateUserLastSeenAt(user.getId(), dateProvider.now());

                    return user;
                })
                .orElseGet(() -> {
                    if (readOnly) {
                        throw OnlyDustException.notFound("User %d not found".formatted(githubUserIdentity.getGithubUserId()));
                    }
                    var user = User.builder()
                            .id(UUID.randomUUID())
                            .roles(List.of(UserRole.USER))
                            .githubUserId(githubUserIdentity.getGithubUserId())
                            .githubAvatarUrl(githubUserIdentity.getGithubAvatarUrl())
                            .githubLogin(githubUserIdentity.getGithubLogin())
                            .githubEmail(githubUserIdentity.getEmail())
                            .oldBillingProfileType(OldBillingProfileType.INDIVIDUAL)
                            .build();
                    final User createdUser = userStoragePort.createUser(user);
                    user = user.toBuilder()
                            .createdAt(createdUser.getCreatedAt())
                            .build();
                    userObserverPort.onUserSignedUp(user);
                    return user;
                });
    }

    @Override
    public UserProfileView getProfileById(UUID userId) {
        return userStoragePort.getProfileById(userId);
    }

    @Override
    public UserProfileView getProfileById(Long githubUserId) {
        return userStoragePort.getProfileById(githubUserId);
    }

    @Override
    public UserProfileView getProfileByLogin(String githubLogin) {
        return userStoragePort.getProfileByLogin(githubLogin);
    }

    @Override
    public UserProfileView updateProfile(UUID userId, UserProfile userProfile) {
        userStoragePort.saveProfile(userId, userProfile);
        return userStoragePort.getProfileById(userId);
    }

    @Override
    public UserPayoutSettings getPayoutSettingsForUserId(UUID userId) {
        return userStoragePort.getPayoutSettingsById(userId);
    }

    @Override
    public void refreshActiveUserProfiles(ZonedDateTime since) {
        final var activeUsers = userStoragePort.getUsersLastSeenSince(since);

        final var userProfiles = activeUsers.stream()
                .map(User::getGithubUserId)
                .map(githubSearchPort::getUserProfile)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(githubUserProfile -> User.builder()
                        .githubUserId(githubUserProfile.getGithubUserId())
                        .githubLogin(githubUserProfile.getGithubLogin())
                        .githubAvatarUrl(githubUserProfile.getGithubAvatarUrl())
                        .githubEmail(githubUserProfile.getEmail())
                        .build())
                .toList();

        if (userProfiles.size() < activeUsers.size()) {
            LOGGER.warn("Only {} user profiles found for {} active users", userProfiles.size(), activeUsers.size());
        }

        userStoragePort.saveUsers(userProfiles);
    }

    @Override
    public void updateGithubProfile(User user) {
        userStoragePort.saveUser(githubSearchPort.getUserProfile(user.getGithubUserId())
                .map(githubUserProfile -> User.builder()
                        .githubUserId(githubUserProfile.getGithubUserId())
                        .githubLogin(githubUserProfile.getGithubLogin())
                        .githubAvatarUrl(githubUserProfile.getGithubAvatarUrl())
                        .githubEmail(githubUserProfile.getEmail())
                        .build()).orElseThrow(() -> OnlyDustException.notFound(String.format("Github user %s to update was not found",
                        user.getGithubUserId()))));
    }

    @Override
    public List<OldBillingProfile> oldGetBillingProfiles(UUID id, Long githubUserId) {
        return oldBillingProfileStoragePort.all(id, githubUserId);
    }

    @Override
    public UserPayoutSettings updatePayoutSettings(UUID userId, UserPayoutSettings userPayoutSettings) {
        return userStoragePort.savePayoutSettingsForUserId(userId, userPayoutSettings);
    }

    @Override
    public void markUserAsOnboarded(UUID userId) {
        userStoragePort.updateOnboardingWizardDisplayDate(userId, dateProvider.now());
    }

    @Override
    public void updateTermsAndConditionsAcceptanceDate(UUID userId) {
        userStoragePort.updateTermsAndConditionsAcceptanceDate(userId, dateProvider.now());
    }

    @Override
    public void acceptInvitationToLeadProject(Long githubUserId, UUID projectId) {
        final var leaderId = userStoragePort.acceptProjectLeaderInvitation(githubUserId, projectId);
        projectObserverPort.onLeaderAssigned(projectId, leaderId);
    }

    @Override
    public void applyOnProject(UUID userId, UUID projectId) {
        final var applicationId = userStoragePort.createApplicationOnProject(userId, projectId);
        projectObserverPort.onUserApplied(projectId, userId, applicationId);
    }

    @Override
    public UserRewardsPageView getRewardsForUserId(UUID userId, UserRewardView.Filters filters,
                                                   int pageIndex, int pageSize,
                                                   UserRewardView.SortBy sortBy, SortDirection sortDirection) {
        return userStoragePort.findRewardsForUserId(userId, filters, pageIndex, pageSize, sortBy, sortDirection);
    }

    @Override
    public RewardView getRewardByIdForRecipientId(UUID rewardId, Long recipientId) {
        final RewardView reward = userStoragePort.findRewardById(rewardId);
        if (!reward.getTo().getGithubUserId().equals(recipientId)) {
            throw OnlyDustException.forbidden("Only recipient user can read it's own reward");
        }
        return reward;
    }

    @Override
    public Page<RewardItemView> getRewardItemsPageByIdForRecipientId(UUID rewardId, Long recipientId, int pageIndex,
                                                                     int pageSize) {
        final Page<RewardItemView> page = userStoragePort.findRewardItemsPageById(rewardId, pageIndex, pageSize);
        if (page.getContent().stream().anyMatch(rewardItemView -> !rewardItemView.getRecipientId().equals(recipientId))) {
            throw OnlyDustException.forbidden("Only recipient user can read it's own reward");
        }
        return page;
    }

    @Override
    public List<UserRewardView> getPendingInvoiceRewardsForRecipientId(Long githubUserId) {
        return userStoragePort.findPendingInvoiceRewardsForRecipientId(githubUserId);
    }

    @Override
    public void claimProjectForAuthenticatedUser(UUID projectId, User user) {
        final ProjectDetailsView projectDetails = projectStoragePort.getById(projectId, user);
        if (!projectDetails.getLeaders().isEmpty() || !projectDetails.getInvitedLeaders().isEmpty()) {
            throw OnlyDustException.forbidden("Project must have no project (pending) leads to be claimable");
        }
        if (projectDetails.getOrganizations().isEmpty()) {
            throw OnlyDustException.forbidden("Project must have at least one organization to be claimable");
        }

        final boolean isNotClaimable = projectDetails.getOrganizations().stream()
                .anyMatch(org -> cannotBeClaimedByUser(user, org));
        if (isNotClaimable) {
            throw OnlyDustException.forbidden("User must be github admin on every organizations not installed and at " +
                                              "least member on every organization already installed linked to the " +
                                              "project");

        }
        userStoragePort.saveProjectLead(user.getId(), projectId);
        projectObserverPort.onLeaderAssigned(projectId, user.getId());
    }

    @Override
    public URL saveAvatarImage(InputStream imageInputStream) {
        return this.imageStoragePort.storeImage(imageInputStream);
    }

    private boolean cannotBeClaimedByUser(User user, ProjectOrganizationView org) {
        if (org.getId().equals(user.getGithubUserId())) {
            return false;
        }
        final GithubMembership githubMembership =
                githubSearchPort.getGithubUserMembershipForOrganization(user.getGithubUserId(),
                        user.getGithubLogin(), org.getLogin());
        if (org.getIsInstalled() && (githubMembership.equals(GithubMembership.MEMBER) || githubMembership.equals(GithubMembership.ADMIN))) {
            return false;
        }
        if (!org.getIsInstalled() && githubMembership.equals(GithubMembership.ADMIN)) {
            return false;
        }
        return true;
    }

    @Override
    @Transactional
    public OldCompanyBillingProfile getCompanyBillingProfile(UUID userId) {
        return oldBillingProfileStoragePort.findCompanyProfileForUser(userId)
                .orElseGet(() -> {
                    OldCompanyBillingProfile newCompanyBillingProfile = OldCompanyBillingProfile.initForUser(userId);
                    oldBillingProfileStoragePort.saveCompanyProfileForUser(newCompanyBillingProfile);
                    oldBillingProfileStoragePort.saveProfileTypeForUser(OldBillingProfileType.COMPANY, userId);
                    return newCompanyBillingProfile;
                });
    }

    @Override
    @Transactional
    public OldIndividualBillingProfile getIndividualBillingProfile(UUID userId) {
        return oldBillingProfileStoragePort.findIndividualBillingProfile(userId)
                .orElseGet(() -> {
                    OldIndividualBillingProfile individualBillingProfile = OldIndividualBillingProfile.initForUser(userId);
                    oldBillingProfileStoragePort.saveIndividualProfileForUser(individualBillingProfile);
                    oldBillingProfileStoragePort.saveProfileTypeForUser(OldBillingProfileType.INDIVIDUAL, userId);
                    return individualBillingProfile;
                });
    }

    @Override
    @Transactional
    public void updateBillingProfileType(UUID userId, OldBillingProfileType oldBillingProfileType) {
        oldBillingProfileStoragePort.updateBillingProfileType(userId, oldBillingProfileType);
        if (oldBillingProfileType.equals(OldBillingProfileType.COMPANY)) {
            accountingUserObserverPort.onBillingProfileSelected(userId, getCompanyBillingProfile(userId));
        } else {
            accountingUserObserverPort.onBillingProfileSelected(userId, getIndividualBillingProfile(userId));
        }
    }
}
