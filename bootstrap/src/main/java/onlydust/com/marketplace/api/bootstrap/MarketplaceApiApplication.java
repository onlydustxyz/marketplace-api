package onlydust.com.marketplace.api.bootstrap;

import onlydust.com.marketplace.api.postgres.adapter.configuration.PostgresConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.Date;

@SpringBootApplication
@EnableConfigurationProperties
@Import(PostgresConfiguration.class)
public class MarketplaceApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketplaceApiApplication.class, args);
    }


    @Bean()
    public Date startingDate() {
        return new Date();
    }
}
