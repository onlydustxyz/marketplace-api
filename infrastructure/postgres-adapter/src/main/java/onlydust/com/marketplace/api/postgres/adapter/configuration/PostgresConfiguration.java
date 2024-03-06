package onlydust.com.marketplace.api.postgres.adapter.configuration;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileVerificationEventEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.IndexerEventEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationEventEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.TrackingEventEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.*;
import onlydust.com.marketplace.project.domain.port.input.TechnologyStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
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
                                                 final ProjectTagRepository projectTagRepository,
                                                 final HistoricalQuoteRepository historicalQuoteRepository,
                                                 final CurrencyRepository currencyRepository,
                                                 final RewardViewRepository rewardViewRepository) {
        return new PostgresProjectAdapter(
                projectRepository,
                projectViewRepository,
                projectIdRepository,
                projectLeaderInvitationRepository,
                projectRepoRepository,
                customProjectRepository,
                customContributorRepository,
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
                projectTagRepository,
                historicalQuoteRepository,
                currencyRepository,
                rewardViewRepository
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
                                                           final ProjectTagRepository projectTagRepository,
                                                           final HistoricalQuoteRepository historicalQuoteRepository,
                                                           final CurrencyRepository currencyRepository,
                                                           final RewardViewRepository rewardViewRepository) {
        return new PostgresProjectAdapter(
                projectRepository,
                projectViewRepository,
                projectIdRepository,
                projectLeaderInvitationRepository,
                projectRepoRepository,
                customProjectRepository,
                customContributorRepository,
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
                projectTagRepository,
                historicalQuoteRepository,
                currencyRepository,
                rewardViewRepository
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
                                                   final OnboardingRepository onboardingRepository,
                                                   final ProjectLeaderInvitationRepository projectLeaderInvitationRepository,
                                                   final ProjectLeadRepository projectLeadRepository,
                                                   final ApplicationRepository applicationRepository,
                                                   final ProjectIdRepository projectIdRepository,
                                                   final UserProfileInfoRepository userProfileInfoRepository,
                                                   final CustomRewardRepository customRewardRepository,
                                                   final ProjectLedIdRepository projectLedIdRepository,
                                                   final RewardStatsRepository rewardStatsRepository,
                                                   final RewardViewRepository rewardViewRepository,
                                                   final CurrencyRepository currencyRepository) {
        return new PostgresUserAdapter(
                customUserRepository,
                customContributorRepository,
                userRepository,
                userViewRepository,
                globalSettingsRepository,
                onboardingRepository,
                projectLeaderInvitationRepository,
                projectLeadRepository,
                applicationRepository,
                projectIdRepository,
                userProfileInfoRepository,
                customRewardRepository,
                projectLedIdRepository,
                rewardStatsRepository,
                rewardViewRepository,
                currencyRepository);
    }

    @Bean
    public CustomProjectBudgetRepository customProjectBudgetRepository(final EntityManager entityManager) {
        return new CustomProjectBudgetRepository(entityManager);
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
    public PostgresRewardAdapter postgresRewardAdapter(final ShortProjectViewEntityRepository shortProjectViewEntityRepository,
                                                       final InvoiceRewardViewRepository invoiceRewardViewRepository,
                                                       final PayableRewardWithPayoutInfoViewRepository payableRewardWithPayoutInfoViewRepository,
                                                       final BatchPaymentRepository batchPaymentRepository,
                                                       final BatchPaymentDetailsViewRepository batchPaymentDetailsViewRepository,
                                                       final RewardViewRepository rewardViewRepository) {
        return new PostgresRewardAdapter(shortProjectViewEntityRepository, invoiceRewardViewRepository, payableRewardWithPayoutInfoViewRepository,
                batchPaymentRepository, batchPaymentDetailsViewRepository, rewardViewRepository);
    }

    @Bean
    public PostgresRewardV2Adapter postgresRewardV2Adapter(final RewardRepository rewardRepository,
                                                           final CurrencyStorage currencyStorage) {
        return new PostgresRewardV2Adapter(rewardRepository, currencyStorage);
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
    public PostgresOutboxAdapter<BillingProfileVerificationEventEntity> billingProfileVerificationOutbox(final BillingProfileVerificationEventRepository billingProfileVerificationEventRepository) {
        return new PostgresOutboxAdapter<>(billingProfileVerificationEventRepository);
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
    public PostgresRewardStatusAdapter postgresRewardStatusAdapter(final RewardStatusRepository rewardStatusRepository) {
        return new PostgresRewardStatusAdapter(rewardStatusRepository);
    }

    @Bean
    PostgresRewardUsdEquivalentAdapter postgresRewardUsdEquivalentAdapter(final RewardUsdEquivalentDataRepository rewardUsdEquivalentDataRepository) {
        return new PostgresRewardUsdEquivalentAdapter(rewardUsdEquivalentDataRepository);
    }

    @Bean
    InvoiceStoragePort invoicePreviewStoragePort(
            final @NonNull BillingProfileRepository billingProfileRepository,
            final @NonNull InvoiceRewardRepository invoiceRewardRepository,
            final @NonNull InvoiceRepository invoiceRepository,
            final @NonNull RewardRepository rewardRepository) {
        return new PostgresInvoiceStorage(billingProfileRepository, invoiceRewardRepository,
                invoiceRepository, rewardRepository);
    }

    @Bean
    public BillingProfileStoragePort accountingBillingProfileStorage(final GlobalSettingsRepository globalSettingsRepository,
                                                                     final KycRepository kycRepository,
                                                                     final KybRepository kybRepository,
                                                                     final BillingProfileRepository billingProfileRepository,
                                                                     final PayoutInfoRepository payoutInfoRepository,
                                                                     final WalletRepository walletRepository,
                                                                     final BillingProfileUserRepository billingProfileUserRepository,
                                                                     final BillingProfileUserViewRepository billingProfileUserViewRepository,
                                                                     final ChildrenKycRepository childrenKycRepository,
                                                                     final BillingProfileUserInvitationRepository billingProfileUserInvitationRepository,
                                                                     final PayoutPreferenceRepository payoutPreferenceRepository) {
        return new PostgresBillingProfileAdapter(globalSettingsRepository,
                billingProfileRepository, kybRepository, kycRepository, payoutInfoRepository, walletRepository, billingProfileUserRepository,
                billingProfileUserViewRepository, childrenKycRepository, billingProfileUserInvitationRepository, payoutPreferenceRepository);
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

    @Bean
    public PostgresPayoutPreferenceAdapter postgresPayoutPreferenceAdapter(final PayoutPreferenceRepository payoutPreferenceRepository,
                                                                           final PayoutPreferenceViewRepository payoutPreferenceViewRepository,
                                                                           final RewardRepository rewardRepository) {
        return new PostgresPayoutPreferenceAdapter(payoutPreferenceRepository, payoutPreferenceViewRepository, rewardRepository);
    }

    @Bean
    public PostgresBackofficeUserAdapter postgresBackofficeUserAdapter(final BackofficeUserRepository backofficeUserRepository) {
        return new PostgresBackofficeUserAdapter(backofficeUserRepository);
    }

    @Bean
    PostgresReceiptStorage postgresReceiptStorage(final RewardRepository rewardRepository) {
        return new PostgresReceiptStorage(rewardRepository);
    }
}
