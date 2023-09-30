package onlydust.com.marketplace.api.postgres.adapter.configuration;

import onlydust.com.marketplace.api.postgres.adapter.PostgresProjectAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresUserAdapter;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomUserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
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
    public PostgresProjectAdapter postgresProjectAdapter(final ProjectRepository projectRepository,
                                                         final CustomProjectRepository customProjectRepository) {
        return new PostgresProjectAdapter(projectRepository, customProjectRepository);
    }

    @Bean
    public CustomUserRepository customUserRepository(final EntityManager entityManager) {
        return new CustomUserRepository(entityManager);
    }

    @Bean
    public PostgresUserAdapter postgresUserAdapter(final CustomUserRepository customUserRepository) {
        return new PostgresUserAdapter(customUserRepository);
    }

}
