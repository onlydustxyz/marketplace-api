package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.gateway.DateProvider;
import onlydust.com.marketplace.api.domain.model.*;
import onlydust.com.marketplace.api.domain.port.input.ProjectObserverPort;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.api.domain.port.input.UserObserverPort;
import onlydust.com.marketplace.api.domain.port.output.BillingProfileStoragePort;
import onlydust.com.marketplace.api.domain.port.output.GithubSearchPort;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;

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
    private final BillingProfileStoragePort billingProfileStoragePort;

    @Override
    @Transactional
    public User getUserByGithubIdentity(GithubUserIdentity githubUserIdentity, boolean readOnly) {
        return userStoragePort
                .getUserByGithubId(githubUserIdentity.getGithubUserId())
                .map(user -> {
                    final var payoutInformationById = userStoragePort.getPayoutInformationById(user.getId());
                    user.setHasValidPayoutInfos(payoutInformationById.isValid());
                    user.setBillingProfileType(billingProfileStoragePort.getBillingProfileTypeForUser(user.getId()).orElse(BillingProfileType.INDIVIDUAL));
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
                            .billingProfileType(BillingProfileType.INDIVIDUAL)
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
    public UserPayoutInformation getPayoutInformationForUserId(UUID userId) {
        return userStoragePort.getPayoutInformationById(userId);
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
    public UserPayoutInformation updatePayoutInformation(UUID userId, UserPayoutInformation userPayoutInformation) {
        return userStoragePort.savePayoutInformationForUserId(userId, userPayoutInformation);
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
    public UserRewardTotalAmountsView getRewardTotalAmountsForUserId(UUID userId) {
        return userStoragePort.findRewardTotalAmountsForUserId(userId);
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
    public CompanyBillingProfile getCompanyBillingProfile(UUID userId) {
        return billingProfileStoragePort.findCompanyProfileForUser(userId)
                .orElseGet(() -> {
                    CompanyBillingProfile newCompanyBillingProfile = CompanyBillingProfile.init();
                    billingProfileStoragePort.saveCompanyProfileForUser(userId, newCompanyBillingProfile);
                    billingProfileStoragePort.saveProfileTypeForUser(BillingProfileType.COMPANY, userId);
                    return newCompanyBillingProfile;
                });
    }

    @Override
    @Transactional
    public IndividualBillingProfile getIndividualBillingProfile(UUID userId) {
        return billingProfileStoragePort.findIndividualBillingProfile(userId)
                .orElseGet(() -> {
                    IndividualBillingProfile individualBillingProfile = IndividualBillingProfile.init();
                    billingProfileStoragePort.saveIndividualProfileForUser(userId, individualBillingProfile);
                    billingProfileStoragePort.saveProfileTypeForUser(BillingProfileType.INDIVIDUAL, userId);
                    return individualBillingProfile;
                });
    }
}
