package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeUserApi;
import onlydust.com.backoffice.api.contract.model.UserDetailsResponse;
import onlydust.com.backoffice.api.contract.model.UserPage;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileFacadePort;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import onlydust.com.marketplace.project.domain.port.input.BackofficeFacadePort;
import onlydust.com.marketplace.project.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.project.domain.view.UserProfileView;
import onlydust.com.marketplace.project.domain.view.backoffice.UserShortView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.mapUserPageToContract;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageIndex;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageSize;

@RestController
@Tags(@Tag(name = "BackofficeUser"))
@AllArgsConstructor
public class BackofficeUserRestApi implements BackofficeUserApi {
    private final BackofficeFacadePort backofficeFacadePort;
    private final UserFacadePort userFacadePort;
    private final BillingProfileFacadePort billingProfileFacadePort;
    final static Integer MAX_PAGE_SIZE = Integer.MAX_VALUE;

    @Override
    public ResponseEntity<UserPage> getUserPage(Integer pageIndex, Integer pageSize, String login) {
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final var filters = UserShortView.Filters.builder()
                .loginLike(login)
                .build();
        final var usersPage = backofficeFacadePort.listUsers(sanitizedPageIndex, sanitizePageSize(pageSize, MAX_PAGE_SIZE), filters);
        final var response = mapUserPageToContract(usersPage, sanitizedPageIndex);

        return response.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response) :
                ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<UserDetailsResponse> getUserById(UUID userId) {
        final var user = userFacadePort.getUserById(userId);
        final var userProfile = userFacadePort.getProfileById(userId);
        final var userBillingProfiles = billingProfileFacadePort.getBillingProfilesForUser(UserId.of(userId))
                .stream().map(bp -> billingProfileFacadePort.getById(bp.getId()));

        final UserDetailsResponse response = new UserDetailsResponse()
                .id(user.getId())
                .githubUserId(user.getGithubUserId())
                .login(user.getGithubLogin())
                .avatarUrl(userProfile.getAvatarUrl())
                .email(user.getGithubEmail())
                .lastSeenAt(DateMapper.toZoneDateTime(userProfile.getLastSeenAt()))
                .signedUpAt(DateMapper.toZoneDateTime(userProfile.getCreateAt()))
                .contacts(BackOfficeMapper.contactToResponse(userProfile.getContacts()))
                .leadedProjectCount((int) userProfile.getProjectsStats().stream().filter(UserProfileView.ProjectStats::getIsProjectLead).count())
                .totalEarnedUsd(userProfile.getProfileStats().getTotalsEarned().getTotalDollarsEquivalent())
                .billingProfiles(userBillingProfiles.map(BackOfficeMapper::mapToShortResponse).toList());
        return ResponseEntity.ok(response);
    }

}
