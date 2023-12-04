package onlydust.com.marketplace.api.bootstrap;

import com.onlydust.marketplace.api.cron.JobScheduler;
import onlydust.com.marketplace.api.postgres.adapter.configuration.PostgresConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
@EnableRetry
@Import({PostgresConfiguration.class, JobScheduler.class})
public class MarketplaceApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketplaceApiApplication.class, args);
    }

    @PostConstruct
    public void init() {
        // Setting default JVM timezone as UTC
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Locale.setDefault(Locale.ENGLISH);
    }

    @Bean()
    public Date startingDate() {
        return new Date();
    }
}
