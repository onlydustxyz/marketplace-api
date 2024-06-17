package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
import onlydust.com.marketplace.project.domain.gateway.DateProvider;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.port.input.ProjectObserverPort;
import onlydust.com.marketplace.project.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.project.domain.port.input.UserObserverPort;
import onlydust.com.marketplace.project.domain.port.output.*;
import onlydust.com.marketplace.project.domain.view.ProjectOrganizationView;
import onlydust.com.marketplace.project.domain.view.RewardDetailsView;
import onlydust.com.marketplace.project.domain.view.RewardItemView;
import onlydust.com.marketplace.project.domain.view.UserProfileView;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.*;

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
    private final GithubUserPermissionsService githubUserPermissionsService;
    private final GithubStoragePort githubStoragePort;
    private final GithubApiPort githubApiPort;
    private final GithubAuthenticationPort githubAuthenticationPort;

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
    public UserProfileView getProfileById(UUID userId) {
        return userStoragePort.getProfileById(userId);
    }

    @Override
    @Transactional
    public UserProfileView updateProfile(UUID userId, UserProfile userProfile) {
        userStoragePort.saveProfile(userId, userProfile);
        return userStoragePort.getProfileById(userId);
    }

    @Override
    @Transactional
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
    @Transactional
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
    @Transactional
    public void refreshUserRanksAndStats() {
        userStoragePort.refreshUserRanksAndStats();
    }

    @Override
    @Transactional
    public void historizeUserRanks() {
        userStoragePort.historizeUserRanks();
    }

    @Override
    @Transactional
    public void markUserAsOnboarded(UUID userId) {
        userStoragePort.updateOnboardingWizardDisplayDate(userId, dateProvider.now());
    }

    @Override
    @Transactional
    public void updateTermsAndConditionsAcceptanceDate(UUID userId) {
        userStoragePort.updateTermsAndConditionsAcceptanceDate(userId, dateProvider.now());
    }

    @Override
    @Transactional
    public void acceptInvitationToLeadProject(Long githubUserId, UUID projectId) {
        userStoragePort.acceptProjectLeaderInvitation(githubUserId, projectId);
    }

    @Override
    @Transactional
    public Application applyOnProject(@NonNull UUID userId,
                                      @NonNull Long githubUserId,
                                      @NonNull UUID projectId,
                                      @NonNull GithubIssue.Id issueId,
                                      @NonNull String motivation,
                                      String problemSolvingApproach) {
        if (!githubUserPermissionsService.isUserAuthorizedToApplyOnProject(githubUserId))
            throw forbidden("User is not authorized to apply on project");

        final var issue = githubStoragePort.findIssueById(issueId)
                .orElseThrow(() -> notFound("Issue %s not found".formatted(issueId)));

        if (issue.isAssigned())
            throw forbidden("Issue %s is already assigned".formatted(issueId));

        if (!userStoragePort.findApplications(userId, projectId, issueId).isEmpty())
            throw badRequest("User already applied to this issue");

        if (!projectStoragePort.getProjectRepoIds(projectId).contains(issue.repoId()))
            throw badRequest("Issue %s does not belong to project %s".formatted(issueId, projectId));

        final var personalAccessToken = githubAuthenticationPort.getGithubPersonalToken(githubUserId);

        final var comment = githubApiPort.createComment(personalAccessToken, issue, """
                I am applying to this issue via [OnlyDust platform](https://app.onlydust.com).
                """);

        final var application = Application.fromMarketplace(projectId, userId, issueId, comment.id(), motivation, problemSolvingApproach);

        userStoragePort.save(application);
        projectObserverPort.onUserApplied(projectId, userId, application.id());

        return application;
    }

    @Override
    public Application updateApplication(@NonNull Application.Id applicationId, @NonNull UUID userId, @NonNull String motivation,
                                         String problemSolvingApproach) {
        final var application = userStoragePort.find(applicationId)
                .orElseThrow(() -> notFound("Application %s not found".formatted(applicationId)));

        if (!application.applicantId().equals(userId))
            throw forbidden("User is not authorized to update this application");

        final var updated = application.update(motivation, problemSolvingApproach);
        userStoragePort.save(updated);

        return updated;
    }

    @Override
    @Transactional
    public RewardDetailsView getRewardByIdForRecipientIdAndAdministratedBillingProfileIds(UUID rewardId, Long recipientId,
                                                                                          List<UUID> companyAdminBillingProfileIds) {
        final RewardDetailsView reward = userStoragePort.findRewardById(rewardId);
        if (!reward.getTo().getGithubUserId().equals(recipientId) &&
            (isNull(reward.getBillingProfileId()) || !companyAdminBillingProfileIds.contains(reward.getBillingProfileId()))) {
            throw forbidden("Only recipient user or billing profile admin linked to this reward can read its details");
        }
        return reward;
    }

    public Page<RewardItemView> getRewardItemsPageByIdForRecipientIdAndAdministratedBillingProfileIds(UUID rewardId, Long recipientId, int pageIndex,
                                                                                                      int pageSize, List<UUID> companyAdminBillingProfileIds) {
        final Page<RewardItemView> page = userStoragePort.findRewardItemsPageById(rewardId, pageIndex, pageSize);
        if (page.getContent().stream().anyMatch(rewardItemView -> !rewardItemView.getRecipientId().equals(recipientId)) &&
            page.getContent().stream().anyMatch(rewardItemView -> isNull(rewardItemView.getBillingProfileId())
                                                                  || !companyAdminBillingProfileIds.contains(rewardItemView.getBillingProfileId()))) {
            throw forbidden("Only recipient user or billing profile admin linked to this reward can read its details");
        }
        return page;
    }

    @Override
    @Transactional
    public void claimProjectForAuthenticatedUser(UUID projectId, User user) {
        final var projectLeaders = projectStoragePort.getProjectLeadIds(projectId);
        final var projectInvitedLeaders = projectStoragePort.getProjectInvitedLeadIds(projectId);
        if (!projectLeaders.isEmpty() || !projectInvitedLeaders.isEmpty()) {
            throw forbidden("Project must have no project (pending) leads to be claimable");
        }

        final var projectOrganizations = projectStoragePort.getProjectOrganizations(projectId);
        if (projectOrganizations.isEmpty()) {
            throw forbidden("Project must have at least one organization to be claimable");
        }

        final boolean isNotClaimable = projectOrganizations.stream()
                .anyMatch(org -> cannotBeClaimedByUser(user, org));
        if (isNotClaimable) {
            throw forbidden("User must be github admin on every organizations not installed and at " +
                            "least member on every organization already installed linked to the " +
                            "project");

        }
        userStoragePort.saveProjectLead(user.getId(), projectId);
    }

    @Override
    @Transactional
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
