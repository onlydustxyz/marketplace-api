package onlydust.com.marketplace.api.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import onlydust.com.marketplace.api.github_api.GithubHttpClient;
import onlydust.com.marketplace.api.github_api.adapters.*;
import onlydust.com.marketplace.api.github_api.properties.GithubPaginationProperties;
import onlydust.com.marketplace.kernel.infrastructure.github.GithubAppJwtBuilder;
import onlydust.com.marketplace.project.domain.port.output.*;
import onlydust.com.marketplace.project.domain.service.GithubAppService;
import onlydust.com.marketplace.project.domain.service.RetriedGithubInstallationFacade;
import onlydust.com.marketplace.user.domain.port.output.GithubOAuthAppPort;
import onlydust.com.marketplace.user.domain.port.output.GithubUserStoragePort;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class GithubApiClientConfiguration {

    @Bean
    @ConfigurationProperties(value = "application.github.installation.retry", ignoreUnknownFields = false)
    public RetriedGithubInstallationFacade.Config config() {
        return new RetriedGithubInstallationFacade.Config();
    }

    @Bean
    @ConfigurationProperties(value = "infrastructure.github-app", ignoreUnknownFields = false)
    GithubAppJwtBuilder.Config githubAppConfig() {
        return new GithubAppJwtBuilder.Config();
    }

    @Bean
    public GithubAppJwtBuilder githubAppJwtBuilder(final GithubAppJwtBuilder.Config githubAppConfig) {
        return new GithubAppJwtBuilder(githubAppConfig);
    }

    @Bean
    public GithubAppService githubAppService(final GithubStoragePort githubStoragePort,
                                             final GithubAppApiPort githubAppApiPort) {
        return new GithubAppService(githubStoragePort, githubAppApiPort);
    }

    @Bean
    public GithubAppAdapter githubAppAdapter(final GithubHttpClient githubHttpClient,
                                             final GithubAppJwtBuilder githubAppJwtBuilder) {
        return new GithubAppAdapter(githubHttpClient, githubAppJwtBuilder);
    }

    @Bean
    public ObjectMapper objectMapper() {
        final var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

    @Bean
    @ConfigurationProperties(value = "infrastructure.github", ignoreUnknownFields = false)
    GithubHttpClient.Config githubConfig() {
        return new GithubHttpClient.Config();
    }

    @Bean
    public GithubSearchPort githubSearchPort(final GithubHttpClient githubHttpClient,
                                             final GithubAuthenticationPort githubAuthenticationPort) {
        return new GithubSearchApiAdapter(githubHttpClient, GithubPaginationProperties.builder().build(),
                githubAuthenticationPort);
    }

    @Bean
    public GithubApiPort githubApiPort(final GithubHttpClient githubHttpClient) {
        return new GithubApiAdapter(githubHttpClient);
    }

    @Bean
    public GithubHttpClient githubHttpClient(final ObjectMapper objectMapper, final HttpClient httpClient,
                                             final GithubHttpClient.Config githubConfig) {
        return new GithubHttpClient(objectMapper, httpClient, githubConfig);
    }

    @Bean
    @ConfigurationProperties(value = "infrastructure.dusty-bot", ignoreUnknownFields = false)
    GithubHttpClient.Config githubDustyBotConfig() {
        return new GithubHttpClient.Config();
    }

    @Bean
    public GithubHttpClient dustyBotClient(final ObjectMapper objectMapper, final HttpClient httpClient,
                                           final GithubHttpClient.Config githubDustyBotConfig) {
        return new GithubHttpClient(objectMapper, httpClient, githubDustyBotConfig);
    }

    @Bean
    public GithubDustyBotAdapter githubDustyBotAdapter(final GithubHttpClient dustyBotClient) {
        return new GithubDustyBotAdapter(dustyBotClient);
    }

    @Bean
    public GithubAuthenticationInfoAdapter githubAuthenticationInfoAdapter(final GithubHttpClient githubHttpClient) {
        return new GithubAuthenticationInfoAdapter(githubHttpClient);
    }

    @Bean
    public GithubUserStoragePort githubUserStoragePort(final GithubHttpClient githubHttpClient,
                                                       final GithubAuthenticationPort githubAuthenticationPort) {
        return new GithubSearchApiAdapter(githubHttpClient, GithubPaginationProperties.builder().build(), githubAuthenticationPort);
    }

    @Bean
    public GithubOAuthAppPort githubOAuthAppPort(final GithubHttpClient.Config githubConfig) {
        return new GithubOAuthAppAdapter(githubConfig);
    }
}
