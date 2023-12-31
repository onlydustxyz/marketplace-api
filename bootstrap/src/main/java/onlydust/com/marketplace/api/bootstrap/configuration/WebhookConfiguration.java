package onlydust.com.marketplace.api.bootstrap.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import onlydust.com.marketplace.api.domain.port.output.WebhookPort;
import onlydust.com.marketplace.api.webhook.Config;
import onlydust.com.marketplace.api.webhook.WebhookHttpClient;
import onlydust.com.marketplace.api.webhook.adapters.WebhookAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class WebhookConfiguration {

    @Bean
    @ConfigurationProperties(value = "infrastructure.webhook")
    public Config webhookHttpClientProperties() {
        return new Config();
    }

    @Bean
    public WebhookHttpClient webhookHttpClient(final ObjectMapper objectMapper,
                                               final HttpClient httpClient,
                                               final Config webhookHttpClientProperties) {
        return new WebhookHttpClient(objectMapper, httpClient, webhookHttpClientProperties);
    }

    @Bean
    public WebhookPort webhookPort(final WebhookHttpClient webhookHttpClient,
                                   final Config webhookHttpClientProperties) {
        return new WebhookAdapter(webhookHttpClient, webhookHttpClientProperties);
    }
}
