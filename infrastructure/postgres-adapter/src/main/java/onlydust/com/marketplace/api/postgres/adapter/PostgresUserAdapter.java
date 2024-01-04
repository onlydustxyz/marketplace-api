package onlydust.com.marketplace.api.postgres.adapter;

import static java.lang.String.format;
import static java.util.Objects.isNull;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.Contributor;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.api.domain.model.ProjectVisibility;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import onlydust.com.marketplace.api.domain.model.UserProfile;
import onlydust.com.marketplace.api.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.api.domain.view.Money;
import onlydust.com.marketplace.api.domain.view.RewardItemView;
import onlydust.com.marketplace.api.domain.view.RewardView;
import onlydust.com.marketplace.api.domain.view.UserProfileView;
import onlydust.com.marketplace.api.domain.view.UserRewardTotalAmountsView;
import onlydust.com.marketplace.api.domain.view.UserRewardView;
import onlydust.com.marketplace.api.domain.view.UserRewardsPageView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLedIdViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectStatsForUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardStatsEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserPayoutInfoValidationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ApplicationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.OnboardingEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeadEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.UserPayoutInfoEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.RewardMapper;
import onlydust.com.marketplace.api.postgres.adapter.mapper.UserMapper;
import onlydust.com.marketplace.api.postgres.adapter.mapper.UserPayoutInfoMapper;
import onlydust.com.marketplace.api.postgres.adapter.mapper.UserRewardMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomContributorRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomRewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomUserPayoutInfoRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomUserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomUserRewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.GlobalSettingsRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectLedIdRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardStatsRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ApplicationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.OnboardingRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectIdRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeadRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeaderInvitationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.UserPayoutInfoRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.UserProfileInfoRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.WalletRepository;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
public class PostgresUserAdapter implements UserStoragePort {

  private final CustomUserRepository customUserRepository;
  private final CustomContributorRepository customContributorRepository;
  private final UserRepository userRepository;
  private final UserViewRepository userViewRepository;
  private final GlobalSettingsRepository globalSettingsRepository;
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
    return user.map(u -> {
      final var projectLedIdsByUserId = projectLedIdRepository.findProjectLedIdsByUserId(u.getId()).stream()
          .sorted(Comparator.comparing(ProjectLedIdViewEntity::getProjectSlug))
          .toList();
      final var applications = applicationRepository.findAllByApplicantId(u.getId());
      return UserMapper.mapUserToDomain(u, settings.getTermsAndConditionsLatestVersionDate(),
          projectLedIdsByUserId, applications);
    });
  }

  @Override
  @Transactional
  public void createUser(User user) {
    userRepository.save(UserMapper.mapUserToEntity(user));
  }

  @Override
  public void updateUserIdentity(UUID userId, String githubLogin, String githubAvatarUrl, String emailFromGithub,
      Date lastSeenAt) {
    userRepository.findById(userId)
        .ifPresentOrElse(userEntity -> {
          if (githubLogin != null) {
            userEntity.setGithubLogin(githubLogin);
          }
          if (githubAvatarUrl != null) {
            userEntity.setGithubAvatarUrl(githubAvatarUrl);
          }
          if (emailFromGithub != null) {
            userEntity.setGithubEmail(emailFromGithub);
          }
          userEntity.setLastSeenAt(lastSeenAt);
          userRepository.save(userEntity);
        }, () -> {
          throw OnlyDustException.notFound(format("User with id %s not found", userId));
        });
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
  public UserRewardsPageView findRewardsForUserId(UUID userId, UserRewardView.Filters filters,
      int pageIndex, int pageSize,
      UserRewardView.SortBy sortBy, SortDirection sortDirection) {

    final var format = new SimpleDateFormat("yyyy-MM-dd");
    final var fromDate = isNull(filters.getFrom()) ? null : format.format(filters.getFrom());
    final var toDate = isNull(filters.getTo()) ? null : format.format(filters.getTo());
    final var currencies =
        filters.getCurrencies().stream().map(CurrencyEnumEntity::of).map(CurrencyEnumEntity::toString).toList();

    final var count = customUserRewardRepository.getCount(userId, currencies, filters.getProjectIds(), fromDate,
        toDate);
    final var userRewardViews = customUserRewardRepository.getViewEntities(userId,
            currencies, filters.getProjectIds(), fromDate, toDate,
            sortBy, sortDirection, pageIndex, pageSize)
        .stream().map(UserRewardMapper::mapEntityToDomain)
        .toList();
    final var rewardsStats = rewardStatsRepository.findByUser(userId, currencies, filters.getProjectIds(),
        fromDate, toDate);

    return UserRewardsPageView.builder()
        .rewards(Page.<UserRewardView>builder()
            .content(userRewardViews)
            .totalItemNumber(count)
            .totalPageNumber(PaginationHelper.calculateTotalNumberOfPage(pageSize, count))
            .build())
        .rewardedAmount(rewardsStats.size() == 1 ?
            new Money(rewardsStats.get(0).getProcessedAmount(),
                rewardsStats.get(0).getCurrency().toDomain(),
                rewardsStats.get(0).getProcessedUsdAmount()) :
            new Money(null, null,
                rewardsStats.stream().map(RewardStatsEntity::getProcessedUsdAmount).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)))
        .pendingAmount(rewardsStats.size() == 1 ?
            new Money(rewardsStats.get(0).getPendingAmount(),
                rewardsStats.get(0).getCurrency().toDomain(),
                rewardsStats.get(0).getPendingUsdAmount()) :
            new Money(null, null,
                rewardsStats.stream().map(RewardStatsEntity::getPendingUsdAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add)))
        .receivedRewardsCount(
            rewardsStats.stream().map(RewardStatsEntity::getRewardIds).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet()).size())
        .rewardedContributionsCount(
            rewardsStats.stream().map(RewardStatsEntity::getRewardItemIds).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet()).size())
        .rewardingProjectsCount(
            rewardsStats.stream().map(RewardStatsEntity::getProjectIds).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet()).size())
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

  @Override
  public List<Currency> listRewardCurrencies(Long githubUserId) {
    return rewardStatsRepository.listRewardCurrenciesByRecipient(githubUserId).stream()
        .map(CurrencyEnumEntity::toDomain)
        .toList();
  }
}
