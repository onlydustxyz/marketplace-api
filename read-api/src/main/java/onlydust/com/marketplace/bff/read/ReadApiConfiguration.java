package onlydust.com.marketplace.bff.read;

import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeJuryVoteViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeLinkViewRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.bff.read.adapters.ReadCommitteesApiPostgresAdapter;
import onlydust.com.marketplace.bff.read.adapters.ReadUsersApiPostgresAdapter;
import onlydust.com.marketplace.bff.read.repositories.*;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = {
        "onlydust.com.marketplace.bff.read.entities"
})
@EnableJpaRepositories(basePackages = {
        "onlydust.com.marketplace.bff.read.repositories"
})
@EnableTransactionManagement
public class ReadApiConfiguration {
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
            final AuthenticatedAppUserService authenticatedAppUserService,
            final CommitteeLinkViewRepository committeeLinkViewRepository,
            final CommitteeJuryVoteViewRepository committeeJuryVoteViewRepository) {
        return new ReadCommitteesApiPostgresAdapter(
                authenticatedAppUserService,
                committeeLinkViewRepository,
                committeeJuryVoteViewRepository
        );
    }
}
