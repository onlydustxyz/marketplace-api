package onlydust.com.marketplace.api.bootstrap.configuration;

import com.onlydust.api.sumsub.api.client.adapter.SumsubApiClientAdapter;
import com.onlydust.api.sumsub.api.client.adapter.SumsubClientProperties;
import com.onlydust.api.sumsub.api.client.adapter.SumsubHttpClient;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileVerificationProviderPort;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SumsubApiClientConfiguration {

    @Bean
    @ConfigurationProperties(value = "infrastructure.sumsub")
    public SumsubClientProperties sumsubClientProperties() {
        return new SumsubClientProperties();
    }

    @Bean
    public BillingProfileVerificationProviderPort billingProfileVerificationProviderPort(final SumsubClientProperties sumsubClientProperties) {
        return new SumsubApiClientAdapter(sumsubClientProperties, new SumsubHttpClient(sumsubClientProperties));
    }
}
