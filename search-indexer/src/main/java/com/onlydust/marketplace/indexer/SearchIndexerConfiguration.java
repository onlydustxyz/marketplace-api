package com.onlydust.marketplace.indexer;

import com.onlydust.marketplace.indexer.elasticsearch.ElasticSearchAdapter;
import com.onlydust.marketplace.indexer.elasticsearch.properties.ElasticSearchProperties;
import com.onlydust.marketplace.indexer.postgres.repository.ReadProjectIndexRepository;
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
    public SearchIndexationService searchIndexationService(final ReadProjectIndexRepository readProjectIndexRepository,
                                                           final ElasticSearchAdapter elasticSearchAdapter) {
        return new SearchIndexationService(readProjectIndexRepository,elasticSearchAdapter);
    }

    @Bean
    public ElasticSearchAdapter elasticSearchAdapter(final ElasticSearchProperties elasticSearchProperties) {
        return new ElasticSearchAdapter(elasticSearchProperties);
    }

    @Bean
    @ConfigurationProperties(value = "infrastructure.elasticsearch")
    public ElasticSearchProperties elasticSearchProperties() {
        return new ElasticSearchProperties();
    }
}
