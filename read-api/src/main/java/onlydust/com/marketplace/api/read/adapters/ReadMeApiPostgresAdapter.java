package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadMeApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationSettingsChannelEntity;
import onlydust.com.marketplace.api.read.entities.LanguageReadEntity;
import onlydust.com.marketplace.api.read.entities.billing_profile.AllBillingProfileUserReadEntity;
import onlydust.com.marketplace.api.read.entities.ecosystem.EcosystemReadEntity;
import onlydust.com.marketplace.api.read.entities.program.ProgramReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ApplicationReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectCategoryReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectLinkReadEntity;
import onlydust.com.marketplace.api.read.entities.project.PublicProjectReadEntity;
import onlydust.com.marketplace.api.read.entities.sponsor.SponsorReadEntity;
import onlydust.com.marketplace.api.read.entities.user.NotificationReadEntity;
import onlydust.com.marketplace.api.read.entities.user.NotificationSettingsForProjectReadEntity;
import onlydust.com.marketplace.api.read.entities.user.NotificationSettingsForProjectReadEntity.PrimaryKey;
import onlydust.com.marketplace.api.read.entities.user.UserProfileInfoReadEntity;
import onlydust.com.marketplace.api.read.mapper.NotificationMapper;
import onlydust.com.marketplace.api.read.mapper.RewardsMapper;
import onlydust.com.marketplace.api.read.repositories.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.project.domain.port.input.GithubUserPermissionsFacadePort;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

