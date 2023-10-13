package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.api.domain.port.input.GithubInstallationFacadePort;
import onlydust.com.marketplace.api.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.api.domain.port.output.ImageStoragePort;
import onlydust.com.marketplace.api.domain.port.output.UUIDGeneratorPort;
import onlydust.com.marketplace.api.domain.service.GithubInstallationService;
import onlydust.com.marketplace.api.domain.service.ProjectService;
import onlydust.com.marketplace.api.domain.service.RetriedGithubInstallationFacade;
import onlydust.com.marketplace.api.domain.service.UserService;
import onlydust.com.marketplace.api.postgres.adapter.PostgresGithubAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresProjectAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresUserAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class DomainConfiguration {


    @Bean
    public UUIDGeneratorPort uuidGeneratorPort() {
        return UUID::randomUUID;
    }

    @Bean
    public ProjectFacadePort projectFacadePort(final PostgresProjectAdapter postgresProjectAdapter,
                                               final ImageStoragePort imageStoragePort,
                                               final UUIDGeneratorPort uuidGeneratorPort) {
        return new ProjectService(postgresProjectAdapter, imageStoragePort, uuidGeneratorPort);
    }

    @Bean
    @ConfigurationProperties("application.github.installation.retry")
    public RetriedGithubInstallationFacade.Config config() {
        return new RetriedGithubInstallationFacade.Config();
    }

    @Bean
    public GithubInstallationFacadePort githubInstallationFacadePort(
            final PostgresGithubAdapter postgresGithubAdapter,
            final RetriedGithubInstallationFacade.Config config
    ) {
        return new RetriedGithubInstallationFacade(new GithubInstallationService(postgresGithubAdapter), config);
    }

    @Bean
    public UserFacadePort userFacadePort(final PostgresUserAdapter postgresUserAdapter) {
        return new UserService(postgresUserAdapter);
    }
}
