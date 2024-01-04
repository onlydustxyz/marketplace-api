package onlydust.com.marketplace.api.bootstrap.configuration;

import java.util.Date;
import java.util.UUID;
import onlydust.com.marketplace.api.domain.gateway.DateProvider;
import onlydust.com.marketplace.api.domain.job.IndexerApiOutboxConsumer;
import onlydust.com.marketplace.api.domain.job.OutboxConsumer;
import onlydust.com.marketplace.api.domain.job.OutboxConsumerJob;
import onlydust.com.marketplace.api.domain.job.WebhookNotificationOutboxConsumer;
import onlydust.com.marketplace.api.domain.observer.ContributionObserver;
import onlydust.com.marketplace.api.domain.observer.ProjectObserver;
import onlydust.com.marketplace.api.domain.observer.UserObserver;
import onlydust.com.marketplace.api.domain.port.input.ContributionFacadePort;
import onlydust.com.marketplace.api.domain.port.input.ContributionObserverPort;
import onlydust.com.marketplace.api.domain.port.input.ContributorFacadePort;
import onlydust.com.marketplace.api.domain.port.input.GithubInstallationFacadePort;
import onlydust.com.marketplace.api.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.api.domain.port.input.ProjectObserverPort;
import onlydust.com.marketplace.api.domain.port.input.TechnologiesPort;
import onlydust.com.marketplace.api.domain.port.input.TechnologyStoragePort;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.api.domain.port.input.UserObserverPort;
import onlydust.com.marketplace.api.domain.port.output.ContributionStoragePort;
import onlydust.com.marketplace.api.domain.port.output.DustyBotStoragePort;
import onlydust.com.marketplace.api.domain.port.output.EventStoragePort;
import onlydust.com.marketplace.api.domain.port.output.GithubSearchPort;
import onlydust.com.marketplace.api.domain.port.output.GithubStoragePort;
import onlydust.com.marketplace.api.domain.port.output.ImageStoragePort;
import onlydust.com.marketplace.api.domain.port.output.IndexerPort;
import onlydust.com.marketplace.api.domain.port.output.OutboxPort;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.domain.port.output.RewardServicePort;
import onlydust.com.marketplace.api.domain.port.output.RewardStoragePort;
import onlydust.com.marketplace.api.domain.port.output.TrackingIssuePort;
import onlydust.com.marketplace.api.domain.port.output.UUIDGeneratorPort;
import onlydust.com.marketplace.api.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.api.domain.port.output.WebhookPort;
import onlydust.com.marketplace.api.domain.service.ContributionService;
import onlydust.com.marketplace.api.domain.service.ContributorService;
import onlydust.com.marketplace.api.domain.service.GithubAccountService;
import onlydust.com.marketplace.api.domain.service.PermissionService;
import onlydust.com.marketplace.api.domain.service.ProjectService;
import onlydust.com.marketplace.api.domain.service.RetriedGithubInstallationFacade;
import onlydust.com.marketplace.api.domain.service.RewardService;
import onlydust.com.marketplace.api.domain.service.TechnologiesService;
import onlydust.com.marketplace.api.domain.service.UserService;
import onlydust.com.marketplace.api.postgres.adapter.PostgresGithubAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresProjectAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresUserAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

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
      final UserObserverPort userObserverPort,
      final PostgresUserAdapter postgresUserAdapter,
      final DateProvider dateProvider,
      final ProjectStoragePort projectStoragePort,
      final GithubSearchPort githubSearchPort,
      final ImageStoragePort imageStoragePort) {
    return new UserService(projectObserverPort, userObserverPort, postgresUserAdapter, dateProvider,
        projectStoragePort,
        githubSearchPort,
        imageStoragePort);
  }

  @Bean
  public ContributorFacadePort contributorFacadePort(final ProjectStoragePort projectStoragePort,
      final GithubSearchPort githubSearchPort,
      final UserStoragePort userStoragePort,
      final ContributionStoragePort contributionStoragePort,
      final RewardStoragePort rewardStoragePort) {
    return new ContributorService(projectStoragePort, githubSearchPort, userStoragePort, contributionStoragePort,
        rewardStoragePort);
  }


  @Bean
  PermissionService permissionService(final ProjectStoragePort projectStoragePort,
      final ContributionStoragePort contributionStoragePort) {
    return new PermissionService(projectStoragePort, contributionStoragePort);
  }

  @Bean
  public RewardService rewardService(final RewardServicePort rewardServicePort,
      final ProjectStoragePort projectStoragePort,
      final PermissionService permissionService,
      final IndexerPort indexerPort,
      final UserStoragePort userStoragePort) {
    return new RewardService(rewardServicePort, projectStoragePort, permissionService, indexerPort,
        userStoragePort);
  }

  @Bean
  public GithubAccountService githubAccountService(final GithubSearchPort githubSearchPort,
      final GithubStoragePort githubStoragePort) {
    return new GithubAccountService(githubStoragePort, githubSearchPort);
  }

  @Bean
  public OutboxConsumerJob notificationOutboxJob(final OutboxPort notificationOutbox,
      final OutboxConsumer webhookNotificationOutboxConsumer) {
    return new OutboxConsumerJob(notificationOutbox, webhookNotificationOutboxConsumer);
  }

  @Bean
  public OutboxConsumerJob indexerOutboxJob(final OutboxPort indexerOutbox,
      final OutboxConsumer indexerApiOutboxConsumer) {
    return new OutboxConsumerJob(indexerOutbox, indexerApiOutboxConsumer);
  }

  @Bean
  public OutboxConsumer webhookNotificationOutboxConsumer(final WebhookPort webhookPort) {
    return new WebhookNotificationOutboxConsumer(webhookPort);
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
  public ProjectObserverPort projectObserverPort(final OutboxPort notificationOutbox,
      final ContributionStoragePort contributionStoragePort,
      final OutboxPort indexerOutbox) {
    return new ProjectObserver(notificationOutbox, contributionStoragePort, indexerOutbox);
  }


  @Bean
  public ContributionObserverPort contributionObserverPort(final ContributionStoragePort contributionStoragePort) {
    return new ContributionObserver(contributionStoragePort);
  }

  @Bean
  public UserObserverPort userObserverPort(final OutboxPort indexerOutbox) {
    return new UserObserver(indexerOutbox);
  }

}
