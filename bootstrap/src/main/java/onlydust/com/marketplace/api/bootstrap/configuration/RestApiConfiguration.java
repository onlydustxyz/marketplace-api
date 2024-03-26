package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.PayoutPreferenceFacadePort;
import onlydust.com.marketplace.api.rest.api.adapter.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.project.domain.port.input.*;
import onlydust.com.marketplace.project.domain.service.GithubAccountService;
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
                                          final AuthenticatedAppUserService authenticatedAppUserService,
                                          final RewardFacadePort rewardFacadePort,
                                          final ContributionFacadePort contributionFacadePort) {
        return new ProjectsRestApi(
                projectFacadePort,
                projectRewardFacadePort,
                projectRewardFacadePortV2,
                authenticatedAppUserService,
                rewardFacadePort,
                contributionFacadePort);
    }

    @Bean
    public UsersRestApi usersRestApi(final UserFacadePort userFacadePort,
                                     final ContributorFacadePort contributorFacadePort) {
        return new UsersRestApi(userFacadePort, contributorFacadePort);
    }

    @Bean
    public MeRestApi meRestApi(final AuthenticatedAppUserService authenticatedAppUserService,
                               final UserFacadePort userFacadePort,
                               final ContributorFacadePort contributorFacadePort,
                               final GithubAccountService githubAccountService,
                               final BillingProfileFacadePort billingProfileFacadePort,
                               final PayoutPreferenceFacadePort payoutPreferenceFacadePort) {
        return new MeRestApi(authenticatedAppUserService, userFacadePort, contributorFacadePort,
                githubAccountService, billingProfileFacadePort, payoutPreferenceFacadePort);
    }

    @Bean
    public TechnologiesRestApi technologiesRestApi(final AuthenticatedAppUserService authenticatedAppUserService,
                                                   final TechnologiesPort technologiesPort) {
        return new TechnologiesRestApi(technologiesPort, authenticatedAppUserService);
    }

    @Bean
    BillingProfileRestApi billingProfileRestApi(final AuthenticatedAppUserService authenticatedAppUserService,
                                                final BillingProfileFacadePort billingProfileFacadePort,
                                                final CurrencyFacadePort currencyFacadePort) {
        return new BillingProfileRestApi(authenticatedAppUserService, billingProfileFacadePort, currencyFacadePort);
    }

    @Bean
    public EventsRestApi eventsRestApi(final ContributionObserverPort contributionObserverPort) {
        return new EventsRestApi(contributionObserverPort);
    }

    @Bean
    public EcosystemsRestApi ecosystemsRestApi(final EcosystemFacadePort ecosystemFacadePort) {
        return new EcosystemsRestApi(ecosystemFacadePort);
    }

}
