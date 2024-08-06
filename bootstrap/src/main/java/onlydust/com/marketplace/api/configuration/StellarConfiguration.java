package onlydust.com.marketplace.api.configuration;

import onlydust.com.marketplace.api.stellar.StellarAccountIdValidator;
import onlydust.com.marketplace.api.stellar.StellarClient;
import onlydust.com.marketplace.api.stellar.StellarERC20ProviderAdapter;
import onlydust.com.marketplace.api.stellar.StellarTransactionStorageAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StellarConfiguration {
    @Bean
    @ConfigurationProperties(value = "infrastructure.stellar", ignoreUnknownFields = false)
    public StellarClient.Properties stellarProperties() {
        return new StellarClient.Properties();
    }

    @Bean
    public StellarClient stellarClient(StellarClient.Properties properties) {
        return new StellarClient(properties);
    }

    @Bean
    public StellarERC20ProviderAdapter stellarERC20Provider() {
        return new StellarERC20ProviderAdapter();
    }

    @Bean
    public StellarAccountIdValidator stellarAccountIdValidator() {
        return new StellarAccountIdValidator();
    }

    @Bean
    public StellarTransactionStorageAdapter stellarTransactionStorageAdapter(StellarClient client) {
        return new StellarTransactionStorageAdapter(client);
    }
}
