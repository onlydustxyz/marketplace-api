package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.api.domain.service.ProjectService;
import onlydust.com.marketplace.api.domain.service.UserService;
import onlydust.com.marketplace.api.postgres.adapter.PostgresProjectAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresUserAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfiguration {

    @Bean
    public ProjectService projectService(final PostgresProjectAdapter postgresProjectAdapter) {
        return new ProjectService(postgresProjectAdapter);
    }

    @Bean
    public UserService userService(final PostgresUserAdapter postgresUserAdapter) {
        return new UserService(postgresUserAdapter);
    }
}
