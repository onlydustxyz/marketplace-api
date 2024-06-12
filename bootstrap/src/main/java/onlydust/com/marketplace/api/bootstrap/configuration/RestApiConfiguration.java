package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.accounting.domain.port.in.*;
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
                                          final ProjectCategoryFacadePort projectCategoryFacadePort,
                                          final ProjectRewardFacadePort projectRewardFacadePort,
                                          final ProjectRewardFacadePort projectRewardFacadePortV2,
                                          final AuthenticatedAppUserService authenticatedAppUserService,
                                          final RewardFacadePort rewardFacadePort,
                                          final ContributionFacadePort contributionFacadePort) {
        return new ProjectsRestApi(
                projectFacadePort,
                projectCategoryFacadePort,
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
    public UsersRestApi usersRestApi(final AuthenticatedAppUserService authenticatedAppUserService,
                                     final ContributorFacadePort contributorFacadePort) {
        return new UsersRestApi(authenticatedAppUserService, contributorFacadePort);
    }

    @Bean
    public MeRestApi meRestApi(final AuthenticatedAppUserService authenticatedAppUserService,
                               final UserFacadePort userFacadePort,
                               final ContributorFacadePort contributorFacadePort,
                               final GithubAccountService githubAccountService,
                               final BillingProfileFacadePort billingProfileFacadePort,
                               final PayoutPreferenceFacadePort payoutPreferenceFacadePort,
                               final HackathonFacadePort hackathonFacadePort,
                               final CommitteeFacadePort committeeFacadePort) {
        return new MeRestApi(authenticatedAppUserService, userFacadePort, contributorFacadePort,
                githubAccountService, billingProfileFacadePort, payoutPreferenceFacadePort, hackathonFacadePort, committeeFacadePort);
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
    public EcosystemsRestApi ecosystemsRestApi(final EcosystemFacadePort ecosystemFacadePort) {
        return new EcosystemsRestApi(ecosystemFacadePort);
    }

    @Bean
    public CommitteeRestApi committeeRestApi(final AuthenticatedAppUserService authenticatedAppUserService,
                                             final CommitteeFacadePort committeeFacadePort) {
        return new CommitteeRestApi(authenticatedAppUserService, committeeFacadePort);
    }
}
