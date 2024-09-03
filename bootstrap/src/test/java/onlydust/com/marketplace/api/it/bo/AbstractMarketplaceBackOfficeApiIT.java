package onlydust.com.marketplace.api.it.bo;

import com.auth0.jwt.interfaces.JWTVerifier;
import com.github.javafaker.Faker;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.accounting.domain.service.CachedAccountBookProvider;
import onlydust.com.marketplace.api.MarketplaceApiApplicationIT;
import onlydust.com.marketplace.api.configuration.SwaggerConfiguration;
import onlydust.com.marketplace.api.helper.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.BackofficeUserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key.ApiKeyAuthenticationService;
import onlydust.com.marketplace.project.domain.port.output.GithubAuthenticationPort;
import onlydust.com.marketplace.user.domain.port.input.AppUserFacadePort;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@ActiveProfiles({"it", "bo", "api"})
@AutoConfigureWebTestClient(timeout = "36000")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = MarketplaceApiApplicationIT.class)
@Testcontainers
@Slf4j
@Import(SwaggerConfiguration.class)
@ContextConfiguration(initializers = WireMockInitializer.class)
@EnableWireMock({
        @ConfigureWireMock(name = "auth0", property = "application.web.auth0.user-info-url"),
        @ConfigureWireMock(name = "auth0-m2m", property = "infrastructure.auth0.api.client.domain-base-uri"),
        @ConfigureWireMock(name = "github", stubLocation = "", property = "infrastructure.github.baseUri"),
        @ConfigureWireMock(name = "indexer-api", property = "infrastructure.indexer.api.client.baseUri"),
        @ConfigureWireMock(name = "customer-io", property = "infrastructure.customer-io.base-uri"),
        @ConfigureWireMock(name = "dustyBot", stubLocation = "", property = "infrastructure.dustyBot.baseUri")
})
public class AbstractMarketplaceBackOfficeApiIT {
    private static PostgreSQLContainer postgresSQLContainer = new PostgreSQLContainer<>("postgres:15.6-alpine")
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
    protected WireMockServer aptosWireMockServer;
    protected WireMockServer sorobanWireMockServer;
    @Autowired
    protected WireMockServer coinmarketcapWireMockServer;
    @InjectWireMock("auth0")
    protected WireMockServer auth0WireMockServer;
    @InjectWireMock("auth0-m2m")
    protected WireMockServer authM2M0WireMockServer;
    @InjectWireMock("github")
    protected WireMockServer githubWireMockServer;
    @InjectWireMock("indexer-api")
    protected WireMockServer indexerApiWireMockServer;
    @InjectWireMock("customer-io")
    protected WireMockServer customerIOWireMockServer;
    @InjectWireMock("dustyBot")
    protected WireMockServer dustyBotApiWireMockServer;
    @Autowired
    ApiKeyAuthenticationService.Config backOfficeApiKeyAuthenticationConfig;
    @Autowired
    CustomerIOProperties customerIOProperties;
    @Autowired
    ProgramHelper programHelper;
    @Autowired
    AccountingHelper accountingHelper;
    @Autowired
    DepositHelper depositHelper;

    @Autowired
    UserRepository userRepository;
    @Autowired
    BackofficeUserRepository backofficeUserRepository;
    @Autowired
    JWTVerifier jwtVerifier;
    @Autowired
    GithubAuthenticationPort githubAuthenticationPort;
    protected UserAuthHelper userAuthHelper;

    @Autowired
    CurrencyFacadePort currencyFacadePort;
    @Autowired
    AppUserFacadePort appUserFacadePort;

    @BeforeEach
    void setupUserAuthHelper() {
        userAuthHelper = new UserAuthHelper(userRepository, backofficeUserRepository, jwtVerifier, githubAuthenticationPort, auth0WireMockServer,
                githubWireMockServer, appUserFacadePort, faker);

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
        final var firstRun = !postgresSQLContainer.isRunning();
        if (firstRun)
            postgresSQLContainer.start();

        assertThat(postgresSQLContainer.execInContainer("/scripts/restore_db.sh", Boolean.toString(firstRun)).getExitCode()).isEqualTo(0);
    }

    @BeforeEach
    void beforeEach(@Autowired CachedAccountBookProvider accountBookProvider) {
        accountBookProvider.evictAll();
    }

