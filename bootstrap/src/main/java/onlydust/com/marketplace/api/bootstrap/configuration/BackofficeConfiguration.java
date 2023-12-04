package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.api.domain.port.input.BackofficeFacadePort;
import onlydust.com.marketplace.api.domain.port.output.BackofficeStoragePort;
import onlydust.com.marketplace.api.domain.service.BackofficeService;
import onlydust.com.marketplace.api.rest.api.adapter.BackofficeRestApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BackofficeConfiguration {

    @Bean
    public BackofficeRestApi backofficeRestApi(final BackofficeFacadePort backofficeFacadePort) {
        return new BackofficeRestApi(backofficeFacadePort);
    }

    @Bean
    public BackofficeFacadePort backofficeFacadePort(final BackofficeStoragePort backofficeStoragePort) {
        return new BackofficeService(backofficeStoragePort);
    }
}
