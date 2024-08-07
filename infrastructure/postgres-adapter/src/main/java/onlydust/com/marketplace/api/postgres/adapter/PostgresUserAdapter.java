package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CurrencyEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.OnboardingEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeadEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.UserProfileInfoEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.RewardMapper;
import onlydust.com.marketplace.api.postgres.adapter.mapper.UserMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.OnboardingRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeadRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeaderInvitationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.UserProfileInfoRepository;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.kernel.model.github.GithubUserIdentity;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.project.domain.model.Contributor;
import onlydust.com.marketplace.project.domain.model.ProjectVisibility;
import onlydust.com.marketplace.project.domain.model.UserProfile;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.project.domain.view.RewardDetailsView;
import onlydust.com.marketplace.project.domain.view.RewardItemView;
import onlydust.com.marketplace.project.domain.view.UserProfileView;
import onlydust.com.marketplace.user.domain.model.NotificationRecipient;
import onlydust.com.marketplace.user.domain.port.output.AppUserStoragePort;
import org.jetbrains.annotations.NotNull;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;

import static java.lang.String.format;
import static onlydust.com.marketplace.api.postgres.adapter.mapper.UserMapper.*;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PostgresUserAdapter implements UserStoragePort, AppUserStoragePort {

    private final CustomUserRepository customUserRepository;
    private final CustomContributorRepository customContributorRepository;
    private final UserRepository userRepository;
    private final UserViewRepository userViewRepository;
    private final AllUserViewRepository allUserViewRepository;
    private final OnboardingRepository onboardingRepository;
    private final ProjectLeaderInvitationRepository projectLeaderInvitationRepository;
    private final ProjectLeadRepository projectLeadRepository;
    private final UserProfileInfoRepository userProfileInfoRepository;
    private final CustomRewardRepository customRewardRepository;
    private final ProjectLedIdRepository projectLedIdRepository;
    private final RewardViewRepository rewardViewRepository;
    private final CurrencyRepository currencyRepository;
    private final BillingProfileUserRepository billingProfileUserRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthenticatedUser> getRegisteredUserByGithubId(Long githubId) {
        return userViewRepository.findByGithubUserId(githubId).map(this::getUserDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GithubUserIdentity> getIndexedUserByGithubId(Long githubId) {
        return allUserViewRepository.findByGithubUserId(githubId).map(AllUserViewEntity::toGithubIdentity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthenticatedUser> getRegisteredUserById(UUID userId) {
        return userViewRepository.findById(userId).map(this::getUserDetails);
    }

    private AuthenticatedUser getUserDetails(@NotNull UserViewEntity user) {
        final var projectLedIdsByUserId = projectLedIdRepository.findProjectLedIdsByUserId(user.id()).stream()
                .sorted(Comparator.comparing(ProjectLedIdQueryEntity::getProjectSlug))
                .toList();
        final var billingProfiles = billingProfileUserRepository.findByUserId(user.id());
        return mapUserToDomain(user, projectLedIdsByUserId, billingProfiles);
    }

    @Override
    @Transactional
    public AuthenticatedUser createUser(AuthenticatedUser user) {
        return mapCreatedUserToDomain(tryCreateUser(user));
    }

    private UserEntity tryCreateUser(AuthenticatedUser user) {
        userRepository.tryInsert(mapUserToEntity(user));
        userRepository.flush();
        return userRepository.findByGithubUserId(user.githubUserId()).orElseThrow();
    }

    @Override
    public void updateUserLastSeenAt(UUID userId, Date lastSeenAt) {
        userRepository.findById(userId)
                .ifPresentOrElse(userEntity -> {
                    userEntity.setLastSeenAt(lastSeenAt);
                    userRepository.saveAndFlush(userEntity);
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
    public Optional<UserProfile> findProfileById(UUID userId) {
        return userProfileInfoRepository.findById(userId).map(UserProfileInfoEntity::toDomain);
    }

    private UserProfileView addProjectsStats(UserProfileView userProfileView) {
        final var projectsStats = customUserRepository.getProjectsStatsForUser(userProfileView.getGithubId());
        for (ProjectStatsForUserQueryEntity stats : projectsStats) {
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
        final UserProfileInfoEntity userProfileInfoEntity = userProfileInfoRepository.findById(userId)
                .orElse(new UserProfileInfoEntity());
        userProfileInfoRepository.saveAndFlush(userProfileInfoEntity.update(userId, userProfile));
    }

    @Override
    @Transactional
    public void updateOnboardingCompletionDate(UUID userId, Date date) {
        onboardingRepository.findById(userId)
                .ifPresentOrElse(onboardingEntity -> {
                    onboardingEntity.setCompletionDate(date);
                    onboardingRepository.saveAndFlush(onboardingEntity);
                }, () -> {
                    final OnboardingEntity onboardingEntity = OnboardingEntity.builder()
                            .userId(userId)
                            .completionDate(date)
                            .build();
                    onboardingRepository.saveAndFlush(onboardingEntity);
                });
    }

    @Override
    @Transactional
    public void updateTermsAndConditionsAcceptanceDate(UUID userId, Date date) {
        onboardingRepository.findById(userId)
                .ifPresentOrElse(onboardingEntity -> {
                    onboardingEntity.setTermsAndConditionsAcceptanceDate(date);
                    onboardingRepository.saveAndFlush(onboardingEntity);
                }, () -> {
                    final OnboardingEntity onboardingEntity = OnboardingEntity.builder()
                            .userId(userId)
                            .termsAndConditionsAcceptanceDate(date)
                            .build();
                    onboardingRepository.saveAndFlush(onboardingEntity);
                });
    }

    @Override
    @Transactional
    public UUID acceptProjectLeaderInvitation(Long githubUserId, UUID projectId) {
        final var invitation = projectLeaderInvitationRepository.findByProjectIdAndGithubUserId(projectId, githubUserId)
                .orElseThrow(() -> notFound(format("Project leader invitation not found for project" +
                                                   " %s and user %d", projectId, githubUserId)));

        final var user = getRegisteredUserByGithubId(githubUserId)
                .orElseThrow(() -> notFound(format("User with githubId %d not found", githubUserId)));

        projectLeaderInvitationRepository.delete(invitation);
        projectLeadRepository.saveAndFlush(new ProjectLeadEntity(projectId, user.id()));
        return user.id();
    }

    @Override
    @Transactional(readOnly = true)
    public RewardDetailsView findRewardById(UUID rewardId) {
        return rewardViewRepository.findById(rewardId)
                .orElseThrow(() -> notFound(format("Reward with id %s not found", rewardId)))
                .toView();
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
        List<ContributorQueryEntity> contributors;
        if (reposIds == null || reposIds.isEmpty()) {
            contributors = customContributorRepository.findAllContributorsByLogin(login, maxContributorCountToReturn);
        } else {
            contributors = customContributorRepository.findReposContributorsByLogin(reposIds, login, maxContributorCountToReturn);
        }

        return contributors.stream()
                .map(entity -> Contributor.builder()
                        .id(GithubUserIdentity.builder()
                                .githubUserId(entity.getGithubUserId())
                                .login(entity.getLogin())
                                .avatarUrl(entity.getAvatarUrl())
                                .build())
                        .isRegistered(entity.getIsRegistered())
                        .build()).toList();
    }

    @Override
    @Transactional
    public void saveProjectLead(UUID userId, UUID projectId) {
        projectLeadRepository.saveAndFlush(new ProjectLeadEntity(projectId, userId));
    }

    @Override
    public List<CurrencyView> listRewardCurrencies(Long githubUserId, List<UUID> administratedBillingProfileIds) {
        return currencyRepository.listUserRewardCurrencies(githubUserId, administratedBillingProfileIds).stream()
                .map(CurrencyEntity::toView)
                .toList();
    }

    @Override
    public List<AuthenticatedUser> getUsersLastSeenSince(ZonedDateTime since) {
        return userRepository.findAllByLastSeenAtAfter(Date.from(since.toInstant()))
                .stream()
                .map(UserMapper::mapCreatedUserToDomain)
                .toList();
    }

    @Override
    public void saveUsers(List<GithubUserIdentity> users) {
        users.forEach(u -> userRepository.findByGithubUserId(u.githubUserId())
                .map(userEntity -> userEntity.toBuilder()
                        .githubLogin(u.login())
                        .githubAvatarUrl(u.avatarUrl())
                        .email(u.email())
                        .build())
                .ifPresent(userRepository::save));
    }

    @Override
    public void saveUser(GithubUserIdentity user) {
        userRepository.findByGithubUserId(user.githubUserId())
                .map(userEntity -> userEntity.toBuilder()
                        .githubLogin(user.login())
                        .githubAvatarUrl(user.avatarUrl())
                        .email(user.email())
                        .build())
                .ifPresent(userRepository::save);
    }

    @Override
    @Transactional
    public void refreshUserRanksAndStats() {
        userRepository.refreshUsersRanksAndStats();
    }

    @Override
    @Transactional
    public void historizeUserRanks() {
        userRepository.historizeGlobalUsersRanks(new Date());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> getGithubUserId(UUID userId) {
        return userRepository.findById(userId).map(UserEntity::getGithubUserId);
    }

    @Override
    @Transactional
    public void replaceUser(UUID userId, Long currentGithubUserId, Long newGithubUserId, String githubLogin, String githubAvatarUrl) {
        userRepository.replaceUserByGithubUser(userId, currentGithubUserId, newGithubUserId, githubLogin, githubAvatarUrl);
    }

    @Override
    public Optional<NotificationRecipient> findById(NotificationRecipient.Id userId) {
        return userRepository.findById(userId.value()).map(UserEntity::toNotificationRecipient);
    }
}