    protected static final String GET_ME = "/bo/v1/me";
    protected static final String GET_GITHUB_REPOS = "/bo/v1/repositories";
    protected static final String GET_SPONSORS = "/bo/v2/sponsors";
    protected static final String GET_SPONSOR = "/bo/v1/sponsors/%s";
    protected static final String POST_SPONSORS = "/bo/v1/sponsors";
    protected static final String PUT_SPONSORS = "/bo/v1/sponsors/%s";
    protected static final String SPONSORS_LOGO = "/bo/v1/sponsors/logos";
    protected static final String SPONSORS_LEADS = "/bo/v1/sponsors/%s/leads/%s";
    protected static final String POST_SPONSORS_ACCOUNTS = "/bo/v1/sponsors/%s/accounts";
    protected static final String GET_SPONSORS_ACCOUNTS = "/bo/v1/sponsors/%s/accounts";
    protected static final String POST_SPONSOR_ACCOUNTS_ALLOWANCE = "/bo/v1/sponsor-accounts/%s/allowance";
    protected static final String PATCH_SPONSOR_ACCOUNTS = "/bo/v1/sponsor-accounts/%s";
    protected static final String POST_SPONSOR_ACCOUNTS_RECEIPTS = "/bo/v1/sponsor-accounts/%s/receipts";
    protected static final String DELETE_SPONSOR_ACCOUNTS_RECEIPTS = "/bo/v1/sponsor-accounts/%s/receipts/%s";
    protected static final String SPONSORS_BY_ID_ALLOCATE = "/bo/v1/sponsors/%s/allocate";
    protected static final String SPONSORS_BY_ID_UNALLOCATE = "/bo/v1/sponsors/%s/unallocate";
    protected static final String POST_REWARDS_PAY = "/bo/v1/rewards/%s/pay";
    protected static final String GET_ECOSYSTEMS = "/bo/v1/ecosystems";
    protected static final String GET_USERS = "/bo/v1/users";
    protected static final String GET_SEARCH_USERS = "/bo/v1/search/users";
    protected static final String GET_SEARCH_USERS_CSV = "/bo/v1/search/users/csv";
    protected static final String GET_USERS_BY_ID = "/bo/v1/users/%s";
    protected static final String GET_PROJECTS = "/bo/v1/projects";
    protected static final String PROJECTS_REWARDS = "/api/v1/projects/%s/rewards";
    protected static final String GET_PROJECT_LEAD_INVITATIONS = "/bo/v1/project-lead-invitations";
    protected static final String CURRENCIES = "/bo/v1/currencies";
    protected static final String CURRENCY = "/bo/v1/currencies/%s";
    protected static final String INVOICES = "/bo/v1/invoices";
    protected static final String V2_INVOICES = "/bo/v2/invoices";
    protected static final String INVOICE = "/bo/v1/invoices/%s";
    protected static final String PUT_INVOICES_STATUS = "/bo/v1/invoices/%s/status";
    protected static final String EXTERNAL_INVOICE = "/bo/v1/external/invoices/%s";
    protected static final String ME_REWARD = "/api/v1/me/rewards/%s";
    protected static final String BO_REWARD = "/bo/v1/rewards/%s";
    protected static final String POST_REWARDS_BATCH_PAYMENTS = "/bo/v1/rewards/batch-payments";
    protected static final String REWARDS_BATCH_PAYMENTS = "/bo/v1/rewards/batch-payments/%s";
    protected static final String GET_REWARDS_BATCH_PAYMENTS_BY_ID = "/bo/v1/rewards/batch-payments/%s";
    protected static final String GET_REWARDS_BATCH_PAYMENTS = "/bo/v1/rewards/batch-payments";
    protected static final String REWARDS = "/bo/v1/rewards";
    protected static final String REWARDS_NOTIFY_PAYMENTS = "/bo/v1/rewards/notify-payments";
    protected static final String GET_REWARDS_CSV = "/bo/v1/rewards/csv";
    protected static final String BILLING_PROFILE = "/bo/v1/billing-profiles/%s";
    protected static final String HACKATHONS = "/bo/v1/hackathons";
    protected static final String HACKATHONS_BY_ID = "/bo/v1/hackathons/%s";
    protected static final String HACKATHONS_BY_ID_USERS = "/bo/v1/hackathons/%s/users";
    protected static final String HACKATHONS_BY_ID_ISSUES = "/bo/v1/hackathons/%s/issues";
    protected static final String BANNERS = "/bo/v1/banners";
    protected static final String BANNER = "/bo/v1/banners/%s";
    protected static final String BANNER_VISIBLE = "/bo/v1/banners/%s/visible";
    protected static final String EARNINGS = "/bo/v1/earnings";
    protected static final String LANGUAGES = "/bo/v1/languages";
    protected static final String LANGUAGES_BY_ID = "/bo/v1/languages/%s";
    protected static final String LANGUAGES_EXTENSIONS = "/bo/v1/languages/extensions";
    protected static final String COMMITTEES = "/bo/v1/committees";
    protected static final String COMMITTEES_BY_ID = "/bo/v1/committees/%s";
    protected static final String COMMITTEES_STATUS = "/bo/v1/committees/%s/status";
    protected static final String COMMITTEES_APPLICATIONS_BY_IDS = "/bo/v1/committees/%s/projects/%s/applications";
    protected static final String COMMITTEE_BUDGET_ALLOCATIONS = "/bo/v1/committees/%s/allocations";
    protected static final String PROJECT_CATEGORIES = "/bo/v1/project-categories";
    protected static final String PROJECT_CATEGORY = "/bo/v1/project-categories/%s";
    protected static final String PROJECT_CATEGORY_SUGGESTION = "/bo/v1/project-category-suggestions/%s";
    protected static final String PROJECT_REWARDS = "/bo/v1/projects/%s/rewards";

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

    protected final static Faker faker = new Faker();
}
