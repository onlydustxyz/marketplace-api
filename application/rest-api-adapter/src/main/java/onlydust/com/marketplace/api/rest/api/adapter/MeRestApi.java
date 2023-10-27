package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.MeApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import onlydust.com.marketplace.api.domain.port.input.ContributorFacadePort;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.ContributionMapper;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.MyRewardMapper;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.RewardMapper;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.SortDirectionMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper.sanitizePageIndex;
import static onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper.sanitizePageSize;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.MyRewardMapper.getSortBy;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.MyRewardMapper.mapMyRewardsToResponse;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.UserMapper.*;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.UserPayoutInfoMapper.userPayoutInformationToDomain;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.UserPayoutInfoMapper.userPayoutInformationToResponse;

@RestController
@Tags(@Tag(name = "Me"))
@AllArgsConstructor
public class MeRestApi implements MeApi {

    private final AuthenticationService authenticationService;
    private final UserFacadePort userFacadePort;
    private final ContributorFacadePort contributorFacadePort;

    @Override
    public ResponseEntity<GetMeResponse> getMe() {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        final GetMeResponse getMeResponse = userToGetMeResponse(authenticatedUser);
        return ResponseEntity.ok(getMeResponse);
    }

    @Override
    public ResponseEntity<UserPayoutInformationResponse> getMyPayoutInfo() {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        final UserPayoutInformation view = userFacadePort.getPayoutInformationForUserId(authenticatedUser.getId());
        final UserPayoutInformationResponse userPayoutInformation = userPayoutInformationToResponse(view);
        return ResponseEntity.ok(userPayoutInformation);
    }

    @Override
    public ResponseEntity<UserPayoutInformationResponse> putMyPayoutInfo(UserPayoutInformationRequest userPayoutInformationRequest) {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        final UserPayoutInformation view = userFacadePort.updatePayoutInformation(authenticatedUser.getId(),
                userPayoutInformationToDomain(userPayoutInformationRequest));
        return ResponseEntity.ok(userPayoutInformationToResponse(view));
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
    public ResponseEntity<MyRewardsPageResponse> getMyRewards(Integer pageIndex, Integer pageSize, String sort,
                                                              String direction) {
        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final int sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        final UserRewardView.SortBy sortBy = getSortBy(sort);
        Page<UserRewardView> page = userFacadePort.getRewardsForUserId(authenticatedUser.getId(), sanitizedPageIndex,
                sanitizedPageSize, sortBy, SortDirectionMapper.requestToDomain(direction));

        final MyRewardsPageResponse myRewardsPageResponse = mapMyRewardsToResponse(sanitizedPageIndex, page);

        return myRewardsPageResponse.getHasMore() ?
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
                                                                       List<UUID> projects,
                                                                       List<Long> repositories,
                                                                       ContributionSort sort,
                                                                       String direction,
                                                                       Integer page,
                                                                       Integer pageSize) {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final int sanitizedPageIndex = sanitizePageIndex(page);

        final var filters = ContributionView.Filters.builder()
                .projects(projects)
                .repos(repositories)
                .types(types.stream().map(ContributionMapper::mapContributionType).toList())
                .build();

        final var contributedProjects = contributorFacadePort.contributedProjects(authenticatedUser.getGithubUserId()
                , filters);
        final var contributedRepos = contributorFacadePort.contributedRepos(authenticatedUser.getGithubUserId(),
                filters);

        final var contributions = contributorFacadePort.contributions(
                authenticatedUser.getGithubUserId(),
                filters,
                ContributionMapper.mapSort(sort),
                SortDirectionMapper.requestToDomain(direction),
                sanitizedPageIndex,
                sanitizedPageSize);

        final var contributionPageResponse = ContributionMapper.mapContributionPageResponse(
                sanitizedPageIndex,
                contributedProjects,
                contributedRepos,
                contributions);

        return contributionPageResponse.getHasMore() ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(contributionPageResponse)
                : ResponseEntity.ok(contributionPageResponse);
    }

    @Override
    public ResponseEntity<RewardResponse> getMyReward(UUID rewardId) {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        final RewardView rewardView = userFacadePort.getRewardByIdForRecipientId(rewardId,
                authenticatedUser.getGithubUserId());
        return ResponseEntity.ok(RewardMapper.rewardToResponse(rewardView));
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
        return rewardItemsPageResponse.getHasMore() ?
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
}
