package com.onlydust.marketplace.indexer;

import com.onlydust.marketplace.indexer.elasticsearch.ElasticSearchAdapter;
import com.onlydust.marketplace.indexer.elasticsearch.ElasticSearchHttpClient;
import com.onlydust.marketplace.indexer.elasticsearch.properties.ElasticSearchProperties;
import com.onlydust.marketplace.indexer.postgres.repository.ReadSearchContributorRepository;
import com.onlydust.marketplace.indexer.postgres.repository.ReadSearchProjectRepository;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.onlydust.marketplace.indexer.postgres.repository")
@EntityScan(basePackages = "com.onlydust.marketplace.indexer.postgres.entity")
@EnableTransactionManagement
public class SearchIndexerConfiguration {

    @Bean
    @ConfigurationProperties(value = "infrastructure.elasticsearch")
    public ElasticSearchProperties elasticSearchProperties() {
        return new ElasticSearchProperties();
    }

    @Bean
    public ElasticSearchHttpClient elasticSearchHttpClient(final ElasticSearchProperties elasticSearchProperties) {
        return new ElasticSearchHttpClient(elasticSearchProperties);
    }

    @Bean
    public SearchIndexationService searchIndexationService(final ReadSearchProjectRepository readSearchProjectRepository,
                                                           final ElasticSearchAdapter elasticSearchAdapter,
                                                           final ReadSearchContributorRepository readSearchContributorRepository) {
        return new SearchIndexationService(elasticSearchAdapter, readSearchProjectRepository, readSearchContributorRepository);
    }

    @Bean
    public ElasticSearchAdapter elasticSearchAdapter(final ElasticSearchHttpClient elasticSearchHttpClient) {
        return new ElasticSearchAdapter(elasticSearchHttpClient);
    }
}
