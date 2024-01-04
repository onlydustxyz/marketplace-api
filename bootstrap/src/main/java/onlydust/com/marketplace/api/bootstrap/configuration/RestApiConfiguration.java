package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.api.domain.port.input.ContributionFacadePort;
import onlydust.com.marketplace.api.domain.port.input.ContributionObserverPort;
import onlydust.com.marketplace.api.domain.port.input.ContributorFacadePort;
import onlydust.com.marketplace.api.domain.port.input.GithubInstallationFacadePort;
import onlydust.com.marketplace.api.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.api.domain.port.input.RewardFacadePort;
import onlydust.com.marketplace.api.domain.port.input.TechnologiesPort;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.api.domain.service.GithubAccountService;
import onlydust.com.marketplace.api.domain.service.RewardService;
import onlydust.com.marketplace.api.rest.api.adapter.EventsRestApi;
import onlydust.com.marketplace.api.rest.api.adapter.GithubRestApi;
import onlydust.com.marketplace.api.rest.api.adapter.MeRestApi;
import onlydust.com.marketplace.api.rest.api.adapter.ProjectsRestApi;
import onlydust.com.marketplace.api.rest.api.adapter.TechnologiesRestApi;
import onlydust.com.marketplace.api.rest.api.adapter.UsersRestApi;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key.ApiKeyAuthenticationService;
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
      final RewardService rewardService,
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
  public MeRestApi meRestApi(final AuthenticationService authenticationService, final UserFacadePort userFacadePort,
      final RewardFacadePort rewardFacadePort,
      final ContributorFacadePort contributorFacadePort,
      final GithubAccountService githubAccountService) {
    return new MeRestApi(authenticationService, userFacadePort, rewardFacadePort, contributorFacadePort,
        githubAccountService);
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
