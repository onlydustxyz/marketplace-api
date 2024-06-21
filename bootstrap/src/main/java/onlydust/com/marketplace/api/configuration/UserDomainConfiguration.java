package onlydust.com.marketplace.api.configuration;

import onlydust.com.marketplace.user.domain.port.input.BackofficeUserFacadePort;
import onlydust.com.marketplace.user.domain.port.output.BackofficeUserStoragePort;
import onlydust.com.marketplace.user.domain.service.BackofficeUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserDomainConfiguration {

    @Bean
    public BackofficeUserFacadePort backofficeUserFacadePort(final BackofficeUserStoragePort backofficeUserStoragePort) {
        return new BackofficeUserService(backofficeUserStoragePort);
    }
}
