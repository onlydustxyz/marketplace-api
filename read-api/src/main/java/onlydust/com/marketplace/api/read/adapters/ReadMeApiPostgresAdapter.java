package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadMeApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.LanguageReadEntity;
import onlydust.com.marketplace.api.read.entities.billing_profile.AllBillingProfileUserReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ApplicationReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectCategoryReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectReadEntity;
import onlydust.com.marketplace.api.read.entities.project.PublicProjectReadEntity;
import onlydust.com.marketplace.api.read.entities.sponsor.SponsorReadEntity;
import onlydust.com.marketplace.api.read.entities.user.NotificationSettingsForProjectReadEntity;
import onlydust.com.marketplace.api.read.entities.user.NotificationSettingsForProjectReadEntity.PrimaryKey;
import onlydust.com.marketplace.api.read.entities.user.UserProfileInfoReadEntity;
import onlydust.com.marketplace.api.read.mapper.RewardsMapper;
import onlydust.com.marketplace.api.read.repositories.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.project.domain.port.input.GithubUserPermissionsFacadePort;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.toZoneDateTime;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
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
    private final RewardReadRepository rewardReadRepository;
    private final UserRewardStatsReadRepository userRewardStatsReadRepository;
    private final PublicProjectReadRepository publicProjectReadRepository;
    private final UserReadRepository userReadRepository;
    private final GithubUserPermissionsFacadePort githubUserPermissionsFacadePort;
    private final PayoutPreferenceReadRepository payoutPreferenceReadRepository;
    private final NotificationSettingsForProjectReadRepository notificationSettingsForProjectReadRepository;
    private final ProjectReadRepository projectReadRepository;
    private final ProjectCategoryReadRepository projectCategoryReadRepository;
    private final LanguageReadRepository languageReadRepository;

    @Override
    public ResponseEntity<GetMeResponse> getMe() {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var user = userReadRepository.findMe(authenticatedUser.getId())
                .orElseThrow(() -> internalServerError("User %s not found".formatted(authenticatedUser.toString())));
        final var userAuthorizedToApplyOnGithubIssues = githubUserPermissionsFacadePort.isUserAuthorizedToApplyOnProject(user.githubUserId());
        final var hasMissingPayoutPreferences = rewardReadRepository.existsByRecipientIdAndStatus_Status(authenticatedUser.getGithubUserId(),
                RewardStatus.Input.PENDING_BILLING_PROFILE);

        final var response = new GetMeResponse()
                .id(user.registered().id())
                .githubUserId(user.githubUserId())
                .avatarUrl(user.avatarUrl())
                .login(user.login())
                .hasCompletedOnboarding(user.onboarding() != null && user.onboarding().getCompletionDate() != null)
                .hasAcceptedLatestTermsAndConditions(user.onboarding() != null && user.onboarding().isHasAcceptedTermsAndConditions())
                .hasCompletedVerificationInformation(user.onboardingCompletion() != null && user.onboardingCompletion().telegramAdded())
                .isAuthorizedToApplyOnGithubIssues(userAuthorizedToApplyOnGithubIssues)
                .projectsLed(user.projectsLed().stream().map(ProjectReadEntity::toLinkResponse).toList())
                .pendingProjectsLed(user.pendingProjectsLed().stream().map(ProjectReadEntity::toLinkResponse).toList())
                .pendingApplications(user.pendingApplications().stream().map(ApplicationReadEntity::toShortResponse).toList())
                .isAdmin(Arrays.stream(user.registered().roles()).anyMatch(r -> r == AuthenticatedUser.Role.ADMIN))
                .createdAt(toZoneDateTime(user.registered().createdAt()))
                .email(user.email())
                .firstName(user.profile().map(UserProfileInfoReadEntity::firstName).orElse(null))
                .lastName(user.profile().map(UserProfileInfoReadEntity::lastName).orElse(null))
                .missingPayoutPreference(hasMissingPayoutPreferences)
                .sponsors(user.sponsors().stream().map(SponsorReadEntity::toDto).toList());

        return ok(response);
    }

    @Override
    public ResponseEntity<OnboardingCompletionResponse> getOnboardingCompletion() {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var journeyCompletion = userReadRepository.findMeOnboarding(authenticatedUser.getId())
                .orElseThrow(() -> internalServerError("User %s not found".formatted(authenticatedUser.toString())));

        return ok(journeyCompletion.onboardingCompletion().toResponse());
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

    @Override
    public ResponseEntity<List<PayoutPreferencesItemResponse>> getMyPayoutPreferences() {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var payoutPreferences = payoutPreferenceReadRepository.findAllForUser(authenticatedUser.getId());
        return ok(payoutPreferences.stream()
                .map(p -> p.toDto(authenticatedUser.getGithubUserId()))
                .sorted(Comparator.comparing(p -> p.getProject().getName()))
                .toList());
    }

    @Override
    public ResponseEntity<PrivateUserProfileResponse> getMyProfile() {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var user = userReadRepository.findMeProfile(authenticatedUser.getId())
                .orElseThrow(() -> internalServerError("User %s not found".formatted(authenticatedUser.toString())));
        final List<ProjectCategoryReadEntity> preferredCategories = projectCategoryReadRepository.findPreferredOnesForUser(authenticatedUser.getId());
        final List<LanguageReadEntity> preferredLanguages = languageReadRepository.findPreferredOnesForUser(authenticatedUser.getId());
        return ok(user.toPrivateUserProfileResponse(preferredCategories, preferredLanguages));
    }

    @Override
    public ResponseEntity<NotificationSettingsForProjectResponse> getMyNotificationSettingsForProject(UUID projectId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var dto = notificationSettingsForProjectReadRepository.findById(new PrimaryKey(authenticatedUser.getId(), projectId))
                .map(NotificationSettingsForProjectReadEntity::toDto)
                .orElseGet(() -> projectReadRepository.findById(projectId)
                        .map(NotificationSettingsForProjectReadEntity::defaultDto)
                        .orElseThrow(() -> notFound("Project %s not found".formatted(projectId))));
        return ok(dto);
    }
}
