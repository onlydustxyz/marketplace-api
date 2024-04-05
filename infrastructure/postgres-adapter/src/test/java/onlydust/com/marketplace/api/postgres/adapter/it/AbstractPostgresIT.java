package onlydust.com.marketplace.api.postgres.adapter.it;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.postgres.adapter.configuration.PostgresConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.testcontainers.utility.MountableFile.forClasspathResource;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = PostgresConfiguration.class)
@Testcontainers
@DirtiesContext
public class AbstractPostgresIT {

    protected final static Faker faker = new Faker();
    @Container
    static PostgreSQLContainer postgresSQLContainer =
            new PostgreSQLContainer<>("postgres:15.6-alpine")
                    .withDatabaseName("marketplace_db")
                    .withUsername("test")
                    .withPassword("test")
                    .withCopyFileToContainer(
                            forClasspathResource("db_init_script/"), "/docker-entrypoint-initdb.d");

    @DynamicPropertySource
    static void updateProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgresSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgresSQLContainer::getUsername);
    }

}
