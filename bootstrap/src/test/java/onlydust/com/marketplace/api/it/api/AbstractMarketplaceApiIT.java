package onlydust.com.marketplace.api.it.api;

import com.auth0.jwt.interfaces.JWTVerifier;
import com.github.javafaker.Faker;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;
import onlydust.com.marketplace.accounting.domain.service.CachedAccountBookProvider;
import onlydust.com.marketplace.api.MarketplaceApiApplicationIT;
import onlydust.com.marketplace.api.configuration.SwaggerConfiguration;
import onlydust.com.marketplace.api.helper.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.kernel.jobs.OutboxConsumerJob;
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
import org.testcontainers.containers.Container;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@ActiveProfiles({"it", "api"})
@AutoConfigureWebTestClient(timeout = "36000")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = MarketplaceApiApplicationIT.class)
@Testcontainers
@Slf4j
@Import(SwaggerConfiguration.class)
@ContextConfiguration(initializers = WireMockInitializer.class)
@EnableWireMock({
        @ConfigureWireMock(name = "github", stubLocation = "", property = "infrastructure.github.baseUri"),
        @ConfigureWireMock(name = "dustyBot", stubLocation = "", property = "infrastructure.dustyBot.baseUri"),
        @ConfigureWireMock(name = "indexer-api", property = "infrastructure.indexer.api.client.baseUri"),
        @ConfigureWireMock(name = "linear", property = "infrastructure.linear.base-uri"),
        @ConfigureWireMock(name = "auth0", property = "application.web.auth0.user-info-url"),
        @ConfigureWireMock(name = "posthog", property = "infrastructure.posthog.base-uri"),
        @ConfigureWireMock(name = "sumsub", property = "infrastructure.sumsub.base-uri"),
        @ConfigureWireMock(name = "customer-io", property = "infrastructure.customer-io.base-uri"),
        @ConfigureWireMock(name = "node-guardians", property = "infrastructure.node-guardians.base-uri"),
        @ConfigureWireMock(name = "langchain", property = "langchain4j.open-ai.chat-model.base-url"),
        @ConfigureWireMock(name = "customer-io-tracking-api", property = "infrastructure.customer-io.tracking-base-uri"),
})
public class AbstractMarketplaceApiIT {

