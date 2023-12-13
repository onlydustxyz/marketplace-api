package onlydust.com.marketplace.api.bootstrap.it.api;

import com.github.javafaker.Faker;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
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


@ActiveProfiles({"it", "api"})
@AutoConfigureWebTestClient(timeout = "36000")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = MarketplaceApiApplicationIT.class)
@Testcontainers
@Slf4j
@DirtiesContext
@Import(SwaggerConfiguration.class)
@EnableWireMock({
        @ConfigureWireMock(name = "github", stubLocation = "", property = "infrastructure.github.baseUri"),
        @ConfigureWireMock(name = "dustyBot", stubLocation = "", property = "infrastructure.dustyBot.baseUri"),
        @ConfigureWireMock(name = "rust-api", property = "infrastructure.od.api.client.baseUri"),
        @ConfigureWireMock(name = "indexer-api", property = "infrastructure.indexer.api.client.baseUri"),
        @ConfigureWireMock(name = "webhook", property = "infrastructure.webhook.url"),
        @ConfigureWireMock(name = "linear", property = "infrastructure.linear.base-uri")
})
public class AbstractMarketplaceApiIT {

    protected static final Faker faker = new Faker();
    protected static final String PROJECTS_GET_CONTRIBUTION_BY_ID = "/api/v1/projects/%s/contributions/%s";
    protected static final String PROJECTS_GET_BY_ID = "/api/v1/projects";
    protected static final String PROJECTS_GET_BY_SLUG = "/api/v1/projects/slug";
    protected static final String PROJECTS_GET = "/api/v1/projects";
    protected static final String USERS_SEARCH_CONTRIBUTORS = "/api/v1/users/search";
    protected static final String PROJECTS_GET_CONTRIBUTORS = "/api/v1/projects/%s/contributors";
    protected static final String PROJECTS_GET_CONTRIBUTIONS = "/api/v1/projects/%s/contributions";
    protected static final String PROJECTS_INSIGHTS_STALED_CONTRIBUTIONS = "/api/v1/projects/%s/insights/contributions/staled";
    protected static final String PROJECTS_INSIGHTS_CHURNED_CONTRIBUTORS = "/api/v1/projects/%s/insights/contributors/churned";
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
    protected static final String ME_PAYOUT_INFO = "/api/v1/me/payout-info";
    protected static final String ME_ACCEPT_PROJECT_LEADER_INVITATION = "/api/v1/me/project-leader-invitations/%s";
    protected static final String ME_CLAIM_PROJECT = "/api/v1/me/project-claims/%s";
    protected static final String ME_APPLY_TO_PROJECT = "/api/v1/me/applications";
    protected static final String ME_GET_REWARDS = "/api/v1/me/rewards";
    protected static final String ME_GET_CONTRIBUTIONS = "/api/v1/me/contributions";
    protected static final String ME_GET_CONTRIBUTED_PROJECTS = "/api/v1/me/contributed-projects";
    protected static final String ME_GET_CONTRIBUTED_REPOS = "/api/v1/me/contributed-repos";
    protected static final String ME_GET_REWARD_TOTAL_AMOUNTS = "/api/v1/me/rewards/amounts";
    protected static final String ME_REWARDS_PENDING_INVOICE = "/api/v1/me/rewards/pending-invoice";
    protected static final String ME_REWARD = "/api/v1/me/rewards/%s";
    protected static final String ME_REWARD_ITEMS = "/api/v1/me/rewards/%s/reward-items";
    protected static final String USERS_GET = "/api/v1/users";
    protected static final String USERS_GET_BY_LOGIN = "/api/v1/users/login";
    protected static final String GITHUB_INSTALLATIONS_GET = "/api/v1/github/installations";
    protected static final String ME_GET_ORGANIZATIONS = "/api/v1/me/organizations";
    protected static final String EVENT_ON_CONTRIBUTIONS_CHANGE_POST = "/api/v1/events/on-contributions-change";
    protected static final String SUGGEST_NEW_TECHNOLOGY = "/api/v1/technologies";
    protected static final String GET_ALL_TECHNOLOGIES = "/api/v1/technologies";

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
    @InjectWireMock("github")
    protected WireMockServer githubWireMockServer;
    @InjectWireMock("rust-api")
    protected WireMockServer rustApiWireMockServer;
    @InjectWireMock("indexer-api")
    protected WireMockServer indexerApiWireMockServer;
    @InjectWireMock("dustyBot")
    protected WireMockServer dustyBotApiWireMockServer;
    @InjectWireMock("webhook")
    protected WireMockServer webhookWireMockServer;
    @InjectWireMock("linear")
    protected WireMockServer linearWireMockServer;

    @LocalServerPort
    int port;
    @Autowired
    WebTestClient client;

    protected static void waitAtLeastOneCycleOfOutboxEventProcessing() throws InterruptedException {
        Thread.sleep(1000);
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

}
