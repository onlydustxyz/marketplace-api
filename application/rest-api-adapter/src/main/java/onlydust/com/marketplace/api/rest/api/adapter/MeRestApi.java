package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.MeApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.model.GithubAccount;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserPayoutSettings;
import onlydust.com.marketplace.api.domain.port.input.ContributorFacadePort;
import onlydust.com.marketplace.api.domain.port.input.GithubOrganizationFacadePort;
import onlydust.com.marketplace.api.domain.port.input.RewardFacadePort;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
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
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageIndex;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageSize;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.MyRewardMapper.getSortBy;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.MyRewardMapper.mapMyRewardsToResponse;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.UserMapper.*;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.UserPayoutInfoMapper.userPayoutSettingsToDomain;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.UserPayoutInfoMapper.userPayoutSettingsToResponse;

@RestController
@Tags(@Tag(name = "Me"))
@AllArgsConstructor
public class MeRestApi implements MeApi {

    private final AuthenticationService authenticationService;
    private final UserFacadePort userFacadePort;
    private final RewardFacadePort rewardFacadePort;
    private final ContributorFacadePort contributorFacadePort;
    private final GithubOrganizationFacadePort githubOrganizationFacadePort;

    @Override
    public ResponseEntity<GetMeResponse> getMe() {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        final GetMeResponse getMeResponse = userToGetMeResponse(authenticatedUser);
        return ResponseEntity.ok(getMeResponse);
    }

    @Override
    public ResponseEntity<UserPayoutSettingsResponse> getMyPayoutSettings() {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        final UserPayoutSettings view = userFacadePort.getPayoutSettingsForUserId(authenticatedUser.getId());
        final UserPayoutSettingsResponse userPayoutSettingsResponse = userPayoutSettingsToResponse(view);
        return ResponseEntity.ok(userPayoutSettingsResponse);
    }

    @Override
    public ResponseEntity<UserPayoutSettingsResponse> putMyPayoutSettings(UserPayoutSettingsRequest userPayoutSettingsRequest) {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        final UserPayoutSettings view = userFacadePort.updatePayoutSettings(authenticatedUser.getId(),
                userPayoutSettingsToDomain(userPayoutSettingsRequest));
        return ResponseEntity.ok(userPayoutSettingsToResponse(view));
    }

