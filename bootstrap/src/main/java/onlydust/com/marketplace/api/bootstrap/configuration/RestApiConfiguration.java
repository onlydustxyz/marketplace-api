package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.accounting.domain.port.in.*;
import onlydust.com.marketplace.api.rest.api.adapter.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.project.domain.port.input.*;
import onlydust.com.marketplace.project.domain.service.ContributionService;
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
    public SponsorsRestApi sponsorsRestApi(final SponsorFacadePort sponsorFacadePort,
                                           final AccountingFacadePort accountingFacadePort,
                                           final AuthenticatedAppUserService authenticatedAppUserService) {
        return new SponsorsRestApi(sponsorFacadePort, accountingFacadePort, authenticatedAppUserService);
    }

    @Bean
    public UsersRestApi usersRestApi(final ContributorFacadePort contributorFacadePort) {
        return new UsersRestApi(contributorFacadePort);
    }

    @Bean
    public MeRestApi meRestApi(final AuthenticatedAppUserService authenticatedAppUserService,
                               final UserFacadePort userFacadePort,
                               final ContributorFacadePort contributorFacadePort,
                               final GithubAccountService githubAccountService,
                               final BillingProfileFacadePort billingProfileFacadePort,
                               final PayoutPreferenceFacadePort payoutPreferenceFacadePort,
                               final HackathonFacadePort hackathonFacadePort) {
        return new MeRestApi(authenticatedAppUserService, userFacadePort, contributorFacadePort,
                githubAccountService, billingProfileFacadePort, payoutPreferenceFacadePort, hackathonFacadePort);
    }

    @Bean
    public TechnologiesRestApi technologiesRestApi(final AuthenticatedAppUserService authenticatedAppUserService,
                                                   final TechnologiesPort technologiesPort) {
        return new TechnologiesRestApi(technologiesPort, authenticatedAppUserService);
    }

    @Bean
    BillingProfileRestApi billingProfileRestApi(final AuthenticatedAppUserService authenticatedAppUserService,
                                                final BillingProfileFacadePort billingProfileFacadePort,
                                                final CurrencyFacadePort currencyFacadePort,
                                                final AccountingFacadePort accountingFacadePort) {
        return new BillingProfileRestApi(authenticatedAppUserService, billingProfileFacadePort, currencyFacadePort, accountingFacadePort);
    }

    @Bean
    public EventsRestApi eventsRestApi(final ContributionService contributionService) {
        return new EventsRestApi(contributionService);
    }

    @Bean
    public EcosystemsRestApi ecosystemsRestApi(final EcosystemFacadePort ecosystemFacadePort) {
        return new EcosystemsRestApi(ecosystemFacadePort);
    }

    @Bean
    public HackathonRestApi hackathonsApi(final AuthenticatedAppUserService authenticatedAppUserService,
                                          final HackathonFacadePort hackathonFacadePort) {
        return new HackathonRestApi(authenticatedAppUserService, hackathonFacadePort);
    }

    @Bean
    public CommitteeRestApi committeeRestApi(){
        return new CommitteeRestApi();
    }
}
