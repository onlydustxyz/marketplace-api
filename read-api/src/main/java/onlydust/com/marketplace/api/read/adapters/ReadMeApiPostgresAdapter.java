package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadMeApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.billing_profile.AllBillingProfileUserReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ApplicationReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectReadEntity;
import onlydust.com.marketplace.api.read.entities.project.PublicProjectReadEntity;
import onlydust.com.marketplace.api.read.entities.sponsor.SponsorReadEntity;
import onlydust.com.marketplace.api.read.mapper.RewardsMapper;
import onlydust.com.marketplace.api.read.repositories.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.project.domain.port.input.GithubUserPermissionsFacadePort;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.toZoneDateTime;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.*;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadMeApiPostgresAdapter implements ReadMeApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final AllBillingProfileUserReadRepository allBillingProfileUserReadRepository;
    private final RewardDetailsReadRepository rewardDetailsReadRepository;
    private final UserRewardStatsReadRepository userRewardStatsReadRepository;
    private final PublicProjectReadRepository publicProjectReadRepository;
    private final UserReadRepository userReadRepository;
    private final GithubUserPermissionsFacadePort githubUserPermissionsFacadePort;

    @Override
    public ResponseEntity<GetMeResponse> getMe() {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var user = userReadRepository.findMe(authenticatedUser.getId())
                .orElseThrow(() -> internalServerError("User %s not found".formatted(authenticatedUser.toString())));
        final var userAuthorizedToApplyOnGithubIssues = githubUserPermissionsFacadePort.isUserAuthorizedToApplyOnProject(user.githubUserId());

        final var response = new GetMeResponse()
                .id(user.registered().id())
                .githubUserId(user.githubUserId())
                .avatarUrl(user.avatarUrl())
                .login(user.login())
                .hasSeenOnboardingWizard(user.onboarding() != null && user.onboarding().getProfileWizardDisplayDate() != null)
                .hasAcceptedLatestTermsAndConditions(user.onboarding() != null && user.onboarding().isHasAcceptedTermsAndConditions())
                .isAuthorizedToApplyOnGithubIssues(userAuthorizedToApplyOnGithubIssues)
                .projectsLed(user.projectsLed().stream().map(ProjectReadEntity::toLinkResponse).toList())
                .pendingProjectsLed(user.pendingProjectsLed().stream().map(ProjectReadEntity::toLinkResponse).toList())
                .pendingApplications(user.pendingApplications().stream().map(ApplicationReadEntity::toShortResponse).toList())
                .isAdmin(Arrays.stream(user.registered().roles()).anyMatch(r -> r == AuthenticatedUser.Role.ADMIN))
                .createdAt(toZoneDateTime(user.registered().createdAt()))
                .email(user.email())
                .firstName(user.profile() == null ? null : user.profile().firstName())
                .lastName(user.profile() == null ? null : user.profile().lastName())
                .missingPayoutPreference(user.hasMissingPayoutPreferences())
                .sponsors(user.sponsors().stream().map(SponsorReadEntity::toDto).toList());

        return ok(response);
    }

    @Override
    public ResponseEntity<JourneyCompletionResponse> getJourneyCompletion() {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var journeyCompletion = userReadRepository.findMeJourney(authenticatedUser.getId())
                .orElseThrow(() -> internalServerError("User %s not found".formatted(authenticatedUser.toString())));

        return ok(journeyCompletion.journeyCompletion().toResponse());
    }

    @Override
    public ResponseEntity<RecommendedProjectsPageResponse> getRecommendedProjects(Integer pageIndex, Integer pageSize) {
        final int sanitizePageIndex = sanitizePageIndex(pageIndex);
        final int sanitizePageSize = sanitizePageSize(pageSize);

        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var page = publicProjectReadRepository.findAllRecommendedForUser(authenticatedUser.getGithubUserId(),
                PageRequest.of(sanitizePageIndex, sanitizePageSize));

        final var response = new RecommendedProjectsPageResponse()
                .projects(page.getContent().stream().map(PublicProjectReadEntity::toProjectLinkWithDescription).toList())
                .hasMore(hasMore(sanitizePageIndex, page.getTotalPages()))
                .nextPageIndex(nextPageIndex(sanitizePageIndex, page.getTotalPages()))
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages());

        return response.getHasMore() ? status(HttpStatus.PARTIAL_CONTENT).body(response) : ok(response);
    }

    @Override
    public ResponseEntity<MyBillingProfilesResponse> getMyBillingProfiles() {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var billingProfiles = allBillingProfileUserReadRepository.findAllByUserId(authenticatedUser.getId());

        final var response = new MyBillingProfilesResponse()
                .billingProfiles(billingProfiles.stream().map(AllBillingProfileUserReadEntity::toShortResponse).toList());
        return ok(response);
    }

    @Override
    public ResponseEntity<MyRewardsPageResponse> getMyRewards(Integer pageIndex, Integer pageSize, RewardsSort sort, SortDirection direction,
                                                              List<UUID> currencies, List<UUID> projects, String fromDate, String toDate,
                                                              RewardStatusContract status) {
        final var sanitizedPageSize = sanitizePageSize(pageSize);
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var sortBy = RewardDetailsReadRepository.sortBy(sort, direction);

        final var page = rewardDetailsReadRepository.findUserRewards(authenticatedUser.getGithubUserId(), currencies, projects,
                authenticatedUser.getAdministratedBillingProfiles(), Optional.ofNullable(status).map(Enum::name).orElse(null), fromDate, toDate,
                PageRequest.of(sanitizedPageIndex, sanitizedPageSize, sortBy));


        final var rewardsStats = userRewardStatsReadRepository.findByUser(authenticatedUser.getGithubUserId(), currencies, projects,
                authenticatedUser.getAdministratedBillingProfiles(), fromDate, toDate);

        final var myRewardsPageResponse = RewardsMapper.mapMyRewardsToResponse(sanitizedPageIndex, page, rewardsStats, authenticatedUser.asAuthenticatedUser());

        return myRewardsPageResponse.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(myRewardsPageResponse) :
                ok(myRewardsPageResponse);
    }
}
