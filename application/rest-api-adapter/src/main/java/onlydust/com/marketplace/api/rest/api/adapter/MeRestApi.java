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
import onlydust.com.marketplace.accounting.domain.view.PayoutPreferenceView;
import onlydust.com.marketplace.api.contract.MeApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.port.input.*;
import onlydust.com.marketplace.project.domain.view.ContributionView;
import onlydust.com.marketplace.project.domain.view.RewardDetailsView;
import onlydust.com.marketplace.project.domain.view.RewardItemView;
import onlydust.com.marketplace.project.domain.view.UserProfileView;
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
public class MeRestApi implements MeApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final UserFacadePort userFacadePort;
    private final ContributorFacadePort contributorFacadePort;
    private final GithubOrganizationFacadePort githubOrganizationFacadePort;
    private final BillingProfileFacadePort billingProfileFacadePort;
    private final PayoutPreferenceFacadePort payoutPreferenceFacadePort;
    private final HackathonFacadePort hackathonFacadePort;
    private final CommitteeFacadePort committeeFacadePort;
    private final GithubUserPermissionsFacadePort githubUserPermissionsFacadePort;

    @Override
    public ResponseEntity<GetMeResponse> getMe() {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final GetMeResponse getMeResponse = userToGetMeResponse(authenticatedUser,
                githubUserPermissionsFacadePort.isUserAuthorizedToApplyOnProject(authenticatedUser.getGithubUserId()));
        return ResponseEntity.ok(getMeResponse);
    }

    @Override
    public ResponseEntity<Void> patchMe(PatchMeContract patchMeContract) {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        if (Boolean.TRUE.equals(patchMeContract.getHasSeenOnboardingWizard())) {
            userFacadePort.markUserAsOnboarded(authenticatedUser.getId());
        }
        if (Boolean.TRUE.equals(patchMeContract.getHasAcceptedTermsAndConditions())) {
            userFacadePort.updateTermsAndConditionsAcceptanceDate(authenticatedUser.getId());
        }
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> acceptInvitationToLeadProject(UUID projectId) {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        userFacadePort.acceptInvitationToLeadProject(authenticatedUser.getGithubUserId(), projectId);
        return noContent().build();
    }

    @Override
    public ResponseEntity<ApplicationResponse> applyOnProject(ApplicationRequest applicationRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var application = userFacadePort.applyOnProject(authenticatedUser.getId(),
                authenticatedUser.getGithubUserId(),
                applicationRequest.getProjectId(),
                GithubIssue.Id.of(applicationRequest.getIssueId()),
                applicationRequest.getMotivation(),
                applicationRequest.getProblemSolvingApproach());
        return ok(new ApplicationResponse().id(application.id().value()));
    }

    @Override
    public ResponseEntity<Void> updateApplication(UUID applicationId, ApplicationUpdateRequest applicationUpdateRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        userFacadePort.updateApplication(Application.Id.of(applicationId),
                authenticatedUser.getId(),
                applicationUpdateRequest.getMotivation(),
                applicationUpdateRequest.getProblemSolvingApproach());
        return noContent().build();
    }

    @Override
    public ResponseEntity<PrivateUserProfileResponse> getMyProfile() {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final UserProfileView userProfileView = userFacadePort.getProfileById(authenticatedUser.getId());
        final PrivateUserProfileResponse userProfileResponse = userProfileToPrivateResponse(userProfileView);
        return ResponseEntity.ok(userProfileResponse);
    }

    @Override
    public ResponseEntity<PrivateUserProfileResponse> setMyProfile(UserProfileRequest userProfileRequest) {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final UserProfileView updatedProfile = userFacadePort.updateProfile(authenticatedUser.getId(),
                userProfileRequestToDomain(userProfileRequest));
        final PrivateUserProfileResponse userProfileResponse = userProfileToPrivateResponse(updatedProfile);
        return ResponseEntity.ok(userProfileResponse);
    }

    @Override
    public ResponseEntity<ProjectListResponse> getMyContributedProjects(List<Long> repositories) {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        final var filters = ContributionView.Filters.builder()
                .repos(Optional.ofNullable(repositories).orElse(List.of()))
                .build();

        final var projects = contributorFacadePort.contributedProjects(authenticatedUser.getGithubUserId(), filters);

        return ResponseEntity.ok(new ProjectListResponse()
                .projects(projects.stream().map(ProjectMapper::mapShortProjectResponse).toList())
        );
    }


    @Override
    public ResponseEntity<ProjectListResponse> getMyRewardingProjects() {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        final var projects = contributorFacadePort.rewardingProjects(authenticatedUser.getGithubUserId());

        return ResponseEntity.ok(new ProjectListResponse()
                .projects(projects.stream().map(ProjectMapper::mapShortProjectResponse).toList())
        );
    }

    @Override
    public ResponseEntity<ContributedReposResponse> getMyContributedRepos(List<UUID> projects) {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        final var filters = ContributionView.Filters.builder()
                .projects(Optional.ofNullable(projects).orElse(List.of()))
                .build();

        final var contributedRepos = contributorFacadePort.contributedRepos(authenticatedUser.getGithubUserId(),
                filters);

        return ResponseEntity.ok(new ContributedReposResponse()
                .repos(contributedRepos.stream().map(GithubRepoMapper::mapRepoToShortResponse).toList())
        );
    }


    @Override
    public ResponseEntity<RewardDetailsResponse> getMyReward(UUID rewardId) {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final RewardDetailsView rewardDetailsView = userFacadePort.getRewardByIdForRecipientIdAndAdministratedBillingProfileIds(rewardId,
                authenticatedUser.getGithubUserId(), authenticatedUser.getAdministratedBillingProfiles());
        return ResponseEntity.ok(RewardMapper.myRewardDetailsToResponse(rewardDetailsView, authenticatedUser.asAuthenticatedUser()));
    }

    @Override
    public ResponseEntity<RewardItemsPageResponse> getMyRewardItemsPage(UUID rewardId, Integer pageIndex,
                                                                        Integer pageSize) {
        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final int sanitizedPageIndex = PaginationHelper.sanitizePageIndex(pageIndex);
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final Page<RewardItemView> page = userFacadePort.getRewardItemsPageByIdForRecipientIdAndAdministratedBillingProfileIds(rewardId,
                authenticatedUser.getGithubUserId(), sanitizedPageIndex, sanitizedPageSize,
                authenticatedUser.getAdministratedBillingProfiles());
        final RewardItemsPageResponse rewardItemsPageResponse = RewardMapper.pageToResponse(sanitizedPageIndex, page);
        return rewardItemsPageResponse.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(rewardItemsPageResponse) :
                ResponseEntity.ok(rewardItemsPageResponse);
    }

    @Override
    public ResponseEntity<CurrencyListResponse> getMyRewardCurrencies() {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var currencies = contributorFacadePort.getRewardCurrencies(
                authenticatedUser.getGithubUserId(),
                authenticatedUser.getAdministratedBillingProfiles()
        );
        return ResponseEntity.ok(new CurrencyListResponse().currencies(currencies.stream()
                .map(RewardMapper::mapCurrency)
                .sorted(comparing(ShortCurrencyResponse::getCode))
                .toList()));
    }

    @Override
    public ResponseEntity<List<GithubOrganizationResponse>> searchGithubUserOrganizations() {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        final List<GithubAccount> githubAccounts =
                githubOrganizationFacadePort.getOrganizationsForAuthenticatedUser(authenticatedUser);
        return githubAccounts.isEmpty() ? ResponseEntity.notFound().build() :
                ResponseEntity.ok(githubAccounts.stream().map(GithubMapper::mapToGithubOrganizationResponse).toList());
    }

    @Override
    public ResponseEntity<Void> claimProject(UUID projectId) {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        userFacadePort.claimProjectForAuthenticatedUser(
                projectId, authenticatedUser
        );
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
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        userFacadePort.updateGithubProfile(authenticatedUser);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<PayoutPreferencesItemResponse>> getMyPayoutPreferences() {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final List<PayoutPreferenceView> payoutPreferences = payoutPreferenceFacadePort.getPayoutPreferences(UserId.of(authenticatedUser.getId()));
        final List<PayoutPreferencesItemResponse> response = isNull(payoutPreferences) ? List.of() : payoutPreferences.stream()
                .map(PayoutPreferenceMapper::mapToResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> setMyPayoutPreferenceForProject(PayoutPreferenceRequest payoutPreferenceRequest) {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        payoutPreferenceFacadePort.setPayoutPreference(ProjectId.of(payoutPreferenceRequest.getProjectId()),
                BillingProfile.Id.of(payoutPreferenceRequest.getBillingProfileId()), UserId.of(authenticatedUser.getId()));
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> acceptOrRejectCoworkerInvitation(UUID billingProfileId, BillingProfileCoworkerInvitationUpdateRequest invitationUpdateRequest) {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (Boolean.TRUE.equals(invitationUpdateRequest.getAccepted())) {
            billingProfileFacadePort.acceptCoworkerInvitation(
                    BillingProfile.Id.of(billingProfileId),
                    GithubUserId.of(authenticatedUser.getGithubUserId()));
        } else {
            billingProfileFacadePort.rejectCoworkerInvitation(
                    BillingProfile.Id.of(billingProfileId),
                    GithubUserId.of(authenticatedUser.getGithubUserId()));
        }
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> registerToHackathon(UUID hackathonId) {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        hackathonFacadePort.registerToHackathon(authenticatedUser.getId(), Hackathon.Id.of(hackathonId));
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> voteForCommitteeAssignment(UUID committeeId, UUID projectId,
                                                           VoteForCommitteeAssignmentRequest voteForCommitteeAssignmentRequest) {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        committeeFacadePort.vote(authenticatedUser.getId(), Committee.Id.of(committeeId), projectId,
                voteForCommitteeAssignmentRequest.getVotes().stream()
                        .collect(Collectors.toMap(v -> JuryCriteria.Id.of(v.getCriteriaId()), v -> v.getVote())));

        return noContent().build();
    }
}
