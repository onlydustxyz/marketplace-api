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
import onlydust.com.marketplace.accounting.domain.view.ShortBillingProfileView;
import onlydust.com.marketplace.api.contract.MeApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.project.domain.model.GithubAccount;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.port.input.ContributorFacadePort;
import onlydust.com.marketplace.project.domain.port.input.GithubOrganizationFacadePort;
import onlydust.com.marketplace.project.domain.port.input.RewardFacadePort;
import onlydust.com.marketplace.project.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.project.domain.view.*;
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

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.MyRewardMapper.getSortBy;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.MyRewardMapper.mapMyRewardsToResponse;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.UserMapper.*;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageIndex;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageSize;

@RestController
@Tags(@Tag(name = "Me"))
@AllArgsConstructor
public class MeRestApi implements MeApi {

    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final UserFacadePort userFacadePort;
    private final RewardFacadePort rewardFacadePort;
    private final ContributorFacadePort contributorFacadePort;
    private final GithubOrganizationFacadePort githubOrganizationFacadePort;
    private final BillingProfileFacadePort billingProfileFacadePort;
    private final PayoutPreferenceFacadePort payoutPreferenceFacadePort;

    @Override
    public ResponseEntity<GetMeResponse> getMe() {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final GetMeResponse getMeResponse = userToGetMeResponse(authenticatedUser);
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
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> acceptInvitationToLeadProject(UUID projectId) {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        userFacadePort.acceptInvitationToLeadProject(authenticatedUser.getGithubUserId(), projectId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> applyOnProject(ApplicationRequest applicationRequest) {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        userFacadePort.applyOnProject(authenticatedUser.getId(), applicationRequest.getProjectId());
        return ResponseEntity.noContent().build();
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
    public ResponseEntity<MyRewardsPageResponse> getMyRewards(Integer pageIndex, Integer pageSize,
                                                              String sort, String direction,
                                                              List<UUID> currencies, List<UUID> projects,
                                                              String fromDate, String toDate) {
        final var sanitizedPageSize = sanitizePageSize(pageSize);
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var sortBy = getSortBy(sort);
        final var filters = UserRewardView.Filters.builder()
                .currencies(Optional.ofNullable(currencies).orElse(List.of()))
                .projectIds(Optional.ofNullable(projects).orElse(List.of()))
                .from(isNull(fromDate) ? null : DateMapper.parse(fromDate))
                .to(isNull(toDate) ? null : DateMapper.parse(toDate))
                .build();

        final var page = userFacadePort.getRewardsForUserId(authenticatedUser.getId(), filters, sanitizedPageIndex,
                sanitizedPageSize, sortBy, SortDirectionMapper.requestToDomain(direction));

        final var myRewardsPageResponse = mapMyRewardsToResponse(sanitizedPageIndex, page);

        return myRewardsPageResponse.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(myRewardsPageResponse) :
                ResponseEntity.ok(myRewardsPageResponse);
    }

    @Override
    public ResponseEntity<ContributionPageResponse> getMyContributions(List<ContributionType> types,
                                                                       List<ContributionStatus> statuses,
                                                                       List<UUID> projects,
                                                                       List<Long> repositories,
                                                                       String fromDate,
                                                                       String toDate,
                                                                       ContributionSort sort,
                                                                       String direction,
                                                                       Integer page,
                                                                       Integer pageSize) {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final int sanitizedPageIndex = sanitizePageIndex(page);

        final var filters = ContributionView.Filters.builder()
                .contributors(List.of(authenticatedUser.getGithubUserId()))
                .projects(Optional.ofNullable(projects).orElse(List.of()))
                .repos(Optional.ofNullable(repositories).orElse(List.of()))
                .types(Optional.ofNullable(types).orElse(List.of()).stream().map(ContributionMapper::mapContributionType).toList())
                .statuses(Optional.ofNullable(statuses).orElse(List.of()).stream().map(ContributionMapper::mapContributionStatus).toList())
                .from(isNull(fromDate) ? null : DateMapper.parse(fromDate))
                .to(isNull(toDate) ? null : DateMapper.parse(toDate))
                .build();

        final var contributions = contributorFacadePort.contributions(
                authenticatedUser.getGithubUserId(),
                filters,
                ContributionMapper.mapSort(sort),
                SortDirectionMapper.requestToDomain(direction),
                sanitizedPageIndex,
                sanitizedPageSize);

        final var contributionPageResponse = ContributionMapper.mapContributionPageResponse(
                sanitizedPageIndex,
                contributions);

        return contributionPageResponse.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(contributionPageResponse)
                : ResponseEntity.ok(contributionPageResponse);

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
        final RewardDetailsView rewardDetailsView = userFacadePort.getRewardByIdForRecipientId(rewardId,
                authenticatedUser.getGithubUserId());
        return ResponseEntity.ok(RewardMapper.rewardDetailsToResponse(rewardDetailsView, true));
    }

    @Override
    public ResponseEntity<RewardItemsPageResponse> getMyRewardItemsPage(UUID rewardId, Integer pageIndex,
                                                                        Integer pageSize) {
        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final int sanitizedPageIndex = PaginationHelper.sanitizePageIndex(pageIndex);
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final Page<RewardItemView> page = userFacadePort.getRewardItemsPageByIdForRecipientId(rewardId,
                authenticatedUser.getGithubUserId(), sanitizedPageIndex, sanitizedPageSize);
        final RewardItemsPageResponse rewardItemsPageResponse = RewardMapper.pageToResponse(sanitizedPageIndex, page);
        return rewardItemsPageResponse.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(rewardItemsPageResponse) :
                ResponseEntity.ok(rewardItemsPageResponse);
    }

    @Override
    public ResponseEntity<MyRewardsListResponse> getMyRewardsPendingInvoice() {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final List<UserRewardView> rewardViews =
                userFacadePort.getPendingInvoiceRewardsForRecipientId(authenticatedUser.getGithubUserId());
        return ResponseEntity.ok(MyRewardMapper.listToResponse(rewardViews));
    }

    @Override
    public ResponseEntity<CurrencyListResponse> getMyRewardCurrencies() {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var currencies = contributorFacadePort.getRewardCurrencies(authenticatedUser.getGithubUserId());
        return ResponseEntity.ok(new CurrencyListResponse().currencies(currencies.stream().map(ProjectBudgetMapper::mapCurrency).toList()));
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
        return ResponseEntity.noContent().build();
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
    public ResponseEntity<Void> markInvoiceAsReceived() {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        rewardFacadePort.markInvoiceAsReceived(authenticatedUser.getGithubUserId());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> updateMyGithubProfileData() {
        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        userFacadePort.updateGithubProfile(authenticatedUser);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<MyBillingProfilesResponse> getMyBillingProfiles() {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final List<ShortBillingProfileView> shortBillingProfileViews = billingProfileFacadePort.getBillingProfilesForUser(UserId.of(authenticatedUser.getId()));
        final MyBillingProfilesResponse myBillingProfilesResponse = BillingProfileMapper.myBillingProfileToResponse(shortBillingProfileViews);
        return ResponseEntity.ok(myBillingProfilesResponse);
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
        return ResponseEntity.noContent().build();
    }
}
