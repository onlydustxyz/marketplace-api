package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.project.domain.port.output.IndexerPort;
import onlydust.com.marketplace.api.indexer.api.client.adapter.IndexerApiClientAdapter;
import onlydust.com.marketplace.api.indexer.api.client.adapter.IndexerApiHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IndexerApiClientConfiguration {

    @Bean
    @ConfigurationProperties(value = "infrastructure.indexer.api.client")
    public IndexerApiHttpClient.Properties indexerApiHttpClientProperties() {
        return new IndexerApiHttpClient.Properties();
    }

    @Bean
    public IndexerApiHttpClient indexerApiHttpClient(final IndexerApiHttpClient.Properties indexerApiHttpClientProperties) {
        return new IndexerApiHttpClient(indexerApiHttpClientProperties);
    }

    @Bean
    public IndexerPort indexerPort(final IndexerApiHttpClient indexerApiHttpClient) {
        return new IndexerApiClientAdapter(indexerApiHttpClient);
    }
}
