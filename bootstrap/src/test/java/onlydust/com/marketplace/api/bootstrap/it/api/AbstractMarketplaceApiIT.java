package onlydust.com.marketplace.api.bootstrap.it.api;

import com.auth0.jwt.interfaces.JWTVerifier;
import com.github.javafaker.Faker;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.service.CachedAccountBookProvider;
import onlydust.com.marketplace.api.bootstrap.MarketplaceApiApplicationIT;
import onlydust.com.marketplace.api.bootstrap.configuration.SwaggerConfiguration;
import onlydust.com.marketplace.api.bootstrap.helper.AccountingHelper;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.kernel.jobs.OutboxConsumerJob;
import onlydust.com.marketplace.project.domain.port.output.GithubAuthenticationPort;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
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


@ActiveProfiles({"it", "api"})
@AutoConfigureWebTestClient(timeout = "36000")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = MarketplaceApiApplicationIT.class)
@Testcontainers
@Slf4j
@Import(SwaggerConfiguration.class)
@EnableWireMock({
        @ConfigureWireMock(name = "github", stubLocation = "", property = "infrastructure.github.baseUri"),
        @ConfigureWireMock(name = "dustyBot", stubLocation = "", property = "infrastructure.dustyBot.baseUri"),
        @ConfigureWireMock(name = "indexer-api", property = "infrastructure.indexer.api.client.baseUri"),
        @ConfigureWireMock(name = "linear", property = "infrastructure.linear.base-uri"),
        @ConfigureWireMock(name = "auth0", property = "application.web.auth0.user-info-url"),
        @ConfigureWireMock(name = "posthog", property = "infrastructure.posthog.base-uri"),
        @ConfigureWireMock(name = "sumsub", property = "infrastructure.sumsub.base-uri")
})
public class AbstractMarketplaceApiIT {

    protected static final Faker faker = new Faker();
    protected static final String BILLING_PROFILE_INVOICE_PREVIEW = "/api/v1/billing-profiles/%s/invoice-preview";
    protected static final String BILLING_PROFILE_INVOICES = "/api/v1/billing-profiles/%s/invoices";
    protected static final String BILLING_PROFILE_INVOICE = "/api/v1/billing-profiles/%s/invoices/%s";
    protected static final String BILLING_PROFILE_INVOICES_MANDATE = "/api/v1/billing-profiles/%s/invoices/mandate";
    protected static final String PROJECTS_GET_CONTRIBUTION_BY_ID = "/api/v1/projects/%s/contributions/%s";
    protected static final String PROJECTS_GET_BY_ID = "/api/v1/projects";
    protected static final String PROJECTS_GET_BY_SLUG = "/api/v1/projects/slug";
    protected static final String PROJECTS_GET = "/api/v1/projects";
    protected static final String USERS_SEARCH_CONTRIBUTORS = "/api/v1/users/search";
    protected static final String PROJECTS_GET_CONTRIBUTORS = "/api/v1/projects/%s/contributors";
    protected static final String PROJECTS_HIDE_CONTRIBUTOR = "/api/v1/projects/%s/contributors/%d/hidden";
    protected static final String PROJECTS_GET_CONTRIBUTIONS = "/api/v1/projects/%s/contributions";
    protected static final String PROJECT_GOOD_FIRST_ISSUES = "/api/v1/projects/%s/good-first-issues";
    protected static final String PROJECTS_INSIGHTS_STALED_CONTRIBUTIONS = "/api/v1/projects/%s/insights" +
                                                                           "/contributions/staled";
    protected static final String PROJECTS_INSIGHTS_CHURNED_CONTRIBUTORS = "/api/v1/projects/%s/insights/contributors" +
                                                                           "/churned";
    protected static final String PROJECTS_INSIGHTS_NEWCOMERS = "/api/v1/projects/%s/insights/contributors/newcomers";
    protected static final String PROJECTS_INSIGHTS_MOST_ACTIVE_CONTRIBUTORS = "/api/v1/projects/%s/insights" +
                                                                               "/contributors/most-actives";
    protected static final String PROJECTS_REWARDS = "/api/v1/projects/%s/rewards";
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
    protected static final String PROJECTS_IGNORED_CONTRIBUTIONS_PUT = "/api/v1/projects/%s/ignored-contributions";
    protected static final String ME_GET = "/api/v1/me";
    protected static final String ME_PATCH = "/api/v1/me";
    protected static final String ME_GET_PROFILE = "/api/v1/me/profile";
    protected static final String ME_PUT_PROFILE = "/api/v1/me/profile";
    protected static final String ME_ACCEPT_PROJECT_LEADER_INVITATION = "/api/v1/me/project-leader-invitations/%s";
    protected static final String ME_CLAIM_PROJECT = "/api/v1/me/project-claims/%s";
    protected static final String ME_APPLY_TO_PROJECT = "/api/v1/me/applications";
    protected static final String ME_GET_REWARDS = "/api/v1/me/rewards";
    protected static final String ME_GET_CONTRIBUTIONS = "/api/v1/me/contributions";
    protected static final String ME_GET_CONTRIBUTED_PROJECTS = "/api/v1/me/contributed-projects";
    protected static final String ME_GET_CONTRIBUTED_REPOS = "/api/v1/me/contributed-repos";
    protected static final String ME_GET_REWARDING_PROJECTS = "/api/v1/me/rewarding-projects";
    protected static final String ME_REWARD = "/api/v1/me/rewards/%s";
    protected static final String ME_REWARD_ITEMS = "/api/v1/me/rewards/%s/reward-items";
    protected static final String ME_GET_REWARD_CURRENCIES = "/api/v1/me/reward-currencies";
    protected static final String ME_BILLING_PROFILES = "/api/v1/me/billing-profiles";
    protected static final String ME_GET_PROFILE_GITHUB = "/api/v1/me/profile/github";
    protected static final String USERS_GET = "/api/v1/users";
    protected static final String USERS_GET_BY_LOGIN = "/api/v1/users/login";
    protected static final String ME_GET_ORGANIZATIONS = "/api/v1/me/organizations";
    protected static final String EVENT_ON_CONTRIBUTIONS_CHANGE_POST = "/api/v1/events/on-contributions-change";
    protected static final String SUGGEST_NEW_TECHNOLOGY = "/api/v1/technologies";
    protected static final String GET_ALL_TECHNOLOGIES = "/api/v1/technologies";
    protected static final String GET_ALL_ECOSYSTEMS = "/api/v1/ecosystems";
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
    protected static final String SPONSOR_TRANSACTIONS = "/api/v1/sponsors/%s/transactions";

