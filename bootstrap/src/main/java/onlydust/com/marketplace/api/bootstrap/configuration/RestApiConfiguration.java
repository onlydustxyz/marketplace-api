package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileFacadePort;
import onlydust.com.marketplace.api.rest.api.adapter.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key.ApiKeyAuthenticationService;
import onlydust.com.marketplace.project.domain.port.input.*;
import onlydust.com.marketplace.project.domain.service.GithubAccountService;
import onlydust.com.marketplace.project.domain.service.RewardService;
import onlydust.com.marketplace.project.domain.service.RewardV2Service;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("api")
public class RestApiConfiguration {

    @Bean
    public ProjectsRestApi projectRestApi(final ProjectFacadePort projectFacadePort,
                                          final ProjectRewardFacadePort projectRewardFacadePort,
                                          final ProjectRewardFacadePort projectRewardFacadePortV2,
                                          final AuthenticationService authenticationService,
                                          final RewardService rewardService,
                                          final RewardV2Service rewardV2Service,
                                          final ContributionFacadePort contributionFacadePort) {
        return new ProjectsRestApi(
                projectFacadePort,
                projectRewardFacadePort,
                projectRewardFacadePortV2,
                authenticationService,
                rewardService,
                rewardV2Service,
                contributionFacadePort);
    }

    @Bean
    public UsersRestApi usersRestApi(final UserFacadePort userFacadePort,
                                     final ContributorFacadePort contributorFacadePort) {
        return new UsersRestApi(userFacadePort, contributorFacadePort);
    }

    @Bean
    public MeRestApi meRestApi(final AuthenticationService authenticationService, final UserFacadePort userFacadePort,
                               final RewardFacadePort rewardFacadePort,
                               final ContributorFacadePort contributorFacadePort,
                               final GithubAccountService githubAccountService,
                               final BillingProfileFacadePort billingProfileFacadePort) {
        return new MeRestApi(authenticationService, userFacadePort, rewardFacadePort, contributorFacadePort,
                githubAccountService, billingProfileFacadePort);
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
    BillingProfileRestApi billingProfileRestApi(final AuthenticationService authenticationService,
                                                final BillingProfileFacadePort billingProfileFacadePort) {
        return new BillingProfileRestApi(authenticationService, billingProfileFacadePort);
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


    @Bean
    public EcosystemsRestApi ecosystemsRestApi(final EcosystemFacadePort ecosystemFacadePort) {
        return new EcosystemsRestApi(ecosystemFacadePort);
    }

}
