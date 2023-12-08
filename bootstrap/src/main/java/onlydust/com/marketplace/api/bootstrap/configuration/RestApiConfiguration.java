package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.api.domain.port.input.*;
import onlydust.com.marketplace.api.domain.service.GithubAccountService;
import onlydust.com.marketplace.api.domain.service.RewardService;
import onlydust.com.marketplace.api.rest.api.adapter.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key.ApiKeyAuthenticationService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraAuthentication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("api")
public class RestApiConfiguration {

    @Bean
    public ProjectsRestApi projectRestApi(final ProjectFacadePort projectFacadePort,
                                          final AuthenticationService authenticationService,
                                          final RewardService<HasuraAuthentication> rewardService,
                                          final ContributionFacadePort contributionFacadePort) {
        return new ProjectsRestApi(projectFacadePort, authenticationService, rewardService,
                contributionFacadePort);
    }

    @Bean
    public UsersRestApi usersRestApi(final UserFacadePort userFacadePort,
                                     final ContributorFacadePort contributorFacadePort) {
        return new UsersRestApi(userFacadePort, contributorFacadePort);
    }

    @Bean
    public MeRestApi meRestApi(final AuthenticationService authenticationService, final UserFacadePort userFacadePort
            , final ContributorFacadePort contributorFacadePort, final GithubAccountService githubAccountService) {
        return new MeRestApi(authenticationService, userFacadePort, contributorFacadePort, githubAccountService);
    }

    @Bean
    public TechnologiesRestApi technologiesRestApi(final AuthenticationService authenticationService,
                                                   final TechnologiesPort technologiesPort) {
        return new TechnologiesRestApi(technologiesPort, authenticationService);
    }

    @Bean
    public GithubRestApi githubRestApi(final GithubInstallationFacadePort githubInstallationFacadePort) {
        return new GithubRestApi(githubInstallationFacadePort);
    }

    @Bean
    public EventsRestApi eventsRestApi(final ContributionObserverPort contributionObserverPort) {
        return new EventsRestApi(contributionObserverPort);
    }

    @Bean
    @ConfigurationProperties("application.web.machine-to-machine")
    public ApiKeyAuthenticationService.Config apiKeyAuthenticationConfig() {
        return new ApiKeyAuthenticationService.Config();
    }

}
