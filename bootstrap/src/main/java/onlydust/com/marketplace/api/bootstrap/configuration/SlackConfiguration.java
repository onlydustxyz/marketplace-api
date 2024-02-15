package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.api.domain.port.output.NotificationPort;
import onlydust.com.marketplace.api.slack.SlackApiAdapter;
import onlydust.com.marketplace.api.slack.SlackProperties;
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
    public NotificationPort notificationPort(final SlackProperties slackProperties) {
        return new SlackApiAdapter(slackProperties);
    }
}
