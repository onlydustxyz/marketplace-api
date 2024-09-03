package onlydust.com.marketplace.api.configuration;

import onlydust.com.marketplace.kernel.port.output.IndexerPort;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
import onlydust.com.marketplace.user.domain.job.IndexerApiUserOutboxConsumer;
import onlydust.com.marketplace.user.domain.job.NotificationSummaryEmailJob;
import onlydust.com.marketplace.user.domain.observer.UserObserverComposite;
import onlydust.com.marketplace.user.domain.port.input.AppUserFacadePort;
import onlydust.com.marketplace.user.domain.port.input.BackofficeUserFacadePort;
import onlydust.com.marketplace.user.domain.port.input.UserObserverPort;
import onlydust.com.marketplace.user.domain.port.output.*;
import onlydust.com.marketplace.user.domain.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserDomainConfiguration {

    @Bean
    public BackofficeUserFacadePort backofficeUserFacadePort(final BackofficeUserStoragePort backofficeUserStoragePort) {
        return new BackofficeUserService(backofficeUserStoragePort);
    }

    @Bean
    public AppUserFacadePort appUserFacadePort(final AppUserStoragePort appUserStoragePort,
                                               final GithubOAuthAppPort githubOAuthAppPort,
                                               final IdentityProviderPort identityProviderPort,
                                               final GithubUserStoragePort githubUserStoragePort,
                                               final IndexerPort indexerPort,
                                               final UserObserverPort userObservers) {
        return new AppUserService(appUserStoragePort, githubOAuthAppPort, identityProviderPort, githubUserStoragePort, indexerPort, userObservers);
    }

    @Bean
    public NotificationSettingsService notificationSettingsService(final NotificationSettingsStoragePort notificationSettingsStoragePort,
                                                                   final MarketingNotificationSettingsStoragePort marketingNotificationSettingsStoragePort,
                                                                   final AppUserStoragePort appUserStoragePort) {
        return new NotificationSettingsService(notificationSettingsStoragePort,marketingNotificationSettingsStoragePort, appUserStoragePort);
    }

    @Bean
    public NotificationSender asyncNotificationEmailProcessor(final NotificationSender notificationInstantEmailSender,
                                                              final NotificationStoragePort notificationStoragePort) {
        return new AsyncNotificationEmailProcessor(notificationInstantEmailSender, notificationStoragePort);
    }

    @Bean
    public NotificationPort notificationPort(final NotificationSettingsStoragePort notificationSettingsStoragePort,
                                             final NotificationStoragePort notificationStoragePort,
                                             final AppUserStoragePort userStoragePort,
                                             final NotificationSender asyncNotificationEmailProcessor) {
        return new NotificationService(notificationSettingsStoragePort,
                notificationStoragePort,
                userStoragePort,
                asyncNotificationEmailProcessor);
    }

    @Bean
    public OutboxConsumer indexerApiUserOutboxConsumer(final IndexerPort indexerPort) {
        return new IndexerApiUserOutboxConsumer(indexerPort);
    }

    @Bean
    public OutboxUserService outboxUserService(final OutboxPort indexerOutbox,
                                               final OutboxPort trackingOutbox) {
        return new OutboxUserService(indexerOutbox, trackingOutbox);
    }

    @Bean
    public UserObserverPort userObservers(final OutboxUserService outboxUserService,
                                          final UserObserverPort notificationSettingsService) {
        return new UserObserverComposite(outboxUserService, notificationSettingsService);
    }

    @Bean
    public NotificationSummaryEmailJob notificationSummaryEmailJob(final NotificationStoragePort notificationStoragePort,
                                                                   final NotificationSender asyncNotificationEmailProcessor) {
        return new NotificationSummaryEmailJob(notificationStoragePort, asyncNotificationEmailProcessor);
    }
}