    protected static final Faker faker = new Faker();
    protected static final String BANNER = "/api/v1/banner";
    protected static final String ME_BANNER = "/api/v1/me/banners/%s";
    protected static final String ME_PROGRAMS = "/api/v1/me/programs";
    protected static final String ME_PROJECTS = "/api/v1/me/projects";
    protected static final String BILLING_PROFILE_INVOICE_PREVIEW = "/api/v1/billing-profiles/%s/invoice-preview";
    protected static final String BILLING_PROFILE_INVOICES = "/api/v1/billing-profiles/%s/invoices";
    protected static final String BILLING_PROFILE_INVOICE = "/api/v1/billing-profiles/%s/invoices/%s";
    protected static final String BILLING_PROFILE_INVOICES_MANDATE = "/api/v1/billing-profiles/%s/invoices/mandate";
    protected static final String CONTRIBUTIONS = "/api/v1/contributions";
    protected static final String CONTRIBUTIONS_BY_ID = "/api/v1/contributions/%s";
    protected static final String PROJECTS_GET_CONTRIBUTION_BY_ID = "/api/v1/projects/%s/contributions/%s";
    protected static final String PROJECTS_GET_BY_ID = "/api/v1/projects";
    protected static final String PROJECTS_GET_BY_SLUG = "/api/v1/projects/slug";
    protected static final String PROJECTS_GET = "/api/v1/projects";
    protected static final String USERS_SEARCH_CONTRIBUTORS = "/api/v1/users/search";
    protected static final String PROJECTS_CONTRIBUTORS = "/api/v1/projects/%s/contributors";
    protected static final String PROJECTS_HIDE_CONTRIBUTOR = "/api/v1/projects/%s/contributors/%d/hidden";
    protected static final String PROJECTS_GET_CONTRIBUTIONS = "/api/v1/projects/%s/contributions";
    protected static final String PROJECT_GOOD_FIRST_ISSUES = "/api/v1/projects/%s/good-first-issues";
    protected static final String PROJECT_PUBLIC_ISSUES = "/api/v1/projects/%s/public-issues";
    protected static final String PROJECTS_INSIGHTS_STALED_CONTRIBUTIONS = "/api/v1/projects/%s/insights" +
                                                                           "/contributions/staled";
    protected static final String PROJECTS_INSIGHTS_CHURNED_CONTRIBUTORS = "/api/v1/projects/%s/insights/contributors" +
                                                                           "/churned";
    protected static final String PROJECTS_INSIGHTS_NEWCOMERS = "/api/v1/projects/%s/insights/contributors/newcomers";
    protected static final String PROJECTS_INSIGHTS_MOST_ACTIVE_CONTRIBUTORS = "/api/v1/projects/%s/insights" +
                                                                               "/contributors/most-actives";
    protected static final String PROJECTS_REWARDS = "/api/v1/projects/%s/rewards";
    protected static final String PROJECTS_REWARDS_V2 = "/api/v2/projects/%s/rewards";
    protected static final String PROJECTS_REWARD = "/api/v1/projects/%s/rewards/%s";
    protected static final String PROJECTS_GET_REWARD_ITEMS = "/api/v1/projects/%s/rewards/%s/reward-items";
    protected static final String PROJECTS_GET_REWARDABLE_ITEMS = "/api/v1/projects/%s/rewardable-items";
    protected static final String PROJECTS_GET_ALL_COMPLETED_REWARDABLE_ITEMS = "/api/v1/projects/%s/rewardable-items" +
                                                                                "/all-completed";
    protected static final String PROJECTS_POST_REWARDABLE_OTHER_WORK = "/api/v1/projects/%s/rewardable-items/other" +
                                                                        "-works";
    protected static final String PROJECTS_POST_REWARDABLE_OTHER_ISSUE = "/api/v1/projects/%s/rewardable-items/other" +
                                                                         "-issues";
    protected static final String PROJECTS_POST_REWARDABLE_OTHER_PR = "/api/v1/projects/%s/rewardable-items/other" +
                                                                      "-pull-requests";
    protected static final String PROJECTS_GET_BUDGETS = "/api/v1/projects/%s/budgets";
    protected static final String PROJECTS_POST = "/api/v1/projects";
    protected static final String PROJECTS_PUT = "/api/v1/projects/%s";
    protected static final String PROJECT_STATS = "/api/v1/projects/%s/stats";
    protected static final String PROJECT_FINANCIAL = "/api/v1/projects/%s/financial";
    protected static final String PROJECT_FINANCIAL_BY_SLUG = "/api/v1/projects/slug/%s/financial";
    protected static final String PROJECTS_IGNORED_CONTRIBUTIONS_PUT = "/api/v1/projects/%s/ignored-contributions";
    protected static final String PROJECT_CONTRIBUTION_UNASSIGN = "/api/v1/projects/%s/contributions/%s/unassign";
    protected static final String PROJECT_TRANSACTIONS = "/api/v1/projects/%s/transactions";
    protected static final String APPLICATIONS = "/api/v1/applications";
    protected static final String APPLICATIONS_BY_ID = "/api/v1/applications/%s";
    protected static final String ME = "/api/v1/me";
    protected static final String ME_LOGOUT = "/api/v1/me/logout";
    protected static final String ME_PROFILE = "/api/v1/me/profile";
    protected static final String ME_ACCEPT_PROJECT_LEADER_INVITATION = "/api/v1/me/project-leader-invitations/%s";
    protected static final String ME_CLAIM_PROJECT = "/api/v1/me/project-claims/%s";
    protected static final String ME_APPLICATIONS = "/api/v1/me/applications";
    protected static final String ME_APPLICATION = "/api/v1/me/applications/%s";
    protected static final String APPLICATION = "/api/v1/applications/%s";
    protected static final String APPLICATION_ACCEPT = "/api/v1/applications/%s/accept";
    protected static final String ME_GET_REWARDS = "/api/v1/me/rewards";
    protected static final String ME_GET_CONTRIBUTED_PROJECTS = "/api/v1/me/contributed-projects";
    protected static final String ME_GET_CONTRIBUTED_REPOS = "/api/v1/me/contributed-repos";
    protected static final String ME_GET_REWARDING_PROJECTS = "/api/v1/me/rewarding-projects";
    protected static final String ME_REWARD = "/api/v1/me/rewards/%s";
    protected static final String ME_REWARD_ITEMS = "/api/v1/me/rewards/%s/reward-items";
    protected static final String ME_GET_REWARD_CURRENCIES = "/api/v1/me/reward-currencies";
    protected static final String ME_BILLING_PROFILES = "/api/v1/me/billing-profiles";
    protected static final String ME_GET_PROFILE_GITHUB = "/api/v1/me/profile/github";
    protected static final String ME_HACKATHON_REGISTRATIONS = "/api/v1/me/hackathons/%s/registrations";
    protected static final String ME_RECOMMENDED_PROJECTS = "/api/v1/me/recommended-projects";
    protected static final String ME_ONBOARDING = "/api/v1/me/onboarding";
    protected static final String ME_NOTIFICATION_SETTINGS_BY_PROJECT_ID = "/api/v1/me/notification-settings/projects/%s";
    protected static final String USERS_GET = "/api/v1/users";
    protected static final String USERS_GET_BY_LOGIN = "/api/v1/users/login";
    protected static final String USERS_GET_CONTRIBUTIONS = "/api/v2/users/%s/contributions";
    protected static final String ME_GET_ORGANIZATIONS = "/api/v1/me/organizations";
    protected static final String GET_ALL_ECOSYSTEMS = "/api/v1/ecosystems";
    protected static final String GET_ECOSYSTEM_CONTRIBUTORS = "/api/v1/ecosystems/%s/contributors";
    protected static final String GET_ECOSYSTEM_PROJECTS = "/api/v1/ecosystems/%s/projects";
    protected static final String V2_ECOSYSTEMS = "/api/v2/ecosystems";
    protected static final String ECOSYSTEM_BY_SLUG = "/api/v1/ecosystems/slug/%s";
    protected static final String ECOSYSTEM_LANGUAGES = "/api/v1/ecosystems/slug/%s/languages";
    protected static final String ECOSYSTEM_PROJECT_CATEGORIES = "/api/v1/ecosystems/slug/%s/project-categories";
    protected static final String BILLING_PROFILES_POST = "/api/v1/billing-profiles";
    protected static final String BILLING_PROFILES_GET_BY_ID = "/api/v1/billing-profiles/%s";
    protected static final String BILLING_PROFILES_DELETE_BY_ID = "/api/v1/billing-profiles/%s";
    protected static final String BILLING_PROFILES_GET_PAYOUT_INFO = "/api/v1/billing-profiles/%s/payout-info";
    protected static final String BILLING_PROFILES_PUT_PAYOUT_INFO = "/api/v1/billing-profiles/%s/payout-info";
    protected static final String BILLING_PROFILES_ENABLE_BY_ID = "/api/v1/billing-profiles/%s/enable";
    protected static final String BILLING_PROFILES_TYPE_BY_ID = "/api/v1/billing-profiles/%s/type";
    protected static final String BILLING_PROFILES_INVOICEABLE_REWARDS = "/api/v1/billing-profiles/%s/invoiceable-rewards";
    protected static final String ME_GET_PAYOUT_PREFERENCES = "/api/v1/me/payout-preferences";
    public static final String ME_PUT_PAYOUT_PREFERENCES = "/api/v1/me/payout-preferences";
    protected static final String BILLING_PROFILES_GET_COWORKERS = "/api/v1/billing-profiles/%s/coworkers";
    protected static final String BILLING_PROFILES_COWORKER_ROLE = "/api/v1/billing-profiles/%s/coworkers/%d/role";
    protected static final String BILLING_PROFILES_POST_COWORKER_INVITATIONS = "/api/v1/billing-profiles/%s/coworkers";
    protected static final String ME_BILLING_PROFILES_POST_COWORKER_INVITATIONS = "/api/v1/me/billing-profiles/%s/invitations";
    protected static final String BILLING_PROFILES_DELETE_COWORKER = "/api/v1/billing-profiles/%s/coworkers/%s";
    protected static final String SPONSOR = "/api/v1/sponsors/%s";
    protected static final String SPONSOR_ALLOCATE = "/api/v1/sponsors/%s/allocate";
    protected static final String SPONSOR_UNALLOCATE = "/api/v1/sponsors/%s/unallocate";
    protected static final String SPONSOR_STATS_TRANSACTIONS = "/api/v1/sponsors/%s/stats/transactions";
    protected static final String SPONSOR_TRANSACTIONS = "/api/v1/sponsors/%s/transactions";
    protected static final String SPONSOR_DEPOSITS = "/api/v1/sponsors/%s/deposits";
    protected static final String DEPOSIT_BY_ID = "/api/v1/deposits/%s";
    protected static final String HACKATHONS = "/api/v1/hackathons";
    protected static final String HACKATHONS_BY_SLUG = "/api/v1/hackathons/slug/%s";
    protected static final String HACKATHON_BY_ID_PROJECT_ISSUES = "/api/v1/hackathons/%s/project-issues";
    protected static final String USER_LANGUAGES = "/api/v1/users/%d/languages";
    protected static final String USER_ECOSYSTEMS = "/api/v1/users/%d/ecosystems";
    protected static final String USER_STATS = "/api/v1/users/%d/stats";
    protected static final String USER_BY_ID = "/api/v1/users/%d";
    protected static final String USER_BY_LOGIN = "/api/v1/users/login/%s";
    protected static final String PUT_COMMITTEES_APPLICATIONS = "/api/v1/committees/%s/projects/%s/applications";
    protected static final String COMMITTEES_APPLICATIONS = "/api/v1/committees/%s/projects/applications";
    protected static final String COMMITTEES_BY_ID = "/api/v1/committees/%s";
    protected static final String ME_COMMITTEE_ASSIGNEMENTS = "/api/v1/me/committees/%s";
    protected static final String ME_COMMITTEE_PROJECTS = "/api/v1/me/committees/%s/projects/%s";
    protected static final String PROJECT_CATEGORIES = "/api/v1/project-categories";
    protected static final String V2_ISSUES_BY_ID = "/api/v2/issues/%s";
    protected static final String ISSUES_BY_ID = "/api/v1/issues/%s";
    protected static final String ISSUES_BY_ID_APPLICANTS = "/api/v1/issues/%s/applicants";
    protected static final String LANGUAGES = "/api/v1/languages";
    protected static final String COUNTRIES = "/api/v1/countries";
    protected static final String ME_NOTIFICATIONS = "/api/v1/me/notifications";
    protected static final String ME_NOTIFICATIONS_COUNT = "/api/v1/me/notifications/count";
    protected static final String ME_NOTIFICATIONS_ALL = "/api/v1/me/notifications/all";
    protected static final String ME_NOTIFICATION_SETTINGS = "/api/v1/me/notification-settings";
    protected static final String SPONSOR_PROGRAMS = "/api/v1/sponsors/%s/programs";
    protected static final String PROGRAM_BY_ID = "/api/v1/programs/%s";
    protected static final String PROGRAMS_LOGOS = "/api/v1/programs/logos";
    protected static final String PROGRAM_STATS_TRANSACTIONS = "/api/v1/programs/%s/stats/transactions";
    protected static final String PROGRAM_TRANSACTIONS = "/api/v1/programs/%s/transactions";
    protected static final String PROGRAM_PROJECTS = "/api/v1/programs/%s/projects";
    protected static final String PROGRAM_PROJECT = "/api/v1/programs/%s/projects/%s";
    protected static final String PROGRAM_GRANT = "/api/v1/programs/%s/grant";
    protected static final String BI_WORLD_MAP = "/api/v1/bi/world-map";
    protected static final String BI_STATS_PROJECTS = "/api/v1/bi/stats/projects";
    protected static final String BI_STATS_CONTRIBUTORS = "/api/v1/bi/stats/contributors";
    protected static final String BI_STATS_FINANCIALS = "/api/v1/bi/stats/financials";
    protected static final String BI_PROJECTS = "/api/v1/bi/projects";
    protected static final String BI_CONTRIBUTORS = "/api/v1/bi/contributors";
    protected static final String BI_CONTRIBUTORS_BY_ID = "/api/v1/bi/contributors/%s";
    protected static final String PROJECT_CONTRIBUTOR_LABELS = "/api/v1/projects/%s/contributor-labels";
    protected static final String CONTRIBUTOR_LABEL_BY_ID = "/api/v1/contributor-labels/%s";
    protected static final String PULL_REQUESTS_BY_ID = "/api/v1/pull-requests/%s";
    protected static final String GET_REWARDS = "/api/v1/rewards";

