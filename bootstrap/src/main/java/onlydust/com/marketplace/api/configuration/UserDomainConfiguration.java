package onlydust.com.marketplace.api.configuration;

import onlydust.com.marketplace.kernel.port.output.IndexerPort;
import onlydust.com.marketplace.user.domain.port.input.AppUserFacadePort;
import onlydust.com.marketplace.user.domain.port.input.BackofficeUserFacadePort;
import onlydust.com.marketplace.user.domain.port.output.*;
import onlydust.com.marketplace.user.domain.service.AppUserService;
import onlydust.com.marketplace.user.domain.service.BackofficeUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserDomainConfiguration {

    @Bean
    public BackofficeUserFacadePort backofficeUserFacadePort(final BackofficeUserStoragePort backofficeUserStoragePort) {
        return new BackofficeUserService(backofficeUserStoragePort);
    }

    @Bean
    public AppUserFacadePort appUserFacadePort(final AppUserStoragePort appUserStoragePort,
                                               final GithubOAuthAppPort githubOAuthAppPort,
                                               final IdentityProviderPort identityProviderPort,
                                               final GithubUserStoragePort githubUserStoragePort,
                                               final IndexerPort indexerPort) {
        return new AppUserService(appUserStoragePort, githubOAuthAppPort, identityProviderPort, githubUserStoragePort, indexerPort);
    }
}
