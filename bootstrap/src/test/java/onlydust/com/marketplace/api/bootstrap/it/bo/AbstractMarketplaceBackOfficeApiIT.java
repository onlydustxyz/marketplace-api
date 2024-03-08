package onlydust.com.marketplace.api.bootstrap.it.bo;

import com.auth0.jwt.interfaces.JWTVerifier;
import com.github.javafaker.Faker;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.recording.RecordingStatus;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.api.bootstrap.MarketplaceApiApplicationIT;
import onlydust.com.marketplace.api.bootstrap.configuration.SwaggerConfiguration;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key.ApiKeyAuthenticationService;
import onlydust.com.marketplace.project.domain.port.output.GithubAuthenticationPort;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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


@ActiveProfiles({"it", "bo", "api"})
@AutoConfigureWebTestClient(timeout = "36000")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = MarketplaceApiApplicationIT.class)
@Testcontainers
@Slf4j
@Import(SwaggerConfiguration.class)
@ContextConfiguration(initializers = AbstractMarketplaceBackOfficeApiIT.WireMockInitializer.class)
@EnableWireMock({
        @ConfigureWireMock(name = "auth0", property = "application.web.auth0.user-info-url"),
        @ConfigureWireMock(name = "indexer-api", property = "infrastructure.indexer.api.client.baseUri"),
        @ConfigureWireMock(name = "rust-api", property = "infrastructure.od.api.client.baseUri"),
        @ConfigureWireMock(name = "make-webhook-send-rejected-invoice-mail", property = "infrastructure.make.webhook.sendRejectedInvoiceMailUrl"),
        @ConfigureWireMock(name = "make-webhook", property = "infrastructure.make.webhook.url"),
        @ConfigureWireMock(name = "make-webhook-send-rewards-paid", property = "infrastructure.make.webhook.sendRewardsPaidMailUrl"),
})
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
    @InjectWireMock("auth0")
    protected WireMockServer auth0WireMockServer;
    @InjectWireMock("indexer-api")
    protected WireMockServer indexerApiWireMockServer;
    @InjectWireMock("rust-api")
    protected WireMockServer rustApiWireMockServer;
    @InjectWireMock("make-webhook-send-rejected-invoice-mail")
    protected WireMockServer makeWebhookSendRejectedInvoiceMailWireMockServer;
    @InjectWireMock("make-webhook-send-rewards-paid")
    protected WireMockServer makeWebhookSendRewardsPaidMailWireMockServer;
    @InjectWireMock("make-webhook")
    protected WireMockServer makeWebhookWireMockServer;

    @Autowired
    ApiKeyAuthenticationService.Config backOfficeApiKeyAuthenticationConfig;

    @Autowired
    UserRepository userRepository;
    @Autowired
    JWTVerifier jwtVerifier;
    @Autowired
    GithubAuthenticationPort githubAuthenticationPort;
    protected UserAuthHelper userAuthHelper;

    @Autowired
    CurrencyFacadePort currencyFacadePort;

    @BeforeEach
    void setupUserAuthHelper() {
        userAuthHelper = new UserAuthHelper(userRepository, jwtVerifier, githubAuthenticationPort, auth0WireMockServer);

        userAuthHelper.mockAuth0UserInfo(134486697L, "axelbconseil");
        userAuthHelper.mockAuth0UserInfo(43467246L, "AnthonyBuisset", "abuisset@gmail.com");
        userAuthHelper.mockAuth0UserInfo(8642470L, "gregcha");
        userAuthHelper.mockAuth0UserInfo(5160414L, "haydencleary", "haydenclearymusic@gmail.com");
        userAuthHelper.mockAuth0UserInfo(595505L, "ofux");
        userAuthHelper.mockAuth0UserInfo(21149076L, "oscarwroche");
        userAuthHelper.mockAuth0UserInfo(16590657L, "PierreOucif");
    }

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

    protected static final String GET_ME = "/bo/v1/me";
    protected static final String GET_GITHUB_REPOS = "/bo/v1/repositories";
    protected static final String GET_SPONSORS = "/bo/v1/sponsors";
    protected static final String GET_SPONSOR = "/bo/v1/sponsors/%s";
    protected static final String POST_SPONSORS = "/bo/v1/sponsors";
    protected static final String PUT_SPONSORS = "/bo/v1/sponsors/%s";
    protected static final String POST_SPONSORS_ACCOUNTS = "/bo/v1/sponsors/%s/accounts";
    protected static final String GET_SPONSORS_ACCOUNTS = "/bo/v1/sponsors/%s/accounts";
    protected static final String POST_SPONSOR_ACCOUNTS_ALLOWANCE = "/bo/v1/sponsor-accounts/%s/allowance";
    protected static final String PATCH_SPONSOR_ACCOUNTS = "/bo/v1/sponsor-accounts/%s";
    protected static final String POST_SPONSOR_ACCOUNTS_RECEIPTS = "/bo/v1/sponsor-accounts/%s/receipts";
    protected static final String DELETE_SPONSOR_ACCOUNTS_RECEIPTS = "/bo/v1/sponsor-accounts/%s/receipts/%s";
    protected static final String POST_PROJECTS_BUDGETS_ALLOCATE = "/bo/v1/projects/%s/budgets/allocate";
    protected static final String POST_PROJECTS_BUDGETS_UNALLOCATE = "/bo/v1/projects/%s/budgets/unallocate";
    protected static final String POST_REWARDS_PAY = "/bo/v1/rewards/%s/pay";
    protected static final String GET_PENDING_PAYMENTS = "/bo/v1/pending-payments";
    protected static final String GET_ECOSYSTEMS = "/bo/v1/ecosystems";
    protected static final String GET_BUDGETS = "/bo/v1/budgets";
    protected static final String GET_USERS = "/bo/v1/users";
    protected static final String GET_PAYMENTS = "/bo/v1/payments";
    protected static final String GET_PROJECTS = "/bo/v1/projects";
    protected static final String PROJECTS_REWARDS = "/api/v2/projects/%s/rewards";
    protected static final String PROJECTS_REWARD = "/api/v2/projects/%s/rewards/%s";
    protected static final String GET_PROJECT_LEAD_INVITATIONS = "/bo/v1/project-lead-invitations";
    protected static final String POST_CURRENCIES = "/bo/v1/currencies";
    protected static final String PUT_CURRENCIES = "/bo/v1/currencies/%s";
    protected static final String GET_CURRENCIES = "/bo/v1/currencies";
    protected static final String INVOICES = "/bo/v1/invoices";
    protected static final String V2_INVOICES = "/bo/v2/invoices";
    protected static final String INVOICE = "/bo/v1/invoices/%s";
    protected static final String PUT_INVOICES_STATUS = "/bo/v1/invoices/%s/status";
    protected static final String EXTERNAL_INVOICE = "/bo/v1/external/invoices/%s";
    protected static final String ME_REWARD = "/api/v1/me/rewards/%s";
    protected static final String POST_REWARDS_PAY_VO = "/bo/v0/rewards/%s/pay";
    protected static final String POST_REWARDS_SEARCH = "/bo/v1/rewards/search";
    protected static final String POST_REWARDS_BATCH_PAYMENTS = "/bo/v1/rewards/batch-payments";
    protected static final String PUT_REWARDS_BATCH_PAYMENTS = "/bo/v1/rewards/batch-payments/%s";
    protected static final String GET_REWARDS_BATCH_PAYMENTS_BY_ID = "/bo/v1/rewards/batch-payments/%s";
    protected static final String GET_REWARDS_BATCH_PAYMENTS = "/bo/v1/rewards/batch-payments";
    protected static final String REWARDS = "/bo/v1/rewards";
    protected static final String GET_REWARDS_CSV = "/bo/v1/rewards/csv";
    protected static final String PUT_REWARDS_NOTIFY_PAYMENTS = "/bo/v1/rewards/notify-payments";

    protected String apiKey() {
        return backOfficeApiKeyAuthenticationConfig.getApiKey();
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

    protected final static Faker faker = new Faker();
}
