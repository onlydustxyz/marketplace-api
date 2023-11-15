package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.api.domain.gateway.DateProvider;
import onlydust.com.marketplace.api.domain.port.input.*;
import onlydust.com.marketplace.api.domain.port.output.*;
import onlydust.com.marketplace.api.domain.service.*;
import onlydust.com.marketplace.api.postgres.adapter.PostgresGithubAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresProjectAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresUserAdapter;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraAuthentication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;
import java.util.UUID;

@Configuration
public class DomainConfiguration {


    @Bean
    public UUIDGeneratorPort uuidGeneratorPort() {
        return UUID::randomUUID;
    }

    @Bean
    public ContributionFacadePort contributionFacadePort(final ContributionStoragePort contributionStoragePort,
                                                         final PermissionService permissionService) {
        return new ContributionService(contributionStoragePort, permissionService);
    }

    @Bean
    public ProjectFacadePort projectFacadePort(final PostgresProjectAdapter postgresProjectAdapter,
                                               final ImageStoragePort imageStoragePort,
                                               final UUIDGeneratorPort uuidGeneratorPort,
                                               final PermissionService permissionService,
                                               final IndexerPort indexerPort,
                                               final DateProvider dateProvider,
                                               final EventStoragePort eventStoragePort,
                                               final ContributionStoragePort contributionStoragePort,
                                               final DustyBotStoragePort dustyBotStoragePort) {
        return new ProjectService(postgresProjectAdapter, imageStoragePort, uuidGeneratorPort, permissionService,
                indexerPort, dateProvider, eventStoragePort, contributionStoragePort, dustyBotStoragePort);
    }



    @Bean
    public GithubInstallationFacadePort githubInstallationFacadePort(
            final PostgresGithubAdapter postgresGithubAdapter,
            final RetriedGithubInstallationFacade.Config config
    ) {
        return new RetriedGithubInstallationFacade(new GithubInstallationService(postgresGithubAdapter), config);
    }

    @Bean
    public DateProvider dateProvider() {
        return Date::new;
    }

    @Bean
    public UserFacadePort userFacadePort(final PostgresUserAdapter postgresUserAdapter,
                                         final DateProvider dateProvider) {
        return new UserService(postgresUserAdapter, dateProvider);
    }

    @Bean
    public ContributorFacadePort contributorFacadePort(final ProjectStoragePort projectStoragePort,
                                                       final GithubSearchPort githubSearchPort,
                                                       final UserStoragePort userStoragePort,
                                                       final ContributionStoragePort contributionStoragePort) {
        return new ContributorService(projectStoragePort, githubSearchPort, userStoragePort, contributionStoragePort);
    }


    @Bean
    PermissionService permissionService(final ProjectStoragePort projectStoragePort,
                                        final ContributionStoragePort contributionStoragePort) {
        return new PermissionService(projectStoragePort, contributionStoragePort);
    }

    @Bean
    public RewardService<HasuraAuthentication> rewardService(final RewardStoragePort<HasuraAuthentication> rewardStoragePort,
                                                             final ProjectStoragePort projectStoragePort,
                                                             final PermissionService permissionService) {
        return new RewardService<>(rewardStoragePort, projectStoragePort, permissionService);
    }
}