import static onlydust.com.marketplace.api.read.entities.user.NotificationReadEntity.isReadFromContract;
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
    private final NotificationMapper notificationMapper;
    private final AllBillingProfileUserReadRepository allBillingProfileUserReadRepository;
    private final RewardDetailsReadRepository rewardDetailsReadRepository;
    private final RewardReadRepository rewardReadRepository;
    private final UserRewardStatsReadRepository userRewardStatsReadRepository;
    private final PublicProjectReadRepository publicProjectReadRepository;
    private final UserReadRepository userReadRepository;
    private final GithubUserPermissionsFacadePort githubUserPermissionsFacadePort;
    private final PayoutPreferenceReadRepository payoutPreferenceReadRepository;
    private final NotificationSettingsForProjectReadRepository notificationSettingsForProjectReadRepository;
    private final ProjectLinkReadRepository projectLinkReadRepository;
    private final ProjectCategoryReadRepository projectCategoryReadRepository;
    private final LanguageReadRepository languageReadRepository;
    private final NotificationReadRepository notificationReadRepository;
    private final NotificationSettingsChannelReadRepository notificationSettingsChannelReadRepository;
    private final ProgramReadRepository programReadRepository;

    @Override
    public ResponseEntity<GetMeResponse> getMe() {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var user = userReadRepository.findMe(authenticatedUser.id().value())
                .orElseThrow(() -> internalServerError("User %s not found".formatted(authenticatedUser.toString())));
        final var userAuthorizedToApplyOnGithubIssues = githubUserPermissionsFacadePort.isUserAuthorizedToApplyOnProject(user.githubUserId());
        final var hasMissingPayoutPreferences = rewardReadRepository.existsByRecipientIdAndStatus_Status(authenticatedUser.githubUserId(),
                RewardStatus.Input.PENDING_BILLING_PROFILE);

        final var response = new GetMeResponse()
                .id(user.registered().id())
                .githubUserId(user.githubUserId())
                .avatarUrl(user.avatarUrl())
                .login(user.login())
                .hasCompletedOnboarding(user.onboarding() != null && user.onboarding().getCompletionDate() != null)
                .hasAcceptedLatestTermsAndConditions(user.onboarding() != null && user.onboarding().isHasAcceptedTermsAndConditions())
                .hasCompletedVerificationInformation(user.onboardingCompletion() != null && user.onboardingCompletion().verificationInformationProvided())
                .isAuthorizedToApplyOnGithubIssues(userAuthorizedToApplyOnGithubIssues)
                .projectsLed(user.projectsLed().stream().map(ProjectLinkReadEntity::toLinkResponse).toList())
                .pendingProjectsLed(user.pendingProjectsLed().stream().map(ProjectLinkReadEntity::toLinkResponse).toList())
                .pendingApplications(user.pendingApplications().stream().map(ApplicationReadEntity::toShortResponse).toList())
                .isAdmin(Arrays.stream(user.registered().roles()).anyMatch(r -> r == AuthenticatedUser.Role.ADMIN))
                .createdAt(toZoneDateTime(user.registered().createdAt()))
                .email(user.email())
                .firstName(user.profile().map(UserProfileInfoReadEntity::firstName).orElse(null))
                .lastName(user.profile().map(UserProfileInfoReadEntity::lastName).orElse(null))
                .missingPayoutPreference(hasMissingPayoutPreferences)
                .programs(user.programs().stream().map(ProgramReadEntity::toLinkResponse).sorted(Comparator.comparing(ProgramLinkResponse::getName)).toList())
                .sponsors(user.sponsors().stream().map(SponsorReadEntity::toLinkResponse).sorted(Comparator.comparing(SponsorLinkResponse::getName)).toList())
                .ecosystems(user.ecosystems().stream().map(EcosystemReadEntity::toLinkResponse).sorted(Comparator.comparing(EcosystemLinkResponse::getName)).toList());

        return ok(response);
    }

    @Override
    public ResponseEntity<OnboardingCompletionResponse> getOnboardingCompletion() {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var journeyCompletion = userReadRepository.findMeOnboarding(authenticatedUser.id().value())
                .orElseThrow(() -> internalServerError("User %s not found".formatted(authenticatedUser.toString())));

        return ok(journeyCompletion.onboardingCompletion().toResponse());
    }

    @Override
    public ResponseEntity<RecommendedProjectsPageResponse> getRecommendedProjects(Integer pageIndex, Integer pageSize) {
        final int sanitizePageIndex = sanitizePageIndex(pageIndex);
        final int sanitizePageSize = sanitizePageSize(pageSize);
        final var pageRequest = PageRequest.of(sanitizePageIndex, sanitizePageSize);

        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var page = Optional.of(publicProjectReadRepository.findTopRecommendedForUser(authenticatedUser.githubUserId(), pageRequest))
                .filter(p -> p.getTotalElements() > sanitizePageSize) // if there are enough top recommendations, return them
                .orElseGet(() -> publicProjectReadRepository.findAllRecommendedForUser(authenticatedUser.githubUserId(), pageRequest));

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
        final var billingProfiles = allBillingProfileUserReadRepository.findAllByUserId(authenticatedUser.id().value());

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

        final var page = rewardDetailsReadRepository.findUserRewards(authenticatedUser.githubUserId(), currencies, projects,
                authenticatedUser.administratedBillingProfiles(), Optional.ofNullable(status).map(Enum::name).orElse(null), fromDate, toDate,
                PageRequest.of(sanitizedPageIndex, sanitizedPageSize, sortBy));


        final var rewardsStats = userRewardStatsReadRepository.findByUser(authenticatedUser.githubUserId(), currencies, projects,
                authenticatedUser.administratedBillingProfiles(), fromDate, toDate);

        final var myRewardsPageResponse = RewardsMapper.mapMyRewardsToResponse(sanitizedPageIndex, page, rewardsStats, authenticatedUser);

        return myRewardsPageResponse.getTotalPageNumber() > 1 ?
                status(HttpStatus.PARTIAL_CONTENT).body(myRewardsPageResponse) :
                ok(myRewardsPageResponse);
    }

    @Override
    public ResponseEntity<List<PayoutPreferencesItemResponse>> getMyPayoutPreferences() {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var payoutPreferences = payoutPreferenceReadRepository.findAllForUser(authenticatedUser.id().value());
        return ok(payoutPreferences.stream()
                .map(p -> p.toDto(authenticatedUser.githubUserId()))
                .sorted(Comparator.comparing(p -> p.getProject().getName()))
                .toList());
    }

    @Override
    public ResponseEntity<PrivateUserProfileResponse> getMyProfile() {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var user = userReadRepository.findMeProfile(authenticatedUser.id().value())
                .orElseThrow(() -> internalServerError("User %s not found".formatted(authenticatedUser.toString())));
        final List<ProjectCategoryReadEntity> preferredCategories = projectCategoryReadRepository.findPreferredOnesForUser(authenticatedUser.id().value());
        final List<LanguageReadEntity> preferredLanguages = languageReadRepository.findPreferredOnesForUser(authenticatedUser.id().value());
        return ok(user.toPrivateUserProfileResponse(preferredCategories, preferredLanguages));
    }

    @Override
    public ResponseEntity<ProgramPageResponse> getMyPrograms(Integer pageIndex, Integer pageSize) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var page = programReadRepository.findAllByLead(authenticatedUser.id().value(),
                PageRequest.of(sanitizePageIndex(pageIndex), sanitizePageSize(pageSize), Sort.by("name").ascending()));

        final var response = new ProgramPageResponse()
                .programs(page.getContent().stream().map(ProgramReadEntity::toPageItemResponse).toList())
                .hasMore(hasMore(pageIndex, page.getTotalPages()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPages()))
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages());

        return response.getHasMore() ? status(HttpStatus.PARTIAL_CONTENT).body(response) : ok(response);
    }

    @Override
    public ResponseEntity<NotificationSettingsForProjectResponse> getMyNotificationSettingsForProject(UUID projectId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var dto = notificationSettingsForProjectReadRepository.findById(new PrimaryKey(authenticatedUser.id().value(), projectId))
                .map(NotificationSettingsForProjectReadEntity::toDto)
                .orElseGet(() -> projectLinkReadRepository.findById(projectId)
                        .map(NotificationSettingsForProjectReadEntity::defaultDto)
                        .orElseThrow(() -> notFound("Project %s not found".formatted(projectId))));
        return ok(dto);
    }


    @Override
    public ResponseEntity<NotificationSettingsResponse> getMyNotificationSettings() {
        final AuthenticatedUser authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final NotificationSettingsResponse notificationSettingsResponse = new NotificationSettingsResponse();
        notificationSettingsChannelReadRepository.findAllByUserId(authenticatedUser.id().value())
                .stream()
                .filter(notificationSettingsChannelEntity -> !notificationSettingsChannelEntity.channel()
                        .equals(onlydust.com.marketplace.kernel.model.notification.NotificationChannel.IN_APP))
                .collect(Collectors.groupingBy(NotificationSettingsChannelEntity::category))
                .entrySet()
                .stream()
                .map(notificationCategoryListEntry -> new NotificationSettingResponse(
                        notificationCategoryListEntry.getValue()
                                .stream()
                                .map(notificationSettingsChannelEntity -> switch (notificationSettingsChannelEntity.channel()) {
                                    case EMAIL -> NotificationChannel.EMAIL;
                                    case SUMMARY_EMAIL -> NotificationChannel.SUMMARY_EMAIL;
                                    case IN_APP -> throw internalServerError("In app notification settings cannot be read");
                                }).toList(),
                        switch (notificationCategoryListEntry.getKey()) {
                            case CONTRIBUTOR_REWARD -> NotificationCategory.CONTRIBUTOR_REWARD;
                            case GLOBAL_MARKETING -> NotificationCategory.GLOBAL_MARKETING;
                            case GLOBAL_BILLING_PROFILE -> NotificationCategory.GLOBAL_BILLING_PROFILE;
                            case MAINTAINER_PROJECT_PROGRAM -> NotificationCategory.MAINTAINER_PROJECT_PROGRAM;
                            case CONTRIBUTOR_PROJECT -> NotificationCategory.CONTRIBUTOR_PROJECT;
                            case MAINTAINER_PROJECT_CONTRIBUTOR -> NotificationCategory.MAINTAINER_PROJECT_CONTRIBUTOR;
                            case SPONSOR_LEAD -> NotificationCategory.SPONSOR_LEAD;
                            case PROGRAM_LEAD -> NotificationCategory.PROGRAM_LEAD;
                        }))
                .forEach(notificationSettingsResponse::addNotificationSettingsItem);
        return ok(notificationSettingsResponse);
    }

    @Override
    public ResponseEntity<NotificationPageResponse> getMyNotifications(Integer pageIndex, Integer pageSize, NotificationStatus status) {
        final AuthenticatedUser authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final int sanitizePageSize = sanitizePageSize(pageSize);
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final Boolean isRead = isReadFromContract(status);
        final Page<NotificationReadEntity> notificationReadEntityPage = notificationReadRepository.findAllInAppByStatusAndUserId(isRead,
                authenticatedUser.id().value(),
                PageRequest.of(sanitizedPageIndex, sanitizePageSize, JpaSort.unsafe(Sort.Direction.DESC, "created_at")));
        final NotificationPageResponse notificationPageResponse = new NotificationPageResponse();
        notificationReadEntityPage.stream()
                .map(notificationMapper::toNotificationPageItemResponse)
                .forEach(notificationPageResponse::addNotificationsItem);
        notificationPageResponse.setHasMore(notificationReadEntityPage.hasNext());
        notificationPageResponse.setNextPageIndex(nextPageIndex(sanitizedPageIndex, notificationReadEntityPage.getTotalPages()));
        notificationPageResponse.setTotalPageNumber(notificationReadEntityPage.getTotalPages());
        notificationPageResponse.setTotalItemNumber(notificationReadEntityPage.getNumberOfElements());

        return notificationPageResponse.getHasMore() ? status(HttpStatus.PARTIAL_CONTENT).body(notificationPageResponse) :
                ok(notificationPageResponse);
    }

    @Override
    public ResponseEntity<NotificationCountResponse> getMyNotificationsCount(NotificationStatus status) {
        final AuthenticatedUser authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final Boolean isRead = isReadFromContract(status);
        return ok(new NotificationCountResponse(notificationReadRepository.countAllInAppByStatusForUserId(isRead, authenticatedUser.id().value())));
    }
}
