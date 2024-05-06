package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.api.slack.SlackApiAdapter;
import onlydust.com.marketplace.api.slack.SlackProperties;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.project.domain.port.output.HackathonStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackConfiguration {

    @Bean
    @ConfigurationProperties("infrastructure.slack")
    public SlackProperties slackProperties() {
        return new SlackProperties();
    }

    @Bean
    public NotificationPort notificationPort(final SlackProperties slackProperties, final UserStoragePort userStoragePort,
                                             final ProjectStoragePort projectStoragePort, final HackathonStoragePort hackathonStoragePort) {
        return new SlackApiAdapter(slackProperties, userStoragePort, projectStoragePort, hackathonStoragePort);
    }
}
