package onlydust.com.marketplace.api.configuration;

import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileVerificationFacadePort;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubWebhookApiAdapter;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubWebhookProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SumsubWebhookConfiguration {

    @Bean
    @ConfigurationProperties(value = "application.sumsub.webhook", ignoreUnknownFields = false)
    public SumsubWebhookProperties sumsubProperties() {
        return new SumsubWebhookProperties();
    }

    @Bean
    public SumsubWebhookApiAdapter sumsubWebhookApiAdapter(final BillingProfileVerificationFacadePort billingProfileVerificationFacadePort,
                                                           SumsubWebhookProperties sumsubWebhookProperties) {
        return new SumsubWebhookApiAdapter(sumsubWebhookProperties, billingProfileVerificationFacadePort);
    }

}
