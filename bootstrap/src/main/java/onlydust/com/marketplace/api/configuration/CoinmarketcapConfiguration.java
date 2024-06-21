package onlydust.com.marketplace.api.configuration;

import onlydust.com.marketplace.api.infrastructure.coinmarketcap.CmcClient;
import onlydust.com.marketplace.api.infrastructure.coinmarketcap.adapters.CmcCurrencyMetadataServiceAdapter;
import onlydust.com.marketplace.api.infrastructure.coinmarketcap.adapters.CmcQuoteServiceAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoinmarketcapConfiguration {
    @Bean
    @ConfigurationProperties(value = "infrastructure.coinmarketcap", ignoreUnknownFields = false)
    public CmcClient.Properties cmcClientProperties() {
        return new CmcClient.Properties();
    }

    @Bean
    public CmcClient cmcClient(final CmcClient.Properties cmcClientProperties) {
        return new CmcClient(cmcClientProperties);
    }

    @Bean
    public CmcQuoteServiceAdapter cmcQuoteServiceAdapter(final CmcClient cmcClient) {
        return new CmcQuoteServiceAdapter(cmcClient);
    }

    @Bean
    public CmcCurrencyMetadataServiceAdapter cmcCurrencyMetadataServiceAdapter(final CmcClient cmcClient) {
        return new CmcCurrencyMetadataServiceAdapter(cmcClient);
    }
}
