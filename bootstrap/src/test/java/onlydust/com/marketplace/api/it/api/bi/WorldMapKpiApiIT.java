package onlydust.com.marketplace.api.it.api.bi;

import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.Country;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static onlydust.com.marketplace.api.helper.CurrencyHelper.ETH;
import static onlydust.com.marketplace.api.helper.CurrencyHelper.STRK;
import static onlydust.com.marketplace.api.helper.DateHelper.at;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@TagBI
public class WorldMapKpiApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    ProjectFacadePort projectFacadePort;

    @Nested
    class ActiveContributors {
        private static final AtomicBoolean setupDone = new AtomicBoolean();
        private static UserAuthHelper.AuthenticatedUser caller;
        private static UUID starknet;
        private static UUID ethereum;
        private static ProgramId explorationTeam;
        private static ProgramId nethermind;
        private static ProgramId ethGrantingProgram;

        @BeforeEach
        synchronized void setup() {
            if (setupDone.compareAndExchange(false, true)) return;

            caller = userAuthHelper.create();
            starknet = ecosystemHelper.create("Starknet ecosystem", caller).id();
            ethereum = ecosystemHelper.create("Ethereum ecosystem", caller).id();

            final var starknetFoundation = sponsorHelper.create();
            accountingHelper.createSponsorAccount(starknetFoundation.id(), 10_000, STRK);

            explorationTeam = programHelper.create(starknetFoundation.id(), "Starkware Exploration Team").id();
            accountingHelper.allocate(starknetFoundation.id(), explorationTeam, 7_000, STRK);
            nethermind = programHelper.create(starknetFoundation.id(), "Nethermind", caller).id();
            accountingHelper.allocate(starknetFoundation.id(), nethermind, 3_000, STRK);

            final var ethFoundation = sponsorHelper.create();
            accountingHelper.createSponsorAccount(ethFoundation.id(), 1_000, ETH);
            ethGrantingProgram = programHelper.create(ethFoundation.id(), "Ethereum Granting Program").id();
            accountingHelper.allocate(ethFoundation.id(), ethGrantingProgram, 300, ETH);

            final var onlyDust = projectHelper.create(userAuthHelper.authenticatePierre(), "OnlyDust").getLeft();
            accountingHelper.grant(nethermind, onlyDust, 100, STRK);
            accountingHelper.grant(ethGrantingProgram, onlyDust, 100, ETH);

            final var marketplace_api = githubHelper.createRepo(onlyDust);
            final var marketplace_frontend = githubHelper.createRepo(onlyDust);

            final var bridge = projectHelper.create(userAuthHelper.create(), "Bridge", List.of(starknet, ethereum)).getLeft();
            accountingHelper.grant(ethGrantingProgram, bridge, 100, ETH);
            accountingHelper.grant(explorationTeam, bridge, 100, STRK);

            final var bridge_api = githubHelper.createRepo(bridge);
            final var bridge_frontend = githubHelper.createRepo(bridge);

            final var madara = projectHelper.create(userAuthHelper.create(), "Madara", List.of(starknet)).getLeft();
            accountingHelper.grant(explorationTeam, madara, 100, STRK);

            final var madara_contracts = githubHelper.createRepo(madara);
            final var madara_app = githubHelper.createRepo(madara);

            final var antho = userAuthHelper.create();
            billingProfileHelper.verify(antho, Country.fromIso3("FRA"));
            final var pierre = userAuthHelper.create();
            billingProfileHelper.verify(pierre, Country.fromIso3("FRA"));
            final var mehdi = userAuthHelper.create();
            billingProfileHelper.verify(mehdi, Country.fromIso3("MAR"));
            final var hayden = userAuthHelper.create();
            billingProfileHelper.verify(hayden, Country.fromIso3("GBR"));
            final var abdel = userAuthHelper.create();
            billingProfileHelper.verify(abdel, Country.fromIso3("MAR"));
            final var emma = userAuthHelper.create();
            billingProfileHelper.verify(emma, Country.fromIso3("GBR"));
            final var james = userAuthHelper.create();
            billingProfileHelper.verify(james, Country.fromIso3("GBR"));

            at("2021-01-01T00:00:00Z", () -> githubHelper.createPullRequest(marketplace_api, antho));
            at("2021-01-01T00:00:03Z", () -> githubHelper.createPullRequest(marketplace_frontend, mehdi));
            at("2021-01-01T00:00:04Z", () -> githubHelper.createPullRequest(marketplace_frontend, mehdi));
            at("2021-01-01T00:00:05Z", () -> githubHelper.createPullRequest(marketplace_frontend, hayden));
            at("2021-01-01T00:00:07Z", () -> githubHelper.createPullRequest(bridge_frontend, emma));
            at("2021-01-01T00:00:09Z", () -> githubHelper.createPullRequest(bridge_frontend, emma));

            at("2021-02-01T00:00:00Z", () -> githubHelper.createPullRequest(marketplace_api, antho));
            at("2021-02-01T00:00:02Z", () -> githubHelper.createPullRequest(marketplace_api, pierre));
            at("2021-02-01T00:00:03Z", () -> githubHelper.createPullRequest(marketplace_frontend, mehdi));
            at("2021-02-01T00:00:04Z", () -> githubHelper.createPullRequest(marketplace_frontend, mehdi));
            at("2021-02-01T00:00:06Z", () -> githubHelper.createPullRequest(madara_contracts, abdel));
            at("2021-02-01T00:00:06Z", () -> githubHelper.createPullRequest(madara_app, emma));
            at("2021-02-01T00:00:08Z", () -> githubHelper.createPullRequest(bridge_frontend, james));
            at("2021-02-01T00:00:08Z", () -> githubHelper.createPullRequest(bridge_api, james));

            projectFacadePort.refreshStats();
        }

        @AfterAll
        @SneakyThrows
        static void restore() {
            restoreIndexerDump();
        }

        @Test
        void should_get_active_contributors_by_country() {
            client.get()
                    .uri(getApiURI(BI_WORLD_MAP, Map.of(
                            "kpi", "ACTIVE_CONTRIBUTORS"
                    )))
                    .header("Authorization", BEARER_PREFIX + caller.jwt())
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .json("""
                            [
                              {
                                "countryCode": "FR",
                                "value": 2
                              },
                              {
                                "countryCode": "GB",
                                "value": 3
                              },
                              {
                                "countryCode": "MA",
                                "value": 2
                              }
                            ]
                            """);
        }

        @Test
        void should_get_active_contributors_by_country_filtered_by_date() {
            client.get()
                    .uri(getApiURI(BI_WORLD_MAP, Map.of(
                            "kpi", "ACTIVE_CONTRIBUTORS",
                            "fromDate", "2021-01-01",
                            "toDate", "2021-01-31"
                    )))
                    .header("Authorization", BEARER_PREFIX + caller.jwt())
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .json("""
                            [
                              {
                                "countryCode": "FR",
                                "value": 1
                              },
                              {
                                "countryCode": "GB",
                                "value": 2
                              },
                              {
                                "countryCode": "MA",
                                "value": 1
                              }
                            ]
                            """);
        }

        @Test
        void should_get_active_contributors_by_country_filtered_by_program_or_ecosystem() {
            client.get()
                    .uri(getApiURI(BI_WORLD_MAP, Map.of(
                            "kpi", "ACTIVE_CONTRIBUTORS",
                            "programOrEcosystemIds", Stream.of(nethermind, ethereum).map(Object::toString).collect(joining(","))
                    )))
                    .header("Authorization", BEARER_PREFIX + caller.jwt())
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .json("""
                            [
                              {
                                "countryCode": "FR",
                                "value": 2
                              },
                              {
                                "countryCode": "GB",
                                "value": 3
                              },
                              {
                                "countryCode": "MA",
                                "value": 1
                              }
                            ]
                            """);
        }
    }
}
