package onlydust.com.marketplace.bff.read;

import onlydust.com.marketplace.bff.read.adapters.ReadUsersApiPostgresAdapter;
import onlydust.com.marketplace.bff.read.repositories.PublicUserProfileResponseV2EntityRepository;
import onlydust.com.marketplace.bff.read.repositories.UserProfileEcosystemPageItemEntityRepository;
import onlydust.com.marketplace.bff.read.repositories.UserProfileLanguagePageItemEntityRepository;
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
                                                                      final PublicUserProfileResponseV2EntityRepository publicUserProfileResponseV2EntityRepository) {
        return new ReadUsersApiPostgresAdapter(
                userProfileLanguagePageItemEntityRepository,
                userProfileEcosystemPageItemEntityRepository,
                publicUserProfileResponseV2EntityRepository);
    }
}
