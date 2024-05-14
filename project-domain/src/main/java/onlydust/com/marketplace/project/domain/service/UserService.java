package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
import onlydust.com.marketplace.project.domain.gateway.DateProvider;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.port.input.ProjectObserverPort;
import onlydust.com.marketplace.project.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.project.domain.port.input.UserObserverPort;
import onlydust.com.marketplace.project.domain.port.output.GithubSearchPort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.project.domain.view.*;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
@Slf4j
public class UserService implements UserFacadePort {

    private final UserObserverPort userObserverPort;
    private final UserStoragePort userStoragePort;
    private final DateProvider dateProvider;
    private final ProjectStoragePort projectStoragePort;
    private final GithubSearchPort githubSearchPort;
    private final ImageStoragePort imageStoragePort;
    private final ProjectObserverPort projectObserverPort;

    @Override
    @Transactional
    public User getUserByGithubIdentity(GithubUserIdentity githubUserIdentity, boolean readOnly) {
        return userStoragePort
                .getUserByGithubId(githubUserIdentity.getGithubUserId())
                .map(user -> {
                    if (!readOnly)
                        userStoragePort.updateUserLastSeenAt(user.getId(), dateProvider.now());

                    return user;
                })
                .orElseGet(() -> {
                    if (readOnly) {
                        throw notFound("User %d not found".formatted(githubUserIdentity.getGithubUserId()));
                    }

                    final var user = userStoragePort.createUser(User.builder()
                            .id(UUID.randomUUID())
                            .roles(List.of(AuthenticatedUser.Role.USER))
                            .githubUserId(githubUserIdentity.getGithubUserId())
                            .githubAvatarUrl(githubUserIdentity.getGithubAvatarUrl())
                            .githubLogin(githubUserIdentity.getGithubLogin())
                            .githubEmail(githubUserIdentity.getEmail())
                            .build());

                    userObserverPort.onUserSignedUp(user);
                    return user;
                });
    }

    @Override
    public User getUserById(UUID userId) {
        return userStoragePort.getUserById(userId)
                .orElseThrow(() -> notFound("User %s not found".formatted(userId)));
    }

    @Override
    public UserProfileView getProfileById(UUID userId) {
        return userStoragePort.getProfileById(userId);
    }

    @Override
    public UserProfileView updateProfile(UUID userId, UserProfile userProfile) {
        userStoragePort.saveProfile(userId, userProfile);
        return userStoragePort.getProfileById(userId);
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
                        .build()).orElseThrow(() -> notFound(String.format("Github user %s to update was not found",
                        user.getGithubUserId()))));
    }

    @Override
    public void refreshUserRanks() {
        userStoragePort.refreshUserRanks();
    }

    @Override
    public void historizeUserRanks() {
        userStoragePort.historizeUserRanks();
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
        userStoragePort.acceptProjectLeaderInvitation(githubUserId, projectId);
    }

    @Override
    public void applyOnProject(UUID userId, UUID projectId) {
        final var applicationId = userStoragePort.createApplicationOnProject(userId, projectId);
        projectObserverPort.onUserApplied(projectId, userId, applicationId);
    }

    @Override
    public UserRewardsPageView getRewardsForUserId(Long githubUserId, UserRewardView.Filters filters,
                                                   int pageIndex, int pageSize,
                                                   Reward.SortBy sortBy, SortDirection sortDirection) {
        return userStoragePort.findRewardsForUserId(githubUserId, filters, pageIndex, pageSize, sortBy, sortDirection);
    }

    @Override
    public RewardDetailsView getRewardByIdForRecipientIdAndAdministratedBillingProfileIds(UUID rewardId, Long recipientId,
                                                                                          List<UUID> companyAdminBillingProfileIds) {
        final RewardDetailsView reward = userStoragePort.findRewardById(rewardId);
        if (!reward.getTo().getGithubUserId().equals(recipientId) &&
                (isNull(reward.getBillingProfileId()) || !companyAdminBillingProfileIds.contains(reward.getBillingProfileId()))) {
            throw OnlyDustException.forbidden("Only recipient user or billing profile admin linked to this reward can read its details");
        }
        return reward;
    }

    public Page<RewardItemView> getRewardItemsPageByIdForRecipientIdAndAdministratedBillingProfileIds(UUID rewardId, Long recipientId, int pageIndex,
                                                                                                      int pageSize, List<UUID> companyAdminBillingProfileIds) {
        final Page<RewardItemView> page = userStoragePort.findRewardItemsPageById(rewardId, pageIndex, pageSize);
        if (page.getContent().stream().anyMatch(rewardItemView -> !rewardItemView.getRecipientId().equals(recipientId)) &&
                page.getContent().stream().anyMatch(rewardItemView -> isNull(rewardItemView.getBillingProfileId())
                        || !companyAdminBillingProfileIds.contains(rewardItemView.getBillingProfileId()))) {
            throw OnlyDustException.forbidden("Only recipient user or billing profile admin linked to this reward can read its details");
        }
        return page;
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
}
