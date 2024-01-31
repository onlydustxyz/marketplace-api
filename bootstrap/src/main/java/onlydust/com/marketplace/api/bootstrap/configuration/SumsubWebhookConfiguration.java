package onlydust.com.marketplace.api.bootstrap.configuration;

import com.github.javafaker.Faker;
import lombok.Data;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.LegalVerificationFacadePort;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubProperties;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubWebhookApiAdapter;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.dto.SumsubWebhookDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SumsubWebhookConfiguration {

    @Bean
    public SumsubProperties sumsubProperties() {
        final Faker faker = new Faker();
        final SumsubProperties sumsubProperties = new SumsubProperties();
        sumsubProperties.setSecret(faker.rickAndMorty().character());
        sumsubProperties.setOdApiHeader(faker.harryPotter().character());
        return sumsubProperties;
    }

    @Bean
    public LegalVerificationFacadePort legalVerificationFacadePort() {
        return new LegalVerificationFacadePortSpy();
    }

    @Bean
    public SumsubWebhookApiAdapter sumsubWebhookApiAdapter(final LegalVerificationFacadePort legalVerificationFacadePort, SumsubProperties sumsubProperties) {
        return new SumsubWebhookApiAdapter(sumsubProperties, legalVerificationFacadePort);
    }

    @Data
    public static class LegalVerificationFacadePortSpy implements LegalVerificationFacadePort {

        private SumsubWebhookDTO sumsubWebhookDTO;

        @Override
        public void update(SumsubWebhookDTO sumsubWebhookDTO) {
            this.sumsubWebhookDTO = sumsubWebhookDTO;
        }
    }

}
