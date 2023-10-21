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
                                                         final ProjectLeadViewRepository projectLeadViewRepository,
                                                         final CustomProjectRewardRepository customProjectRewardRepository,
                                                         final CustomProjectBudgetRepository customProjectBudgetRepository) {
        return new PostgresProjectAdapter(projectRepository,
                projectIdRepository,
                projectLeaderInvitationRepository,
                projectRepoRepository,
                customProjectRepository,
                customContributorRepository,
                customRepoRepository,
                customUserRepository,
                customProjectListRepository,
                projectLeadViewRepository,
                customProjectRewardRepository,
                customProjectBudgetRepository);
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
                                                   final UserPayoutInfoRepository userPayoutInfoRepository,
                                                   final OnboardingRepository onboardingRepository,
                                                   final ProjectLeaderInvitationRepository projectLeaderInvitationRepository,
                                                   final ProjectLeadRepository projectLeadRepository,
                                                   final ApplicationRepository applicationRepository,
                                                   final ProjectIdRepository projectIdRepository,
                                                   final UserProfileInfoRepository userProfileInfoRepository,
                                                   final CustomUserRewardRepository customUserRewardRepository) {
        return new PostgresUserAdapter(customUserRepository,
                userRepository,
                userViewRepository,
                globalSettingsRepository,
                registeredUserRepository,
                userPayoutInfoRepository,
                onboardingRepository,
                projectLeaderInvitationRepository,
                projectLeadRepository,
                applicationRepository,
                projectIdRepository,
                userProfileInfoRepository,
                customUserRewardRepository);
    }

    @Bean
    public CustomProjectRewardRepository customProjectRewardRepository(final EntityManager entityManager) {
        return new CustomProjectRewardRepository(entityManager);
    }

    @Bean
    public CustomProjectBudgetRepository customProjectBudgetRepository(final EntityManager entityManager) {
        return new CustomProjectBudgetRepository(entityManager);
    }

    @Bean
    public CustomUserRewardRepository customUserRewardRepository(final EntityManager entityManager) {
        return new CustomUserRewardRepository(entityManager);
    }
}
