package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.api.domain.port.input.UserVerificationFacadePort;
import onlydust.com.marketplace.api.domain.port.output.OutboxPort;
import onlydust.com.marketplace.api.domain.service.UserVerificationService;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubProperties;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubWebhookApiAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SumsubWebhookConfiguration {

    @Bean
    @ConfigurationProperties("application.sumsub.webhook")
    public SumsubProperties sumsubProperties() {
        return new SumsubProperties();
    }

    @Bean
    public UserVerificationFacadePort userVerificationFacadePort(OutboxPort userVerificationOutbox) {
        return new UserVerificationService(userVerificationOutbox);
    }

    @Bean
    public SumsubWebhookApiAdapter sumsubWebhookApiAdapter(final UserVerificationFacadePort userVerificationFacadePort, SumsubProperties sumsubProperties) {
        return new SumsubWebhookApiAdapter(sumsubProperties, userVerificationFacadePort);
    }

}
