package onlydust.com.marketplace.api.postgres.adapter.configuration;

import jakarta.persistence.EntityManager;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BatchPaymentRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BoEcosystemRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.*;
import onlydust.com.marketplace.project.domain.port.input.TechnologyStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectCategoryStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
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
    public ProjectStoragePort projectStoragePort(final ProjectRepository projectRepository,
                                                 final ProjectViewRepository projectViewRepository,
                                                 final ProjectLeaderInvitationRepository projectLeaderInvitationRepository,
                                                 final ProjectRepoRepository projectRepoRepository,
                                                 final CustomProjectRepository customProjectRepository,
                                                 final ProjectLeadViewRepository projectLeadViewRepository,
                                                 final RewardableItemRepository rewardableItemRepository,
                                                 final CustomProjectRankingRepository customProjectRankingRepository,
                                                 final ChurnedContributorViewEntityRepository churnedContributorViewEntityRepository,
                                                 final NewcomerViewEntityRepository newcomerViewEntityRepository,
                                                 final ContributorActivityViewEntityRepository contributorActivityViewEntityRepository,
                                                 final HiddenContributorRepository hiddenContributorRepository,
                                                 final ProjectTagRepository projectTagRepository,
                                                 final ProjectInfosViewRepository projectInfosViewRepository) {
        return new PostgresProjectAdapter(
                projectRepository,
                projectViewRepository,
                projectLeaderInvitationRepository,
                projectRepoRepository,
                customProjectRepository,
                projectLeadViewRepository,
                rewardableItemRepository,
                customProjectRankingRepository,
                churnedContributorViewEntityRepository,
                newcomerViewEntityRepository,
                contributorActivityViewEntityRepository,
                hiddenContributorRepository,
                projectTagRepository,
                projectInfosViewRepository
        );
    }

    @Bean
    public ProjectCategoryStoragePort projectCategoryStoragePort(final ProjectCategorySuggestionRepository projectCategorySuggestionRepository,
                                                                 final ProjectCategoryRepository projectCategoryRepository) {
        return new PostgresProjectCategoryAdapter(projectCategorySuggestionRepository, projectCategoryRepository);
    }

    @Bean
    public PostgresProjectAdapter projectRewardStoragePort(final ProjectRepository projectRepository,
                                                           final ProjectViewRepository projectViewRepository,
                                                           final ProjectLeaderInvitationRepository projectLeaderInvitationRepository,
                                                           final ProjectRepoRepository projectRepoRepository,
                                                           final CustomProjectRepository customProjectRepository,
                                                           final ProjectLeadViewRepository projectLeadViewRepository,
                                                           final RewardableItemRepository rewardableItemRepository,
                                                           final CustomProjectRankingRepository customProjectRankingRepository,
                                                           final ChurnedContributorViewEntityRepository churnedContributorViewEntityRepository,
                                                           final NewcomerViewEntityRepository newcomerViewEntityRepository,
                                                           final ContributorActivityViewEntityRepository contributorActivityViewEntityRepository,
                                                           final HiddenContributorRepository hiddenContributorRepository,
                                                           final ProjectTagRepository projectTagRepository,
                                                           final ProjectInfosViewRepository projectInfosViewRepository) {
        return new PostgresProjectAdapter(
                projectRepository,
                projectViewRepository,
                projectLeaderInvitationRepository,
                projectRepoRepository,
                customProjectRepository,
                projectLeadViewRepository,
                rewardableItemRepository,
                customProjectRankingRepository,
                churnedContributorViewEntityRepository,
                newcomerViewEntityRepository,
                contributorActivityViewEntityRepository,
                hiddenContributorRepository,
                projectTagRepository,
                projectInfosViewRepository
        );
    }

    @Bean
    public PostgresProjectRewardAdapter postgresProjectRewardAdapter(final ProjectAllowanceRepository projectAllowanceRepository,
                                                                     final HistoricalQuoteRepository historicalQuoteRepository,
                                                                     final CurrencyRepository currencyRepository,
                                                                     final CustomRewardRepository customRewardRepository,
                                                                     final RewardViewRepository rewardViewRepository
    ) {
        return new PostgresProjectRewardAdapter(projectAllowanceRepository, historicalQuoteRepository, currencyRepository, rewardViewRepository,
                customRewardRepository);
    }

    @Bean
    public PostgresGithubAdapter postgresGithubAdapter(final GithubAppInstallationRepository githubAppInstallationRepository,
                                                       final GithubRepoViewEntityRepository githubRepoViewEntityRepository,
                                                       final GithubIssueViewRepository githubIssueViewRepository) {
        return new PostgresGithubAdapter(githubAppInstallationRepository, githubRepoViewEntityRepository, githubIssueViewRepository);
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
                                                   final AllUserViewRepository allUserViewRepository,
                                                   final OnboardingRepository onboardingRepository,
                                                   final ProjectLeaderInvitationRepository projectLeaderInvitationRepository,
                                                   final ProjectLeadRepository projectLeadRepository,
                                                   final UserProfileInfoRepository userProfileInfoRepository,
                                                   final CustomRewardRepository customRewardRepository,
                                                   final ProjectLedIdRepository projectLedIdRepository,
                                                   final RewardViewRepository rewardViewRepository,
                                                   final CurrencyRepository currencyRepository,
                                                   final BillingProfileUserRepository billingProfileUserRepository) {
        return new PostgresUserAdapter(
                customUserRepository,
                customContributorRepository,
                userRepository,
                userViewRepository,
                allUserViewRepository,
                onboardingRepository,
                projectLeaderInvitationRepository,
                projectLeadRepository,
                userProfileInfoRepository,
                customRewardRepository,
                projectLedIdRepository,
                rewardViewRepository,
                currencyRepository,
                billingProfileUserRepository);
    }

    @Bean
    public PostgresProjectApplicationAdapter postgresProjectApplicationAdapter(final ApplicationRepository applicationRepository,
                                                                               final ApplicationsToReviewRepository applicationsToReviewRepository) {
        return new PostgresProjectApplicationAdapter(applicationRepository, applicationsToReviewRepository);
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
                                                       final BackofficeRewardViewRepository backofficeRewardViewRepository,
                                                       final RewardRepository rewardRepository,
                                                       final PaymentShortViewRepository paymentShortViewRepository,
                                                       final ShortRewardViewRepository shortRewardViewRepository,
                                                       final BackofficeEarningsViewRepository backofficeEarningsViewRepository,
                                                       final NodeGuardianBoostRewardRepository nodeGuardianBoostRewardRepository) {
        return new PostgresRewardAdapter(shortProjectViewEntityRepository,
                batchPaymentRepository,
                backofficeRewardViewRepository,
                rewardRepository,
                paymentShortViewRepository,
                shortRewardViewRepository,
                backofficeEarningsViewRepository,
                nodeGuardianBoostRewardRepository);
    }

    @Bean
    public CustomRewardRepository customRewardRepository(final EntityManager entityManager) {
        return new CustomRewardRepository(entityManager);
    }

    @Bean
    public PostgresBackofficeAdapter postgresBackofficeAdapter(final SponsorRepository sponsorRepository,
                                                               final SponsorViewRepository sponsorViewRepository,
                                                               final BoEcosystemRepository boEcosystemRepository,
                                                               final EcosystemRepository ecosystemRepository,
                                                               final ProjectRepository projectRepository) {
        return new PostgresBackofficeAdapter(sponsorRepository, sponsorViewRepository, boEcosystemRepository, ecosystemRepository,
                projectRepository);
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
    public PostgresOutboxAdapter<IndexingEventEntity> indexingEventsOutbox(final IndexingEventRepository indexingEventRepository) {
        return new PostgresOutboxAdapter<>(indexingEventRepository);
    }

    @Bean
    public PostgresOutboxAdapter<BillingProfileVerificationEventEntity> billingProfileVerificationOutbox(final BillingProfileVerificationEventRepository billingProfileVerificationEventRepository) {
        return new PostgresOutboxAdapter<>(billingProfileVerificationEventRepository);
    }

    @Bean
    public PostgresOutboxAdapter<AccountingMailEventEntity> accountingMailOutbox(final AccountingMailEventRepository accountingMailEventRepository) {
        return new PostgresOutboxAdapter<>(accountingMailEventRepository);
    }

    @Bean
    public PostgresOutboxAdapter<ProjectMailEventEntity> projectMailOutbox(final ProjectMailEventRepository projectMailEventRepository) {
        return new PostgresOutboxAdapter<>(projectMailEventRepository);
    }

    @Bean
    public PostgresOutboxAdapter<BoostNodeGuardiansRewardsEventEntity> boostNodeGuardiansRewardsOutbox(final BoostNodeGuardiansRewardsRepository boostNodeGuardiansRewardsRepository) {
        return new PostgresOutboxAdapter<>(boostNodeGuardiansRewardsRepository);
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
    public PostgresQuoteAdapter postgresQuoteAdapter(final HistoricalQuoteRepository historicalQuoteRepository,
                                                     final LatestQuoteRepository latestQuoteRepository,
                                                     final OldestQuoteRepository oldestQuoteRepository) {
        return new PostgresQuoteAdapter(historicalQuoteRepository, latestQuoteRepository, oldestQuoteRepository);
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
    public PostgresAccountBookEventAdapter postgresAccountBookEventStorage(final AccountBookRepository accountBookRepository,
                                                                           final AccountBookEventRepository accountBookEventRepository) {
        return new PostgresAccountBookEventAdapter(accountBookRepository, accountBookEventRepository);
    }

    @Bean
    public PostgresSponsorAccountStorageAdapter postgresSponsorAccountStorageAdapter(final SponsorAccountRepository sponsorAccountRepository,
                                                                                     final SponsorAccountTransactionViewRepository sponsorAccountTransactionViewRepository) {
        return new PostgresSponsorAccountStorageAdapter(sponsorAccountRepository, sponsorAccountTransactionViewRepository);
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
                                                                     final RewardViewRepository rewardViewRepository,
                                                                     final RewardRepository rewardRepository,
                                                                     final UserRepository userRepository) {
        return new PostgresBillingProfileAdapter(globalSettingsRepository,
                billingProfileRepository, kybRepository, kycRepository, payoutInfoRepository, payoutInfoViewRepository, walletRepository,
                billingProfileUserRepository, billingProfileUserViewRepository, childrenKycRepository, billingProfileUserInvitationRepository,
                payoutPreferenceRepository, bankAccountRepository, shortBillingProfileViewRepository, billingProfileUserRightsViewRepository,
                rewardViewRepository, rewardRepository, userRepository);
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
                                                                           final RewardRepository rewardRepository,
                                                                           final ShortBillingProfileViewRepository shortBillingProfileViewRepository) {
        return new PostgresPayoutPreferenceAdapter(payoutPreferenceRepository, payoutPreferenceViewRepository, rewardRepository,
                shortBillingProfileViewRepository);
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
    PostgresSponsorAdapter postgresSponsorAdapter(final SponsorViewRepository sponsorViewRepository) {
        return new PostgresSponsorAdapter(sponsorViewRepository);
    }

    @Bean
    public PostgresHackathonAdapter postgresHackathonAdapter(final HackathonRepository hackathonRepository,
                                                             final HackathonRegistrationRepository hackathonRegistrationRepository) {
        return new PostgresHackathonAdapter(hackathonRepository,
                hackathonRegistrationRepository);
    }

    @Bean
    public PostgresLanguageAdapter postgresLanguageAdapter(final LanguageRepository languageRepository,
                                                           final LanguageExtensionRepository languageExtensionRepository) {
        return new PostgresLanguageAdapter(languageRepository, languageExtensionRepository);
    }

    @Bean
    PostgresCommitteeAdapter postgresCommitteeAdapter(final CommitteeRepository committeeRepository,
                                                      final CommitteeLinkViewRepository committeeLinkViewRepository,
                                                      final CommitteeJuryVoteRepository committeeJuryVoteRepository,
                                                      final CommitteeBudgetAllocationRepository committeeBudgetAllocationRepository) {
        return new PostgresCommitteeAdapter(committeeRepository,
                committeeLinkViewRepository,
                committeeJuryVoteRepository,
                committeeBudgetAllocationRepository);
    }

    @Bean
    PostgresBannersAdapter postgresBannersAdapter(final BannerRepository bannerRepository) {
        return new PostgresBannersAdapter(bannerRepository);
    }
}
