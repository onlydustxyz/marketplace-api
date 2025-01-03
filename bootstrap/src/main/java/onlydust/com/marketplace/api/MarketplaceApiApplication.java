package onlydust.com.marketplace.api;

import com.onlydust.marketplace.api.cron.JobScheduler;
import jakarta.annotation.PostConstruct;
import com.onlydust.marketplace.indexer.SearchIndexerConfiguration;
import onlydust.com.marketplace.api.postgres.adapter.configuration.PostgresConfiguration;
import onlydust.com.marketplace.api.read.ReadApiConfiguration;
import onlydust.com.marketplace.api.rest.api.adapter.AppRestApi;
import onlydust.com.marketplace.api.rest.api.adapter.VersionRestApi;
import onlydust.com.marketplace.api.rest.api.adapter.exception.OnlydustExceptionRestHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
@EnableRetry
@EnableAsync
@Import({PostgresConfiguration.class, JobScheduler.class, ReadApiConfiguration.class, SearchIndexerConfiguration.class})
public class MarketplaceApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketplaceApiApplication.class, args);
    }

    @PostConstruct
    public static void setGlobalDefaults() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Locale.setDefault(Locale.ENGLISH);
    }

    @Bean()
    public Date startingDate() {
        return new Date();
    }

    @Bean
    public AppRestApi appRestApi() {
        return new AppRestApi();
    }

    @Bean
    public VersionRestApi versionRestApi(final Date startingDate) {
        return new VersionRestApi(startingDate);
    }

    @Bean
    public OnlydustExceptionRestHandler onlydustExceptionRestHandler() {
        return new OnlydustExceptionRestHandler();
    }

}
