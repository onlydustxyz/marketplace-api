package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.api.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.api.rest.api.adapter.ProjectsRestApi;
import onlydust.com.marketplace.api.rest.api.adapter.UsersRestApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestApiConfiguration {

    @Bean
    public ProjectsRestApi projectRestApi(final ProjectFacadePort projectFacadePort) {
        return new ProjectsRestApi(projectFacadePort);
    }

    @Bean
    public UsersRestApi usersRestApi(final UserFacadePort userFacadePort) {
        return new UsersRestApi(userFacadePort);
    }
}
