package onlydust.com.marketplace.bff.read;

import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ApplicationRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.bff.read.adapters.*;
import onlydust.com.marketplace.bff.read.repositories.*;
import onlydust.com.marketplace.project.domain.service.PermissionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
@Profile("api")
public class ReadMarketplaceApiConfiguration {

    @Bean
    public ReadUsersApiPostgresAdapter bffReadUsersApiPostgresAdapter(final UserProfileLanguagePageItemEntityRepository userProfileLanguagePageItemEntityRepository,
                                                                      final UserProfileEcosystemPageItemEntityRepository userProfileEcosystemPageItemEntityRepository,
                                                                      final PublicUserProfileResponseV2EntityRepository publicUserProfileResponseV2EntityRepository,
                                                                      final UserProfileProjectEarningsEntityRepository userProfileProjectEarningsEntityRepository,
                                                                      final UserWorkDistributionEntityRepository userWorkDistributionEntityRepository,
                                                                      final UserWeeklyStatsEntityRepository userWeeklyStatsEntityRepository) {
        return new ReadUsersApiPostgresAdapter(
                userProfileLanguagePageItemEntityRepository,
                userProfileEcosystemPageItemEntityRepository,
                publicUserProfileResponseV2EntityRepository,
                userProfileProjectEarningsEntityRepository,
                userWorkDistributionEntityRepository,
                userWeeklyStatsEntityRepository);
    }

    @Bean
    public ReadCommitteesApiPostgresAdapter readCommitteesApiPostgresAdapter(
            final PermissionService permissionService,
            final AuthenticatedAppUserService authenticatedAppUserService,
            final CommitteeLinkViewRepository committeeLinkViewRepository,
            final CommitteeJuryVoteViewRepository committeeJuryVoteViewRepository,
            final ProjectInfosViewRepository projectInfosViewRepository,
            final CommitteeProjectAnswerViewRepository committeeProjectAnswerViewRepository,
            final CommitteeReadRepository committeeReadRepository) {
        return new ReadCommitteesApiPostgresAdapter(
                authenticatedAppUserService,
                permissionService,
                committeeLinkViewRepository,
                committeeJuryVoteViewRepository,
                projectInfosViewRepository,
                committeeProjectAnswerViewRepository,
                committeeReadRepository
        );
    }

    @Bean
    public ReadEcosystemsApiPostgresAdapter readEcosystemsApiPostgresAdapter(final ProjectEcosystemCardReadEntityRepository projectEcosystemCardReadEntityRepository,
                                                                             final EcosystemContributorPageItemEntityRepository ecosystemContributorPageItemEntityRepository,
                                                                             final EcosystemReadRepository ecosystemReadRepository,
                                                                             final LanguageReadRepository languageReadRepository,
                                                                             final ProjectCategoryReadRepository projectCategoryReadRepository) {
        return new ReadEcosystemsApiPostgresAdapter(
                ecosystemContributorPageItemEntityRepository,
                projectEcosystemCardReadEntityRepository,
                ecosystemReadRepository,
                languageReadRepository,
                projectCategoryReadRepository
        );
    }

    @Bean
    public ReadProjectsApiPostgresAdapter readProjectsApiPostgresAdapter(final AuthenticatedAppUserService authenticatedAppUserService,
                                                                         final PermissionService permissionService,
                                                                         final GithubIssueReadRepository githubIssueReadRepository,
                                                                         final ProjectReadRepository projectReadRepository,
                                                                         final CustomProjectRepository customProjectRepository,
                                                                         final CustomContributorRepository customContributorRepository,
                                                                         final ProjectLeadViewRepository projectLeadViewRepository,
                                                                         final ApplicationRepository applicationRepository,
                                                                         final ContributionViewEntityRepository contributionViewEntityRepository,
                                                                         final ProjectsPageRepository projectsPageRepository,
                                                                         final ProjectsPageFiltersRepository projectsPageFiltersRepository,
                                                                         final RewardDetailsReadRepository rewardDetailsReadRepository,
                                                                         final BudgetStatsReadRepository budgetStatsReadRepository,
                                                                         final ProjectContributorQueryRepository projectContributorQueryRepository) {
        return new ReadProjectsApiPostgresAdapter(authenticatedAppUserService,
                permissionService,
                githubIssueReadRepository,
                projectReadRepository,
                customProjectRepository,
                customContributorRepository,
                projectLeadViewRepository,
                applicationRepository,
                contributionViewEntityRepository,
                projectsPageRepository,
                projectsPageFiltersRepository,
                rewardDetailsReadRepository,
                budgetStatsReadRepository,
                projectContributorQueryRepository);
    }

    @Bean
    public ReadProjectCategoriesApiPostgresAdapter readProjectCategoriesApiPostgresAdapter(final ProjectCategoryReadRepository projectCategoryReadRepository) {
        return new ReadProjectCategoriesApiPostgresAdapter(projectCategoryReadRepository);
    }

    @Bean
    public ReadMeApiPostgresAdapter readMeApiPostgresAdapter(final AuthenticatedAppUserService authenticatedAppUserService,
                                                             final AllBillingProfileUserReadRepository allBillingProfileUserReadRepository,
                                                             final RewardDetailsReadRepository rewardDetailsReadRepository,
                                                             final UserRewardStatsReadRepository userRewardStatsReadRepository,
                                                             final PublicProjectReadRepository publicProjectReadRepository,
                                                             final UserReadRepository userReadRepository) {
        return new ReadMeApiPostgresAdapter(authenticatedAppUserService, allBillingProfileUserReadRepository, rewardDetailsReadRepository,
                userRewardStatsReadRepository, publicProjectReadRepository, userReadRepository);
    }

    @Bean
    public ReadActivityApiPostgresAdapter readActivityApiPostgresAdapter(final ActivityReadRepository activityReadRepository) {
        return new ReadActivityApiPostgresAdapter(activityReadRepository);
    }

    @Bean
    public ReadHackathonsApiPostgresAdapter readHackathonsApiPostgresAdapter(final AuthenticatedAppUserService authenticatedAppUserService,
                                                                             final HackathonShortReadRepository hackathonShortReadRepository,
                                                                             final HackathonDetailsReadRepository hackathonDetailsReadRepository) {
        return new ReadHackathonsApiPostgresAdapter(authenticatedAppUserService, hackathonShortReadRepository, hackathonDetailsReadRepository);
    }
}
