package onlydust.com.marketplace.api.bootstrap.configuration;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.ERC20ProviderFactory;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.BlockchainFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.accounting.domain.service.BlockchainService;
import onlydust.com.marketplace.accounting.domain.service.CurrencyService;
import onlydust.com.marketplace.api.infrastructure.accounting.AccountingServiceAdapter;
import onlydust.com.marketplace.api.infrastructure.aptosrpc.adapters.AptosTransactionStorageAdapter;
import onlydust.com.marketplace.api.infura.adapters.InfuraEvmTransactionStorageAdapter;
import onlydust.com.marketplace.api.infura.adapters.StarknetInfuraTransactionStorageAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresGithubAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresOutboxAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresRewardAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresUserAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileVerificationEventEntity;
import onlydust.com.marketplace.kernel.jobs.OutboxConsumerJob;
import onlydust.com.marketplace.kernel.port.output.*;
import onlydust.com.marketplace.project.domain.gateway.DateProvider;
import onlydust.com.marketplace.project.domain.job.IndexerApiOutboxConsumer;
import onlydust.com.marketplace.project.domain.job.WebhookNotificationOutboxConsumer;
import onlydust.com.marketplace.project.domain.observer.ContributionObserver;
import onlydust.com.marketplace.project.domain.observer.ProjectObserver;
import onlydust.com.marketplace.project.domain.observer.UserObserver;
import onlydust.com.marketplace.project.domain.port.input.*;
import onlydust.com.marketplace.project.domain.port.output.*;
import onlydust.com.marketplace.project.domain.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

import java.util.Date;
import java.util.UUID;

@Configuration
@EnableRetry
public class DomainConfiguration {
    @Bean
    public UUIDGeneratorPort uuidGeneratorPort() {
        return UUID::randomUUID;
    }

    @Bean
    public ContributionFacadePort contributionFacadePort(final ContributionStoragePort contributionStoragePort,
                                                         final PermissionService permissionService) {
        return new ContributionService(contributionStoragePort, permissionService);
    }

    @Bean
    public ProjectFacadePort projectFacadePort(final ProjectObserverPort projectObserverPort,
                                               final ProjectStoragePort projectStoragePort,
                                               final ImageStoragePort imageStoragePort,
                                               final UUIDGeneratorPort uuidGeneratorPort,
                                               final PermissionService permissionService,
                                               final IndexerPort indexerPort,
                                               final DateProvider dateProvider,
                                               final ContributionStoragePort contributionStoragePort,
                                               final DustyBotStoragePort dustyBotStoragePort,
                                               final GithubStoragePort githubStoragePort) {
        return new ProjectService(projectObserverPort,
                projectStoragePort,
                imageStoragePort,
                uuidGeneratorPort,
                permissionService,
                indexerPort,
                dateProvider,
                contributionStoragePort,
                dustyBotStoragePort,
                githubStoragePort);
    }

    @Bean
    public ProjectRewardFacadePort projectRewardFacadePort(final ProjectRewardStoragePort projectRewardStoragePort,
                                                           final PermissionService permissionService
    ) {
        return new ProjectRewardService(projectRewardStoragePort, permissionService);
    }

    @Bean
    public GithubInstallationFacadePort githubInstallationFacadePort(
            final PostgresGithubAdapter postgresGithubAdapter,
            final GithubSearchPort githubSearchPort,
            final RetriedGithubInstallationFacade.Config config
    ) {
        return new RetriedGithubInstallationFacade(new GithubAccountService(postgresGithubAdapter, githubSearchPort),
                config);
    }

    @Bean
    public DateProvider dateProvider() {
        return Date::new;
    }

    @Bean
    public UserFacadePort userFacadePort(final UserObserverPort userObserverPort,
                                         final PostgresUserAdapter postgresUserAdapter,
                                         final DateProvider dateProvider,
                                         final ProjectStoragePort projectStoragePort,
                                         final GithubSearchPort githubSearchPort,
                                         final ImageStoragePort imageStoragePort,
                                         final ProjectObserverPort projectObserverPort) {
        return new UserService(
                userObserverPort,
                postgresUserAdapter,
                dateProvider,
                projectStoragePort,
                githubSearchPort,
                imageStoragePort,
                projectObserverPort);
    }

    @Bean
    public ContributorFacadePort contributorFacadePort(final ProjectStoragePort projectStoragePort,
                                                       final GithubSearchPort githubSearchPort,
                                                       final UserStoragePort userStoragePort,
                                                       final ContributionStoragePort contributionStoragePort,
                                                       final PostgresRewardAdapter postgresRewardAdapter) {
        return new ContributorService(projectStoragePort, githubSearchPort, userStoragePort, contributionStoragePort,
                postgresRewardAdapter);
    }


    @Bean
    PermissionService permissionService(final ProjectStoragePort projectStoragePort,
                                        final ContributionStoragePort contributionStoragePort) {
        return new PermissionService(projectStoragePort, contributionStoragePort);
    }

    @Bean
    public RewardFacadePort rewardFacadePort(final PostgresRewardAdapter postgresRewardAdapter,
                                             final PermissionService permissionService,
                                             final IndexerPort indexerPort,
                                             final AccountingServicePort accountingServicePort) {
        return new RewardService(postgresRewardAdapter, permissionService, indexerPort, accountingServicePort);
    }

