package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.domain.service.ProjectService;
import onlydust.com.marketplace.api.postgres.adapter.PostgresProjectAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfiguration {

    @Bean
    public ProjectService projectService(final PostgresProjectAdapter postgresProjectAdapter) {
        return new ProjectService(postgresProjectAdapter);
    }
}
