package onlydust.com.marketplace.api.bootstrap.it.bo;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.recording.RecordingStatus;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.bootstrap.MarketplaceApiApplicationIT;
import onlydust.com.marketplace.api.bootstrap.configuration.SwaggerConfiguration;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key.ApiKeyAuthenticationService;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@ActiveProfiles({"it", "bo", "jobs"})
@AutoConfigureWebTestClient(timeout = "36000")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = MarketplaceApiApplicationIT.class)
@Testcontainers
@Slf4j
@Import(SwaggerConfiguration.class)
@ContextConfiguration(initializers = AbstractMarketplaceBackOfficeApiIT.WireMockInitializer.class)
public class AbstractMarketplaceBackOfficeApiIT {
    static PostgreSQLContainer postgresSQLContainer = new PostgreSQLContainer<>("postgres:14.3-alpine")
            .withDatabaseName("marketplace_db")
            .withUsername("test")
            .withPassword("test")
            .withCopyFileToContainer(MountableFile.forClasspathResource("/database/dumps"), "/tmp")
            .withCopyFileToContainer(MountableFile.forClasspathResource("/database/docker_init"), "/docker-entrypoint-initdb.d")
            .withCopyFileToContainer(MountableFile.forClasspathResource("/database/scripts"), "/scripts")
            .waitingFor(Wait.forLogMessage(".*PostgreSQL init process complete; ready for start up.*", 1));
    @LocalServerPort
    int port;
    @Autowired
    WebTestClient client;

    @Autowired
    protected WireMockServer ethereumWireMockServer;
    @Autowired
    protected WireMockServer optimismWireMockServer;
    @Autowired
    protected WireMockServer starknetWireMockServer;
    @Autowired
    protected WireMockServer coinmarketcapWireMockServer;
    @Autowired
    ApiKeyAuthenticationService.Config config;

    @DynamicPropertySource
    static void updateProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgresSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgresSQLContainer::getUsername);
    }

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        if (!postgresSQLContainer.isRunning()) {
            postgresSQLContainer.start();
        }
        assertThat(postgresSQLContainer.execInContainer("/scripts/restore_db.sh").getExitCode()).isEqualTo(0);
    }

    protected static final String GET_GITHUB_REPOS = "/bo/v1/repositories";
    protected static final String GET_SPONSORS = "/bo/v1/sponsors";
    protected static final String POST_SPONSOR_FUNDS = "/bo/v1/sponsors/%s/funds";
    protected static final String POST_SPONSOR_TRANSACTIONS = "/bo/v1/sponsors/%s/transactions";
    protected static final String POST_PROJECT_ALLOCATIONS = "/bo/v1/projects/%s/allocations";
    protected static final String POST_PROJECT_REFUNDS = "/bo/v1/projects/%s/refunds";
    protected static final String GET_BUDGETS = "/bo/v1/budgets";
    protected static final String GET_USERS = "/bo/v1/users";
    protected static final String GET_PAYMENTS = "/bo/v1/payments";
    protected static final String GET_PROJECTS = "/bo/v1/projects";
    protected static final String GET_PROJECT_LEAD_INVITATIONS = "/bo/v1/project-lead-invitations";
    protected static final String POST_CURRENCIES = "/bo/v1/currencies";
    protected static final String PUT_CURRENCIES = "/bo/v1/currencies/%s";
    protected static final String GET_CURRENCIES = "/bo/v1/currencies";

    protected String apiKey() {
        return config.getApiKey();
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

    public static class WireMockInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(final @NonNull ConfigurableApplicationContext context) {
            WiremockServerRegistration.builder()
                    .beanName("ethereumWireMockServer")
                    .stubLocation("ethereum")
                    .property("infrastructure.ethereum.base-uri")
//                    .recordFrom("https://mainnet.infura.io/v3")
                    .build()
                    .register(context);

            WiremockServerRegistration.builder()
                    .beanName("optimismWireMockServer")
                    .stubLocation("optimism")
                    .property("infrastructure.optimism.base-uri")
                    .build()
                    .register(context);

            WiremockServerRegistration.builder()
                    .beanName("starknetWireMockServer")
                    .stubLocation("starknet")
                    .property("infrastructure.starknet.base-uri")
                    .build()
                    .register(context);

            WiremockServerRegistration.builder()
                    .beanName("coinmarketcapWireMockServer")
                    .stubLocation("coinmarketcap")
                    .property("infrastructure.coinmarketcap.base-uri")
                    .build()
                    .register(context);
        }
    }

    @Builder
    public static class WiremockServerRegistration {
        private final @NonNull String beanName;
        private final @NonNull String stubLocation;
        private final @NonNull String property;
        private final String recordFrom;

        public void register(final @NonNull ConfigurableApplicationContext context) {
            final var wireMockServer = new WireMockServer(
                    options()
                            .dynamicPort()
                            .extensions(new ResponseTemplateTransformer(true))
                            .usingFilesUnderClasspath("wiremock/" + stubLocation)
            );

            wireMockServer.start();

            if (recordFrom != null)
                wireMockServer.startRecording(recordFrom);

            context.getBeanFactory().registerSingleton(beanName, wireMockServer);

            TestPropertyValues.of("%s:http://localhost:%d".formatted(property, wireMockServer.port()))
                    .applyTo(context);

            context.addApplicationListener(event -> {
                if (event instanceof ContextClosedEvent) {
                    if (wireMockServer.getRecordingStatus().getStatus() == RecordingStatus.Recording)
                        wireMockServer.stopRecording();
                    wireMockServer.stop();
                }
            });
        }
    }
}
