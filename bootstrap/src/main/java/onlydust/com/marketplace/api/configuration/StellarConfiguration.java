package onlydust.com.marketplace.api.configuration;

import onlydust.com.marketplace.api.stellar.HorizonClient;
import onlydust.com.marketplace.api.stellar.SorobanClient;
import onlydust.com.marketplace.api.stellar.adapters.StellarAccountIdValidator;
import onlydust.com.marketplace.api.stellar.adapters.StellarERC20ProviderAdapter;
import onlydust.com.marketplace.api.stellar.adapters.StellarTransactionStorageAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StellarConfiguration {
    @Bean
    @ConfigurationProperties(value = "infrastructure.stellar.soroban", ignoreUnknownFields = false)
    public SorobanClient.Properties sorobanProperties() {
        return new SorobanClient.Properties();
    }

    @Bean
    public SorobanClient sorobanClient(SorobanClient.Properties sorobanProperties) {
        return new SorobanClient(sorobanProperties);
    }

    @Bean
    @ConfigurationProperties(value = "infrastructure.stellar.horizon", ignoreUnknownFields = false)
    public HorizonClient.Properties horizonProperties() {
        return new HorizonClient.Properties();
    }

    @Bean
    public HorizonClient horizonClient(HorizonClient.Properties horizonProperties) {
        return new HorizonClient(horizonProperties);
    }

    @Bean
    public StellarERC20ProviderAdapter stellarERC20Provider(SorobanClient sorobanClient) {
        return new StellarERC20ProviderAdapter(sorobanClient);
    }

    @Bean
    public StellarAccountIdValidator stellarAccountIdValidator() {
        return new StellarAccountIdValidator();
    }

    @Bean
    public StellarTransactionStorageAdapter stellarTransactionStorageAdapter(HorizonClient horizonClient) {
        return new StellarTransactionStorageAdapter(horizonClient);
    }
}
