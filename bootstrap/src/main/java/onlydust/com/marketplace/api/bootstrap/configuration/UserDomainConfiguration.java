package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.user.domain.port.input.BackofficeUserFacadePort;
import onlydust.com.marketplace.user.domain.service.BackofficeUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserDomainConfiguration {
    @Bean
    public BackofficeUserFacadePort backofficeUserFacadePort() {
        return new BackofficeUserService();
    }
}
