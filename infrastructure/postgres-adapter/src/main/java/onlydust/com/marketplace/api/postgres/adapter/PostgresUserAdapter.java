package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CurrencyEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ApplicationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.OnboardingEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeadEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.RewardMapper;
import onlydust.com.marketplace.api.postgres.adapter.mapper.UserMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.project.domain.view.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.postgres.adapter.mapper.UserMapper.*;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PostgresUserAdapter implements UserStoragePort {

    private final CustomUserRepository customUserRepository;
    private final CustomContributorRepository customContributorRepository;
    private final UserRepository userRepository;
    private final UserViewRepository userViewRepository;
    private final GlobalSettingsRepository globalSettingsRepository;
    private final OnboardingRepository onboardingRepository;
    private final ProjectLeaderInvitationRepository projectLeaderInvitationRepository;
    private final ProjectLeadRepository projectLeadRepository;
    private final ApplicationRepository applicationRepository;
    private final ProjectRepository projectRepository;
    private final UserProfileInfoRepository userProfileInfoRepository;
    private final CustomRewardRepository customRewardRepository;
    private final ProjectLedIdRepository projectLedIdRepository;
    private final RewardStatsRepository rewardStatsRepository;
    private final RewardViewRepository rewardViewRepository;
    private final CurrencyRepository currencyRepository;
    private final BillingProfileLinkViewRepository billingProfileLinkViewRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserByGithubId(Long githubId) {
        return userViewRepository.findByGithubUserId(githubId).map(this::getUserDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserById(UUID userId) {
        return userViewRepository.findById(userId).map(this::getUserDetails);
    }

    private User getUserDetails(@NotNull UserViewEntity user) {
        final var projectLedIdsByUserId = projectLedIdRepository.findProjectLedIdsByUserId(user.getId()).stream()
                .sorted(Comparator.comparing(ProjectLedIdViewEntity::getProjectSlug))
                .toList();
        final var applications = applicationRepository.findAllByApplicantId(user.getId());
        final var billingProfiles = billingProfileLinkViewRepository.findByUserId(user.getId()).stream()
                .map(BillingProfileLinkViewEntity::toDomain)
                .toList();
        final var hasAnyRewardPendingBillingProfile = rewardViewRepository.existsPendingBillingProfileByRecipientId(user.getGithubUserId());
        return mapUserToDomain(user, globalSettingsRepository.get().getTermsAndConditionsLatestVersionDate(),
                projectLedIdsByUserId, applications, billingProfiles, hasAnyRewardPendingBillingProfile);
    }

    @Override
    @Transactional
    public User createUser(User user) {
        return mapCreatedUserToDomain(userRepository.saveAndFlush(mapUserToEntity(user)));
    }

    @Override
    public void updateUserLastSeenAt(UUID userId, Date lastSeenAt) {
        userRepository.findById(userId)
                .ifPresentOrElse(userEntity -> {
                    userEntity.setLastSeenAt(lastSeenAt);
                    userRepository.save(userEntity);
                }, () -> {
                    throw notFound(format("User with id %s not found", userId));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileView getProfileById(UUID userId) {
        return customUserRepository.findProfileById(userId)
                .map(this::addProjectsStats)
                .orElseThrow(() -> notFound(format("User profile %s not found", userId)));
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileView getProfileById(Long githubUserId) {
        return customUserRepository.findProfileById(githubUserId)
                .map(this::addProjectsStats)
                .orElseThrow(() -> notFound(format("User profile %d not found", githubUserId)));
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileView getProfileByLogin(String githubLogin) {
        return customUserRepository.findProfileByLogin(githubLogin)
                .map(this::addProjectsStats)
                .orElseThrow(() -> notFound(format("User profile %s not found", githubLogin)));
    }

    private UserProfileView addProjectsStats(UserProfileView userProfileView) {
        final var projectsStats = customUserRepository.getProjectsStatsForUser(userProfileView.getGithubId());
        for (ProjectStatsForUserEntity stats : projectsStats) {
            userProfileView.addProjectStats(UserProfileView.ProjectStats.builder()
                    .id(stats.getId())
                    .slug(stats.getSlug())
                    .name(stats.getName())
                    .contributorCount(stats.getContributorsCount())
                    .isProjectLead(stats.getIsLead())
                    .projectLeadSince(stats.getLeadSince())
                    .totalGranted(stats.getTotalGranted())
                    .userContributionCount(stats.getUserContributionsCount())
                    .userLastContributedAt(stats.getLastContributionDate())
                    .userFirstContributedAt(stats.getFirstContributionDate())
                    .logoUrl(stats.getLogoUrl())
                    .visibility(switch (stats.getVisibility()) {
                        case PUBLIC -> ProjectVisibility.PUBLIC;
                        case PRIVATE -> ProjectVisibility.PRIVATE;
                    })
                    .build());
        }
        return userProfileView;
    }

    @Override
    @Transactional
    public void saveProfile(UUID userId, UserProfile userProfile) {
        userProfileInfoRepository.save(mapUserProfileToEntity(userId, userProfile));
    }

    @Override
    @Transactional
    public void updateOnboardingWizardDisplayDate(UUID userId, Date date) {
        onboardingRepository.findById(userId)
                .ifPresentOrElse(onboardingEntity -> {
                    onboardingEntity.setProfileWizardDisplayDate(date);
                    onboardingRepository.save(onboardingEntity);
                }, () -> {
                    final OnboardingEntity onboardingEntity = OnboardingEntity.builder()
                            .id(userId)
                            .profileWizardDisplayDate(date)
                            .build();
                    onboardingRepository.save(onboardingEntity);
                });
    }

    @Override
    @Transactional
    public void updateTermsAndConditionsAcceptanceDate(UUID userId, Date date) {
        onboardingRepository.findById(userId)
                .ifPresentOrElse(onboardingEntity -> {
                    onboardingEntity.setTermsAndConditionsAcceptanceDate(date);
                    onboardingRepository.save(onboardingEntity);
                }, () -> {
                    final OnboardingEntity onboardingEntity = OnboardingEntity.builder()
                            .id(userId)
                            .termsAndConditionsAcceptanceDate(date)
                            .build();
                    onboardingRepository.save(onboardingEntity);
                });
    }

    @Override
    @Transactional
    public UUID acceptProjectLeaderInvitation(Long githubUserId, UUID projectId) {
        final var invitation = projectLeaderInvitationRepository.findByProjectIdAndGithubUserId(projectId, githubUserId)
                .orElseThrow(() -> notFound(format("Project leader invitation not found for project" +
                                                   " %s and user %d", projectId, githubUserId)));

        final var user = getUserByGithubId(githubUserId)
                .orElseThrow(() -> notFound(format("User with githubId %d not found", githubUserId)));

        projectLeaderInvitationRepository.delete(invitation);
        projectLeadRepository.save(new ProjectLeadEntity(projectId, user.getId()));
        return user.getId();
    }

    @Override
    @Transactional
    public UUID createApplicationOnProject(UUID userId, UUID projectId) {
        final var applicationId = UUID.randomUUID();
        projectRepository.findById(projectId)
                .orElseThrow(() -> notFound(format("Project with id %s not found", projectId)));
        applicationRepository.findByProjectIdAndApplicantId(projectId, userId)
                .ifPresentOrElse(applicationEntity -> {
                            throw OnlyDustException.badRequest(format("Application already exists for project %s " +
                                                                      "and user %s", projectId, userId));
                        },
                        () -> applicationRepository.save(ApplicationEntity.builder()
                                .applicantId(userId)
                                .projectId(projectId)
                                .id(applicationId)
                                .receivedAt(new Date())
                                .build())
                );
        return applicationId;
    }

    @Override
    @Transactional(readOnly = true)
    public UserRewardsPageView findRewardsForUserId(Long githubUserId, UserRewardView.Filters filters,
                                                    int pageIndex, int pageSize,
                                                    Reward.SortBy sort, SortDirection sortDirection) {

        final var format = new SimpleDateFormat("yyyy-MM-dd");
        final var fromDate = isNull(filters.getFrom()) ? null : format.format(filters.getFrom());
        final var toDate = isNull(filters.getTo()) ? null : format.format(filters.getTo());

        final var pageRequest = PageRequest.of(pageIndex, pageSize,
                RewardViewRepository.sortBy(sort, sortDirection == SortDirection.asc ? Direction.ASC : Direction.DESC));

        final var page = rewardViewRepository.findUserRewards(githubUserId, filters.getCurrencies(), filters.getProjectIds(),
                filters.getAdministratedBillingProfilesIds(), fromDate, toDate, pageRequest);

        final var rewardsStats = rewardStatsRepository.findByUser(githubUserId, filters.getCurrencies(), filters.getProjectIds(),
                filters.getAdministratedBillingProfilesIds(), fromDate, toDate);

        return UserRewardsPageView.builder()
                .rewards(Page.<UserRewardView>builder()
                        .content(page.getContent().stream().map(RewardViewEntity::toUserReward).toList())
                        .totalItemNumber((int) page.getTotalElements())
                        .totalPageNumber(page.getTotalPages())
                        .build())
                .rewardedAmount(rewardsStats.size() == 1 ?
                        new Money(rewardsStats.get(0).getProcessedAmount(),
                                rewardsStats.get(0).getCurrency().toView(),
                                rewardsStats.get(0).getProcessedUsdAmount()) :
                        new Money(null, null,
                                rewardsStats.stream().map(RewardStatsEntity::getProcessedUsdAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO,
                                        BigDecimal::add)))
                .pendingAmount(rewardsStats.size() == 1 ?
                        new Money(rewardsStats.get(0).getPendingAmount(),
                                rewardsStats.get(0).getCurrency().toView(),
                                rewardsStats.get(0).getPendingUsdAmount()) :
                        new Money(null, null,
                                rewardsStats.stream().map(RewardStatsEntity::getPendingUsdAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO,
                                        BigDecimal::add)))
                .pendingRequestCount(rewardsStats.size() == 1 ? rewardsStats.get(0).getPendingRequestCount() :
                        rewardsStats.stream().map(RewardStatsEntity::getPendingRequestCount).filter(Objects::nonNull).reduce(0, Integer::sum))
                .receivedRewardsCount(rewardsStats.stream().map(RewardStatsEntity::getRewardIds).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet()).size())
                .rewardedContributionsCount(rewardsStats.stream().map(RewardStatsEntity::getRewardItemIds).flatMap(Collection::stream).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet()).size())
                .rewardingProjectsCount(rewardsStats.stream().map(RewardStatsEntity::getProjectIds).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet()).size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RewardDetailsView findRewardById(UUID rewardId) {
        return rewardViewRepository.find(rewardId)
                .orElseThrow(() -> notFound(format("Reward with id %s not found", rewardId)))
                .toDomain();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RewardItemView> findRewardItemsPageById(UUID rewardId, int pageIndex, int pageSize) {
        final Integer count = customRewardRepository.countRewardItemsForRewardId(rewardId);
        final List<RewardItemView> rewardItemViews =
                customRewardRepository.findRewardItemsByRewardId(rewardId, pageIndex, pageSize)
                        .stream()
                        .map(RewardMapper::itemToDomain)
                        .toList();
        return Page.<RewardItemView>builder()
                .content(rewardItemViews)
                .totalItemNumber(count)
                .totalPageNumber(PaginationHelper.calculateTotalNumberOfPage(pageSize, count))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contributor> searchContributorsByLogin(Set<Long> reposIds, String login,
                                                       int maxContributorCountToReturn) {
        List<ContributorViewEntity> contributors;
        if (reposIds == null || reposIds.isEmpty()) {
            contributors = customContributorRepository.findAllContributorsByLogin(login, maxContributorCountToReturn);
        } else {
            contributors = customContributorRepository.findReposContributorsByLogin(reposIds, login, maxContributorCountToReturn);
        }

        return contributors.stream()
                .map(entity -> Contributor.builder()
                        .id(GithubUserIdentity.builder()
                                .githubUserId(entity.getGithubUserId())
                                .githubLogin(entity.getLogin())
                                .githubAvatarUrl(entity.getAvatarUrl())
                                .build())
                        .isRegistered(entity.getIsRegistered())
                        .build()).toList();
    }

    @Override
    @Transactional
    public void saveProjectLead(UUID userId, UUID projectId) {
        projectLeadRepository.save(new ProjectLeadEntity(projectId, userId));
    }

    @Override
    public List<CurrencyView> listRewardCurrencies(Long githubUserId) {
        return currencyRepository.listRewardCurrenciesByRecipient(githubUserId).stream()
                .map(CurrencyEntity::toView)
                .toList();
    }

    @Override
    public List<User> getUsersLastSeenSince(ZonedDateTime since) {
        return userRepository.findAllByLastSeenAtAfter(Date.from(since.toInstant()))
                .stream()
                .map(UserMapper::mapCreatedUserToDomain)
                .toList();
    }

    @Override
    public void saveUsers(List<User> users) {
        users.forEach(u -> userRepository.findByGithubUserId(u.getGithubUserId())
                .map(userEntity -> userEntity.toBuilder()
                        .githubLogin(u.getGithubLogin())
                        .githubAvatarUrl(u.getGithubAvatarUrl())
                        .githubEmail(u.getGithubEmail())
                        .build())
                .ifPresent(userRepository::save));
    }

    @Override
    public void saveUser(User user) {
        userRepository.findByGithubUserId(user.getGithubUserId())
                .map(userEntity -> userEntity.toBuilder()
                        .githubLogin(user.getGithubLogin())
                        .githubAvatarUrl(user.getGithubAvatarUrl())
                        .githubEmail(user.getGithubEmail())
                        .build())
                .ifPresent(userRepository::save);
    }


}
