package onlydust.com.marketplace.api.bootstrap.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import onlydust.com.marketplace.api.domain.port.input.ContributorFacadePort;
import onlydust.com.marketplace.api.domain.port.input.GithubInstallationFacadePort;
import onlydust.com.marketplace.api.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.api.domain.port.output.GithubSearchPort;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.domain.port.output.ImageStoragePort;
import onlydust.com.marketplace.api.domain.port.output.UUIDGeneratorPort;
import onlydust.com.marketplace.api.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.api.domain.service.*;
import onlydust.com.marketplace.api.github_api.GithubHttpClient;
import onlydust.com.marketplace.api.github_api.adapters.GithubSearchApiAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresGithubAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresProjectAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresUserAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
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

    @Bean
    public ContributorFacadePort contributorFacadePort(final ProjectStoragePort projectStoragePort, final GithubSearchPort githubSearchPort, final UserStoragePort userStoragePort) {
        return new ContributorService(projectStoragePort, githubSearchPort, userStoragePort);
    }

    @Bean
    public GithubSearchPort githubSearchPort(final GithubHttpClient githubHttpClient) {
        return new GithubSearchApiAdapter(githubHttpClient);
    }

    @Bean
    public GithubHttpClient githubHttpClient(final ObjectMapper objectMapper, final HttpClient httpClient, final GithubHttpClient.Config config) {
        return new GithubHttpClient(objectMapper, httpClient, config);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

    @Bean
    @ConfigurationProperties("infrastructure.github")
    GithubHttpClient.Config githubConfig() {
        return new GithubHttpClient.Config();
    }
}
