package onlydust.com.marketplace.api.it.api.bi;

import lombok.SneakyThrows;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.it.api.AbstractMarketplaceApiIT;
import onlydust.com.marketplace.api.suites.tags.TagBI;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.project.domain.port.input.ProjectFacadePort;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static onlydust.com.marketplace.api.helper.CurrencyHelper.*;
import static onlydust.com.marketplace.api.helper.DateHelper.at;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@TagBI
public class ProjectDiagramKpisApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    ProjectFacadePort projectFacadePort;
    UserAuthHelper.AuthenticatedUser caller;

    @BeforeEach
    void setup() {
        caller = userAuthHelper.authenticateOlivier();
    }

    @Test
    public void should_get_aggregate_project_stats_for_diagram() {
        // When
        client.get()
                .uri(getApiURI(BI_STATS_PROJECTS, Map.of("timeGrouping", "WEEK")))
                .header("Authorization", BEARER_PREFIX + caller.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.stats").isArray()
                .jsonPath("$.stats[0].timestamp").isEqualTo("2021-05-24T00:00:00Z");
    }

    @Test
    public void should_get_aggregate_project_stats_for_diagram_between_dates() {
        // When
        client.get()
                .uri(getApiURI(BI_STATS_PROJECTS, Map.of("timeGrouping", "DAY", "fromDate", "2022-01-01", "toDate", "2022-12-31")))
                .header("Authorization", BEARER_PREFIX + caller.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.stats").isArray()
                .jsonPath("$.stats.length()").isEqualTo(365)
                .jsonPath("$.stats[0].timestamp").isEqualTo("2022-01-01T00:00:00Z")
                .jsonPath("$.stats[364].timestamp").isEqualTo("2022-12-31T00:00:00Z");
    }

    @Nested
    class ActiveContributors {
        private static final AtomicBoolean setupDone = new AtomicBoolean();
        private static UUID starknet;
        private static UUID ethereum;
        private static ProgramId explorationTeam;
        private static ProgramId nethermind;
        private static ProgramId ethGrantingProgram;

        @BeforeEach
        synchronized void setup() {
            if (setupDone.compareAndExchange(false, true)) return;

            final var antho = userAuthHelper.create();
            final var pierre = userAuthHelper.create();
            final var mehdi = userAuthHelper.create();
            final var hayden = userAuthHelper.create();
            final var abdel = userAuthHelper.create();
            final var emma = userAuthHelper.create();
            final var james = userAuthHelper.create();

            starknet = ecosystemHelper.create("Starknet ecosystem", caller).id();
            ethereum = ecosystemHelper.create("Ethereum ecosystem", caller).id();

            final var starknetFoundation = sponsorHelper.create();
            accountingHelper.createSponsorAccount(starknetFoundation.id(), 10_000, STRK);

            explorationTeam = programHelper.create(starknetFoundation.id(), "Starkware Exploration Team", caller).id();
            accountingHelper.allocate(starknetFoundation.id(), explorationTeam, 7_000, STRK);
            nethermind = programHelper.create(starknetFoundation.id(), "Nethermind", caller).id();
            accountingHelper.allocate(starknetFoundation.id(), nethermind, 3_000, STRK);

            final var ethFoundation = sponsorHelper.create();
            accountingHelper.createSponsorAccount(ethFoundation.id(), 1_000, ETH);
            ethGrantingProgram = programHelper.create(ethFoundation.id(), "Ethereum Granting Program", caller).id();
            accountingHelper.allocate(ethFoundation.id(), ethGrantingProgram, 300, ETH);

            final var onlyDust = projectHelper.create(pierre, "OnlyDust").getLeft();
            at("2021-01-01T00:00:00Z", () -> accountingHelper.grant(nethermind, onlyDust, 100, STRK));
            at("2021-01-05T00:00:00Z", () -> accountingHelper.grant(ethGrantingProgram, onlyDust, 100, ETH));

            final var marketplace_api = githubHelper.createRepo(onlyDust);
            final var marketplace_frontend = githubHelper.createRepo(onlyDust);

            final var bridge = projectHelper.create(mehdi, "Bridge", List.of(starknet, ethereum)).getLeft();
            at("2021-01-01T00:00:00Z", () -> accountingHelper.grant(ethGrantingProgram, bridge, 100, ETH));
            at("2021-02-05T00:00:00Z", () -> accountingHelper.grant(explorationTeam, bridge, 100, STRK));

            final var bridge_api = githubHelper.createRepo(bridge);
            final var bridge_frontend = githubHelper.createRepo(bridge);

            final var madara = projectHelper.create(hayden, "Madara", List.of(starknet)).getLeft();
            at("2021-01-06T00:00:00Z", () -> accountingHelper.grant(explorationTeam, madara, 120, STRK));

            final var madara_contracts = githubHelper.createRepo(madara);
            final var madara_app = githubHelper.createRepo(madara);


            at("2021-01-01T00:00:00Z", () -> githubHelper.createPullRequest(marketplace_api, antho));
            at("2021-01-01T00:00:03Z", () -> githubHelper.createPullRequest(marketplace_frontend, mehdi));
            at("2021-01-01T00:00:04Z", () -> githubHelper.createPullRequest(marketplace_frontend, mehdi));
            at("2021-01-01T00:00:05Z", () -> githubHelper.createPullRequest(marketplace_frontend, hayden));
            at("2021-01-01T00:00:07Z", () -> githubHelper.createPullRequest(bridge_frontend, emma));
            at("2021-01-01T00:00:09Z", () -> githubHelper.createPullRequest(bridge_frontend, emma));

            at("2021-01-02T00:00:00Z", () -> githubHelper.createPullRequest(marketplace_api, antho));
            at("2021-01-03T00:00:03Z", () -> githubHelper.createPullRequest(marketplace_frontend, mehdi));
            at("2021-01-04T00:00:04Z", () -> githubHelper.createPullRequest(marketplace_frontend, mehdi));
            at("2021-01-05T00:00:05Z", () -> githubHelper.createPullRequest(marketplace_frontend, hayden));
            at("2021-01-06T00:00:07Z", () -> githubHelper.createPullRequest(bridge_frontend, emma));
            at("2021-01-07T00:00:09Z", () -> githubHelper.createPullRequest(bridge_frontend, emma));

            at("2021-02-01T00:00:00Z", () -> githubHelper.createPullRequest(marketplace_api, antho));
            at("2021-02-02T00:00:02Z", () -> githubHelper.createPullRequest(marketplace_api, pierre));
            at("2021-02-03T00:00:03Z", () -> githubHelper.createPullRequest(marketplace_frontend, mehdi));
            at("2021-02-05T00:00:04Z", () -> githubHelper.createPullRequest(marketplace_frontend, mehdi));
            at("2021-02-08T00:00:06Z", () -> githubHelper.createPullRequest(madara_contracts, abdel));
            at("2021-02-13T00:00:06Z", () -> githubHelper.createPullRequest(madara_app, emma));
            at("2021-02-21T00:00:08Z", () -> githubHelper.createPullRequest(bridge_frontend, james));
            at("2021-02-28T00:00:08Z", () -> githubHelper.createPullRequest(bridge_api, james));

            currencyHelper.setQuote("2020-12-31T00:00:00Z", STRK, USD, BigDecimal.valueOf(0.5));
            currencyHelper.setQuote("2020-12-31T00:00:00Z", ETH, USD, BigDecimal.valueOf(2));

            at("2021-01-01T00:00:00Z", () -> rewardHelper.create(onlyDust, pierre, antho.githubUserId(), 1, STRK));
            at("2021-01-01T00:00:00Z", () -> rewardHelper.create(onlyDust, pierre, antho.githubUserId(), 2, STRK));
            at("2021-01-01T00:00:00Z", () -> rewardHelper.create(onlyDust, pierre, james.githubUserId(), 3, STRK));
            at("2021-01-03T00:00:00Z", () -> rewardHelper.create(onlyDust, pierre, james.githubUserId(), 4, STRK));

            at("2021-02-10T00:00:00Z", () -> rewardHelper.create(onlyDust, pierre, abdel.githubUserId(), 5, STRK));

            projectFacadePort.refreshStats();
        }

        @AfterAll
        @SneakyThrows
        static void restore() {
            restoreIndexerDump();
        }

        @Test
        public void should_get_aggregate_project_stats_daily_for_diagram_between_dates() {
            // When
            client.get()
                    .uri(getApiURI(BI_STATS_PROJECTS, Map.of("timeGrouping", "DAY", "fromDate", "2021-01-01", "toDate", "2021-01-10",
                            "dataSourceIds",
                            String.join(",", Stream.of(explorationTeam, nethermind, ethGrantingProgram).map(ProgramId::toString).toList()))))
                    .header("Authorization", BEARER_PREFIX + caller.jwt())
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .json("""
                            {
                              "stats": [
                                {
                                  "timestamp": "2021-01-01T00:00:00Z",
                                  "totalGranted": 250.0,
                                  "totalRewarded": 3.0,
                                  "mergedPrCount": 6,
                                  "newProjectCount": 2,
                                  "activeProjectCount": 0,
                                  "reactivatedProjectCount": 0,
                                  "churnedProjectCount": 0
                                },
                                {
                                  "timestamp": "2021-01-02T00:00:00Z",
                                  "totalGranted": 0,
                                  "totalRewarded": 0,
                                  "mergedPrCount": 1,
                                  "newProjectCount": 0,
                                  "activeProjectCount": 1,
                                  "reactivatedProjectCount": 0,
                                  "churnedProjectCount": 1
                                },
                                {
                                  "timestamp": "2021-01-03T00:00:00Z",
                                  "totalGranted": 0,
                                  "totalRewarded": 2.0,
                                  "mergedPrCount": 1,
                                  "newProjectCount": 0,
                                  "activeProjectCount": 1,
                                  "reactivatedProjectCount": 0,
                                  "churnedProjectCount": 0
                                },
                                {
                                  "timestamp": "2021-01-04T00:00:00Z",
                                  "totalGranted": 0,
                                  "totalRewarded": 0,
                                  "mergedPrCount": 1,
                                  "newProjectCount": 0,
                                  "activeProjectCount": 1,
                                  "reactivatedProjectCount": 0,
                                  "churnedProjectCount": 0
                                },
                                {
                                  "timestamp": "2021-01-05T00:00:00Z",
                                  "totalGranted": 200,
                                  "totalRewarded": 0,
                                  "mergedPrCount": 1,
                                  "newProjectCount": 0,
                                  "activeProjectCount": 1,
                                  "reactivatedProjectCount": 0,
                                  "churnedProjectCount": 0
                                },
                                {
                                  "timestamp": "2021-01-06T00:00:00Z",
                                  "totalGranted": 60.0,
                                  "totalRewarded": 0,
                                  "mergedPrCount": 1,
                                  "newProjectCount": 0,
                                  "activeProjectCount": 0,
                                  "reactivatedProjectCount": 1,
                                  "churnedProjectCount": 1
                                },
                                {
                                  "timestamp": "2021-01-07T00:00:00Z",
                                  "totalGranted": 0,
                                  "totalRewarded": 0,
                                  "mergedPrCount": 1,
                                  "newProjectCount": 0,
                                  "activeProjectCount": 1,
                                  "reactivatedProjectCount": 0,
                                  "churnedProjectCount": 0
                                },
                                {
                                  "timestamp": "2021-01-08T00:00:00Z",
                                  "totalGranted": 0,
                                  "totalRewarded": 0,
                                  "mergedPrCount": 0,
                                  "newProjectCount": 0,
                                  "activeProjectCount": 0,
                                  "reactivatedProjectCount": 0,
                                  "churnedProjectCount": 1
                                },
                                {
                                  "timestamp": "2021-01-09T00:00:00Z",
                                  "totalGranted": 0,
                                  "totalRewarded": 0,
                                  "mergedPrCount": 0,
                                  "newProjectCount": 0,
                                  "activeProjectCount": 0,
                                  "reactivatedProjectCount": 0,
                                  "churnedProjectCount": 0
                                },
                                {
                                  "timestamp": "2021-01-10T00:00:00Z",
                                  "totalGranted": 0,
                                  "totalRewarded": 0,
                                  "mergedPrCount": 0,
                                  "newProjectCount": 0,
                                  "activeProjectCount": 0,
                                  "reactivatedProjectCount": 0,
                                  "churnedProjectCount": 0
                                }
                              ]
                            }
                            """);

            // When
            client.get()
                    .uri(getApiURI(BI_STATS_PROJECTS, Map.of("timeGrouping", "DAY", "fromDate", "2021-01-02", "toDate", "2021-01-02",
                            "dataSourceIds",
                            String.join(",", Stream.of(explorationTeam, nethermind, ethGrantingProgram).map(ProgramId::toString).toList()))))
                    .header("Authorization", BEARER_PREFIX + caller.jwt())
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .json("""
                            {
                              "stats": [
                                {
                                  "timestamp": "2021-01-02T00:00:00Z",
                                  "totalGranted": 0,
                                  "totalRewarded": 0,
                                  "mergedPrCount": 1,
                                  "newProjectCount": 0,
                                  "activeProjectCount": 1,
                                  "reactivatedProjectCount": 0,
                                  "churnedProjectCount": 1
                                }
                              ]
                            }
                            """);
        }

        @Test
        public void should_get_aggregate_project_stats_weekly_for_diagram_between_dates() {
            // When
            client.get()
                    .uri(getApiURI(BI_STATS_PROJECTS, Map.of("timeGrouping", "WEEK", "fromDate", "2021-01-01", "toDate", "2021-01-10",
                            "dataSourceIds",
                            String.join(",", Stream.of(explorationTeam, nethermind, ethGrantingProgram).map(ProgramId::toString).toList()))))
                    .header("Authorization", BEARER_PREFIX + caller.jwt())
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .json("""
                            {
                              "stats": [
                                {
                                  "timestamp": "2020-12-28T00:00:00Z",
                                  "totalGranted": 250.0,
                                  "totalRewarded": 5.0,
                                  "mergedPrCount": 8,
                                  "newProjectCount": 2,
                                  "activeProjectCount": 0,
                                  "reactivatedProjectCount": 0,
                                  "churnedProjectCount": 0
                                },
                                {
                                  "timestamp": "2021-01-04T00:00:00Z",
                                  "totalGranted": 260.0,
                                  "totalRewarded": 0,
                                  "mergedPrCount": 4,
                                  "newProjectCount": 0,
                                  "activeProjectCount": 2,
                                  "reactivatedProjectCount": 0,
                                  "churnedProjectCount": 0
                                }
                              ]
                            }
                            """);
        }
    }
}
