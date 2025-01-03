package onlydust.com.marketplace.api.configuration;

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
import onlydust.com.marketplace.api.infura.adapters.StarknetTransactionStorageAdapter;
import onlydust.com.marketplace.api.infura.adapters.Web3EvmTransactionStorageAdapter;
import onlydust.com.marketplace.api.near.adapters.NearTransactionStorageAdapter;
import onlydust.com.marketplace.api.postgres.adapter.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileVerificationEventEntity;
import onlydust.com.marketplace.api.posthog.adapters.PosthogApiClientAdapter;
import onlydust.com.marketplace.api.slack.SlackApiAdapter;
import onlydust.com.marketplace.api.stellar.adapters.StellarTransactionStorageAdapter;
import onlydust.com.marketplace.kernel.infrastructure.postgres.PostgresOutboxAdapter;
import onlydust.com.marketplace.kernel.jobs.OutboxAsyncConsumerJob;
import onlydust.com.marketplace.kernel.jobs.OutboxConsumerJob;
import onlydust.com.marketplace.kernel.model.blockchain.MetaBlockExplorer;
import onlydust.com.marketplace.kernel.port.output.*;
import onlydust.com.marketplace.project.domain.gateway.DateProvider;
import onlydust.com.marketplace.project.domain.job.*;
import onlydust.com.marketplace.project.domain.job.githubcommands.GithubCommandOutboxConsumer;
import onlydust.com.marketplace.project.domain.job.githubcommands.GithubCreateCommentCommandConsumer;
import onlydust.com.marketplace.project.domain.job.githubcommands.GithubCreateCommentCommandConsumerCleaner;
import onlydust.com.marketplace.project.domain.model.GlobalConfig;
import onlydust.com.marketplace.project.domain.observer.ApplicationObserverComposite;
import onlydust.com.marketplace.project.domain.observer.ContributionObserverComposite;
import onlydust.com.marketplace.project.domain.observer.HackathonObserverComposite;
import onlydust.com.marketplace.project.domain.observer.ProjectObserverComposite;
import onlydust.com.marketplace.project.domain.port.input.*;
import onlydust.com.marketplace.project.domain.port.output.*;
import onlydust.com.marketplace.project.domain.service.*;
import org.springframework.beans.factory.annotation.Value;
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
                                               final GithubStoragePort githubStoragePort,
                                               final FgaPort.Project projectFgaPort) {
        return new ProjectService(projectObservers,
                projectStoragePort,
                imageStoragePort,
                uuidGeneratorPort,
                permissionService,
                indexerPort,
                dateProvider,
                contributionStoragePort,
                dustyBotStoragePort,
                githubStoragePort,
                projectFgaPort);
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
    public UserFacadePort userFacadePort(final PostgresUserAdapter postgresUserAdapter,
                                         final DateProvider dateProvider,
                                         final ProjectStoragePort projectStoragePort,
                                         final GithubSearchPort githubSearchPort,
                                         final ImageStoragePort imageStoragePort,
                                         final MetaBlockExplorer blockExplorer,
                                         final IndexerPort indexerPort) {
        return new UserService(
                postgresUserAdapter,
                dateProvider,
                projectStoragePort,
                githubSearchPort,
                imageStoragePort,
                blockExplorer,
                indexerPort);
    }

    @Bean
    GithubCommandService githubCommandService(final OutboxPort githubCommandOutbox) {
        return new GithubCommandService(githubCommandOutbox);
    }

    @Bean
    public ApplicationFacadePort applicationFacadePort(final PostgresUserAdapter postgresUserAdapter,
                                                       final PostgresProjectApplicationAdapter postgresProjectApplicationAdapter,
                                                       final ProjectStoragePort projectStoragePort,
                                                       final ApplicationObserverPort applicationObservers,
                                                       final GithubUserPermissionsService githubUserPermissionsService,
                                                       final GithubStoragePort githubStoragePort,
                                                       final GithubApiPort githubApiPort,
                                                       final GithubCommandService githubCommandService,
                                                       final GithubAuthenticationPort githubAuthenticationPort,
                                                       final GithubAppService githubAppService
    ) {
        return new ApplicationService(
                postgresUserAdapter,
                postgresProjectApplicationAdapter,
                projectStoragePort,
                applicationObservers,
                githubUserPermissionsService,
                githubStoragePort,
                githubApiPort,
                githubCommandService,
                githubAuthenticationPort,
                githubAppService);
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
                                        final SponsorStoragePort sponsorStoragePort,
                                        final ProgramStoragePort programStoragePort,
                                        final EcosystemStoragePort ecosystemStoragePort) {
        return new PermissionService(projectStoragePort, contributionStoragePort, sponsorStoragePort, programStoragePort, ecosystemStoragePort);
    }

    @Bean
    public RewardFacadePort rewardFacadePort(final PostgresRewardAdapter postgresRewardAdapter,
                                             final PostgresContributionAdapter postgresContributionAdapter,
                                             final PermissionService permissionService,
                                             final IndexerPort indexerPort,
                                             final AccountingServicePort accountingServicePort) {
        return new RewardService(postgresRewardAdapter, postgresContributionAdapter, permissionService, indexerPort, accountingServicePort);
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
                                              final OutboxConsumer indexerApiProjectOutboxConsumer,
                                              final OutboxConsumer indexerApiUserOutboxConsumer) {
        return new OutboxConsumerJob(indexerOutbox, new OutboxConsumerComposite(indexerApiProjectOutboxConsumer, indexerApiUserOutboxConsumer));
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

    @Value("${application.cron.github-commands-concurrency-level}")
    private int githubCommandsConcurrencyLevel;

    @Bean
    public OutboxAsyncConsumerJob githubCommandOutboxJob(final OutboxPort githubCommandOutbox,
                                                         final OutboxConsumer githubCommandOutboxConsumer) {
        return new OutboxAsyncConsumerJob(githubCommandOutbox, githubCommandOutboxConsumer, githubCommandsConcurrencyLevel);
    }

    @Bean
    public OutboxConsumer trackingOutboxConsumer(final PosthogApiClientAdapter posthogApiClientAdapter,
                                                 final UserStoragePort userStoragePort,
                                                 final ProjectStoragePort projectStoragePort) {
        return new RetriedOutboxConsumer(new TrackingEventPublisherOutboxConsumer(posthogApiClientAdapter, userStoragePort, projectStoragePort));
    }

    @Bean
    public GithubCreateCommentCommandConsumerCleaner githubCreateCommentCommandConsumerCleaner(final ProjectApplicationStoragePort projectApplicationStoragePort,
                                                                                               final ApplicationObserverPort applicationObservers) {
        return new GithubCreateCommentCommandConsumerCleaner(projectApplicationStoragePort, applicationObservers);
    }

    @Bean
    public GithubCreateCommentCommandConsumer githubCreateCommentCommandConsumer(final GithubApiPort githubApiPort,
                                                                                 final GithubAuthenticationPort githubAuthenticationPort,
                                                                                 final ProjectApplicationStoragePort projectApplicationStoragePort,
                                                                                 final ApplicationObserverPort applicationObservers,
                                                                                 final GithubCreateCommentCommandConsumerCleaner githubCreateCommentCommandConsumerCleaner) {
        return new GithubCreateCommentCommandConsumer(githubApiPort, githubAuthenticationPort, projectApplicationStoragePort, applicationObservers,
                githubCreateCommentCommandConsumerCleaner);
    }

    @Bean
    public OutboxConsumer githubCommandOutboxConsumer(final GithubCreateCommentCommandConsumer githubCreateCommentCommandConsumer) {
        return new GithubCommandOutboxConsumer(githubCreateCommentCommandConsumer);
    }

    @Bean
    ContributionObserverPort contributionObservers(final ContributionService contributionService,
                                                   final PostgresBiProjectorAdapter postgresBiProjectorAdapter) {
        return new ContributionObserverComposite(contributionService, postgresBiProjectorAdapter);
    }

    @Bean
    public OutboxConsumer contributionRefresher(final ContributionObserverPort contributionObservers) {
        return new RetriedOutboxConsumer(new ContributionRefresher(contributionObservers));
    }

    @Bean
    public OutboxConsumer applicationsUpdater(final ProjectStoragePort projectStoragePort,
                                              final ProjectApplicationStoragePort projectApplicationStoragePort,
                                              final IndexerPort indexerPort,
                                              final ApplicationObserverPort applicationObservers,
                                              final LangchainLLMAdapter langchainLLMAdapter) {
        return new SkippedOnFailureOutboxConsumer(new RetriedOutboxConsumer(
                new ApplicationsUpdater(projectStoragePort,
                        projectApplicationStoragePort,
                        langchainLLMAdapter,
                        indexerPort,
                        applicationObservers)));
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
    public ApplicationMailNotifier applicationMailNotifier(final ProjectStoragePort projectStoragePort,
                                                           final GithubStoragePort githubStoragePort,
                                                           final UserStoragePort userStoragePort,
                                                           final NotificationPort notificationPort) {
        return new ApplicationMailNotifier(projectStoragePort,
                githubStoragePort,
                userStoragePort,
                notificationPort);
    }

    @Bean
    public OutboxConsumer indexingEventsOutboxConsumer(final OutboxConsumer contributionRefresher,
                                                       final OutboxConsumer applicationsUpdater,
                                                       final OutboxConsumer trackingOutboxConsumer) {
        return new OutboxConsumerComposite(contributionRefresher, applicationsUpdater, trackingOutboxConsumer);
    }

    @Bean
    public OutboxConsumer indexerApiProjectOutboxConsumer(final IndexerPort indexerPort) {
        return new IndexerApiProjectOutboxConsumer(indexerPort);
    }

    @Bean
    public TechnologiesPort technologiesPort(final TechnologyStoragePort technologyStoragePort) {
        return new TechnologiesService(technologyStoragePort);
    }

    @Bean
    public OutboxProjectService outboxProjectService(final OutboxPort indexerOutbox,
                                                     final OutboxPort trackingOutbox) {
        return new OutboxProjectService(indexerOutbox, trackingOutbox);
    }

    @Bean
    public ProjectObserverPort projectObservers(final OutboxProjectService outboxProjectService,
                                                final SlackApiAdapter slackApiAdapter,
                                                final ContributionService contributionService,
                                                final PostgresBiProjectorAdapter postgresBiProjectorAdapter) {
        return new ProjectObserverComposite(postgresBiProjectorAdapter, outboxProjectService, contributionService, slackApiAdapter);
    }

    @Bean
    public ApplicationObserverPort applicationObservers(final SlackApiAdapter slackApiAdapter,
                                                        final OutboxProjectService outboxProjectService,
                                                        final ApplicationMailNotifier applicationMailNotifier,
                                                        final PostgresBiProjectorAdapter postgresBiProjectorAdapter) {
        return new ApplicationObserverComposite(postgresBiProjectorAdapter, slackApiAdapter, outboxProjectService, applicationMailNotifier);
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
                                                     final @NonNull ERC20Provider aptosERC20Provider,
                                                     final @NonNull ERC20Provider stellarERC20Provider
    ) {
        return new ERC20ProviderFactory(ethereumERC20Provider, optimismERC20Provider, starknetERC20Provider, aptosERC20Provider, stellarERC20Provider);
    }


    @Bean
    OutboxConsumerJob billingProfileVerificationOutboxJob(final PostgresOutboxAdapter<BillingProfileVerificationEventEntity> billingProfileVerificationOutbox,
                                                          final OutboxConsumer billingProfileVerificationOutboxConsumer) {
        return new OutboxConsumerJob(billingProfileVerificationOutbox, billingProfileVerificationOutboxConsumer);
    }

    @Bean
    public EcosystemFacadePort ecosystemFacadePort(final EcosystemStoragePort ecosystemStoragePort,
                                                   final ImageStoragePort imageStoragePort) {
        return new EcosystemService(ecosystemStoragePort, imageStoragePort);
    }

    @Bean
    public HackathonFacadePort hackathonFacadePort(final HackathonStoragePort hackathonStoragePort,
                                                   final HackathonObserverPort hackathonObservers) {
        return new HackathonService(hackathonStoragePort, hackathonObservers);
    }

    @Bean
    public BlockchainFacadePort blockchainFacadePort(final Web3EvmTransactionStorageAdapter ethereumTransactionStorageAdapter,
                                                     final Web3EvmTransactionStorageAdapter optimismTransactionStorageAdapter,
                                                     final StarknetTransactionStorageAdapter starknetTransactionStoragePort,
                                                     final AptosTransactionStorageAdapter aptosTransactionStorageAdapter,
                                                     final StellarTransactionStorageAdapter stellarTransactionStorageAdapter,
                                                     final NearTransactionStorageAdapter nearTransactionStorageAdapter) {
        return new BlockchainService(ethereumTransactionStorageAdapter, optimismTransactionStorageAdapter, aptosTransactionStorageAdapter,
                starknetTransactionStoragePort, stellarTransactionStorageAdapter, nearTransactionStorageAdapter);
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
    public ProjectNotifier projectNotifier(final NotificationPort notificationPort,
                                           final ProjectStoragePort projectStoragePort,
                                           final CommitteeStoragePort committeeStoragePort,
                                           final ProgramStoragePort programStoragePort,
                                           final SponsorStoragePort sponsorStoragePort) {
        return new ProjectNotifier(notificationPort, projectStoragePort, committeeStoragePort, programStoragePort, sponsorStoragePort);
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


    @Bean
    public SponsorFacadePort sponsorFacadePort(final SponsorStoragePort sponsorStoragePort, final ImageStoragePort imageStoragePort) {
        return new SponsorService(sponsorStoragePort, imageStoragePort);
    }


    @Bean
    public ProgramFacadePort programFacadePort(final ProgramStoragePort programStoragePort, final ImageStoragePort imageStoragePort) {
        return new ProgramService(programStoragePort, imageStoragePort);
    }

    @Bean
    public GoodFirstIssueCreatedNotifierJob goodFirstIssueCreatedNotifierJob(final GithubStoragePort githubStoragePort,
                                                                             final ProjectStoragePort projectStoragePort,
                                                                             final UserStoragePort userStoragePort,
                                                                             final NotificationPort notificationPort) {
        return new GoodFirstIssueCreatedNotifierJob(githubStoragePort, projectStoragePort, userStoragePort, notificationPort);
    }

    @Bean
    public ProjectContributorLabelFacadePort projectContributorLabelFacadePort(final PermissionService permissionService,
                                                                               final ProjectContributorLabelStoragePort projectContributorLabelStoragePort,
                                                                               final ProjectObserverPort projectObservers) {
        return new ProjectContributorLabelService(permissionService, projectContributorLabelStoragePort, projectObservers);
    }

    @Bean
    public IssueFacadePort issueFacadePort(final PermissionPort permissionPort, final ContributionStoragePort contributionStoragePort,
                                           final GithubApiPort githubApiPort, final GithubStoragePort githubStoragePort,
                                           final GithubAppService githubAppService,
                                           final ContributionObserverPort contributionObservers) {
        return new IssueService(permissionPort, contributionStoragePort, githubApiPort, githubStoragePort, githubAppService, contributionObservers);
    }

    @Bean
    public PullRequestFacadePort pullRequestFacadePort(final PermissionPort permissionPort,
                                                       final ContributionStoragePort contributionStoragePort,
                                                       final ContributionObserverPort contributionObservers) {
        return new PullRequestService(permissionPort, contributionStoragePort, contributionObservers);
    }
}
