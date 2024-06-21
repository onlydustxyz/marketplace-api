package onlydust.com.marketplace.api.configuration;

import onlydust.com.marketplace.api.posthog.adapters.PosthogApiClientAdapter;
import onlydust.com.marketplace.api.posthog.client.PosthogHttpClient;
import onlydust.com.marketplace.api.posthog.properties.PosthogProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PosthogConfiguration {
    @Bean
    @ConfigurationProperties(value = "infrastructure.posthog", ignoreUnknownFields = false)
    public PosthogProperties posthogProperties() {
        return new PosthogProperties();
    }

    @Bean
    public PosthogApiClientAdapter posthogApiClientAdapter(final PosthogProperties posthogProperties) {
        return new PosthogApiClientAdapter(posthogProperties, new PosthogHttpClient(posthogProperties));
    }
}
