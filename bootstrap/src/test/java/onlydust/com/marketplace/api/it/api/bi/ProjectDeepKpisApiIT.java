package onlydust.com.marketplace.api.it.api.bi;

import lombok.SneakyThrows;
import onlydust.com.marketplace.api.contract.model.BiProjectsPageResponse;
import onlydust.com.marketplace.api.contract.model.ProjectCategoryResponse;
import onlydust.com.marketplace.api.contract.model.RegisteredUserResponse;
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
import java.util.stream.Stream;

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
        private static UUID universe;
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

        private static String allProgramOrEcosystemIds;

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

            universe = ecosystemHelper.create("Universe ecosystem").id();
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

            final var onlyDust = projectHelper.create(pierre, "OnlyDust", List.of(universe));
            projectHelper.addCategory(onlyDust, defi.id());
            at("2021-01-01T00:00:00Z", () -> accountingHelper.grant(nethermind, onlyDust, 100, STRK));
            at("2021-01-05T00:00:00Z", () -> accountingHelper.grant(ethGrantingProgram, onlyDust, 100, ETH));

            final var marketplace_api = githubHelper.createRepo(onlyDust);
            final var marketplace_frontend = githubHelper.createRepo(onlyDust);

            final var bridge = projectHelper.create(mehdi, "Bridge", List.of(universe, starknet, ethereum));
            projectHelper.addCategory(bridge, gaming.id());
            at("2021-01-01T00:00:00Z", () -> accountingHelper.grant(ethGrantingProgram, bridge, 100, ETH));
            at("2021-02-05T00:00:00Z", () -> accountingHelper.grant(explorationTeam, bridge, 100, STRK));

            final var bridge_api = githubHelper.createRepo(bridge);
            final var bridge_frontend = githubHelper.createRepo(bridge);

            final var madara = projectHelper.create(hayden, "Madara", List.of(universe, starknet));
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

            allProgramOrEcosystemIds = String.join(",", Stream.of(universe).map(UUID::toString).toList());
            projectFacadePort.refreshStats();
        }

        @Test
        public void should_get_projects_stats_between_dates() {
            // When
            client.get()
                    .uri(getApiURI(BI_PROJECTS, Map.of("pageIndex", "0", "pageSize", "100", "fromDate", "2021-01-01", "toDate", "2021-01-10",
                            "programOrEcosystemIds", allProgramOrEcosystemIds)))
                    .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticateOlivier().jwt())
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .jsonPath("$.projects[0].project.name").<String>value(name -> assertThat(name).contains("Bridge"))
                    .jsonPath("$.projects[1].project.name").<String>value(name -> assertThat(name).contains("OnlyDust"))
                    .json("""
                            {
                              "totalPageNumber": 1,
                              "totalItemNumber": 2,
                              "hasMore": false,
                              "nextPageIndex": 0,
                              "projects": [
                                {
                                  "projectLeads": [
                                    {
                                      "login": "mehdi"
                                    }
                                  ],
                                  "categories": [
                                    {
                                      "slug": "gaming",
                                      "name": "Gaming"
                                    }
                                  ],
                                  "languages": null,
                                  "ecosystems": [
                                    {
                                      "name": "Universe ecosystem"
                                    },
                                    {
                                      "name": "Starknet ecosystem"
                                    },
                                    {
                                      "name": "Ethereum ecosystem"
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
                                  "projectLeads": [
                                    {
                                      "login": "pierre"
                                    }
                                  ],
                                  "categories": [
                                    {
                                      "slug": "defi",
                                      "name": "DeFi"
                                    }
                                  ],
                                  "languages": null,
                                  "ecosystems": [
                                    {
                                      "name": "Universe ecosystem"
                                    }
                                  ],
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
                                    "value": 0,
                                    "trend": "STABLE"
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
                                }
                              ]
                            }
                            """);
        }

        private void test_projects_stats(String queryParam, boolean assertNotEmpty, Map<String, Consumer<BiProjectsPageResponse>> possibleQueryParamValues) {
            final var queryParams = new HashMap<String, String>();
            queryParams.put("pageIndex", "0");
            queryParams.put("pageSize", "100");
            queryParams.put("fromDate", "2021-01-01");
            queryParams.put("toDate", "2021-01-10");
            queryParams.put("programOrEcosystemIds", allProgramOrEcosystemIds);
            for (String possibleQueryParamValue : possibleQueryParamValues.keySet()) {
                queryParams.put(queryParam, possibleQueryParamValue);
                final var response = client.get()
                        .uri(getApiURI(BI_PROJECTS, queryParams))
                        .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticateOlivier().jwt())
                        .exchange()
                        .expectStatus()
                        .is2xxSuccessful()
                        .expectBody(BiProjectsPageResponse.class).returnResult().getResponseBody();
                if (assertNotEmpty)
                    assertThat(response.getProjects()).isNotEmpty();
                possibleQueryParamValues.get(possibleQueryParamValue).accept(response);
                queryParams.remove(queryParam);
            }
        }

        @Test
        public void should_get_projects_stats_with_filters() {
            // TODO: test all filters:
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

            test_projects_stats("projectLeadIds", true, Map.of(
                    mehdi.userId().toString(),
                    response -> response.getProjects().forEach(project -> assertThat(project.getProjectLeads().stream().map(RegisteredUserResponse::getGithubUserId))
                            .contains(mehdi.githubUserId().value()))
            ));
            test_projects_stats("search", true, Map.of(
                    "gaming",
                    response -> response.getProjects().forEach(project -> assertThat(project.getCategories().stream().map(ProjectCategoryResponse::getName).toList())
                            .contains("Gaming"))
            ));
        }
    }
}