    private static PostgreSQLContainer postgresSQLContainer = new PostgreSQLContainer<>("postgres:15.6-alpine")
            .withDatabaseName("marketplace_db")
            .withUsername("test")
            .withPassword("test")
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
    @InjectWireMock("linear")
    protected WireMockServer linearWireMockServer;
    @InjectWireMock("auth0")
    protected WireMockServer auth0WireMockServer;
    @InjectWireMock("posthog")
    protected WireMockServer posthogWireMockServer;
    @InjectWireMock("sumsub")
    protected WireMockServer sumsubWireMockServer;

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
    OutboxConsumerJob indexerOutboxJob;
    @Autowired
    OutboxConsumerJob trackingOutboxJob;
    @Autowired
    OutboxConsumerJob billingProfileVerificationOutboxJob;

    @Autowired
    RewardRepository rewardRepository;
    @Autowired
    RewardStatusRepository rewardStatusRepository;
    @Autowired
    InvoiceRewardRepository invoiceRewardRepository;
    @Autowired
    CurrencyRepository currencyRepository;
    @Autowired
    InvoiceRepository invoiceRepository;
    @Autowired
    BillingProfileRepository billingProfileRepository;
    @Autowired
    KybRepository kybRepository;
    @Autowired
    KycRepository kycRepository;

    @Autowired
    AccountingHelper accountingHelper;
    @Autowired
    EntityManagerFactory entityManagerFactory;

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        if (!postgresSQLContainer.isRunning()) {
            postgresSQLContainer.start();
        }
        assertThat(postgresSQLContainer.execInContainer("/scripts/restore_db.sh").getExitCode()).isEqualTo(0);
    }

    @BeforeEach
    void beforeEach(@Autowired CachedAccountBookProvider accountBookProvider) {
        accountBookProvider.evictAll();
    }

    @BeforeEach
    void setupUserAuthHelper() {
        userAuthHelper = new UserAuthHelper(userRepository, backofficeUserRepository, jwtVerifier, githubAuthenticationPort, auth0WireMockServer);

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

    protected void addSponsorFor(UserAuthHelper.AuthenticatedUser user, SponsorId sponsorId) {
        final EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("""
                        INSERT INTO sponsors_users
                        VALUES (:sponsorId, :userId)
                        ON CONFLICT DO NOTHING
                        """)
                .setParameter("userId", user.user().getId())
                .setParameter("sponsorId", sponsorId.value())
                .executeUpdate();
        em.flush();
        em.getTransaction().commit();
        em.close();
    }

    protected void removeSponsorFor(UserAuthHelper.AuthenticatedUser user, SponsorId sponsorId) {
        final EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("""
                        DELETE FROM sponsors_users
                        WHERE user_id = :userId AND sponsor_id = :sponsorId
                        """)
                .setParameter("userId", user.user().getId())
                .setParameter("sponsorId", sponsorId.value())
                .executeUpdate();
        em.flush();
        em.getTransaction().commit();
        em.close();
    }
}
