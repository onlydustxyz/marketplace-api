package onlydust.com.marketplace.api.bootstrap.configuration;

import com.onlydust.marketplace.api.cron.properties.CronProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CronConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "application.cron")
    public CronProperties cronProperties() {
        return new CronProperties();
    }
}
