package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.api.domain.gateway.DateProvider;
import onlydust.com.marketplace.api.domain.job.OutboxConsumer;
import onlydust.com.marketplace.api.domain.job.OutboxConsumerJob;
import onlydust.com.marketplace.api.domain.job.WebhookNotificationJob;
import onlydust.com.marketplace.api.domain.port.input.*;
import onlydust.com.marketplace.api.domain.port.output.*;
import onlydust.com.marketplace.api.domain.service.*;
import onlydust.com.marketplace.api.postgres.adapter.PostgresGithubAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresProjectAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresUserAdapter;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraAuthentication;
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
                                               final PostgresProjectAdapter postgresProjectAdapter,
                                               final ImageStoragePort imageStoragePort,
                                               final UUIDGeneratorPort uuidGeneratorPort,
                                               final PermissionService permissionService,
                                               final IndexerPort indexerPort,
                                               final DateProvider dateProvider,
                                               final EventStoragePort eventStoragePort,
                                               final ContributionStoragePort contributionStoragePort,
                                               final DustyBotStoragePort dustyBotStoragePort,
                                               final GithubStoragePort githubStoragePort) {
        return new ProjectService(projectObserverPort, postgresProjectAdapter, imageStoragePort, uuidGeneratorPort,
                permissionService,
                indexerPort, dateProvider, eventStoragePort, contributionStoragePort, dustyBotStoragePort,
                githubStoragePort);
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
    public UserFacadePort userFacadePort(final ProjectObserverPort projectObserverPort,
                                         final PostgresUserAdapter postgresUserAdapter,
                                         final DateProvider dateProvider,
                                         final ProjectStoragePort projectStoragePort,
                                         final GithubSearchPort githubSearchPort,
                                         final ImageStoragePort imageStoragePort) {
        return new UserService(projectObserverPort, postgresUserAdapter, dateProvider, projectStoragePort,
                githubSearchPort,
                imageStoragePort);
    }

    @Bean
    public ContributorFacadePort contributorFacadePort(final ProjectStoragePort projectStoragePort,
                                                       final GithubSearchPort githubSearchPort,
                                                       final UserStoragePort userStoragePort,
                                                       final ContributionStoragePort contributionStoragePort) {
        return new ContributorService(projectStoragePort, githubSearchPort, userStoragePort, contributionStoragePort);
    }


    @Bean
    PermissionService permissionService(final ProjectStoragePort projectStoragePort,
                                        final ContributionStoragePort contributionStoragePort) {
        return new PermissionService(projectStoragePort, contributionStoragePort);
    }

    @Bean
    public RewardService<HasuraAuthentication> rewardService(final RewardStoragePort<HasuraAuthentication> rewardStoragePort,
                                                             final ProjectStoragePort projectStoragePort,
                                                             final PermissionService permissionService,
                                                             final IndexerPort indexerPort) {
        return new RewardService<>(rewardStoragePort, projectStoragePort, permissionService, indexerPort);
    }

    @Bean
    public GithubAccountService githubAccountService(final GithubSearchPort githubSearchPort,
                                                     final GithubStoragePort githubStoragePort) {
        return new GithubAccountService(githubStoragePort, githubSearchPort);
    }

    @Bean
    public OutboxConsumerJob notificationOutboxJob(final OutboxPort notificationOutbox,
                                                   final OutboxConsumer webhookNotificationJob) {
        return new OutboxConsumerJob(notificationOutbox, webhookNotificationJob);
    }

    @Bean
    public OutboxConsumer webhookNotificationJob(final WebhookPort webhookPort) {
        return new WebhookNotificationJob(webhookPort);
    }

    @Bean
    public TechnologiesPort technologiesPort(final TrackingIssuePort trackingIssuePort) {
        return new TechnologiesService(trackingIssuePort);
    }

    @Bean
    public ProjectObserverPort projectObserverPort(final OutboxPort notificationOutbox,
                                                   final ContributionStoragePort contributionStoragePort,
                                                   final IndexerPort indexerPort) {
        return new ProjectObserver(notificationOutbox, contributionStoragePort, indexerPort);
    }


    @Bean
    public ContributionObserverPort contributionObserverPort(final ContributionStoragePort contributionStoragePort) {
        return new ContributionObserver(contributionStoragePort);
    }

}
