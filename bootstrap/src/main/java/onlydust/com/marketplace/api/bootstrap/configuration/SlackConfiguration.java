package onlydust.com.marketplace.api.bootstrap.configuration;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import onlydust.com.marketplace.api.slack.SlackApiAdapter;
import onlydust.com.marketplace.api.slack.SlackProperties;
import onlydust.com.marketplace.project.domain.port.output.HackathonStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackConfiguration {

    @Bean
    @ConfigurationProperties(value = "infrastructure.slack", ignoreUnknownFields = false)
    public SlackProperties slackProperties() {
        return new SlackProperties();
    }

    @Bean
    public MethodsClient slackClient(final SlackProperties slackProperties) {
        return Slack.getInstance().methods(slackProperties.getToken());
    }

    @Bean
    public SlackApiAdapter slackApiAdapter(final SlackProperties slackProperties,
                                           final MethodsClient slackClient,
                                           final UserStoragePort userStoragePort,
                                           final ProjectStoragePort projectStoragePort,
                                           final HackathonStoragePort hackathonStoragePort) {
        return new SlackApiAdapter(slackProperties, slackClient, userStoragePort, projectStoragePort, hackathonStoragePort);
    }
}
