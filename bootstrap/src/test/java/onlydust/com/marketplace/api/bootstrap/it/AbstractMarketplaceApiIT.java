package onlydust.com.marketplace.api.bootstrap.it;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.bootstrap.MarketplaceApiApplicationIT;
import onlydust.com.marketplace.api.bootstrap.configuration.SwaggerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.net.URI;
import java.util.Map;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@ActiveProfiles({"it"})
@AutoConfigureWebTestClient(timeout = "36000")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = MarketplaceApiApplicationIT.class)
@Testcontainers
@Slf4j
@DirtiesContext
@Import(SwaggerConfiguration.class)
public class AbstractMarketplaceApiIT {

    protected static final Faker faker = new Faker();
    protected static final String PROJECTS_GET_BY_ID = "/api/v1/projects";
    protected static final String PROJECTS_GET_BY_SLUG = "/api/v1/projects/slug";
    protected static final String PROJECTS_GET = "/api/v1/projects";
    protected static final String ME_GET = "/api/v1/me";
    protected static final String USERS_GET = "api/v1/users";

    @Container
    static PostgreSQLContainer postgresSQLContainer =
            new PostgreSQLContainer<>("postgres:14.3-alpine")
                    .withDatabaseName("marketplace_db")
                    .withUsername("test")
                    .withPassword("test")
                    .withCopyFileToContainer(
                            MountableFile.forClasspathResource("/staging_db/dump"), "/tmp")
                    .withCopyFileToContainer(
                            MountableFile.forClasspathResource("/staging_db/scripts"), "/docker-entrypoint-initdb.d")
                    .waitingFor(Wait.forLogMessage(".*PostgreSQL init process complete; ready for start up.*", 1));
    @LocalServerPort
    int port;
    @Autowired
    WebTestClient client;

    @DynamicPropertySource
    static void updateProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgresSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgresSQLContainer::getUsername);
    }

    protected URI getApiURI(final String path) {
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(port)
                .path(path)
                .build()
                .toUri();
    }

    protected URI getApiURI(final String path, String paramName, String paramValue) {
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(port)
                .path(path)
                .queryParam(paramName, paramValue)
                .build()
                .toUri();
    }

    protected URI getApiURI(final String path, final Map<String, String> params) {
        final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(port)
                .path(path);
        params.forEach(uriComponentsBuilder::queryParam);
        return uriComponentsBuilder
                .build()
                .toUri();
    }

}
