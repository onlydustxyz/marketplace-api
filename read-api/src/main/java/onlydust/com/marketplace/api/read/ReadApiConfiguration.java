package onlydust.com.marketplace.api.read;

import jakarta.persistence.EntityManager;
import onlydust.com.marketplace.api.read.mapper.NotificationMapper;
import onlydust.com.marketplace.api.read.properties.Cache;
import onlydust.com.marketplace.api.read.repositories.*;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAutoConfiguration
@EntityScan(basePackages = {
        "onlydust.com.marketplace.api.read.entities"
})
@EnableJpaRepositories(basePackages = {
        "onlydust.com.marketplace.api.read.repositories"
})
@EnableTransactionManagement
@Configuration
public class ReadApiConfiguration {

    @Bean
    public NotificationMapper notificationMapper(
            final ProjectLinkReadRepository projectLinkReadRepository,
            final SponsorReadRepository sponsorReadRepository,
            final ProgramReadRepository programReadRepository,
            final ProjectReadRepository projectReadRepository,
            final CurrencyReadRepository currencyReadRepository
    ) {
        return new NotificationMapper(
                projectLinkReadRepository,
                sponsorReadRepository,
                programReadRepository,
                projectReadRepository,
                currencyReadRepository
        );
    }

    @Bean
    public AggregatedKpisReadRepository aggregatedKpisReadRepository(
            final EntityManager entityManager
    ) {
        return new AggregatedKpisReadRepository(entityManager);
    }

    @Bean
    public BiFinancialMonthlyStatsReadRepository biFinancialMonthlyStatsReadRepository(
            final EntityManager entityManager
    ) {
        return new BiFinancialMonthlyStatsReadRepository(entityManager);
    }

    @Bean
    @ConfigurationProperties(value = "application.cache", ignoreUnknownFields = false)
    public Cache cache() {
        return new Cache();
    }

}