    @Bean
    public AccountingServiceAdapter accountingServicePort(final AccountingFacadePort accountingFacadePort) {
        return new AccountingServiceAdapter(accountingFacadePort);
    }


    @Bean
    public GithubAccountService githubAccountService(final GithubSearchPort githubSearchPort,
                                                     final GithubStoragePort githubStoragePort) {
        return new GithubAccountService(githubStoragePort, githubSearchPort);
    }

    @Bean
    public OutboxConsumerJob indexerOutboxJob(final OutboxPort indexerOutbox,
                                              final OutboxConsumer indexerApiOutboxConsumer) {
        return new OutboxConsumerJob(indexerOutbox, indexerApiOutboxConsumer);
    }

    @Bean
    public OutboxConsumerJob trackingOutboxJob(final OutboxPort trackingOutbox,
                                               final OutboxConsumer webhookTrackingOutboxConsumer) {
        return new OutboxConsumerJob(trackingOutbox, webhookTrackingOutboxConsumer);
    }

    @Bean
    public OutboxConsumer webhookNotificationOutboxConsumer(final WebhookPort webhookNotificationPort) {
        return new WebhookNotificationOutboxConsumer(webhookNotificationPort);
    }

    @Bean
    public OutboxConsumer webhookTrackingOutboxConsumer(final WebhookPort webhookTrackingPort) {
        return new WebhookNotificationOutboxConsumer(webhookTrackingPort);
    }

    @Bean
    public OutboxConsumer indexerApiOutboxConsumer(final IndexerPort indexerPort) {
        return new IndexerApiOutboxConsumer(indexerPort);
    }

    @Bean
    public TechnologiesPort technologiesPort(final TrackingIssuePort trackingIssuePort,
                                             final TechnologyStoragePort technologyStoragePort) {
        return new TechnologiesService(trackingIssuePort, technologyStoragePort);
    }

    @Bean
    public ProjectObserverPort projectObserverPort(final ContributionStoragePort contributionStoragePort,
                                                   final OutboxPort indexerOutbox,
                                                   final NotificationPort notificationPort) {
        return new ProjectObserver(contributionStoragePort, indexerOutbox, notificationPort);
    }


    @Bean
    public ContributionObserverPort contributionObserverPort(final ContributionStoragePort contributionStoragePort) {
        return new ContributionObserver(contributionStoragePort);
    }

    @Bean
    public UserObserverPort userObserverPort(final OutboxPort indexerOutbox, final OutboxPort trackingOutbox) {
        return new UserObserver(indexerOutbox, trackingOutbox);
    }

    @Bean
    public CurrencyFacadePort currencyFacadePort(final @NonNull ERC20ProviderFactory erc20ProviderFactory,
                                                 final @NonNull CurrencyStorage currencyStorage,
                                                 final @NonNull CurrencyMetadataService currencyMetadataService,
                                                 final @NonNull QuoteService quoteService,
                                                 final @NonNull QuoteStorage quoteStorage,
                                                 final @NonNull IsoCurrencyService isoCurrencyService,
                                                 final @NonNull ImageStoragePort imageStoragePort) {
        return new CurrencyService(erc20ProviderFactory, currencyStorage, currencyMetadataService, quoteService, quoteStorage,
                isoCurrencyService, imageStoragePort);
    }

    @Bean
    public ERC20ProviderFactory erc20ProviderFactory(final @NonNull ERC20Provider ethereumERC20Provider,
                                                     final @NonNull ERC20Provider optimismERC20Provider,
                                                     final @NonNull ERC20Provider starknetERC20Provider,
                                                     final @NonNull ERC20Provider aptosERC20Provider
    ) {
        return new ERC20ProviderFactory(ethereumERC20Provider, optimismERC20Provider, starknetERC20Provider, aptosERC20Provider);
    }


    @Bean
    OutboxConsumerJob billingProfileVerificationOutboxJob(final PostgresOutboxAdapter<BillingProfileVerificationEventEntity> billingProfileVerificationOutbox,
                                                          final OutboxConsumer billingProfileVerificationOutboxConsumer) {
        return new OutboxConsumerJob(billingProfileVerificationOutbox, billingProfileVerificationOutboxConsumer);
    }

    @Bean
    public EcosystemFacadePort ecosystemFacadePort(final EcosystemStorage ecosystemStorage) {
        return new EcosystemService(ecosystemStorage);
    }

    @Bean
    public HackathonFacadePort hackathonFacadePort(final HackathonStoragePort hackathonStoragePort) {
        return new HackathonService(hackathonStoragePort);
    }

    @Bean
    public BlockchainFacadePort blockchainFacadePort(final InfuraEvmTransactionStorageAdapter ethereumTransactionStorageAdapter,
                                                     final InfuraEvmTransactionStorageAdapter optimismTransactionStorageAdapter,
                                                     final StarknetInfuraTransactionStorageAdapter starknetTransactionStoragePort,
                                                     final AptosTransactionStorageAdapter aptosTransactionStorageAdapter) {
        return new BlockchainService(ethereumTransactionStorageAdapter, optimismTransactionStorageAdapter, aptosTransactionStorageAdapter,
                starknetTransactionStoragePort);
    }
}
