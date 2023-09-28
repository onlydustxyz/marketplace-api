package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.api.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.api.rest.api.adapter.ProjectRestApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestApiConfiguration {

    @Bean
    public ProjectRestApi projectRestApi(final ProjectFacadePort projectFacadePort) {
        return new ProjectRestApi(projectFacadePort);
    }
}
