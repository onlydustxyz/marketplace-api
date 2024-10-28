package onlydust.com.marketplace.api.configuration;

import onlydust.com.marketplace.api.near.NearClient;
import onlydust.com.marketplace.api.near.adapters.NearTransactionStorageAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NearConfiguration {
    @Bean
    @ConfigurationProperties(value = "infrastructure.near", ignoreUnknownFields = false)
    public NearClient.Properties nearProperties() {
        return new NearClient.Properties();
    }

    @Bean
    public NearClient nearClient(NearClient.Properties nearProperties) {
        return NearClient.create(nearProperties);
    }

    @Bean
    public NearTransactionStorageAdapter nearTransactionStorageAdapter(NearClient nearClient) {
        return new NearTransactionStorageAdapter(nearClient);
    }
}
