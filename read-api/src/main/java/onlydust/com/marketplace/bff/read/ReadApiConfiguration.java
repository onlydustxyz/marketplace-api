package onlydust.com.marketplace.bff.read;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAutoConfiguration
@EntityScan(basePackages = {
        "onlydust.com.marketplace.bff.read.entities"
})
@EnableJpaRepositories(basePackages = {
        "onlydust.com.marketplace.bff.read.repositories"
})
@EnableTransactionManagement
@Configuration
public class ReadApiConfiguration {
}
