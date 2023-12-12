package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.model.*;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLedIdViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectStatsForUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserPayoutInfoValidationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.old.RegisteredUserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ApplicationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.OnboardingEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeadEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.UserPayoutInfoEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.RewardMapper;
import onlydust.com.marketplace.api.postgres.adapter.mapper.UserMapper;
import onlydust.com.marketplace.api.postgres.adapter.mapper.UserPayoutInfoMapper;
import onlydust.com.marketplace.api.postgres.adapter.mapper.UserRewardMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.lang.String.format;

@AllArgsConstructor
public class PostgresUserAdapter implements UserStoragePort {

    private final CustomUserRepository customUserRepository;
    private final CustomContributorRepository customContributorRepository;
    private final UserRepository userRepository;
    private final UserViewRepository userViewRepository;
    private final GlobalSettingsRepository globalSettingsRepository;
    private final RegisteredUserRepository registeredUserRepository;
    private final UserPayoutInfoRepository userPayoutInfoRepository;
    private final OnboardingRepository onboardingRepository;
    private final ProjectLeaderInvitationRepository projectLeaderInvitationRepository;
    private final ProjectLeadRepository projectLeadRepository;
    private final ApplicationRepository applicationRepository;
    private final ProjectIdRepository projectIdRepository;
    private final UserProfileInfoRepository userProfileInfoRepository;
    private final CustomUserRewardRepository customUserRewardRepository;
    private final WalletRepository walletRepository;
    private final CustomUserPayoutInfoRepository customUserPayoutInfoRepository;
    private final CustomRewardRepository customRewardRepository;
    private final ProjectLedIdRepository projectLedIdRepository;
    private final RewardStatsRepository rewardStatsRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserByGithubId(Long githubId) {
        final var settings =
                globalSettingsRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> OnlyDustException.internalServerError("No global settings found", null));
        Optional<UserViewEntity> user = userViewRepository.findByGithubUserId(githubId);
        if (user.isPresent()) {
            return user.map(u -> UserMapper.mapUserToDomain(u, settings.getTermsAndConditionsLatestVersionDate()));
        }
        // Fallback on hasura auth user
        Optional<RegisteredUserViewEntity> hasuraUser = registeredUserRepository.findByGithubId(githubId);
        return hasuraUser.map(u -> {
            final List<ProjectLedIdViewEntity> projectLedIdsByUserId =
                    projectLedIdRepository.findProjectLedIdsByUserId(u.getId());
            return UserMapper.mapUserToDomain(u, settings.getTermsAndConditionsLatestVersionDate(),
                    projectLedIdsByUserId);
        });
    }

    @Override
    @Transactional
    public void createUser(User user) {
        userRepository.save(UserMapper.mapUserToEntity(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileView getProfileById(UUID userId) {
        return customUserRepository.findProfileById(userId)
                .map(this::addProjectsStats)
                .orElseThrow(() -> OnlyDustException.notFound(format("User profile %s not found", userId)));
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileView getProfileById(Long githubUserId) {
        return customUserRepository.findProfileById(githubUserId)
                .map(this::addProjectsStats)
                .orElseThrow(() -> OnlyDustException.notFound(format("User profile %d not found", githubUserId)));
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileView getProfileByLogin(String githubLogin) {
        return customUserRepository.findProfileByLogin(githubLogin)
                .map(this::addProjectsStats)
                .orElseThrow(() -> OnlyDustException.notFound(format("User profile %s not found", githubLogin)));
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
        userProfileInfoRepository.save(UserMapper.mapUserProfileToEntity(userId, userProfile));
    }

    @Override
    @Transactional(readOnly = true)
    public UserPayoutInformation getPayoutInformationById(UUID userId) {
        final Optional<UserPayoutInfoEntity> userPayoutInfoEntity = userPayoutInfoRepository.findByUserId(userId);
        final UserPayoutInfoValidationEntity userPayoutInfoValidationEntity =
                customUserPayoutInfoRepository.getUserPayoutInfoValidationEntity(userId);
        return UserPayoutInfoMapper.mapEntityToDomain(userPayoutInfoEntity.orElseGet(UserPayoutInfoEntity::new),
                userPayoutInfoValidationEntity);
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
                .orElseThrow(() -> OnlyDustException.notFound(format("Project leader invitation not found for project" +
                                                                     " %s and user %d", projectId, githubUserId)));

        final var user = getUserByGithubId(githubUserId)
                .orElseThrow(() -> OnlyDustException.notFound(format("User with githubId %d not found", githubUserId)));

        projectLeaderInvitationRepository.delete(invitation);
        projectLeadRepository.save(new ProjectLeadEntity(projectId, user.getId()));
        return user.getId();
    }

    @Override
    @Transactional
    public UUID createApplicationOnProject(UUID userId, UUID projectId) {
        final var applicationId = UUID.randomUUID();
        projectIdRepository.findById(projectId)
                .orElseThrow(() -> OnlyDustException.notFound(format("Project with id %s not found", projectId)));
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

    @Transactional
    @Override
    public UserPayoutInformation savePayoutInformationForUserId(UUID userId,
                                                                UserPayoutInformation userPayoutInformation) {
        final UserPayoutInfoEntity userPayoutInfoEntity = UserPayoutInfoMapper.mapDomainToEntity(userId,
                userPayoutInformation);
        userPayoutInfoRepository.findById(userId).ifPresent(entity -> walletRepository.deleteByUserId(userId));
        final UserPayoutInfoEntity saved = userPayoutInfoRepository.save(userPayoutInfoEntity);
        final UserPayoutInfoValidationEntity userPayoutInfoValidationEntity =
                customUserPayoutInfoRepository.getUserPayoutInfoValidationEntity(userId);
        return UserPayoutInfoMapper.mapEntityToDomain(saved, userPayoutInfoValidationEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public UserRewardsPageView findRewardsForUserId(UUID userId, int pageIndex, int pageSize,
                                                    UserRewardView.SortBy sortBy, SortDirection sortDirection) {
        final var count = customUserRewardRepository.getCount(userId);
        final var userRewardViews = customUserRewardRepository.getViewEntities(userId,
                        sortBy, sortDirection, pageIndex, pageSize)
                .stream().map(UserRewardMapper::mapEntityToDomain)
                .toList();
        final var rewardsStats = rewardStatsRepository.findByUser(userId);

        return UserRewardsPageView.builder()
                .rewards(Page.<UserRewardView>builder()
                        .content(userRewardViews)
                        .totalItemNumber(count)
                        .totalPageNumber(PaginationHelper.calculateTotalNumberOfPage(pageSize, count))
                        .build())
                .rewardedAmount(new Money(rewardsStats.getProcessedAmount(), Currency.Usd,
                        rewardsStats.getProcessedUsdAmount()))
                .pendingAmount(new Money(rewardsStats.getPendingAmount(), Currency.Usd,
                        rewardsStats.getPendingUsdAmount()))
                .receivedRewardsCount(rewardsStats.getRewardsCount())
                .rewardedContributionsCount(rewardsStats.getRewardItemsCount())
                .rewardingProjectsCount(rewardsStats.getProjectsCount())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserRewardTotalAmountsView findRewardTotalAmountsForUserId(UUID userId) {
        return UserRewardMapper.mapTotalAmountEntitiesToDomain(customUserRewardRepository.getTotalAmountEntities(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public RewardView findRewardById(UUID rewardId) {
        return RewardMapper.rewardWithReceiptToDomain(customRewardRepository.findUserRewardViewEntityByd(rewardId));
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
    public List<UserRewardView> findPendingInvoiceRewardsForRecipientId(Long githubUserId) {
        return customUserRewardRepository.getPendingInvoicesViewEntities(githubUserId)
                .stream().map(UserRewardMapper::mapEntityToDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contributor> searchContributorsByLogin(Set<Long> reposIds, String login,
                                                       int maxContributorCountToReturn) {
        return customContributorRepository.findReposContributorsByLogin(reposIds, login, maxContributorCountToReturn)
                .stream()
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
}