    @Override
    public ResponseEntity<Void> patchMe(PatchMeContract patchMeContract) {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
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
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        userFacadePort.acceptInvitationToLeadProject(authenticatedUser.getGithubUserId(), projectId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> applyOnProject(ApplicationRequest applicationRequest) {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        userFacadePort.applyOnProject(authenticatedUser.getId(), applicationRequest.getProjectId());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<PrivateUserProfileResponse> getMyProfile() {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        final UserProfileView userProfileView = userFacadePort.getProfileById(authenticatedUser.getId());
        final PrivateUserProfileResponse userProfileResponse = userProfileToPrivateResponse(userProfileView);
        return ResponseEntity.ok(userProfileResponse);
    }

    @Override
    public ResponseEntity<PrivateUserProfileResponse> setMyProfile(UserProfileRequest userProfileRequest) {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        final UserProfileView updatedProfile = userFacadePort.updateProfile(authenticatedUser.getId(),
                userProfileRequestToDomain(userProfileRequest));
        final PrivateUserProfileResponse userProfileResponse = userProfileToPrivateResponse(updatedProfile);
        return ResponseEntity.ok(userProfileResponse);
    }

    @Override
    public ResponseEntity<MyRewardsPageResponse> getMyRewards(Integer pageIndex, Integer pageSize,
                                                              String sort, String direction,
                                                              List<CurrencyContract> currencies, List<UUID> projects,
                                                              String fromDate, String toDate) {
        final var sanitizedPageSize = sanitizePageSize(pageSize);
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final var authenticatedUser = authenticationService.getAuthenticatedUser();
        final var sortBy = getSortBy(sort);
        final var filters = UserRewardView.Filters.builder()
                .currencies(Optional.ofNullable(currencies).orElse(List.of()).stream().map(ProjectBudgetMapper::mapCurrency).toList())
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
    public ResponseEntity<RewardTotalAmountsResponse> getMyRewardTotalAmounts() {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        return ResponseEntity.ok(MyRewardMapper.mapUserRewardTotalAmountsToResponse(
                userFacadePort.getRewardTotalAmountsForUserId(authenticatedUser.getId())));
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
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
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
        final User authenticatedUser = authenticationService.getAuthenticatedUser();

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
        final User authenticatedUser = authenticationService.getAuthenticatedUser();

        final var projects = contributorFacadePort.rewardingProjects(authenticatedUser.getGithubUserId());

        return ResponseEntity.ok(new ProjectListResponse()
                .projects(projects.stream().map(ProjectMapper::mapShortProjectResponse).toList())
        );
    }

    @Override
    public ResponseEntity<ContributedReposResponse> getMyContributedRepos(List<UUID> projects) {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();

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
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        final RewardView rewardView = userFacadePort.getRewardByIdForRecipientId(rewardId,
                authenticatedUser.getGithubUserId());
        return ResponseEntity.ok(RewardMapper.rewardDetailsToResponse(rewardView));
    }

    @Override
    public ResponseEntity<RewardItemsPageResponse> getMyRewardItemsPage(UUID rewardId, Integer pageIndex,
                                                                        Integer pageSize) {
        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final int sanitizedPageIndex = PaginationHelper.sanitizePageIndex(pageIndex);
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        final Page<RewardItemView> page = userFacadePort.getRewardItemsPageByIdForRecipientId(rewardId,
                authenticatedUser.getGithubUserId(), sanitizedPageIndex, sanitizedPageSize);
        final RewardItemsPageResponse rewardItemsPageResponse = RewardMapper.pageToResponse(sanitizedPageIndex, page);
        return rewardItemsPageResponse.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(rewardItemsPageResponse) :
                ResponseEntity.ok(rewardItemsPageResponse);
    }

    @Override
    public ResponseEntity<MyRewardsListResponse> getMyRewardsPendingInvoice() {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        final List<UserRewardView> rewardViews =
                userFacadePort.getPendingInvoiceRewardsForRecipientId(authenticatedUser.getGithubUserId());
        return ResponseEntity.ok(MyRewardMapper.listToResponse(rewardViews));
    }

    @Override
    public ResponseEntity<CurrencyListResponse> getMyRewardCurrencies() {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        final var currencies = contributorFacadePort.getRewardCurrencies(authenticatedUser.getGithubUserId());
        return ResponseEntity.ok(new CurrencyListResponse().currencies(currencies.stream().map(ProjectBudgetMapper::mapCurrency).toList()));
    }

    @Override
    public ResponseEntity<List<GithubOrganizationResponse>> searchGithubUserOrganizations() {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();

        final List<GithubAccount> githubAccounts =
                githubOrganizationFacadePort.getOrganizationsForAuthenticatedUser(authenticatedUser);
        return githubAccounts.isEmpty() ? ResponseEntity.notFound().build() :
                ResponseEntity.ok(githubAccounts.stream().map(GithubMapper::mapToGithubOrganizationResponse).toList());
    }

    @Override
    public ResponseEntity<Void> claimProject(UUID projectId) {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
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
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        rewardFacadePort.markInvoiceAsReceived(authenticatedUser.getGithubUserId());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<CompanyBillingProfileResponse> getMyCompanyBillingProfile() {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        return ResponseEntity.ok(BillingProfileMapper.companyDomainToResponse(userFacadePort.getCompanyBillingProfile(authenticatedUser.getId())));
    }

    @Override
    public ResponseEntity<IndividualBillingProfileResponse> getMyIndividualBillingProfile() {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        return ResponseEntity.ok(BillingProfileMapper.individualDomainToResponse(userFacadePort.getIndividualBillingProfile(authenticatedUser.getId())));
    }

    @Override
    public ResponseEntity<Void> updateBillingProfileType(BillingProfileTypeRequest billingProfileTypeRequest) {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        userFacadePort.updateBillingProfileType(authenticatedUser.getId(), BillingProfileMapper.billingProfileToDomain(billingProfileTypeRequest));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> updateMyGithubProfileData() {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        userFacadePort.updateGithubProfile(authenticatedUser);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<MyBillingProfilesResponse> getMyBillingProfiles() {
        final var authenticatedUser = authenticationService.getAuthenticatedUser();
        final var billingProfiles = userFacadePort.getBillingProfiles(authenticatedUser.getId(), authenticatedUser.getGithubUserId());
        return ResponseEntity.ok(new MyBillingProfilesResponse()
                .billingProfiles(billingProfiles.stream().map(BillingProfileMapper::map).toList()));
    }
}
