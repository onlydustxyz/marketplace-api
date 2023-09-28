package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.api.rest.api.adapter.ProjectRestApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestApiConfiguration {

    @Bean
    public ProjectRestApi projectRestApi() {
        return new ProjectRestApi();
    }
}