    private static PostgreSQLContainer postgresSQLContainer = new PostgreSQLContainer<>("postgres:15.6-alpine")
            .withDatabaseName("marketplace_db")
            .withUsername("test")
            .withPassword("test")
            .withCommand("postgres -c max_wal_size=2GB")
            .withCopyFileToContainer(MountableFile.forClasspathResource("/database/dumps"), "/tmp")
            .withCopyFileToContainer(MountableFile.forClasspathResource("/database/docker_init"), "/docker-entrypoint-initdb.d")
            .withCopyFileToContainer(MountableFile.forClasspathResource("/database/scripts"), "/scripts")
            .waitingFor(Wait.forLogMessage(".*PostgreSQL init process complete; ready for start up.*", 1));

    @InjectWireMock("github")
    protected WireMockServer githubWireMockServer;
    @InjectWireMock("indexer-api")
    protected WireMockServer indexerApiWireMockServer;
    @InjectWireMock("dustyBot")
    protected WireMockServer dustyBotApiWireMockServer;
    @InjectWireMock("auth0")
    protected WireMockServer auth0WireMockServer;
    @InjectWireMock("posthog")
    protected WireMockServer posthogWireMockServer;
    @InjectWireMock("sumsub")
    protected WireMockServer sumsubWireMockServer;
    @Autowired
    protected WireMockServer ethereumWireMockServer;
    @Autowired
    protected WireMockServer starknetWireMockServer;
    @Autowired
    protected WireMockServer sorobanWireMockServer;
    @InjectWireMock("customer-io")
    protected WireMockServer customerIOWireMockServer;
    @InjectWireMock("node-guardians")
    protected WireMockServer nodeGuardiansWireMockServer;
    @InjectWireMock("langchain")
    protected WireMockServer langchainWireMockServer;
    @InjectWireMock("customer-io-tracking-api")
    protected WireMockServer customerIOTrackingApiWireMockServer;

