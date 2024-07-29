package onlydust.com.marketplace.api.configuration;

import com.onlydust.customer.io.adapter.CustomerIOInstantEmailSenderAdapter;
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
import onlydust.com.marketplace.api.infrastructure.langchain.adapters.LangchainLLMAdapter;
import onlydust.com.marketplace.api.infura.adapters.InfuraEvmTransactionStorageAdapter;
import onlydust.com.marketplace.api.infura.adapters.StarknetInfuraTransactionStorageAdapter;
import onlydust.com.marketplace.api.postgres.adapter.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileVerificationEventEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectMailEventEntity;
import onlydust.com.marketplace.api.posthog.adapters.PosthogApiClientAdapter;
import onlydust.com.marketplace.api.slack.SlackApiAdapter;
import onlydust.com.marketplace.kernel.jobs.OutboxConsumerJob;
import onlydust.com.marketplace.kernel.port.output.*;
import onlydust.com.marketplace.project.domain.gateway.DateProvider;
import onlydust.com.marketplace.project.domain.job.*;
import onlydust.com.marketplace.project.domain.model.GlobalConfig;
import onlydust.com.marketplace.project.domain.observer.*;
import onlydust.com.marketplace.project.domain.port.input.*;
import onlydust.com.marketplace.project.domain.port.output.*;
import onlydust.com.marketplace.project.domain.service.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

import java.util.Date;
import java.util.UUID;

@Configuration
@EnableRetry
public class ProjectConfiguration {
    @Bean
    public UUIDGeneratorPort uuidGeneratorPort() {
        return UUID::randomUUID;
    }

    @Bean
    public ContributionService contributionService(final ContributionStoragePort contributionStoragePort,
                                                   final PermissionService permissionService,
                                                   final GithubAppService githubAppService,
                                                   final GithubApiPort githubApiPort) {
        return new ContributionService(contributionStoragePort, permissionService, githubAppService, githubApiPort);
    }

