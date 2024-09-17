package onlydust.com.marketplace.api.it.api.bi;

import lombok.SneakyThrows;
import onlydust.com.marketplace.api.contract.model.BiProjectsPageResponse;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.it.api.AbstractMarketplaceApiIT;
import onlydust.com.marketplace.api.suites.tags.TagBI;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.project.domain.model.ProjectCategory;
import onlydust.com.marketplace.project.domain.port.input.ProjectFacadePort;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static onlydust.com.marketplace.api.helper.CurrencyHelper.*;
import static onlydust.com.marketplace.api.helper.DateHelper.at;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

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
        private static UserAuthHelper.AuthenticatedUser antho;
        private static UserAuthHelper.AuthenticatedUser pierre;
        private static UserAuthHelper.AuthenticatedUser mehdi;
        private static UserAuthHelper.AuthenticatedUser hayden;
        private static UserAuthHelper.AuthenticatedUser abdel;
        private static UserAuthHelper.AuthenticatedUser emma;
        private static UserAuthHelper.AuthenticatedUser james;
        private static ProjectCategory defi;
        private static ProjectCategory gaming;

        @AfterAll
        @SneakyThrows
        static void restore() {
            restoreIndexerDump();
        }

        @BeforeEach
        synchronized void setup() {
            if (setupDone.compareAndExchange(false, true)) return;

            antho = userAuthHelper.create("antho");
            pierre = userAuthHelper.create("pierre");
            mehdi = userAuthHelper.create("mehdi");
            hayden = userAuthHelper.create("hayden");
            abdel = userAuthHelper.create("abdel");
            emma = userAuthHelper.create("emma");
            james = userAuthHelper.create("james");

            starknet = ecosystemHelper.create("Starknet ecosystem").id();
            ethereum = ecosystemHelper.create("Ethereum ecosystem").id();

            defi = projectHelper.createCategory("DeFi");
            gaming = projectHelper.createCategory("Gaming");

            final var starknetFoundation = sponsorHelper.create("The Starknet Foundation");
            accountingHelper.createSponsorAccount(starknetFoundation.id(), 10_000, STRK);

            explorationTeam = programHelper.create(starknetFoundation.id(), "Starkware Exploration Team").id();
            accountingHelper.allocate(starknetFoundation.id(), explorationTeam, 7_000, STRK);
            nethermind = programHelper.create(starknetFoundation.id(), "Nethermind").id();
            accountingHelper.allocate(starknetFoundation.id(), nethermind, 3_000, STRK);

            final var ethFoundation = sponsorHelper.create("The Ethereum Foundation");
            accountingHelper.createSponsorAccount(ethFoundation.id(), 1_000, ETH);
            ethGrantingProgram = programHelper.create(ethFoundation.id(), "Ethereum Granting Program").id();
            accountingHelper.allocate(ethFoundation.id(), ethGrantingProgram, 300, ETH);

            final var onlyDust = projectHelper.create(pierre, "OnlyDust");
            projectHelper.addCategory(onlyDust, defi.id());
            at("2021-01-01T00:00:00Z", () -> accountingHelper.grant(nethermind, onlyDust, 100, STRK));
            at("2021-01-05T00:00:00Z", () -> accountingHelper.grant(ethGrantingProgram, onlyDust, 100, ETH));

            final var marketplace_api = githubHelper.createRepo(onlyDust);
            final var marketplace_frontend = githubHelper.createRepo(onlyDust);

            final var bridge = projectHelper.create(mehdi, "Bridge", List.of(starknet, ethereum));
            projectHelper.addCategory(bridge, gaming.id());
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
                              "totalItemNumber": 6,
                              "hasMore": false,
                              "nextPageIndex": 0,
                              "projects": [
                                {
                                  "project": {
                                    "logoUrl": null
                                  },
                                  "projectLeads": [
                                    {
                                      "login": "mehdi",
                                      "avatarUrl": "https://avatars.githubusercontent.com/u/mehdi"
                                    }
                                  ],
                                  "categories": null,
                                  "languages": null,
                                  "ecosystems": [
                                    {
                                      "name": "Starknet ecosystem",
                                      "slug": "starknet-ecosystem"
                                    },
                                    {
                                      "name": "Ethereum ecosystem",
                                      "slug": "ethereum-ecosystem"
                                    }
                                  ],
                                  "programs": [
                                    {
                                      "name": "Starkware Exploration Team"
                                    },
                                    {
                                      "name": "Ethereum Granting Program"
                                    }
                                  ],
                                  "availableBudget": null,
                                  "percentUsedBudget": null,
                                  "totalGrantedUsdAmount": {
                                    "value": 200,
                                    "trend": "UP"
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
                                    "value": 1,
                                    "trend": "UP"
                                  },
                                  "mergedPrCount": {
                                    "value": 4,
                                    "trend": "UP"
                                  },
                                  "rewardCount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "contributionCount": {
                                    "value": 4,
                                    "trend": "UP"
                                  }
                                },
                                {
                                  "project": {
                                    "logoUrl": null
                                  },
                                  "projectLeads": [
                                    {
                                      "login": "hayden",
                                      "avatarUrl": "https://avatars.githubusercontent.com/u/hayden"
                                    }
                                  ],
                                  "categories": null,
                                  "languages": null,
                                  "ecosystems": [
                                    {
                                      "name": "Starknet ecosystem",
                                      "slug": "starknet-ecosystem"
                                    }
                                  ],
                                  "programs": [
                                    {
                                      "name": "Starkware Exploration Team"
                                    }
                                  ],
                                  "availableBudget": null,
                                  "percentUsedBudget": null,
                                  "totalGrantedUsdAmount": {
                                    "value": 60.0,
                                    "trend": "UP"
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
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "activeContributorCount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "mergedPrCount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "rewardCount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "contributionCount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  }
                                },
                                {
                                  "project": {
                                    "logoUrl": null
                                  },
                                  "projectLeads": [
                                    {
                                      "login": "pierre",
                                      "avatarUrl": "https://avatars.githubusercontent.com/u/pierre"
                                    }
                                  ],
                                  "categories": null,
                                  "languages": null,
                                  "ecosystems": null,
                                  "programs": [
                                    {
                                      "name": "Nethermind"
                                    },
                                    {
                                      "name": "Ethereum Granting Program"
                                    }
                                  ],
                                  "availableBudget": null,
                                  "percentUsedBudget": null,
                                  "totalGrantedUsdAmount": {
                                    "value": 250.0,
                                    "trend": "UP"
                                  },
                                  "totalRewardedUsdAmount": {
                                    "value": 5.0,
                                    "trend": "UP"
                                  },
                                  "averageRewardUsdAmount": {
                                    "value": 1.2500000000000000,
                                    "trend": "UP"
                                  },
                                  "onboardedContributorCount": {
                                    "value": 3,
                                    "trend": "UP"
                                  },
                                  "activeContributorCount": {
                                    "value": 4,
                                    "trend": "UP"
                                  },
                                  "mergedPrCount": {
                                    "value": 8,
                                    "trend": "UP"
                                  },
                                  "rewardCount": {
                                    "value": 4,
                                    "trend": "UP"
                                  },
                                  "contributionCount": {
                                    "value": 8,
                                    "trend": "UP"
                                  }
                                },
                                {
                                  "project": {
                                    "slug": "pacos-project",
                                    "name": "Paco's project"
                                  },
                                  "projectLeads": null,
                                  "categories": null,
                                  "languages": [
                                    {
                                      "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                                      "slug": "javascript",
                                      "name": "Javascript",
                                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                                    },
                                    {
                                      "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                                      "slug": "solidity",
                                      "name": "Solidity",
                                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                                    }
                                  ],
                                  "ecosystems": [
                                    {
                                      "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                                      "name": "Aztec",
                                      "url": "https://aztec.network/",
                                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                                      "bannerUrl": null,
                                      "slug": "aztec"
                                    },
                                    {
                                      "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                                      "name": "Starknet",
                                      "url": "https://www.starknet.io/en",
                                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                                      "bannerUrl": null,
                                      "slug": "starknet"
                                    }
                                  ],
                                  "programs": [
                                    {
                                      "name": "Theodo"
                                    },
                                    {
                                      "name": "No Sponsor"
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
                                    "logoUrl": null
                                  },
                                  "projectLeads": null,
                                  "categories": null,
                                  "languages": [
                                    {
                                      "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                                      "slug": "javascript",
                                      "name": "Javascript",
                                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                                    },
                                    {
                                      "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                                      "slug": "solidity",
                                      "name": "Solidity",
                                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                                    }
                                  ],
                                  "ecosystems": [
                                    {
                                      "name": "Starknet",
                                      "slug": "starknet"
                                    }
                                  ],
                                  "programs": [
                                    {
                                      "name": "Theodo"
                                    },
                                    {
                                      "name": "No Sponsor"
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
                                    "name": "Zero title 5"
                                  },
                                  "projectLeads": [
                                    {
                                      "githubUserId": 595505,
                                      "login": "ofux",
                                      "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4",
                                      "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                                    }
                                  ],
                                  "categories": null,
                                  "languages": [
                                    {
                                      "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                                      "slug": "rust",
                                      "name": "Rust",
                                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                                    }
                                  ],
                                  "ecosystems": null,
                                  "programs": [
                                    {
                                      "name": "No Sponsor"
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

        private void test_projects_stats(String queryParam, Map<String, Consumer<BiProjectsPageResponse>> possibleQueryParamValues) {
            final var queryParams = new HashMap<String, String>();
            queryParams.put("pageIndex", "0");
            queryParams.put("pageSize", "100");
            queryParams.put("fromDate", "2021-01-01");
            queryParams.put("toDate", "2021-01-10");
            for (String possibleQueryParamValue : possibleQueryParamValues.keySet()) {
                queryParams.put(queryParam, possibleQueryParamValue);
                possibleQueryParamValues.get(possibleQueryParamValue).accept(client.get()
                        .uri(getApiURI(BI_PROJECTS, queryParams))
                        .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticateOlivier().jwt())
                        .exchange()
                        .expectStatus()
                        .is2xxSuccessful()
                        .expectBody(BiProjectsPageResponse.class).returnResult().getResponseBody());
                queryParams.remove(queryParam);
            }
        }

        @Test
        public void should_get_projects_stats_with_filters() {
//            List<UUID> projectLeadIds,
//            List<UUID> categoryIds,
//            List<UUID> languageIds,
//            List<UUID> ecosystemIds,
//            DecimalNumberKpiFilter availableBudgetUsdAmount,
//            NumberKpiFilter percentUsedBudget,
//            DecimalNumberKpiFilter totalGrantedUsdAmount,
//            DecimalNumberKpiFilter averageRewardUsdAmount,
//            DecimalNumberKpiFilter totalRewardedUsdAmount,
//            NumberKpiFilter onboardedContributorCount,
//            NumberKpiFilter activeContributorCount,
//            NumberKpiFilter mergedPrCount,
//            NumberKpiFilter rewardCount,
//            NumberKpiFilter contributionCount,
//            ProjectKpiSortEnum sort,

            test_projects_stats("projectLeadIds", Map.of(
                    mehdi.userId().toString(), response -> response.getProjects().forEach(project -> assertThat(project.getProjectLeads()).contains(mehdi))
            ));
            test_projects_stats("categoryIds", Map.of(
                    gaming.id().toString(), body -> body.jsonPath("$.projects.length()")
                            .value(val -> body.jsonPath("$.projects[?(@.categories[?(@.name == 'Gaming')])].length()").isEqualTo(val))
            ));
        }
    }
}
