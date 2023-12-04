package onlydust.com.marketplace.api.bootstrap.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import onlydust.com.marketplace.api.linear.LinearGraphqlClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertySource;

import java.net.http.HttpClient;

@Configuration
public class LinearConfiguration {
    @Bean
    @ConfigurationProperties("infrastructure.linear")
    public LinearGraphqlClient.Config linearConfig() {
        return new LinearGraphqlClient.Config();
    }

    @Bean
    public LinearGraphqlClient linearGraphqlClient(final LinearGraphqlClient.Config config) {
        return new LinearGraphqlClient(new ObjectMapper(), HttpClient.newHttpClient(), config);
    }
}
