package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.api.domain.port.input.BackofficeFacadePort;
import onlydust.com.marketplace.api.domain.port.output.BackofficeStoragePort;
import onlydust.com.marketplace.api.domain.service.BackofficeService;
import onlydust.com.marketplace.api.rest.api.adapter.BackofficeRestApi;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key.ApiKeyAuthenticationService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("bo")
public class BackofficeConfiguration {

    @Bean
    public BackofficeRestApi backofficeRestApi(final BackofficeFacadePort backofficeFacadePort) {
        return new BackofficeRestApi(backofficeFacadePort);
    }

    @Bean
    public BackofficeFacadePort backofficeFacadePort(final BackofficeStoragePort backofficeStoragePort) {
        return new BackofficeService(backofficeStoragePort);
    }

    @Bean
    @ConfigurationProperties("application.web.back-office")
    public ApiKeyAuthenticationService.Config apiKeyAuthenticationConfig() {
        return new ApiKeyAuthenticationService.Config();
    }
}
