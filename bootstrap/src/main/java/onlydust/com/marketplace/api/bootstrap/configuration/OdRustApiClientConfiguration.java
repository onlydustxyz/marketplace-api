package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.accounting.domain.port.out.OldRewardStoragePort;
import onlydust.com.marketplace.api.od.rust.api.client.adapter.OdRustApiClientAdapter;
import onlydust.com.marketplace.api.od.rust.api.client.adapter.OdRustApiHttpClient;
import onlydust.com.marketplace.project.domain.port.output.RewardServicePort;
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

    @Bean
    public OldRewardStoragePort oldRewardStoragePort(final OdRustApiHttpClient odRustApiHttpClient) {
        return new OdRustApiClientAdapter(odRustApiHttpClient);
    }
}
