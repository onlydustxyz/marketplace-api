package onlydust.com.marketplace.api.bootstrap;

import com.onlydust.marketplace.api.cron.JobScheduler;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import onlydust.com.marketplace.api.postgres.adapter.configuration.PostgresConfiguration;
import onlydust.com.marketplace.api.rest.api.adapter.AppRestApi;
import onlydust.com.marketplace.api.rest.api.adapter.VersionRestApi;
import onlydust.com.marketplace.api.rest.api.adapter.exception.OnlydustExceptionRestHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

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
