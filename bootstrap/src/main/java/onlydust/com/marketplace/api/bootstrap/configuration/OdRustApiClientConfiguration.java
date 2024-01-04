package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.api.domain.port.output.RewardServicePort;
import onlydust.com.marketplace.api.od.rust.api.client.adapter.OdRustApiClientAdapter;
import onlydust.com.marketplace.api.od.rust.api.client.adapter.OdRustApiHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OdRustApiClientConfiguration {

    @Bean
    @ConfigurationProperties(value = "infrastructure.od.api.client")
    public OdRustApiHttpClient.Properties odRustApiHttpClientProperties() {
        return new OdRustApiHttpClient.Properties();
    }

    @Bean
    public OdRustApiHttpClient odRustApiHttpClient(final OdRustApiHttpClient.Properties odRustApiHttpClientProperties) {
        return new OdRustApiHttpClient(odRustApiHttpClientProperties);
    }

    @Bean
    public RewardServicePort rewardStoragePort(final OdRustApiHttpClient odRustApiHttpClient) {
        return new OdRustApiClientAdapter(odRustApiHttpClient);
    }
}
