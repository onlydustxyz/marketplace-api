package onlydust.com.marketplace.api.configuration;

import onlydust.com.marketplace.kernel.port.output.IndexerPort;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.user.domain.port.input.AppUserFacadePort;
import onlydust.com.marketplace.user.domain.port.input.BackofficeUserFacadePort;
import onlydust.com.marketplace.user.domain.port.input.NotificationSettingsPort;
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
                                               final IndexerPort indexerPort) {
        return new AppUserService(appUserStoragePort, githubOAuthAppPort, identityProviderPort, githubUserStoragePort, indexerPort);
    }

    @Bean
    public NotificationSettingsPort notificationSettingsPort(final NotificationSettingsStoragePort notificationSettingsStoragePort) {
        return new NotificationSettingsService(notificationSettingsStoragePort);
    }

    @Bean
    public NotificationSender asyncNotificationEmailProcessor(final NotificationSender notificationSender,
                                                              final NotificationStoragePort notificationStoragePort) {
        return new AsyncNotificationEmailProcessor(notificationSender, notificationStoragePort);
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
}