    @LocalServerPort
    int port;
    @Autowired
    protected WebTestClient client;
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
    protected ProgramHelper programHelper;
    @Autowired
    protected EcosystemHelper ecosystemHelper;
    @Autowired
    protected SponsorHelper sponsorHelper;
    @Autowired
    protected AccountingHelper accountingHelper;
    @Autowired
    protected ProjectHelper projectHelper;
    @Autowired
    protected DepositHelper depositHelper;
    @Autowired
    protected RewardHelper rewardHelper;
    @Autowired
    protected HackathonHelper hackathonHelper;
    @Autowired
    protected CurrencyHelper currencyHelper;
    @Autowired
    protected DatabaseHelper databaseHelper;
    @Autowired
    OutboxConsumerJob indexerOutboxJob;
    @Autowired
    OutboxConsumerJob trackingOutboxJob;
    @Autowired
    OutboxConsumerJob billingProfileVerificationOutboxJob;


    @Autowired
    RewardRepository rewardRepository;
    @Autowired
    RewardStatusStorage rewardStatusStorage;
    @Autowired
    CurrencyRepository currencyRepository;
    @Autowired
    KybRepository kybRepository;
    @Autowired
    KycRepository kycRepository;

    @Autowired
    EntityManagerFactory entityManagerFactory;
    @Autowired
    AppUserFacadePort appUserFacadePort;
    @Autowired
    protected GithubHelper githubHelper;
    @Autowired
    protected BillingProfileHelper billingProfileHelper;
    @Autowired
    CustomerIOProperties customerIOProperties;

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        final boolean firstRun = !postgresSQLContainer.isRunning();
        if (firstRun)
            postgresSQLContainer.start();

