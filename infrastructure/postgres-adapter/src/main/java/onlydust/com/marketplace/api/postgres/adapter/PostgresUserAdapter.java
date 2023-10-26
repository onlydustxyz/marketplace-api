package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import onlydust.com.marketplace.api.domain.model.UserProfile;
import onlydust.com.marketplace.api.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.api.domain.view.UserProfileView;
import onlydust.com.marketplace.api.domain.view.UserRewardTotalAmountsView;
import onlydust.com.marketplace.api.domain.view.UserRewardView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectIdsForUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserPayoutInfoValidationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.old.RegisteredUserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ApplicationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.OnboardingEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeadEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.UserPayoutInfoEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.UserMapper;
import onlydust.com.marketplace.api.postgres.adapter.mapper.UserPayoutInfoMapper;
import onlydust.com.marketplace.api.postgres.adapter.mapper.UserRewardMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;

@AllArgsConstructor
public class PostgresUserAdapter implements UserStoragePort {

    private final CustomUserRepository customUserRepository;
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
        return hasuraUser.map(u -> UserMapper.mapUserToDomain(u, settings.getTermsAndConditionsLatestVersionDate()));
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
    public UserProfileView getProfileById(Long githubUserId) {
        return customUserRepository.findProfileById(githubUserId)
                .map(this::addProjectsStats)
                .orElseThrow(() -> OnlyDustException.notFound(format("User profile %d not found", githubUserId)));
    }

    private UserProfileView addProjectsStats(UserProfileView userProfileView) {
        for (ProjectIdsForUserEntity projectIdsForUserEntity :
                customUserRepository.getProjectIdsForUserId(userProfileView.getGithubId())) {
            userProfileView.addProjectStats(UserProfileView.ProjectStats.builder()
                    .contributorCount(projectIdsForUserEntity.getContributorsCount())
                    .isProjectLead(projectIdsForUserEntity.getIsLead())
                    .totalGranted(projectIdsForUserEntity.getTotalGranted())
                    .userContributionCount(projectIdsForUserEntity.getUserContributionsCount())
                    .userLastContributedAt(projectIdsForUserEntity.getLastContributionDate())
                    .id(projectIdsForUserEntity.getId())
                    .logoUrl(projectIdsForUserEntity.getLogoUrl())
                    .name(projectIdsForUserEntity.getName())
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
        final Optional<UserPayoutInfoValidationEntity> userPayoutInfoValidationEntity =
                customUserPayoutInfoRepository.getUserPayoutInfoValidationEntity(userId);
        return userPayoutInfoEntity.map(entity -> UserPayoutInfoMapper.mapEntityToDomain(entity,
                userPayoutInfoValidationEntity.orElseGet(
                        UserPayoutInfoValidationEntity::defaultValue
                ))).orElseGet(() -> UserPayoutInformation.builder().build());
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
    public void acceptProjectLeaderInvitation(Long githubUserId, UUID projectId) {
        final var invitation = projectLeaderInvitationRepository.findByProjectIdAndGithubUserId(projectId, githubUserId)
                .orElseThrow(() -> OnlyDustException.notFound(format("Project leader invitation not found for project" +
                                                                     " %s and user %d", projectId, githubUserId)));

        final var user = getUserByGithubId(githubUserId)
                .orElseThrow(() -> OnlyDustException.notFound(format("User with githubId %d not found", githubUserId)));

        projectLeaderInvitationRepository.delete(invitation);
        projectLeadRepository.save(new ProjectLeadEntity(projectId, user.getId()));
    }

    @Override
    @Transactional
    public void createApplicationOnProject(UUID userId, UUID projectId) {
        projectIdRepository.findById(projectId)
                .orElseThrow(() -> OnlyDustException.notFound(format("Project with id %s not found", projectId)));
        applicationRepository.findByProjectIdAndApplicantId(projectId, userId)
                .ifPresentOrElse(applicationEntity -> {
                            throw OnlyDustException.invalidInput(format("Application already exists for project %s " +
                                                                        "and user %s", projectId, userId));
                        },
                        () -> applicationRepository.save(ApplicationEntity.builder()
                                .applicantId(userId)
                                .projectId(projectId)
                                .id(UUID.randomUUID())
                                .receivedAt(new Date())
                                .build())
                );
    }

    @Transactional
    @Override
    public UserPayoutInformation savePayoutInformationForUserId(UUID userId,
                                                                UserPayoutInformation userPayoutInformation) {
        final UserPayoutInfoEntity userPayoutInfoEntity = UserPayoutInfoMapper.mapDomainToEntity(userId,
                userPayoutInformation);
        userPayoutInfoRepository.findById(userId).ifPresent(entity -> walletRepository.deleteByUserId(userId));
        final UserPayoutInfoEntity saved = userPayoutInfoRepository.save(userPayoutInfoEntity);
        final Optional<UserPayoutInfoValidationEntity> userPayoutInfoValidationEntity =
                customUserPayoutInfoRepository.getUserPayoutInfoValidationEntity(userId);
        return UserPayoutInfoMapper.mapEntityToDomain(saved,
                userPayoutInfoValidationEntity.orElseGet(UserPayoutInfoValidationEntity::defaultValue));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserRewardView> findRewardsForUserId(UUID userId, int pageIndex, int pageSize,
                                                     UserRewardView.SortBy sortBy, SortDirection sortDirection) {
        final Integer count = customUserRewardRepository.getCount(userId);
        final List<UserRewardView> userRewardViews = customUserRewardRepository.getViewEntities(userId,
                        sortBy, sortDirection, pageIndex, pageSize)
                .stream().map(UserRewardMapper::mapEntityToDomain)
                .toList();
        return Page.<UserRewardView>builder()
                .content(userRewardViews)
                .totalItemNumber(count)
                .totalPageNumber(PaginationHelper.calculateTotalNumberOfPage(pageSize, count))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserRewardTotalAmountsView findRewardTotalAmountsForUserId(UUID userId) {
        return UserRewardMapper.mapTotalAmountEntitiesToDomain(customUserRewardRepository.getTotalAmountEntities(userId));
    }
}
