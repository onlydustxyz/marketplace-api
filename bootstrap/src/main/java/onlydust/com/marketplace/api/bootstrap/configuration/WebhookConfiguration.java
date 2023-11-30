package onlydust.com.marketplace.api.bootstrap.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import onlydust.com.marketplace.api.domain.port.output.WebhookPort;
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
    public WebhookHttpClient.Config webhookHttpClientProperties() {
        return new WebhookHttpClient.Config();
    }

    @Bean
    public WebhookHttpClient webhookHttpClient(final ObjectMapper objectMapper,
                                               final HttpClient httpClient,
                                               final WebhookHttpClient.Config webhookHttpClientProperties) {
        return new WebhookHttpClient(objectMapper, httpClient, webhookHttpClientProperties);
    }

    @Bean
    public WebhookPort webhookPort(final WebhookHttpClient webhookHttpClient) {
        return new WebhookAdapter(webhookHttpClient);
    }
}
