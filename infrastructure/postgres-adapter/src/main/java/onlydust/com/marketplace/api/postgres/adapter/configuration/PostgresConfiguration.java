package onlydust.com.marketplace.api.postgres.adapter.configuration;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
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
                                                 final ProjectLeaderInvitationRepository projectLeaderInvitationRepository,
                                                 final ProjectRepoRepository projectRepoRepository,
                                                 final CustomProjectRepository customProjectRepository,
                                                 final CustomContributorRepository customContributorRepository,
                                                 final ProjectLeadViewRepository projectLeadViewRepository,
                                                 final ProjectsPageRepository projectsPageRepository,
                                                 final ProjectsPageFiltersRepository projectsPageFiltersRepository,
                                                 final RewardableItemRepository rewardableItemRepository,
                                                 final CustomProjectRankingRepository customProjectRankingRepository,
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
                projectLeaderInvitationRepository,
                projectRepoRepository,
                customProjectRepository,
                customContributorRepository,
                projectLeadViewRepository,
                projectsPageRepository,
                projectsPageFiltersRepository,
                rewardableItemRepository,
                customProjectRankingRepository,
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
                                                           final ProjectLeaderInvitationRepository projectLeaderInvitationRepository,
                                                           final ProjectRepoRepository projectRepoRepository,
                                                           final CustomProjectRepository customProjectRepository,
                                                           final CustomContributorRepository customContributorRepository,
                                                           final ProjectLeadViewRepository projectLeadViewRepository,
                                                           final ProjectsPageRepository projectsPageRepository,
                                                           final ProjectsPageFiltersRepository projectsPageFiltersRepository,
                                                           final RewardableItemRepository rewardableItemRepository,
                                                           final CustomProjectRankingRepository customProjectRankingRepository,
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
                projectLeaderInvitationRepository,
                projectRepoRepository,
                customProjectRepository,
                customContributorRepository,
                projectLeadViewRepository,
                projectsPageRepository,
                projectsPageFiltersRepository,
                rewardableItemRepository,
                customProjectRankingRepository,
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
    public PostgresProjectRewardAdapter postgresProjectRewardAdapter(final ProjectAllowanceRepository projectAllowanceRepository,
                                                                     final HistoricalQuoteRepository historicalQuoteRepository,
                                                                     final CurrencyRepository currencyRepository,
                                                                     final CustomRewardRepository customRewardRepository,
                                                                     final BudgetStatsRepository budgetStatsRepository,
                                                                     final RewardDetailsViewRepository rewardDetailsViewRepository
    ) {
        return new PostgresProjectRewardAdapter(projectAllowanceRepository, historicalQuoteRepository, currencyRepository, budgetStatsRepository,
                rewardDetailsViewRepository, customRewardRepository);
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
                                                   final ProjectRepository projectRepository,
                                                   final UserProfileInfoRepository userProfileInfoRepository,
                                                   final CustomRewardRepository customRewardRepository,
                                                   final ProjectLedIdRepository projectLedIdRepository,
                                                   final RewardStatsRepository rewardStatsRepository,
                                                   final RewardDetailsViewRepository rewardDetailsViewRepository,
                                                   final CurrencyRepository currencyRepository,
                                                   final BillingProfileUserRepository billingProfileUserRepository) {
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
                projectRepository,
                userProfileInfoRepository,
                customRewardRepository,
                projectLedIdRepository,
                rewardStatsRepository,
                rewardDetailsViewRepository,
                currencyRepository,
                billingProfileUserRepository);
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
                                                       final BatchPaymentRepository batchPaymentRepository,
                                                       final RewardDetailsViewRepository rewardDetailsViewRepository,
                                                       final BackofficeRewardViewRepository backofficeRewardViewRepository,
                                                       final RewardRepository rewardRepository) {
        return new PostgresRewardAdapter(shortProjectViewEntityRepository,
                batchPaymentRepository, rewardDetailsViewRepository, backofficeRewardViewRepository, rewardRepository);
    }

    @Bean
    public CustomRewardRepository customRewardRepository(final EntityManager entityManager) {
        return new CustomRewardRepository(entityManager);
    }

    @Bean
    public PostgresBackofficeAdapter postgresBackofficeAdapter(final GithubRepositoryLinkedToProjectRepository githubRepositoryLinkedToProjectRepository,
                                                               final BoSponsorRepository boSponsorRepository,
                                                               final ProjectLeadInvitationRepository projectLeadInvitationRepository,
                                                               final BoUserRepository boUserRepository,
                                                               final BoProjectRepository boProjectRepository,
                                                               final BoEcosystemRepository boEcosystemRepository,
                                                               final EcosystemRepository ecosystemRepository,
                                                               final ProjectRepository projectRepository) {
        return new PostgresBackofficeAdapter(githubRepositoryLinkedToProjectRepository, boSponsorRepository, projectLeadInvitationRepository,
                boUserRepository, boProjectRepository, boEcosystemRepository, ecosystemRepository, projectRepository);
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
    public PostgresSponsorAccountStorageAdapter postgresSponsorAccountStorageAdapter(final SponsorAccountRepository sponsorAccountRepository,
                                                                                     final HistoricalTransactionRepository historicalTransactionRepository) {
        return new PostgresSponsorAccountStorageAdapter(sponsorAccountRepository, historicalTransactionRepository);
    }

    @Bean
    public PostgresRewardStatusAdapter postgresRewardStatusAdapter(final RewardStatusRepository rewardStatusRepository,
                                                                   final RewardRepository rewardRepository) {
        return new PostgresRewardStatusAdapter(rewardStatusRepository, rewardRepository);
    }

    @Bean
    PostgresRewardUsdEquivalentAdapter postgresRewardUsdEquivalentAdapter(final RewardUsdEquivalentDataRepository rewardUsdEquivalentDataRepository) {
        return new PostgresRewardUsdEquivalentAdapter(rewardUsdEquivalentDataRepository);
    }

    @Bean
    InvoiceStoragePort invoicePreviewStoragePort(
            final @NonNull InvoiceRewardRepository invoiceRewardRepository,
            final @NonNull InvoiceRepository invoiceRepository,
            final @NonNull RewardRepository rewardRepository,
            final @NonNull RewardViewRepository rewardViewRepository,
            final @NonNull InvoiceViewRepository invoiceViewRepository) {
        return new PostgresInvoiceStorage(invoiceRewardRepository,
                invoiceRepository, invoiceViewRepository, rewardRepository, rewardViewRepository);
    }

    @Bean
    public BillingProfileStoragePort accountingBillingProfileStorage(final GlobalSettingsRepository globalSettingsRepository,
                                                                     final KycRepository kycRepository,
                                                                     final KybRepository kybRepository,
                                                                     final BillingProfileRepository billingProfileRepository,
                                                                     final PayoutInfoRepository payoutInfoRepository,
                                                                     final PayoutInfoViewRepository payoutInfoViewRepository,
                                                                     final WalletRepository walletRepository,
                                                                     final BillingProfileUserRepository billingProfileUserRepository,
                                                                     final BillingProfileUserViewRepository billingProfileUserViewRepository,
                                                                     final ChildrenKycRepository childrenKycRepository,
                                                                     final BillingProfileUserInvitationRepository billingProfileUserInvitationRepository,
                                                                     final PayoutPreferenceRepository payoutPreferenceRepository,
                                                                     final BankAccountRepository bankAccountRepository,
                                                                     final ShortBillingProfileViewRepository shortBillingProfileViewRepository,
                                                                     final BillingProfileUserRightsViewRepository billingProfileUserRightsViewRepository,
                                                                     final RewardDetailsViewRepository rewardDetailsViewRepository) {
        return new PostgresBillingProfileAdapter(globalSettingsRepository,
                billingProfileRepository, kybRepository, kycRepository, payoutInfoRepository, payoutInfoViewRepository, walletRepository,
                billingProfileUserRepository, billingProfileUserViewRepository, childrenKycRepository, billingProfileUserInvitationRepository,
                payoutPreferenceRepository, bankAccountRepository, shortBillingProfileViewRepository, billingProfileUserRightsViewRepository,
                rewardDetailsViewRepository);
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

    @Bean
    PostgresSponsorAdapter postgresSponsorAdapter(final SponsorRepository sponsorRepository) {
        return new PostgresSponsorAdapter(sponsorRepository);
    }
}
