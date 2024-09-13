package onlydust.com.marketplace.api.it.api.bi;

import lombok.SneakyThrows;
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

import static onlydust.com.marketplace.api.helper.CurrencyHelper.*;
import static onlydust.com.marketplace.api.helper.DateHelper.at;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@TagBI
public class ProjectDeepKpisApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    ProjectFacadePort projectFacadePort;

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

            starknet = ecosystemHelper.create("Starknet ecosystem").id();
            ethereum = ecosystemHelper.create("Ethereum ecosystem").id();

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

            final var onlyDust = projectHelper.create(pierre, "OnlyDust");
            at("2021-01-01T00:00:00Z", () -> accountingHelper.grant(nethermind, onlyDust, 100, STRK));
            at("2021-01-05T00:00:00Z", () -> accountingHelper.grant(ethGrantingProgram, onlyDust, 100, ETH));

            final var marketplace_api = githubHelper.createRepo(onlyDust);
            final var marketplace_frontend = githubHelper.createRepo(onlyDust);

            final var bridge = projectHelper.create(mehdi, "Bridge", List.of(starknet, ethereum));
            at("2021-01-01T00:00:00Z", () -> accountingHelper.grant(ethGrantingProgram, bridge, 100, ETH));
            at("2021-02-05T00:00:00Z", () -> accountingHelper.grant(explorationTeam, bridge, 100, STRK));

            final var bridge_api = githubHelper.createRepo(bridge);
            final var bridge_frontend = githubHelper.createRepo(bridge);

            final var madara = projectHelper.create(hayden, "Madara", List.of(starknet));
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
        public void should_get_projects_stats_between_dates() {
            // When
            client.get()
                    .uri(getApiURI(BI_PROJECTS, Map.of("pageIndex", "0", "pageSize", "100", "fromDate", "2021-01-01", "toDate", "2021-01-10")))
                    .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticateOlivier().jwt())
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .json("""
                            {
                              "totalPageNumber": 1,
                              "totalItemNumber": 3,
                              "hasMore": false,
                              "nextPageIndex": 0,
                              "projects": [
                                {
                                  "project": {
                                    "slug": "pacos-project",
                                    "name": "Paco's project",
                                  },
                                  "projectLeads": null,
                                  "categories": null,
                                  "languages": [
                                    {
                                      "slug": "javascript",
                                      "name": "Javascript",
                                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                                    },
                                    {
                                      "slug": "solidity",
                                      "name": "Solidity",
                                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                                    }
                                  ],
                                  "ecosystems": [
                                    {
                                      "name": "Aztec",
                                      "url": "https://aztec.network/",
                                      "bannerUrl": null,
                                      "slug": "aztec"
                                    },
                                    {
                                      "name": "Starknet",
                                      "url": "https://www.starknet.io/en",
                                      "bannerUrl": null,
                                      "slug": "starknet"
                                    }
                                  ],
                                  "programs": [
                                    {
                                      "name": "Theodo",
                                    },
                                    {
                                      "name": "No Sponsor",
                                    }
                                  ],
                                  "availableBudget": null,
                                  "percentUsedBudget": null,
                                  "totalGrantedUsdAmount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "totalRewardedUsdAmount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "averageRewardUsdAmount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "onboardedContributorCount": {
                                    "value": 1,
                                    "trend": "DOWN"
                                  },
                                  "activeContributorCount": {
                                    "value": 3,
                                    "trend": "DOWN"
                                  },
                                  "mergedPrCount": {
                                    "value": 9,
                                    "trend": "UP"
                                  },
                                  "rewardCount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "contributionCount": {
                                    "value": 13,
                                    "trend": "UP"
                                  }
                                },
                                {
                                  "project": {
                                    "slug": "zero-title-11",
                                    "name": "Zero title 11",
                                    "logoUrl": null
                                  },
                                  "projectLeads": null,
                                  "categories": null,
                                  "languages": [
                                    {
                                      "slug": "javascript",
                                      "name": "Javascript",
                                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                                    },
                                    {
                                      "slug": "solidity",
                                      "name": "Solidity",
                                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                                    }
                                  ],
                                  "ecosystems": [
                                    {
                                      "name": "Starknet",
                                      "url": "https://www.starknet.io/en",
                                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                                      "bannerUrl": null,
                                      "slug": "starknet"
                                    }
                                  ],
                                  "programs": [
                                    {
                                      "name": "Theodo",
                                    },
                                    {
                                      "name": "No Sponsor",
                                    }
                                  ],
                                  "availableBudget": null,
                                  "percentUsedBudget": null,
                                  "totalGrantedUsdAmount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "totalRewardedUsdAmount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "averageRewardUsdAmount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "onboardedContributorCount": {
                                    "value": 1,
                                    "trend": "UP"
                                  },
                                  "activeContributorCount": {
                                    "value": 3,
                                    "trend": "DOWN"
                                  },
                                  "mergedPrCount": {
                                    "value": 9,
                                    "trend": "UP"
                                  },
                                  "rewardCount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "contributionCount": {
                                    "value": 13,
                                    "trend": "UP"
                                  }
                                },
                                {
                                  "project": {
                                    "slug": "zero-title-5",
                                    "name": "Zero title 5",
                                  },
                                  "projectLeads": [
                                    {
                                      "githubUserId": 595505,
                                      "login": "ofux",
                                      "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4",
                                    }
                                  ],
                                  "categories": null,
                                  "languages": [
                                    {
                                      "slug": "rust",
                                      "name": "Rust",
                                    }
                                  ],
                                  "ecosystems": null,
                                  "programs": [
                                    {
                                      "name": "No Sponsor",
                                    }
                                  ],
                                  "availableBudget": null,
                                  "percentUsedBudget": null,
                                  "totalGrantedUsdAmount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "totalRewardedUsdAmount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "averageRewardUsdAmount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "onboardedContributorCount": {
                                    "value": 2,
                                    "trend": "UP"
                                  },
                                  "activeContributorCount": {
                                    "value": 5,
                                    "trend": "DOWN"
                                  },
                                  "mergedPrCount": {
                                    "value": 6,
                                    "trend": "DOWN"
                                  },
                                  "rewardCount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "contributionCount": {
                                    "value": 8,
                                    "trend": "DOWN"
                                  }
                                }
                              ]
                            }
                            """);
        }
    }
}
