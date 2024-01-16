package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.api.infura.InfuraClient;
import onlydust.com.marketplace.api.infura.adapters.InfuraERC20ProviderAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfuraConfiguration {
    @Bean
    @ConfigurationProperties("infrastructure.ethereum")
    public InfuraClient.Properties ethereumProperties() {
        return new InfuraClient.Properties();
    }

    @Bean
    public InfuraERC20ProviderAdapter ethereumERC20Provider(final InfuraClient.Properties ethereumProperties) {
        return new InfuraERC20ProviderAdapter(ethereumProperties);
    }

    @Bean
    @ConfigurationProperties("infrastructure.optimism")
    public InfuraClient.Properties optimismProperties() {
        return new InfuraClient.Properties();
    }

    @Bean
    public InfuraERC20ProviderAdapter optimismERC20Provider(final InfuraClient.Properties optimismProperties) {
        return new InfuraERC20ProviderAdapter(optimismProperties);
    }
}
