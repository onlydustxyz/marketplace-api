package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.PayoutPreferenceFacadePort;
import onlydust.com.marketplace.api.contract.MeApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.GithubMapper;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.GithubRepoMapper;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectMapper;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.RewardMapper;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.notification.Notification;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.port.input.*;
import onlydust.com.marketplace.project.domain.view.ContributionView;
import onlydust.com.marketplace.project.domain.view.RewardDetailsView;
import onlydust.com.marketplace.project.domain.view.RewardItemView;
import onlydust.com.marketplace.user.domain.model.NotificationRecipient;
import onlydust.com.marketplace.user.domain.model.NotificationSettings;
import onlydust.com.marketplace.user.domain.model.NotificationStatusUpdateRequest;
import onlydust.com.marketplace.user.domain.port.input.NotificationSettingsPort;
import onlydust.com.marketplace.user.domain.service.NotificationService;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.UserMapper.*;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageSize;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Tags(@Tag(name = "Me"))
@AllArgsConstructor
@Profile("api")
public class MeRestApi implements MeApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final UserFacadePort userFacadePort;
    private final ApplicationFacadePort applicationFacadePort;
    private final ContributorFacadePort contributorFacadePort;
    private final GithubOrganizationFacadePort githubOrganizationFacadePort;
    private final BillingProfileFacadePort billingProfileFacadePort;
    private final PayoutPreferenceFacadePort payoutPreferenceFacadePort;
    private final HackathonFacadePort hackathonFacadePort;
    private final CommitteeFacadePort committeeFacadePort;
    private final BannerFacadePort bannerFacadePort;
    private final NotificationSettingsPort notificationSettingsPort;
    private final NotificationService notificationService;

    @Override
    public ResponseEntity<Void> patchMe(PatchMeContract patchMeContract) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        if (Boolean.TRUE.equals(patchMeContract.getHasAcceptedTermsAndConditions())) {
            userFacadePort.updateTermsAndConditionsAcceptanceDate(authenticatedUser.id());
        }
        if (Boolean.TRUE.equals(patchMeContract.getHasCompletedOnboarding())) {
            userFacadePort.markUserAsOnboarded(authenticatedUser.id());
        }
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> acceptInvitationToLeadProject(UUID projectId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        userFacadePort.acceptInvitationToLeadProject(authenticatedUser.githubUserId(), projectId);
        return noContent().build();
    }

    @Override
    public ResponseEntity<ProjectApplicationCreateResponse> applyOnProject(ProjectApplicationCreateRequest applicationRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var application = applicationFacadePort.applyOnProject(authenticatedUser.githubUserId(),
                applicationRequest.getProjectId(),
                GithubIssue.Id.of(applicationRequest.getIssueId()),
                applicationRequest.getMotivation(),
                applicationRequest.getProblemSolvingApproach());
        return ok(new ProjectApplicationCreateResponse().id(application.id().value()));
    }

    @Override
    public ResponseEntity<Void> updateProjectApplication(UUID applicationId, ProjectApplicationUpdateRequest applicationUpdateRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        applicationFacadePort.updateApplication(Application.Id.of(applicationId),
                authenticatedUser.githubUserId(),
                applicationUpdateRequest.getMotivation(),
                applicationUpdateRequest.getProblemSolvingApproach());
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> updateMyProfile(UserProfileUpdateRequest userProfileRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        userFacadePort.updateProfile(authenticatedUser.id(),
                userProfileRequest.getAvatarUrl(),
                userProfileRequest.getLocation(),
                userProfileRequest.getBio(),
                userProfileRequest.getWebsite(),
                userProfileRequest.getContactEmail(),
                contactToDomain(userProfileRequest.getContacts()),
                allocatedTimeToDomain(userProfileRequest.getAllocatedTimeToContribute()),
                userProfileRequest.getIsLookingForAJob(),
                userProfileRequest.getFirstName(),
                userProfileRequest.getLastName(),
                isNull(userProfileRequest.getJoiningReason()) ? null : switch (userProfileRequest.getJoiningReason()) {
                    case MAINTAINER -> UserProfile.JoiningReason.MAINTAINER;
                    case CONTRIBUTOR -> UserProfile.JoiningReason.CONTRIBUTOR;
                },
                isNull(userProfileRequest.getJoiningGoal()) ? null : switch (userProfileRequest.getJoiningGoal()) {
                    case CHALLENGE -> UserProfile.JoiningGoal.CHALLENGE;
                    case EARN -> UserProfile.JoiningGoal.EARN;
                    case LEARN -> UserProfile.JoiningGoal.LEARN;
                    case NOTORIETY -> UserProfile.JoiningGoal.NOTORIETY;
                },
                userProfileRequest.getPreferredLanguages(),
                userProfileRequest.getPreferredCategories()
        );

        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> replaceMyProfile(UserProfileUpdateRequest userProfileUpdateRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        userFacadePort.replaceProfile(authenticatedUser.id(),
                userProfileRequestToDomain(userProfileUpdateRequest));
        return noContent().build();
    }

    @Override
    public ResponseEntity<ProjectListResponse> getMyContributedProjects(List<Long> repositories) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        final var filters = ContributionView.Filters.builder()
                .repos(Optional.ofNullable(repositories).orElse(List.of()))
                .build();

        final var projects = contributorFacadePort.contributedProjects(authenticatedUser.githubUserId(), filters);

        return ResponseEntity.ok(new ProjectListResponse()
                .projects(projects.stream().map(ProjectMapper::mapShortProjectResponse).toList())
        );
    }


    @Override
    public ResponseEntity<ProjectListResponse> getMyRewardingProjects() {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        final var projects = contributorFacadePort.rewardingProjects(authenticatedUser.githubUserId());

        return ResponseEntity.ok(new ProjectListResponse()
                .projects(projects.stream().map(ProjectMapper::mapShortProjectResponse).toList())
        );
    }

    @Override
    public ResponseEntity<Void> logoutMe() {
        authenticatedAppUserService.logout();
        return noContent().build();
    }

    @Override
    public ResponseEntity<ContributedReposResponse> getMyContributedRepos(List<UUID> projects) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        final var filters = ContributionView.Filters.builder()
                .projects(Optional.ofNullable(projects).orElse(List.of()))
                .build();

        final var contributedRepos = contributorFacadePort.contributedRepos(authenticatedUser.githubUserId(),
                filters);

        return ResponseEntity.ok(new ContributedReposResponse()
                .repos(contributedRepos.stream().map(GithubRepoMapper::mapRepoToShortResponse).toList())
        );
    }


    @Override
    public ResponseEntity<RewardDetailsResponse> getMyReward(UUID rewardId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final RewardDetailsView rewardDetailsView = userFacadePort.getRewardByIdForRecipientIdAndAdministratedBillingProfileIds(rewardId,
                authenticatedUser.githubUserId(), authenticatedUser.administratedBillingProfiles());
        return ResponseEntity.ok(RewardMapper.myRewardDetailsToResponse(rewardDetailsView, authenticatedUser));
    }

    @Override
    public ResponseEntity<RewardItemsPageResponse> getMyRewardItemsPage(UUID rewardId, Integer pageIndex,
                                                                        Integer pageSize) {
        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final int sanitizedPageIndex = PaginationHelper.sanitizePageIndex(pageIndex);
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final Page<RewardItemView> page = userFacadePort.getRewardItemsPageByIdForRecipientIdAndAdministratedBillingProfileIds(rewardId,
                authenticatedUser.githubUserId(), sanitizedPageIndex, sanitizedPageSize,
                authenticatedUser.administratedBillingProfiles());
        final RewardItemsPageResponse rewardItemsPageResponse = RewardMapper.pageToResponse(sanitizedPageIndex, page);
        return rewardItemsPageResponse.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(rewardItemsPageResponse) :
                ResponseEntity.ok(rewardItemsPageResponse);
    }

    @Override
    public ResponseEntity<CurrencyListResponse> getMyRewardCurrencies() {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var currencies = contributorFacadePort.getRewardCurrencies(
                authenticatedUser.githubUserId(),
                authenticatedUser.administratedBillingProfiles()
        );
        return ResponseEntity.ok(new CurrencyListResponse().currencies(currencies.stream()
                .map(RewardMapper::mapCurrency)
                .sorted(comparing(ShortCurrencyResponse::getCode))
                .toList()));
    }

    @Override
    public ResponseEntity<List<GithubOrganizationResponse>> searchGithubUserOrganizations() {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        final List<GithubAccount> githubAccounts =
                githubOrganizationFacadePort.getOrganizationsForAuthenticatedUser(authenticatedUser);
        return githubAccounts.isEmpty() ? ResponseEntity.notFound().build() :
                ResponseEntity.ok(githubAccounts.stream().map(GithubMapper::mapToGithubOrganizationResponse).toList());
    }

    @Override
    public ResponseEntity<Void> claimProject(UUID projectId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        userFacadePort.claimProjectForAuthenticatedUser(
                projectId, authenticatedUser
        );
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> closeBanner(UUID bannerId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        bannerFacadePort.closeBanner(Banner.Id.of(bannerId), authenticatedUser.id());
        return noContent().build();
    }

    @Override
    public ResponseEntity<UploadImageResponse> uploadAvatar(Resource image) {
        InputStream imageInputStream;
        try {
            imageInputStream = image.getInputStream();
        } catch (IOException e) {
            throw OnlyDustException.badRequest("Error while reading image data", e);
        }

        final URL imageUrl = userFacadePort.saveAvatarImage(imageInputStream);
        final UploadImageResponse response = new UploadImageResponse();
        response.url(imageUrl.toString());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> updateMyGithubProfileData() {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        userFacadePort.updateGithubProfile(authenticatedUser.githubUserId());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> setMyPayoutPreferenceForProject(PayoutPreferenceRequest payoutPreferenceRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        payoutPreferenceFacadePort.setPayoutPreference(ProjectId.of(payoutPreferenceRequest.getProjectId()),
                BillingProfile.Id.of(payoutPreferenceRequest.getBillingProfileId()), UserId.of(authenticatedUser.id()));
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> acceptOrRejectCoworkerInvitation(UUID billingProfileId, BillingProfileCoworkerInvitationUpdateRequest invitationUpdateRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (Boolean.TRUE.equals(invitationUpdateRequest.getAccepted())) {
            billingProfileFacadePort.acceptCoworkerInvitation(
                    BillingProfile.Id.of(billingProfileId),
                    GithubUserId.of(authenticatedUser.githubUserId()));
        } else {
            billingProfileFacadePort.rejectCoworkerInvitation(
                    BillingProfile.Id.of(billingProfileId),
                    GithubUserId.of(authenticatedUser.githubUserId()));
        }
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> registerToHackathon(UUID hackathonId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        hackathonFacadePort.registerToHackathon(authenticatedUser.id(), Hackathon.Id.of(hackathonId));
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> voteForCommitteeAssignment(UUID committeeId, UUID projectId,
                                                           VoteForCommitteeAssignmentRequest voteForCommitteeAssignmentRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        committeeFacadePort.vote(authenticatedUser.id(), Committee.Id.of(committeeId), projectId,
                voteForCommitteeAssignmentRequest.getVotes().stream()
                        .collect(Collectors.toMap(v -> JuryCriteria.Id.of(v.getCriteriaId()), v -> v.getVote())));

        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> patchMyNotificationSettingsForProject(UUID projectId,
                                                                      NotificationSettingsForProjectPatchRequest request) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        notificationSettingsPort.patchNotificationSettingsForProject(
                NotificationRecipient.Id.of(authenticatedUser.id()),
                new NotificationSettings.Project(onlydust.com.marketplace.user.domain.model.ProjectId.of(projectId),
                        Optional.ofNullable(request.getOnGoodFirstIssueAdded())));
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> patchMyNotificationsStatus(NotificationsPatchRequest notificationsPatchRequest) {
        final AuthenticatedUser authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        notificationService.updateInAppNotificationsStatus(authenticatedUser.id(),
                notificationsPatchRequest.getNotifications().stream()
                        .map(notificationPatchRequest -> new NotificationStatusUpdateRequest(Notification.Id.of(notificationPatchRequest.getId()),
                                switch (notificationPatchRequest.getStatus()) {
                                    case READ -> NotificationStatusUpdateRequest.NotificationStatus.READ;
                                    case UNREAD -> NotificationStatusUpdateRequest.NotificationStatus.UNREAD;
                                })).toList());
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> markAllInAppNotificationsAsRead() {
        final AuthenticatedUser authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        notificationService.markAllInAppUnreadAsRead(authenticatedUser.id());
        return noContent().build();
    }
}
