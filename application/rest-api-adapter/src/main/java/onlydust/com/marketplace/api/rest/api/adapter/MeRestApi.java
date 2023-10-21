package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.MeApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.api.domain.view.UserRewardView;
import onlydust.com.marketplace.api.domain.view.UserProfileView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.SortDirectionMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper.sanitizePageIndex;
import static onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper.sanitizePageSize;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.MyRewardMapper.getSortBy;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.MyRewardMapper.mapMyRewardsToResponse;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.UserMapper.*;

@RestController
@Tags(@Tag(name = "Me"))
@AllArgsConstructor
public class MeRestApi implements MeApi {

    private final AuthenticationService authenticationService;
    private final UserFacadePort userFacadePort;

    @Override
    public ResponseEntity<GetMeResponse> getMe() {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        final GetMeResponse getMeResponse = userToGetMeResponse(authenticatedUser);
        return ResponseEntity.ok(getMeResponse);
    }

    @Override
    public ResponseEntity<UserPayoutInformationContract> getMyPayoutInfo() {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        final UserPayoutInformation view = userFacadePort.getPayoutInformationForUserId(authenticatedUser.getId());
        final UserPayoutInformationContract userPayoutInformation = userPayoutInformationToResponse(view);
        return ResponseEntity.ok(userPayoutInformation);
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
}
