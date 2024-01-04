package onlydust.com.marketplace.api.postgres.adapter.configuration;

import javax.persistence.EntityManager;
import onlydust.com.marketplace.api.domain.port.input.TechnologyStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.PostgresBackofficeAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresContributionAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresEventStorageAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresGithubAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresOutboxAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresProjectAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresRewardAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresTechnologyAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresUserAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.IndexerEventEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationEventEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.BudgetStatsRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ChurnedContributorViewEntityRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ContributionDetailsViewEntityRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ContributionRewardViewEntityRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ContributionViewEntityRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ContributorActivityViewEntityRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomContributorRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomIgnoredContributionsRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomProjectBudgetRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomProjectRankingRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomProjectRewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomRewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomUserPayoutInfoRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomUserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomUserRewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.GithubAppInstallationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.GithubRepoViewEntityRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.GlobalSettingsRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.IgnoredContributionsRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.IndexerEventRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.NewcomerViewEntityRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.NotificationEventRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectLeadViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectLedIdRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectsPageFiltersRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectsPageRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardStatsRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardableItemRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ShortProjectViewEntityRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.TechnologyViewEntityRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BoPaymentRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BoProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BoSponsorRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BoUserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.GithubRepositoryLinkedToProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.ProjectBudgetRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.ProjectLeadInvitationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ApplicationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.EventRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.OnboardingRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectIdRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeadRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeaderInvitationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectRepoRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.UserPayoutInfoRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.UserProfileInfoRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.WalletRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = {
    "onlydust.com.marketplace.api.postgres.adapter.entity"
})
@EnableJpaRepositories(basePackages = {
    "onlydust.com.marketplace.api.postgres.adapter.repository"
})
@EnableTransactionManagement
@EnableJpaAuditing
public class PostgresConfiguration {

  @Bean
  public CustomProjectRepository customProjectRepository(final EntityManager entityManager) {
    return new CustomProjectRepository(entityManager);
  }

  @Bean
  public CustomContributorRepository customContributorRepository(final EntityManager entityManager) {
    return new CustomContributorRepository(entityManager);
  }

  @Bean
  public PostgresProjectAdapter postgresProjectAdapter(final ProjectRepository projectRepository,
      final ProjectViewRepository projectViewRepository,
      final ProjectIdRepository projectIdRepository,
      final ProjectLeaderInvitationRepository projectLeaderInvitationRepository,
      final ProjectRepoRepository projectRepoRepository,
      final CustomProjectRepository customProjectRepository,
      final CustomContributorRepository customContributorRepository,
      final CustomProjectRewardRepository customProjectRewardRepository,
      final CustomProjectBudgetRepository customProjectBudgetRepository,
      final ProjectLeadViewRepository projectLeadViewRepository,
      final CustomRewardRepository customRewardRepository,
      final ProjectsPageRepository projectsPageRepository,
      final ProjectsPageFiltersRepository projectsPageFiltersRepository,
      final RewardableItemRepository rewardableItemRepository,
      final CustomProjectRankingRepository customProjectRankingRepository,
      final BudgetStatsRepository budgetStatsRepository,
      final ChurnedContributorViewEntityRepository churnedContributorViewEntityRepository,
      final NewcomerViewEntityRepository newcomerViewEntityRepository,
      final ContributorActivityViewEntityRepository contributorActivityViewEntityRepository,
      final ApplicationRepository applicationRepository,
      final ContributionViewEntityRepository contributionViewEntityRepository) {
    return new PostgresProjectAdapter(
        projectRepository,
        projectViewRepository,
        projectIdRepository,
        projectLeaderInvitationRepository,
        projectRepoRepository,
        customProjectRepository,
        customContributorRepository,
        customProjectRewardRepository,
        customProjectBudgetRepository,
        projectLeadViewRepository,
        customRewardRepository,
        projectsPageRepository,
        projectsPageFiltersRepository,
        rewardableItemRepository,
        customProjectRankingRepository,
        budgetStatsRepository,
        churnedContributorViewEntityRepository,
        newcomerViewEntityRepository,
        contributorActivityViewEntityRepository,
        applicationRepository,
        contributionViewEntityRepository
    );
  }

  @Bean
  public PostgresGithubAdapter postgresGithubAdapter(final GithubAppInstallationRepository githubAppInstallationRepository,
      final GithubRepoViewEntityRepository githubRepoViewEntityRepository) {
    return new PostgresGithubAdapter(githubAppInstallationRepository, githubRepoViewEntityRepository);
  }

  @Bean
  public CustomUserRepository customUserRepository(final EntityManager entityManager) {
    return new CustomUserRepository(entityManager);
  }

