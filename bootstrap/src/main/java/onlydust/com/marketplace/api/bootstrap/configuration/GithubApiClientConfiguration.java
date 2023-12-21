package onlydust.com.marketplace.api.bootstrap.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import onlydust.com.marketplace.api.domain.port.output.GithubAuthenticationPort;
import onlydust.com.marketplace.api.domain.port.output.GithubSearchPort;
import onlydust.com.marketplace.api.domain.service.RetriedGithubInstallationFacade;
import onlydust.com.marketplace.api.github_api.GithubHttpClient;
import onlydust.com.marketplace.api.github_api.adapters.GithubDustyBotAdapter;
import onlydust.com.marketplace.api.github_api.adapters.GithubSearchApiAdapter;
import onlydust.com.marketplace.api.github_api.properties.GithubPaginationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class GithubApiClientConfiguration {

    @Bean
    @ConfigurationProperties("application.github.installation.retry")
    public RetriedGithubInstallationFacade.Config config() {
        return new RetriedGithubInstallationFacade.Config();
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
    @ConfigurationProperties("infrastructure.github")
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
    public GithubHttpClient githubHttpClient(final ObjectMapper objectMapper, final HttpClient httpClient,
                                             final GithubHttpClient.Config githubConfig) {
        return new GithubHttpClient(objectMapper, httpClient, githubConfig);
    }

    @Bean
    @ConfigurationProperties("infrastructure.dusty-bot")
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


}
