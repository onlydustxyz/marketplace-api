package onlydust.com.marketplace.api.postgres.adapter.configuration;

import onlydust.com.marketplace.api.postgres.adapter.PostgresGithubAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresProjectAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresUserAdapter;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.*;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;

@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = {
        "onlydust.com.marketplace.api.postgres.adapter.entity"
})
@EnableJpaRepositories(basePackages = {
        "onlydust.com.marketplace.api.postgres.adapter.repository"
})
@EnableTransactionManagement
@EnableJpaAuditing
public class PostgresConfiguration {

    @Bean
    public CustomProjectRepository customProjectRepository(final EntityManager entityManager) {
        return new CustomProjectRepository(entityManager);
    }

    @Bean
    public CustomProjectListRepository customProjectListRepository(final EntityManager entityManager) {
        return new CustomProjectListRepository(entityManager);
    }

    @Bean
    public CustomContributorRepository customContributorRepository(final EntityManager entityManager) {
        return new CustomContributorRepository(entityManager);
    }

    @Bean
    public CustomRepoRepository customRepoRepository(final EntityManager entityManager) {
        return new CustomRepoRepository(entityManager);
    }

    @Bean
    public PostgresProjectAdapter postgresProjectAdapter(final ProjectRepository projectRepository,
                                                         final ProjectIdRepository projectIdRepository,
                                                         final ProjectLeaderInvitationRepository projectLeaderInvitationRepository,
                                                         final ProjectRepoRepository projectRepoRepository,
                                                         final CustomProjectRepository customProjectRepository,
                                                         final CustomContributorRepository customContributorRepository,
                                                         final CustomRepoRepository customRepoRepository,
                                                         final CustomProjectListRepository customProjectListRepository,
                                                         final CustomUserRepository customUserRepository,
                                                         final ProjectLeadViewRepository projectLeadViewRepository) {
        return new PostgresProjectAdapter(projectRepository,
                projectIdRepository,
                projectLeaderInvitationRepository,
                projectRepoRepository,
                customProjectRepository,
                customContributorRepository,
                customRepoRepository,
                customUserRepository,
                customProjectListRepository,
                projectLeadViewRepository);
    }

    @Bean
    public PostgresGithubAdapter postgresGithubAdapter(final GithubAccountRepository githubAccountRepository) {
        return new PostgresGithubAdapter(githubAccountRepository);
    }

    @Bean
    public CustomUserRepository customUserRepository(final EntityManager entityManager) {
        return new CustomUserRepository(entityManager);
    }

    @Bean
    public PostgresUserAdapter postgresUserAdapter(final CustomUserRepository customUserRepository,
                                                   final UserRepository userRepository,
                                                   final UserViewRepository userViewRepository,
                                                   final GlobalSettingsRepository globalSettingsRepository,
                                                   final RegisteredUserRepository registeredUserRepository,
                                                   final UserPayoutInfoRepository userPayoutInfoRepository) {
        return new PostgresUserAdapter(customUserRepository,
                userRepository,
                userViewRepository,
                globalSettingsRepository,
                registeredUserRepository,
                userPayoutInfoRepository);
    }
}