  @Bean
  public PostgresUserAdapter postgresUserAdapter(final CustomUserRepository customUserRepository,
      final CustomContributorRepository customContributorRepository,
      final UserRepository userRepository,
      final UserViewRepository userViewRepository,
      final GlobalSettingsRepository globalSettingsRepository,
      final UserPayoutInfoRepository userPayoutInfoRepository,
      final OnboardingRepository onboardingRepository,
      final ProjectLeaderInvitationRepository projectLeaderInvitationRepository,
      final ProjectLeadRepository projectLeadRepository,
      final ApplicationRepository applicationRepository,
      final ProjectIdRepository projectIdRepository,
      final UserProfileInfoRepository userProfileInfoRepository,
      final CustomUserRewardRepository customUserRewardRepository,
      final WalletRepository walletRepository,
      final CustomUserPayoutInfoRepository customUserPayoutInfoRepository,
      final CustomRewardRepository customRewardRepository,
      final ProjectLedIdRepository projectLedIdRepository,
      final RewardStatsRepository rewardStatsRepository) {
    return new PostgresUserAdapter(
        customUserRepository,
        customContributorRepository,
        userRepository,
        userViewRepository,
        globalSettingsRepository,
        userPayoutInfoRepository,
        onboardingRepository,
        projectLeaderInvitationRepository,
        projectLeadRepository,
        applicationRepository,
        projectIdRepository,
        userProfileInfoRepository,
        customUserRewardRepository,
        walletRepository,
        customUserPayoutInfoRepository,
        customRewardRepository,
        projectLedIdRepository,
        rewardStatsRepository);
  }

  @Bean
  public CustomProjectRewardRepository customProjectRewardRepository(final EntityManager entityManager) {
    return new CustomProjectRewardRepository(entityManager);
  }

  @Bean
  public CustomProjectBudgetRepository customProjectBudgetRepository(final EntityManager entityManager) {
    return new CustomProjectBudgetRepository(entityManager);
  }

  @Bean
  public CustomUserRewardRepository customUserRewardRepository(final EntityManager entityManager) {
    return new CustomUserRewardRepository(entityManager);
  }

  @Bean
  public CustomUserPayoutInfoRepository customUserPayoutInfoRepository(final EntityManager entityManager) {
    return new CustomUserPayoutInfoRepository(entityManager);
  }

  @Bean
  public PostgresContributionAdapter postgresContributionAdapter(final ContributionViewEntityRepository contributionViewEntityRepository,
      final ShortProjectViewEntityRepository shortProjectViewEntityRepository,
      final GithubRepoViewEntityRepository githubRepoViewEntityRepository,
      final ContributionDetailsViewEntityRepository contributionDetailsViewEntityRepository,
      final ContributionRewardViewEntityRepository contributionRewardViewEntityRepository,
      final CustomContributorRepository customContributorRepository,
      final CustomIgnoredContributionsRepository customIgnoredContributionsRepository,
      final IgnoredContributionsRepository ignoredContributionsRepository,
      final ProjectRepository projectRepository) {
    return new PostgresContributionAdapter(contributionViewEntityRepository, shortProjectViewEntityRepository,
        githubRepoViewEntityRepository, contributionDetailsViewEntityRepository,
        contributionRewardViewEntityRepository, customContributorRepository,
        customIgnoredContributionsRepository, ignoredContributionsRepository, projectRepository);
  }

  @Bean
  public PostgresRewardAdapter postgresRewardAdapter(final ShortProjectViewEntityRepository shortProjectViewEntityRepository) {
    return new PostgresRewardAdapter(shortProjectViewEntityRepository);
  }

  @Bean
  public CustomRewardRepository customRewardRepository(final EntityManager entityManager) {
    return new CustomRewardRepository(entityManager);
  }

  @Bean
  public PostgresEventStorageAdapter postgresEventStorageAdapter(final EventRepository eventRepository) {
    return new PostgresEventStorageAdapter(eventRepository);
  }

  @Bean
  public PostgresBackofficeAdapter postgresBackofficeAdapter(
      final GithubRepositoryLinkedToProjectRepository githubRepositoryLinkedToProjectRepository,
      final BoSponsorRepository boSponsorRepository,
      final ProjectBudgetRepository projectBudgetRepository,
      final ProjectLeadInvitationRepository projectLeadInvitationRepository,
      final BoUserRepository boUserRepository,
      final BoPaymentRepository boPaymentRepository,
      final BoProjectRepository boProjectRepository) {
    return new PostgresBackofficeAdapter(githubRepositoryLinkedToProjectRepository, projectBudgetRepository,
        boSponsorRepository,
        projectLeadInvitationRepository, boUserRepository, boPaymentRepository, boProjectRepository);
  }

  @Bean
  public PostgresOutboxAdapter<NotificationEventEntity> notificationOutbox(final NotificationEventRepository notificationEventRepository) {
    return new PostgresOutboxAdapter<>(notificationEventRepository);
  }

  @Bean
  public PostgresOutboxAdapter<IndexerEventEntity> indexerOutbox(final IndexerEventRepository indexerEventRepository) {
    return new PostgresOutboxAdapter<>(indexerEventRepository);
  }

  @Bean
  public CustomProjectRankingRepository customProjectRankingRepository(final EntityManager entityManager) {
    return new CustomProjectRankingRepository(entityManager);
  }

  @Bean
  public TechnologyStoragePort technologyStoragePort(final TechnologyViewEntityRepository technologyViewEntityRepository) {
    return new PostgresTechnologyAdapter(technologyViewEntityRepository);
  }
}