    @Bean
    public ProjectFacadePort projectFacadePort(final ProjectObserverPort projectObservers,
                                               final ProjectStoragePort projectStoragePort,
                                               final ImageStoragePort imageStoragePort,
                                               final UUIDGeneratorPort uuidGeneratorPort,
                                               final PermissionService permissionService,
                                               final IndexerPort indexerPort,
                                               final DateProvider dateProvider,
                                               final ContributionStoragePort contributionStoragePort,
                                               final DustyBotStoragePort dustyBotStoragePort,
                                               final GithubStoragePort githubStoragePort) {
        return new ProjectService(projectObservers,
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
    public ProjectCategoryFacadePort projectCategoryFacadePort(final ProjectCategoryStoragePort projectCategoryStoragePort) {
        return new ProjectCategoryService(projectCategoryStoragePort);
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
    public UserFacadePort userFacadePort(final UserObserverPort userObservers,
                                         final PostgresUserAdapter postgresUserAdapter,
                                         final DateProvider dateProvider,
                                         final ProjectStoragePort projectStoragePort,
                                         final GithubSearchPort githubSearchPort,
                                         final ImageStoragePort imageStoragePort) {
        return new UserService(
                userObservers,
                postgresUserAdapter,
                dateProvider,
                projectStoragePort,
                githubSearchPort,
                imageStoragePort);
    }

    @Bean
    public ApplicationFacadePort applicationFacadePort(final PostgresUserAdapter postgresUserAdapter,
                                                       final PostgresProjectApplicationAdapter postgresProjectApplicationAdapter,
                                                       final ProjectStoragePort projectStoragePort,
                                                       final ApplicationObserverPort applicationObservers,
                                                       final GithubUserPermissionsService githubUserPermissionsService,
                                                       final GithubStoragePort githubStoragePort,
                                                       final GithubApiPort githubApiPort,
                                                       final GithubAuthenticationPort githubAuthenticationPort,
                                                       final GithubAppService githubAppService,
                                                       final GlobalConfig globalConfig
    ) {
        return new ApplicationService(
                postgresUserAdapter,
                postgresProjectApplicationAdapter,
                projectStoragePort,
                applicationObservers,
                githubUserPermissionsService,
                githubStoragePort,
                githubApiPort,
                githubAuthenticationPort,
                githubAppService,
                globalConfig);
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
                                        final ContributionStoragePort contributionStoragePort,
                                        final ProjectSponsorStoragePort sponsorStoragePort) {
        return new PermissionService(projectStoragePort, contributionStoragePort, sponsorStoragePort);
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
                                               final OutboxConsumer trackingOutboxConsumer) {
        return new OutboxConsumerJob(trackingOutbox, trackingOutboxConsumer);
    }

    @Bean
    public OutboxConsumerJob indexingEventsOutboxJob(final OutboxPort indexingEventsOutbox,
                                                     final OutboxConsumer indexingEventsOutboxConsumer) {
        return new OutboxConsumerJob(indexingEventsOutbox, indexingEventsOutboxConsumer);
    }

    @Bean
    public OutboxConsumer trackingOutboxConsumer(final PosthogApiClientAdapter posthogApiClientAdapter,
                                                 final UserStoragePort userStoragePort,
                                                 final ProjectStoragePort projectStoragePort) {
        return new RetriedOutboxConsumer(new TrackingEventPublisherOutboxConsumer(posthogApiClientAdapter, userStoragePort, projectStoragePort));
    }

    @Bean
    public OutboxConsumer contributionRefresher(final ContributionObserverPort contributionObserverPort) {
        return new RetriedOutboxConsumer(new ContributionRefresher(contributionObserverPort));
    }

    @Bean
    public OutboxConsumer applicationsUpdater(final ProjectStoragePort projectStoragePort,
                                              final ProjectApplicationStoragePort projectApplicationStoragePort,
                                              final IndexerPort indexerPort,
                                              final GithubStoragePort githubStoragePort,
                                              final ApplicationObserverPort applicationObservers,
                                              final LangchainLLMAdapter langchainLLMAdapter,
                                              final HackathonStoragePort hackathonStoragePort) {
        return new SkippedOnFailureOutboxConsumer(new RetriedOutboxConsumer(
                new ApplicationsUpdater(projectStoragePort,
                        projectApplicationStoragePort,
                        langchainLLMAdapter,
                        indexerPort,
                        githubStoragePort,
                        applicationObservers,
                        hackathonStoragePort)));
    }

    @Bean
    GithubIssueCommenter githubIssueCommenter(final UserStoragePort userStoragePort,
                                              final ProjectStoragePort projectStoragePort,
                                              final GithubStoragePort githubStoragePort,
                                              final GithubAppService githubAppService,
                                              final GithubApiPort githubApiPort,
                                              final GlobalConfig globalConfig) {
        return new GithubIssueCommenter(
                userStoragePort,
                projectStoragePort,
                githubStoragePort,
                githubAppService,
                githubApiPort,
                globalConfig);
    }

    @Bean
    @ConfigurationProperties(value = "global", ignoreUnknownFields = false)
    GlobalConfig globalConfig() {
        return new GlobalConfig();
    }

    @Bean
    public ApplicationsCleaner applicationsCleaner(final ProjectApplicationStoragePort projectApplicationStoragePort) {
        return new ApplicationsCleaner(projectApplicationStoragePort);
    }

    @Bean
    public ApplicationMailNotifier applicationMailNotifier(final OutboxPort projectMailOutbox,
                                                           final ProjectApplicationStoragePort projectApplicationStoragePort,
                                                           final ProjectStoragePort projectStoragePort,
                                                           final GithubStoragePort githubStoragePort,
                                                           final UserStoragePort userStoragePort) {
        return new ApplicationMailNotifier(projectMailOutbox,
                projectApplicationStoragePort,
                projectStoragePort,
                githubStoragePort,
                userStoragePort);
    }

    @Bean
    public OutboxConsumer indexingEventsOutboxConsumer(final OutboxConsumer contributionRefresher,
                                                       final OutboxConsumer applicationsUpdater,
                                                       final OutboxConsumer trackingOutboxConsumer) {
        return new OutboxConsumerComposite(contributionRefresher, applicationsUpdater, trackingOutboxConsumer);
    }

    @Bean
    public OutboxConsumer indexerApiOutboxConsumer(final IndexerPort indexerPort) {
        return new IndexerApiOutboxConsumer(indexerPort);
    }

    @Bean
    public TechnologiesPort technologiesPort(final TechnologyStoragePort technologyStoragePort) {
        return new TechnologiesService(technologyStoragePort);
    }

    @Bean
    public OutboxService outboxService(final OutboxPort indexerOutbox,
                                       final OutboxPort trackingOutbox) {
        return new OutboxService(indexerOutbox, trackingOutbox);
    }

    @Bean
    public ProjectObserverPort projectObservers(final OutboxService outboxService,
                                                final SlackApiAdapter slackApiAdapter,
                                                final ContributionService contributionService) {
        return new ProjectObserverComposite(outboxService, contributionService, slackApiAdapter);
    }

    @Bean
    public ApplicationObserverPort applicationObservers(final SlackApiAdapter slackApiAdapter,
                                                        final GithubIssueCommenter githubIssueCommenter,
                                                        final OutboxService outboxService,
                                                        final ApplicationMailNotifier applicationMailNotifier) {
        return new ApplicationObserverComposite(slackApiAdapter, githubIssueCommenter, outboxService, applicationMailNotifier);
    }

    @Bean
    public UserObserverPort userObservers(final OutboxService outboxService) {
        return new UserObserverComposite(outboxService);
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
    public HackathonFacadePort hackathonFacadePort(final HackathonStoragePort hackathonStoragePort,
                                                   final HackathonObserverPort hackathonObservers) {
        return new HackathonService(hackathonStoragePort, hackathonObservers);
    }

    @Bean
    public BlockchainFacadePort blockchainFacadePort(final InfuraEvmTransactionStorageAdapter ethereumTransactionStorageAdapter,
                                                     final InfuraEvmTransactionStorageAdapter optimismTransactionStorageAdapter,
                                                     final StarknetInfuraTransactionStorageAdapter starknetTransactionStoragePort,
                                                     final AptosTransactionStorageAdapter aptosTransactionStorageAdapter) {
        return new BlockchainService(ethereumTransactionStorageAdapter, optimismTransactionStorageAdapter, aptosTransactionStorageAdapter,
                starknetTransactionStoragePort);
    }

    @Bean
    public BoostNodeGuardiansRewardsPort boostNodeGuardiansRewardsPort(final ProjectFacadePort projectFacadePort,
                                                                       final BoostedRewardStoragePort boostedRewardStoragePort,
                                                                       final RewardFacadePort rewardFacadePort, final NodeGuardiansApiPort nodeGuardiansApiPort,
                                                                       final OutboxPort boostNodeGuardiansRewardsOutbox) {
        return new BoostNodeGuardiansRewardsService(projectFacadePort, boostedRewardStoragePort, rewardFacadePort, nodeGuardiansApiPort,
                boostNodeGuardiansRewardsOutbox);
    }

    @Bean
    public OutboxConsumer nodeGuardiansOutboxConsumer(final ProjectFacadePort projectFacadePort,
                                                      final BoostedRewardStoragePort boostedRewardStoragePort,
                                                      final RewardFacadePort rewardFacadePort, final NodeGuardiansApiPort nodeGuardiansApiPort,
                                                      final OutboxPort boostNodeGuardiansRewardsOutbox) {
        return new BoostNodeGuardiansRewardsService(projectFacadePort, boostedRewardStoragePort, rewardFacadePort, nodeGuardiansApiPort,
                boostNodeGuardiansRewardsOutbox);
    }

    @Bean
    public OutboxConsumerJob nodeGuardiansOutboxJob(final OutboxConsumer nodeGuardiansOutboxConsumer,
                                                    final OutboxPort boostNodeGuardiansRewardsOutbox) {
        return new OutboxConsumerJob(boostNodeGuardiansRewardsOutbox, nodeGuardiansOutboxConsumer);
    }

    @Bean
    public HackathonObserverPort hackathonObservers(final SlackApiAdapter slackApiAdapter) {
        return new HackathonObserverComposite(slackApiAdapter);
    }

    @Bean
    public CommitteeService committeeService(final CommitteeStoragePort committeeStoragePort,
                                             final ProjectStoragePort projectStoragePort,
                                             final PermissionService permissionService,
                                             final CommitteeObserverPort committeeObserverPort) {
        return new CommitteeService(committeeStoragePort, permissionService, projectStoragePort, committeeObserverPort);
    }

    @Bean
    public CommitteeObserverPort committeeObserverPort(final NotificationPort notificationPort,
                                                       final ProjectStoragePort projectStoragePort,
                                                       final CommitteeStoragePort committeeStoragePort) {
        return new ProjectNotifier(notificationPort, projectStoragePort, committeeStoragePort);
    }

    @Bean
    public OutboxConsumerJob projectMailOutboxJob(final PostgresOutboxAdapter<ProjectMailEventEntity> projectMailOutbox,
                                                  final OutboxConsumer projectMailOutboxConsumer) {
        return new OutboxConsumerJob(projectMailOutbox, projectMailOutboxConsumer);
    }

    @Bean
    public OutboxConsumer projectMailOutboxConsumer(final CustomerIOInstantEmailSenderAdapter customerIOInstantEmailSenderAdapter) {
        return new RetriedOutboxConsumer(customerIOInstantEmailSenderAdapter);
    }

    @Bean
    public LanguageFacadePort languageFacadePort(final LanguageStorage languageStorage, final ImageStoragePort imageStoragePort) {
        return new LanguageService(languageStorage, imageStoragePort);
    }

    @Bean
    public GithubUserPermissionsService githubUserPermissionsService(final GithubAuthenticationPort githubAuthenticationPort,
                                                                     final GithubAuthenticationInfoPort githubAuthenticationInfoPort) {
        return new GithubUserPermissionsService(githubAuthenticationPort, githubAuthenticationInfoPort);
    }

    @Bean
    public BannerFacadePort bannerFacadePort(final BannerStoragePort bannerStoragePort) {
        return new BannerService(bannerStoragePort);
    }

    @Bean
    public AutomatedRewardFacadePort automatedRewardFacadePort(final GithubSearchPort githubSearchPort, final ProjectFacadePort projectFacadePort,
                                                               final RewardFacadePort rewardFacadePort, final ProjectStoragePort projectStoragePort,
                                                               final ProjectCurrencyStoragePort projectCurrencyStoragePort) {
        return new AutomatedRewardService(githubSearchPort, projectFacadePort, rewardFacadePort, projectStoragePort, projectCurrencyStoragePort);
    }
}
