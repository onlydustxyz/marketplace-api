package onlydust.com.marketplace.api.postgres.adapter.configuration;

import onlydust.com.marketplace.api.postgres.adapter.*;
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
    public CustomContributorRepository customContributorRepository(final EntityManager entityManager) {
        return new CustomContributorRepository(entityManager);
    }

    @Bean
    public PostgresProjectAdapter postgresProjectAdapter(final ProjectRepository projectRepository,
                                                         final ProjectViewRepository projectViewRepository,
                                                         final ProjectIdRepository projectIdRepository,
                                                         final ProjectLeaderInvitationRepository projectLeaderInvitationRepository,
                                                         final ProjectLeadRepository projectLeadRepository,
                                                         final ProjectRepoRepository projectRepoRepository,
                                                         final CustomProjectRepository customProjectRepository,
                                                         final CustomContributorRepository customContributorRepository,
                                                         final CustomProjectRewardRepository customProjectRewardRepository,
                                                         final CustomProjectBudgetRepository customProjectBudgetRepository,
                                                         final ProjectLeadViewRepository projectLeadViewRepository,
                                                         final CustomRewardRepository customRewardRepository,
                                                         final ProjectsPageRepository projectsPageRepository,
                                                         final ProjectsPageFiltersRepository projectsPageFiltersRepository,
                                                         final RewardableItemRepository rewardableItemRepository) {
        return new PostgresProjectAdapter(projectRepository,
                projectViewRepository,
                projectIdRepository,
                projectLeaderInvitationRepository,
                projectLeadRepository,
                projectRepoRepository,
                customProjectRepository,
                customContributorRepository,
                customProjectRewardRepository,
                customProjectBudgetRepository,
                projectLeadViewRepository,
                customRewardRepository,
                projectsPageRepository,
                projectsPageFiltersRepository,
                rewardableItemRepository
        );
    }

    @Bean
    public PostgresGithubAdapter postgresGithubAdapter(final GithubAppInstallationRepository githubAppInstallationRepository) {
        return new PostgresGithubAdapter(githubAppInstallationRepository);
    }

    @Bean
    public CustomUserRepository customUserRepository(final EntityManager entityManager) {
        return new CustomUserRepository(entityManager);
    }

    @Bean
    public PostgresUserAdapter postgresUserAdapter(final CustomUserRepository customUserRepository,
                                                   final CustomContributorRepository customContributorRepository,
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
                                                   final CustomUserRewardRepository customUserRewardRepository,
                                                   final WalletRepository walletRepository,
                                                   final CustomUserPayoutInfoRepository customUserPayoutInfoRepository,
                                                   final CustomRewardRepository customRewardRepository,
                                                   final ProjectLedIdRepository projectLedIdRepository) {
        return new PostgresUserAdapter(customUserRepository,
                customContributorRepository,
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
                customUserRewardRepository,
                walletRepository,
                customUserPayoutInfoRepository,
                customRewardRepository,
                projectLedIdRepository);
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

    @Bean
    public CustomUserPayoutInfoRepository customUserPayoutInfoRepository(final EntityManager entityManager) {
        return new CustomUserPayoutInfoRepository(entityManager);
    }

    @Bean
    public PostgresContributionAdapter postgresContributionAdapter(final ContributionViewEntityRepository contributionViewEntityRepository,
                                                                   final ShortProjectViewEntityRepository shortProjectViewEntityRepository,
                                                                   final GithubRepoViewEntityRepository githubRepoViewEntityRepository,
                                                                   final ContributionDetailsViewEntityRepository contributionDetailsViewEntityRepository,
                                                                   final ContributionRewardViewEntityRepository contributionRewardViewEntityRepository,
                                                                   final CustomContributorRepository customContributorRepository,
                                                                   final CustomIgnoredContributionsRepository customIgnoredContributionsRepository,
                                                                   final IgnoredContributionsRepository ignoredContributionsRepository,
                                                                   final ProjectRepository projectRepository) {
        return new PostgresContributionAdapter(contributionViewEntityRepository, shortProjectViewEntityRepository,
                githubRepoViewEntityRepository, contributionDetailsViewEntityRepository,
                contributionRewardViewEntityRepository, customContributorRepository,
                customIgnoredContributionsRepository, ignoredContributionsRepository, projectRepository);
    }

    @Bean
    public CustomRewardRepository customRewardRepository(final EntityManager entityManager) {
        return new CustomRewardRepository(entityManager);
    }

    @Bean
    public PostgresEventStorageAdapter postgresEventStorageAdapter(final EventRepository eventRepository) {
        return new PostgresEventStorageAdapter(eventRepository);
    }
}
