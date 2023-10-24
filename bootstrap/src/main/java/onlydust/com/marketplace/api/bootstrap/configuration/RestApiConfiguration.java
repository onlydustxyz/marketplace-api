package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.api.domain.port.input.ContributorFacadePort;
import onlydust.com.marketplace.api.domain.port.input.GithubInstallationFacadePort;
import onlydust.com.marketplace.api.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.api.domain.service.RewardService;
import onlydust.com.marketplace.api.rest.api.adapter.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraAuthentication;
import onlydust.com.marketplace.api.rest.api.adapter.exception.OnlydustExceptionRestHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

@Configuration
public class RestApiConfiguration {

    @Bean
    public ProjectsRestApi projectRestApi(final ProjectFacadePort projectFacadePort,
                                          final ContributorFacadePort contributorFacadePort,
                                          final AuthenticationService authenticationService,
                                          final RewardService<HasuraAuthentication> rewardService) {
        return new ProjectsRestApi(projectFacadePort, contributorFacadePort, authenticationService, rewardService);
    }

    @Bean
    public UsersRestApi usersRestApi(final UserFacadePort userFacadePort) {
        return new UsersRestApi(userFacadePort);
    }

    @Bean
    public MeRestApi meRestApi(final AuthenticationService authenticationService, final UserFacadePort userFacadePort) {
        return new MeRestApi(authenticationService, userFacadePort);
    }

    @Bean
    public VersionRestApi versionRestApi(final Date startingDate) {
        return new VersionRestApi(startingDate);
    }

    @Bean
    public TechnologiesRestApi technologiesRestApi() {
        return new TechnologiesRestApi();
    }

    @Bean
    public RewardsRestApi rewardsRestApi() {
        return new RewardsRestApi();
    }

    @Bean
    public GithubRestApi githubRestApi(final GithubInstallationFacadePort githubInstallationFacadePort) {
        return new GithubRestApi(githubInstallationFacadePort);
    }

    @Bean
    public OnlydustExceptionRestHandler onlydustExceptionRestHandler() {
        return new OnlydustExceptionRestHandler();
    }

    @Bean
    public AppRestApi appRestApi() {
        return new AppRestApi();
    }

}
