package onlydust.com.marketplace.bff.read;

import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeProjectAnswerViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectInfosViewRepository;
import onlydust.com.marketplace.bff.read.adapters.BackofficeCommitteesReadApiPostgresAdapter;
import onlydust.com.marketplace.bff.read.adapters.BackofficeHackathonsReadApiPostgresAdapter;
import onlydust.com.marketplace.bff.read.adapters.BackofficeReadProjectCategoriesApiPostgresAdapter;
import onlydust.com.marketplace.bff.read.adapters.BackofficeUsersReadApiPostgresAdapter;
import onlydust.com.marketplace.bff.read.repositories.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("bo")
public class ReadBackofficeApiConfiguration {

    @Bean
    public BackofficeUsersReadApiPostgresAdapter backofficeUsersReadApiPostgresAdapter(final UserReadRepository userReadRepository) {
        return new BackofficeUsersReadApiPostgresAdapter(userReadRepository);
    }

    @Bean
    public BackofficeHackathonsReadApiPostgresAdapter backofficeHackathonsReadApiPostgresAdapter(final UserReadRepository userReadRepository) {
        return new BackofficeHackathonsReadApiPostgresAdapter(userReadRepository);
    }

    @Bean
    public BackofficeCommitteesReadApiPostgresAdapter backofficeCommitteesReadApiPostgresAdapter(final CommitteeReadRepository committeeReadRepository,
                                                                                                 final CommitteeBudgetAllocationsResponseEntityRepository committeeBudgetAllocationsResponseEntityRepository,
                                                                                                 final ProjectInfosViewRepository projectInfosViewRepository,
                                                                                                 final CommitteeProjectAnswerViewRepository committeeProjectAnswerViewRepository) {
        return new BackofficeCommitteesReadApiPostgresAdapter(committeeReadRepository,
                committeeBudgetAllocationsResponseEntityRepository,
                projectInfosViewRepository,
                committeeProjectAnswerViewRepository);
    }

    @Bean
    public BackofficeReadProjectCategoriesApiPostgresAdapter backofficeReadProjectCategoriesApiPostgresAdapter(final ProjectCategoryPageItemReadRepository projectCategoryPageItemReadRepository,
                                                                                                               final ProjectCategoryReadRepository projectCategoryReadRepository) {
        return new BackofficeReadProjectCategoriesApiPostgresAdapter(projectCategoryPageItemReadRepository, projectCategoryReadRepository);
    }
}
