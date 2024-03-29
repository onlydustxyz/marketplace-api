package onlydust.com.marketplace.api.bootstrap.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import onlydust.com.marketplace.accounting.domain.port.out.MailNotificationPort;
import onlydust.com.marketplace.api.webhook.Config;
import onlydust.com.marketplace.api.webhook.MakeWebhookHttpClient;
import onlydust.com.marketplace.api.webhook.adapters.MakeWebhookAdapter;
import onlydust.com.marketplace.kernel.port.output.WebhookPort;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class MakeWebhookConfiguration {

    @Bean
    @ConfigurationProperties(value = "infrastructure.make.webhook")
    public Config webhookHttpClientProperties() {
        return new Config();
    }

    @Bean
    public MakeWebhookHttpClient webhookHttpClient(final ObjectMapper objectMapper,
                                                   final HttpClient httpClient,
                                                   final Config webhookHttpClientProperties) {
        return new MakeWebhookHttpClient(objectMapper, httpClient, webhookHttpClientProperties);
    }

    @Bean
    public WebhookPort webhookNotificationPort(final MakeWebhookHttpClient makeWebhookHttpClient,
                                               final Config webhookHttpClientProperties) {
        return new MakeWebhookAdapter(makeWebhookHttpClient, webhookHttpClientProperties);
    }

    @Bean
    public MailNotificationPort mailNotificationPort(final MakeWebhookHttpClient makeWebhookHttpClient,
                                                        final Config webhookHttpClientProperties) {
        return new MakeWebhookAdapter(makeWebhookHttpClient, webhookHttpClientProperties);
    }


}
