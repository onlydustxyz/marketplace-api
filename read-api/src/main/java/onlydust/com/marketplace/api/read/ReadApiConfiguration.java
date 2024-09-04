package onlydust.com.marketplace.api.read;

import onlydust.com.marketplace.api.read.mapper.NotificationMapper;
import onlydust.com.marketplace.api.read.repositories.CurrencyReadRepository;
import onlydust.com.marketplace.api.read.repositories.ProgramReadRepository;
import onlydust.com.marketplace.api.read.repositories.ProjectLinkReadRepository;
import onlydust.com.marketplace.api.read.repositories.SponsorReadRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAutoConfiguration
@EntityScan(basePackages = {
        "onlydust.com.marketplace.api.read.entities"
})
@EnableJpaRepositories(basePackages = {
        "onlydust.com.marketplace.api.read.repositories"
})
@EnableTransactionManagement
@Configuration
public class ReadApiConfiguration {

    @Bean
    public NotificationMapper notificationMapper(
            final ProjectLinkReadRepository projectLinkReadRepository,
            final SponsorReadRepository sponsorReadRepository,
            final ProgramReadRepository programReadRepository,
            final CurrencyReadRepository currencyReadRepository
    ) {
        return new NotificationMapper(
                projectLinkReadRepository,
                sponsorReadRepository,
                programReadRepository,
                currencyReadRepository
        );
    }
}
