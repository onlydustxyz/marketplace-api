package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.api.domain.port.input.UserVerificationFacadePort;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubWebhookProperties;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubWebhookApiAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SumsubWebhookConfiguration {

    @Bean
    @ConfigurationProperties("application.sumsub.webhook")
    public SumsubWebhookProperties sumsubProperties() {
        return new SumsubWebhookProperties();
    }

    @Bean
    public SumsubWebhookApiAdapter sumsubWebhookApiAdapter(final UserVerificationFacadePort userVerificationFacadePort, SumsubWebhookProperties sumsubWebhookProperties) {
        return new SumsubWebhookApiAdapter(sumsubWebhookProperties, userVerificationFacadePort);
    }

}