        restoreDB(firstRun);
    }

    protected static void restoreDB(boolean firstRun) throws IOException, InterruptedException {
        assertThat(postgresSQLContainer.execInContainer("/scripts/restore_db.sh", Boolean.toString(firstRun)).getExitCode()).isEqualTo(0);
    }

    protected static void restoreIndexerDump() throws IOException, InterruptedException {
        Container.ExecResult execResult = postgresSQLContainer.execInContainer("/scripts/restore_indexer_dump.sh");
        assertThat(execResult.getExitCode()).isEqualTo(0);
    }

    @BeforeEach
    void beforeEach(@Autowired CachedAccountBookProvider accountBookProvider) {
        accountBookProvider.evictAll();
    }

    @BeforeEach
    void setupUserHelper() {
        userAuthHelper = new UserAuthHelper(userRepository, backofficeUserRepository, jwtVerifier, githubAuthenticationPort, auth0WireMockServer,
                githubWireMockServer, appUserFacadePort, faker, githubHelper);

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

    protected void addSponsorFor(UserAuthHelper.AuthenticatedUser user, UUID sponsorId) {
        final EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("""
                        INSERT INTO sponsor_leads
                        VALUES (:sponsorId, :userId)
                        ON CONFLICT DO NOTHING
                        """)
                .setParameter("userId", user.user().getId())
                .setParameter("sponsorId", sponsorId)
                .executeUpdate();
        em.flush();
        em.getTransaction().commit();
        em.close();
    }

    protected void removeSponsorFor(UserAuthHelper.AuthenticatedUser user, UUID sponsorId) {
        final EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("""
                        DELETE FROM sponsor_leads
                        WHERE user_id = :userId AND sponsor_id = :sponsorId
                        """)
                .setParameter("userId", user.user().getId())
                .setParameter("sponsorId", sponsorId)
                .executeUpdate();
        em.flush();
        em.getTransaction().commit();
        em.close();
    }
}
