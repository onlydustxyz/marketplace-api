package onlydust.com.marketplace.api.read;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
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
}
