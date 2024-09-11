package onlydust.com.marketplace.api.it.api.bi;

import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.api.it.api.AbstractMarketplaceApiIT;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.project.domain.port.input.ProjectFacadePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static onlydust.com.marketplace.api.helper.CurrencyHelper.ETH;
import static onlydust.com.marketplace.api.helper.CurrencyHelper.STRK;
import static onlydust.com.marketplace.api.helper.DateHelper.at;

public class WorldMapKpiApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    ProjectFacadePort projectFacadePort;

    @Nested
    class ActiveContributors {
        private static final AtomicBoolean setupDone = new AtomicBoolean();
        private ProgramId explorationTeam;
        private ProgramId nethermind;
        private ProgramId ethGrantingProgram;

        @BeforeEach
        synchronized void setup() {
            if (setupDone.compareAndExchange(false, true)) {
                explorationTeam = programHelper.getByName("Starkware Exploration Team");
                nethermind = programHelper.getByName("Nethermind");
                ethGrantingProgram = programHelper.getByName("Ethereum Granting Program");
            } else {

                final var starknetFoundation = sponsorHelper.create();
                accountingHelper.createSponsorAccount(starknetFoundation.id(), 10_000, STRK);

                explorationTeam = programHelper.create(starknetFoundation.id(), "Starkware Exploration Team").id();
                accountingHelper.allocate(starknetFoundation.id(), explorationTeam, 7_000, STRK);
                nethermind = programHelper.create(starknetFoundation.id(), "Nethermind").id();
                accountingHelper.allocate(starknetFoundation.id(), nethermind, 3_000, STRK);

                final var ethFoundation = sponsorHelper.create();
                accountingHelper.createSponsorAccount(ethFoundation.id(), 1_000, ETH);
                ethGrantingProgram = programHelper.create(ethFoundation.id(), "Ethereum Granting Program").id();
                accountingHelper.allocate(ethFoundation.id(), ethGrantingProgram, 300, ETH);

                final var onlyDust = projectHelper.create(userAuthHelper.authenticatePierre(), "OnlyDust");
                accountingHelper.grant(nethermind, onlyDust, 100, STRK);
                accountingHelper.grant(ethGrantingProgram, onlyDust, 100, ETH);

                final var marketplace_api = githubHelper.createRepo(onlyDust);
                final var marketplace_frontend = githubHelper.createRepo(onlyDust);

                final var bridge = projectHelper.create(userAuthHelper.create(), "Bridge");
                accountingHelper.grant(ethGrantingProgram, bridge, 100, ETH);
                accountingHelper.grant(explorationTeam, bridge, 100, STRK);

                final var bridge_api = githubHelper.createRepo(bridge);
                final var bridge_frontend = githubHelper.createRepo(bridge);

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
                at("2021-01-01T00:00:08Z", () -> githubHelper.createPullRequest(bridge_frontend, james));
                at("2021-01-01T00:00:09Z", () -> githubHelper.createPullRequest(bridge_frontend, emma));

                at("2021-02-01T00:00:00Z", () -> githubHelper.createPullRequest(marketplace_api, antho));
                at("2021-02-01T00:00:02Z", () -> githubHelper.createPullRequest(marketplace_api, pierre));
                at("2021-02-01T00:00:03Z", () -> githubHelper.createPullRequest(marketplace_frontend, mehdi));
                at("2021-02-01T00:00:04Z", () -> githubHelper.createPullRequest(marketplace_frontend, mehdi));
                at("2021-02-01T00:00:06Z", () -> githubHelper.createPullRequest(bridge_api, abdel));
                at("2021-02-01T00:00:08Z", () -> githubHelper.createPullRequest(bridge_frontend, james));

                projectFacadePort.refreshStats();
            }
        }

        @Test
        void should_get_active_contributors_by_country() {
            client.get()
                    .uri(getApiURI(BI_WORLD_MAP, Map.of(
                            "kpi", "ACTIVE_CONTRIBUTORS"
                    )))
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .json("""
                            [
                              {
                                "countryCode": "FRA",
                                "value": 3
                              },
                              {
                                "countryCode": "GBR",
                                "value": 3
                              },
                              {
                                "countryCode": "MAR",
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
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .json("""
                            [
                              {
                                "countryCode": "FRA",
                                "value": 1
                              },
                              {
                                "countryCode": "GBR",
                                "value": 3
                              },
                              {
                                "countryCode": "MAR",
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
                            "programOrEcosystemIds", Stream.of(nethermind).map(Object::toString).collect(joining(","))
                    )))
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .json("""
                            [
                              {
                                "countryCode": "FRA",
                                "value": 2
                              },
                              {
                                "countryCode": "GBR",
                                "value": 1
                              },
                              {
                                "countryCode": "MAR",
                                "value": 1
                              }
                            ]
                            """);
        }
    }
}
