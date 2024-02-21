package onlydust.com.marketplace.api.postgres.adapter.configuration;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingBillingProfileStorage;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.project.domain.port.input.TechnologyStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.IndexerEventEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationEventEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.TrackingEventEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserVerificationEventEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.*;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;

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
    public ProjectStoragePort projectStoragePort(final ProjectRepository projectRepository,
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
                                                 final ContributionViewEntityRepository contributionViewEntityRepository,
                                                 final HiddenContributorRepository hiddenContributorRepository,
                                                 final ProjectTagRepository projectTagRepository) {
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
                contributionViewEntityRepository,
                hiddenContributorRepository,
                projectTagRepository
        );
    }

    @Bean
    public PostgresProjectAdapter projectRewardStoragePort(final ProjectRepository projectRepository,
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
                                                           final ContributionViewEntityRepository contributionViewEntityRepository,
                                                           final HiddenContributorRepository hiddenContributorRepository,
                                                           final ProjectTagRepository projectTagRepository) {
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
                contributionViewEntityRepository,
                hiddenContributorRepository,
                projectTagRepository
        );
    }

    @Bean
    public PostgresProjectRewardV2Adapter projectRewardStoragePortV2(final ProjectAllowanceRepository projectAllowanceRepository,
                                                                     final HistoricalQuoteRepository historicalQuoteRepository,
                                                                     final CurrencyRepository currencyRepository) {
        return new PostgresProjectRewardV2Adapter(projectAllowanceRepository, historicalQuoteRepository, currencyRepository);
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
    public PostgresRewardV2Adapter postgresRewardV2Adapter(final RewardRepository rewardRepository) {
        return new PostgresRewardV2Adapter(rewardRepository);
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
    public PostgresBackofficeAdapter postgresBackofficeAdapter(final GithubRepositoryLinkedToProjectRepository githubRepositoryLinkedToProjectRepository,
                                                               final BoSponsorRepository boSponsorRepository,
                                                               final ProjectBudgetRepository projectBudgetRepository,
                                                               final ProjectLeadInvitationRepository projectLeadInvitationRepository,
                                                               final BoUserRepository boUserRepository,
                                                               final BoPaymentRepository boPaymentRepository,
                                                               final BoProjectRepository boProjectRepository,
                                                               final BoEcosystemRepository boEcosystemRepository,
                                                               final EcosystemRepository ecosystemRepository) {
        return new PostgresBackofficeAdapter(githubRepositoryLinkedToProjectRepository, projectBudgetRepository,
                boSponsorRepository,
                projectLeadInvitationRepository, boUserRepository, boPaymentRepository, boProjectRepository, boEcosystemRepository, ecosystemRepository);
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
    public PostgresOutboxAdapter<TrackingEventEntity> trackingOutbox(final TrackingEventRepository trackingEventRepository) {
        return new PostgresOutboxAdapter<>(trackingEventRepository);
    }

    @Bean
    public PostgresOutboxAdapter<UserVerificationEventEntity> userVerificationOutbox(final UserVerificationEventRepository userVerificationEventRepository) {
        return new PostgresOutboxAdapter<>(userVerificationEventRepository);
    }

    @Bean
    public CustomProjectRankingRepository customProjectRankingRepository(final EntityManager entityManager) {
        return new CustomProjectRankingRepository(entityManager);
    }

    @Bean
    public TechnologyStoragePort technologyStoragePort(final TechnologyViewEntityRepository technologyViewEntityRepository) {
        return new PostgresTechnologyAdapter(technologyViewEntityRepository);
    }

    @Bean
    public PostgresQuoteAdapter postgresQuoteAdapter(final HistoricalQuoteRepository historicalQuoteRepository) {
        return new PostgresQuoteAdapter(historicalQuoteRepository);
    }

    @Bean
    public PostgresCurrencyAdapter postgresCurrencyAdapter(final CurrencyRepository currencyRepository) {
        return new PostgresCurrencyAdapter(currencyRepository);
    }

    @Bean
    public PostgresIsoCurrencyServiceAdapter postgresIsoCurrencyServiceAdapter(final IsoCurrencyRepository isoCurrencyRepository) {
        return new PostgresIsoCurrencyServiceAdapter(isoCurrencyRepository);
    }

    @Bean
    public PostgresAccountBookEventStorage postgresAccountBookEventStorage(final AccountBookRepository accountBookRepository) {
        return new PostgresAccountBookEventStorage(accountBookRepository);
    }

    @Bean
    public PostgresSponsorAccountStorageAdapter postgresLedgerStorageAdapter(final SponsorAccountRepository sponsorAccountRepository) {
        return new PostgresSponsorAccountStorageAdapter(sponsorAccountRepository);
    }

    @Bean
    public PostgresBillingProfileAdapter postgresBillingProfileAdapter(final UserBillingProfileTypeRepository userBillingProfileTypeRepository,
                                                                       final IndividualBillingProfileRepository individualBillingProfileRepository,
                                                                       final CompanyBillingProfileRepository companyBillingProfileRepository,
                                                                       final CustomUserRewardRepository customUserRewardRepository,
                                                                       final ChildrenKycRepository childrenKycRepository,
                                                                       final GlobalSettingsRepository globalSettingsRepository) {
        return new PostgresBillingProfileAdapter(userBillingProfileTypeRepository, companyBillingProfileRepository, individualBillingProfileRepository,
                customUserRewardRepository, childrenKycRepository, globalSettingsRepository);
    }

    @Bean
    public PostgresRewardStatusAdapter postgresRewardStatusAdapter(final RewardStatusRepository rewardStatusRepository) {
        return new PostgresRewardStatusAdapter(rewardStatusRepository);
    }

    @Bean
    PostgresRewardUsdEquivalentAdapter postgresRewardUsdEquivalentAdapter(final RewardUsdEquivalentDataRepository rewardUsdEquivalentDataRepository) {
        return new PostgresRewardUsdEquivalentAdapter(rewardUsdEquivalentDataRepository);
    }

    @Bean
    InvoiceStoragePort invoicePreviewStoragePort(final @NonNull CompanyBillingProfileRepository companyBillingProfileRepository,
                                                 final @NonNull IndividualBillingProfileRepository individualBillingProfileRepository,
                                                 final @NonNull InvoiceRewardRepository invoiceRewardRepository,
                                                 final @NonNull WalletRepository walletRepository,
                                                 final @NonNull BankAccountRepository bankAccountRepository,
                                                 final @NonNull InvoiceRepository invoiceRepository,
                                                 final @NonNull PaymentRequestRepository paymentRequestRepository) {
        return new PostgresInvoiceStorage(companyBillingProfileRepository, individualBillingProfileRepository, invoiceRewardRepository,
                walletRepository, bankAccountRepository, invoiceRepository, paymentRequestRepository);
    }

    @Bean
    public AccountingBillingProfileStorage accountingBillingProfileStorage(final CompanyBillingProfileRepository companyBillingProfileRepository,
                                                                           final IndividualBillingProfileRepository individualBillingProfileRepository,
                                                                           final GlobalSettingsRepository globalSettingsRepository) {
        return new PostgresAccountingBillingProfileStorage(companyBillingProfileRepository, individualBillingProfileRepository, globalSettingsRepository);
    }

    @Bean
    PostgresProjectAccountingObserverAdapter postgresProjectAccountingObserverAdapter(final ProjectAllowanceRepository projectAllowanceRepository,
                                                                                      final ProjectSponsorRepository projectSponsorRepository) {
        return new PostgresProjectAccountingObserverAdapter(projectAllowanceRepository, projectSponsorRepository);
    }

    @Bean
    public PostgresEcosystemAdapter postgresEcosystemAdapter(final EcosystemRepository ecosystemRepository) {
        return new PostgresEcosystemAdapter(ecosystemRepository);
    }
}
