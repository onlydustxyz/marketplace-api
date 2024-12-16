package onlydust.com.marketplace.api.it.api.bi;

import lombok.SneakyThrows;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.it.api.AbstractMarketplaceApiIT;
import onlydust.com.marketplace.api.suites.tags.TagBI;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.ProjectCategory;
import onlydust.com.marketplace.project.domain.port.input.ProjectFacadePort;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static onlydust.com.marketplace.api.contract.model.EngagementStatus.INACTIVE;
import static onlydust.com.marketplace.api.contract.model.EngagementStatus.NEW;
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
        private static UserAuthHelper.AuthenticatedUser caller;
        private static UserAuthHelper.AuthenticatedUser antho;
        private static UserAuthHelper.AuthenticatedUser pierre;
        private static UserAuthHelper.AuthenticatedUser mehdi;
        private static UserAuthHelper.AuthenticatedUser hayden;
        private static UserAuthHelper.AuthenticatedUser abdel;
        private static UserAuthHelper.AuthenticatedUser emma;
        private static UserAuthHelper.AuthenticatedUser james;
        private static ProjectCategory defi;
        private static ProjectCategory gaming;
        private static ProjectId onlyDustId;
        private static String onlyDustSlug;

        private static String allProgramOrEcosystemIds;

        @AfterAll
        @SneakyThrows
        static void restore() {
            restoreIndexerDump();
        }

        @BeforeEach
        synchronized void setup() {
            if (setupDone.compareAndExchange(false, true)) return;

            currencyHelper.setQuote("2020-12-31T00:00:00Z", STRK, USD, BigDecimal.valueOf(0.5));
            currencyHelper.setQuote("2020-12-31T00:00:00Z", ETH, USD, BigDecimal.valueOf(2));

            caller = userAuthHelper.create("olivier");
            antho = userAuthHelper.create("antho");
            pierre = userAuthHelper.create("pierre");
            mehdi = userAuthHelper.create("mehdi");
            hayden = userAuthHelper.create("hayden");
            abdel = userAuthHelper.create("abdel");
            emma = userAuthHelper.create("emma");
            james = userAuthHelper.create("james");

            universe = ecosystemHelper.create("Universe ecosystem", caller).id();
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
            accountingHelper.createSponsorAccount(ethFoundation.id(), 10_000, ETH);
            ethGrantingProgram = programHelper.create(ethFoundation.id(), "Ethereum Granting Program").id();
            accountingHelper.allocate(ethFoundation.id(), ethGrantingProgram, 3_000, ETH);

            final var onlyDust = projectHelper.create(pierre, "OnlyDust", List.of(universe));
            onlyDustId = onlyDust.getLeft();
            onlyDustSlug = onlyDust.getRight();
            projectHelper.addCategory(onlyDustId, defi.id());
            at("2021-01-01T00:00:00Z", () -> accountingHelper.grant(nethermind, onlyDustId, 100, STRK));
            at("2021-01-05T00:00:00Z", () -> accountingHelper.grant(ethGrantingProgram, onlyDustId, 25, ETH));

            final var marketplace_api = githubHelper.createRepo(onlyDustId);
            final var marketplace_frontend = githubHelper.createRepo(onlyDustId);

            final var bridge = projectHelper.create(mehdi, "Bridge", List.of(universe, starknet, ethereum)).getLeft();
            projectHelper.addCategory(bridge, gaming.id());
            at("2021-01-01T00:00:00Z", () -> accountingHelper.grant(ethGrantingProgram, bridge, 1_000, ETH));
            at("2021-02-05T00:00:00Z", () -> accountingHelper.grant(explorationTeam, bridge, 10, STRK));

            final var bridge_api = githubHelper.createRepo(bridge);
            final var bridge_frontend = githubHelper.createRepo(bridge);

            final var madara = projectHelper.create(hayden, "Madara", List.of(universe, starknet)).getLeft();
            at("2021-01-06T00:00:00Z", () -> accountingHelper.grant(explorationTeam, madara, 120, STRK));

            final var madara_contracts = githubHelper.createRepo(madara);
            final var madara_app = githubHelper.createRepo(madara);


            at("2021-01-01T00:00:00Z", () -> githubHelper.createPullRequest(marketplace_api, antho, List.of("java")));
            final var issueId = at("2021-01-01T00:00:03Z", () -> githubHelper.createIssue(marketplace_frontend, mehdi));
            githubHelper.assignIssueToContributor(issueId, mehdi.user().getGithubUserId());
            at("2021-01-01T00:00:04Z", () -> githubHelper.createPullRequest(marketplace_frontend, mehdi, List.of("ts")));
            at("2021-01-01T00:00:05Z", () -> githubHelper.createPullRequest(marketplace_frontend, hayden, List.of("ts")));
            at("2021-01-01T00:00:07Z", () -> githubHelper.createPullRequest(bridge_frontend, emma, List.of("cairo")));
            at("2021-01-01T00:00:09Z", () -> githubHelper.createPullRequest(bridge_frontend, emma));

            final var prId = at("2021-01-02T00:00:00Z", () -> githubHelper.createPullRequest(marketplace_api, antho, List.of("rs")));
            at("2021-01-03T00:00:03Z", () -> githubHelper.createCodeReview(marketplace_frontend, prId, mehdi));
            at("2021-01-04T00:00:04Z", () -> githubHelper.createPullRequest(marketplace_frontend, mehdi, List.of("ts")));
            at("2021-01-05T00:00:05Z", () -> githubHelper.createPullRequest(marketplace_frontend, hayden, List.of("ts")));
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

            at("2021-01-01T00:00:00Z", () -> rewardHelper.create(onlyDustId, pierre, antho.githubUserId(), 1, STRK));
            at("2021-01-01T00:00:00Z", () -> rewardHelper.create(onlyDustId, pierre, antho.githubUserId(), 2, STRK));
            at("2021-01-01T00:00:00Z", () -> rewardHelper.create(onlyDustId, pierre, james.githubUserId(), 3, STRK));
            at("2021-01-03T00:00:00Z", () -> rewardHelper.create(onlyDustId, pierre, james.githubUserId(), 4, STRK));

            at("2021-02-10T00:00:00Z", () -> rewardHelper.create(onlyDustId, pierre, abdel.githubUserId(), 5, STRK));

            allProgramOrEcosystemIds = String.join(",", Stream.of(universe).map(UUID::toString).toList());

            // Change the "current" usd quotes of STRK and ETH so that the available budget is affected
            currencyHelper.setQuote("2024-09-01T00:00:00Z", STRK, USD, BigDecimal.valueOf(0.25));
            currencyHelper.setQuote("2024-09-01T00:00:00Z", ETH, USD, BigDecimal.valueOf(3));

            at("2024-09-02T00:00:00Z", () -> accountingHelper.ungrant(madara, explorationTeam, 10, STRK));
            projectFacadePort.refreshStats();
        }

        @Test
        public void should_get_projects_activity_status() {
            test_projects_activity_status("2021-01-01", "2021-01-01", Map.of(
                    onlyDustId, EngagementStatus.NEW
            ));
            test_projects_activity_status("2021-01-02", "2021-01-02", Map.of(
                    onlyDustId, EngagementStatus.ACTIVE
            ));
            test_projects_activity_status("2021-01-06", "2021-01-06", Map.of(
                    onlyDustId, EngagementStatus.CHURNED
            ));
            test_projects_activity_status("2021-01-07", "2021-01-07", Map.of(
                    onlyDustId, INACTIVE
            ));
            test_projects_activity_status("2021-02-01", "2021-02-01", Map.of(
                    onlyDustId, EngagementStatus.REACTIVATED
            ));
        }

        private void test_projects_activity_status(String fromDate, String toDate, Map<ProjectId, EngagementStatus> expectedStatusPerProject) {
            final var queryParams = new HashMap<String, String>();
            queryParams.put("pageIndex", "0");
            queryParams.put("pageSize", "100");
            queryParams.put("fromDate", fromDate);
            queryParams.put("toDate", toDate);
            queryParams.put("dataSourceIds", allProgramOrEcosystemIds);
            final var response = client.get()
                    .uri(getApiURI(BI_PROJECTS, queryParams))
                    .header("Authorization", BEARER_PREFIX + userAuthHelper.signInUser(caller).jwt())
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody(BiProjectsPageResponse.class).returnResult().getResponseBody();
            assertThat(response.getProjects()).isNotEmpty();
            expectedStatusPerProject.forEach((projectId, expectedStatus) -> {
                final var project = response.getProjects().stream().filter(p -> projectId.value().equals(p.getProject().getId())).findFirst();
                if (expectedStatus == INACTIVE) {
                    assertThat(project.isEmpty()
                               || project.get().getContributionCount().getValue() > 0
                               || project.get().getRewardCount().getValue() > 0
                               || project.get().getTotalGrantedUsdAmount().getValue().doubleValue() > 0).isTrue();
                    return;
                }
                assertThat(project).isNotEmpty();
                assertThat(project.get().getEngagementStatus()).isEqualTo(expectedStatus);
            });
        }

        @Test
        public void should_get_projects_stats_between_dates() {
            // When
            client.get()
                    .uri(getApiURI(BI_PROJECTS, Map.of("pageIndex", "0",
                            "pageSize", "100",
                            "fromDate", "2021-01-01",
                            "toDate", "2021-01-07")))
                    .header("Authorization", BEARER_PREFIX + caller.jwt())
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .jsonPath("$.projects[0].project.name").<String>value(name -> assertThat(name).contains("Bridge"))
                    .jsonPath("$.projects[1].project.name").<String>value(name -> assertThat(name).contains("Madara"))
                    .jsonPath("$.projects[2].project.name").<String>value(name -> assertThat(name).contains("OnlyDust"))
                    .json("""
                            {
                              "totalPageNumber": 1,
                              "totalItemNumber": 3,
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
                                  "languages": [
                                    {
                                      "slug": "cairo",
                                      "name": "Cairo"
                                    }
                                  ],
                                  "ecosystems": [
                                    {
                                      "name": "Ethereum ecosystem"
                                    },
                                    {
                                      "name": "Starknet ecosystem"
                                    },
                                    {
                                      "name": "Universe ecosystem"
                                    }
                                  ],
                                  "programs": [
                                    {
                                      "name": "Ethereum Granting Program"
                                    },
                                    {
                                      "name": "Starkware Exploration Team"
                                    }
                                  ],
                                  "engagementStatus": "NEW",
                                  "availableBudget": {
                                    "totalUsdEquivalent": 3002.50,
                                    "totalPerCurrency": [
                                      {
                                        "amount": 1000,
                                        "prettyAmount": 1000,
                                        "currency": {
                                          "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                          "code": "ETH",
                                          "name": "Ether",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 3000,
                                        "usdConversionRate": null,
                                        "ratio": null
                                      },
                                      {
                                        "amount": 10,
                                        "prettyAmount": 10,
                                        "currency": {
                                          "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                          "code": "STRK",
                                          "name": "StarkNet Token",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 2.5,
                                        "usdConversionRate": null,
                                        "ratio": null
                                      }
                                    ]
                                  },
                                  "percentUsedBudget": 0.00,
                                  "totalGrantedUsdAmount": {
                                    "value": 2000,
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
                                  "rewardCount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "issueCount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "prCount": {
                                    "value": 4,
                                    "trend": "UP"
                                  },
                                  "codeReviewCount": {
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
                                      "login": "hayden"
                                    }
                                  ],
                                  "categories": null,
                                  "languages": null,
                                  "ecosystems": [
                                    {
                                      "name": "Starknet ecosystem"
                                    },
                                    {
                                      "name": "Universe ecosystem"
                                    }
                                  ],
                                  "programs": [
                                    {
                                      "name": "Starkware Exploration Team"
                                    }
                                  ],
                                  "engagementStatus": "INACTIVE",
                                  "availableBudget": {
                                    "totalUsdEquivalent": 27.50,
                                    "totalPerCurrency": [
                                      {
                                        "amount": 110,
                                        "prettyAmount": 110,
                                        "currency": {
                                          "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                          "code": "STRK",
                                          "name": "StarkNet Token",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 27.50,
                                        "usdConversionRate": null,
                                        "ratio": null
                                      }
                                    ]
                                  },
                                  "percentUsedBudget": 0.00,
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
                                  "rewardCount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "issueCount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "prCount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "codeReviewCount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "contributionCount": {
                                    "value": 0,
                                    "trend": "STABLE"
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
                                  "languages": [
                                    {
                                      "slug": "java",
                                      "name": "Java"
                                    },
                                    {
                                      "slug": "rust",
                                      "name": "Rust"
                                    },
                                    {
                                      "slug": "typescript",
                                      "name": "TypeScript"
                                    }
                                  ],
                                  "ecosystems": [
                                    {
                                      "name": "Universe ecosystem"
                                    }
                                  ],
                                  "programs": [
                                    {
                                      "name": "Ethereum Granting Program"
                                    },
                                    {
                                      "name": "Nethermind"
                                    }
                                  ],
                                  "engagementStatus": "NEW",
                                  "availableBudget": {
                                    "totalUsdEquivalent": 96.25,
                                    "totalPerCurrency": [
                                      {
                                        "amount": 25,
                                        "prettyAmount": 25,
                                        "currency": {
                                          "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                          "code": "ETH",
                                          "name": "Ether",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 75,
                                        "usdConversionRate": null,
                                        "ratio": null
                                      },
                                      {
                                        "amount": 85,
                                        "prettyAmount": 85,
                                        "currency": {
                                          "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                          "code": "STRK",
                                          "name": "StarkNet Token",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 21.25,
                                        "usdConversionRate": null,
                                        "ratio": null
                                      }
                                    ]
                                  },
                                  "percentUsedBudget": 0.04,
                                  "totalGrantedUsdAmount": {
                                    "value": 100.0,
                                    "trend": "UP"
                                  },
                                  "totalRewardedUsdAmount": {
                                    "value": 5.0,
                                    "trend": "UP"
                                  },
                                  "averageRewardUsdAmount": {
                                    "value": 1.25,
                                    "trend": "UP"
                                  },
                                  "onboardedContributorCount": {
                                    "value": 3,
                                    "trend": "UP"
                                  },
                                  "activeContributorCount": {
                                    "value": 3,
                                    "trend": "UP"
                                  },
                                  "rewardCount": {
                                    "value": 4,
                                    "trend": "UP"
                                  },
                                  "issueCount": {
                                    "value": 1,
                                    "trend": "UP"
                                  },
                                  "prCount": {
                                    "value": 6,
                                    "trend": "UP"
                                  },
                                  "codeReviewCount": {
                                    "value": 1,
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

        @Test
        public void should_get_projects_stats_with_pagination() {
            // When
            client.get()
                    .uri(getApiURI(BI_PROJECTS, Map.of("pageIndex", "0",
                            "pageSize", "2",
                            "fromDate", "2021-01-01",
                            "toDate", "2021-01-10")))
                    .header("Authorization", BEARER_PREFIX + caller.jwt())
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .json("""
                            {
                              "totalPageNumber": 2,
                              "totalItemNumber": 3,
                              "hasMore": true,
                              "nextPageIndex": 1
                            }
                            """);
        }

        @Test
        public void should_export_projects_stats_between_dates() {
            // When
            final var csv = client.get()
                    .uri(getApiURI(BI_PROJECTS, Map.of("pageIndex", "0",
                            "pageSize", "100",
                            "fromDate", "2021-01-01",
                            "toDate", "2021-01-10")))
                    .header("Authorization", BEARER_PREFIX + caller.jwt())
                    .accept(MediaType.valueOf("text/csv"))
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody(String.class)
                    .returnResult().getResponseBody();

            final var lines = Objects.requireNonNull(csv).split("\\R");
            assertThat(lines).hasSize(4);
            assertThat(lines[0]).isEqualTo("project;leads;categories;languages;ecosystems;programs;available_budget_usd_amount;percent_used_budget;" +
                                           "total_granted_usd_amount;total_rewarded_usd_amount;average_reward_usd_amount;onboarded_contributor_count;" +
                                           "active_contributor_count;reward_count;issue_count;pr_count;code_review_count;contribution_count;" +
                                           "available_budget_amount_STRK;total_granted_amount_STRK;total_rewarded_amount_STRK;" +
                                           "available_budget_amount_ETH;total_granted_amount_ETH;total_rewarded_amount_ETH");
            assertThat(lines[1]).endsWith(";mehdi;Gaming;Cairo;Ethereum ecosystem,Starknet ecosystem,Universe ecosystem;Ethereum Granting Program,Starkware " +
                                          "Exploration Team;3002.50;0.00;2000;0;0;1;1;0;0;4;0;4;10;0;0;1000;1000;0");
            assertThat(lines[2]).endsWith(";hayden;;;Starknet ecosystem,Universe ecosystem;Starkware " +
                                          "Exploration Team;27.50;0.00;60.0;0;0;0;0;0;0;0;0;0;110;120;0;0;0;0");
            assertThat(lines[3]).endsWith(";pierre;DeFi;Java,Rust,TypeScript;Universe ecosystem;Ethereum Granting Program," +
                                          "Nethermind;96.25;0.04;100.0;5.0;1.25;3;3;4;1;6;1;8;85;100;10;25;25;0");

        }

        private void test_projects_stats(String queryParam, String value, Consumer<BiProjectsPageResponse> asserter, boolean assertNotEmpty) {
            test_projects_stats(Map.of(queryParam, value), asserter, assertNotEmpty);
        }

        private void test_projects_stats(Map<String, String> queryParamsWithValues, Consumer<BiProjectsPageResponse> asserter, boolean assertNotEmpty) {
            final var queryParams = new HashMap<String, String>();
            queryParams.put("pageIndex", "0");
            queryParams.put("pageSize", "100");
            queryParams.put("fromDate", "2021-01-01");
            queryParams.put("toDate", "2021-01-10");
            queryParams.put("dataSourceIds", allProgramOrEcosystemIds);
            queryParams.putAll(queryParamsWithValues);
            final var response = client.get()
                    .uri(getApiURI(BI_PROJECTS, queryParams))
                    .header("Authorization", BEARER_PREFIX + caller.jwt())
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody(BiProjectsPageResponse.class).returnResult().getResponseBody();
            if (assertNotEmpty)
                assertThat(response.getProjects()).isNotEmpty();
            asserter.accept(response);
        }

        @Test
        public void should_get_projects_stats_with_filters() {
            test_projects_stats("search", "gaming",
                    response -> response.getProjects().forEach(project -> assertThat(project.getCategories().stream().map(ProjectCategoryResponse::getName).toList())
                            .contains("Gaming")), true
            );
            test_projects_stats("search", "eth",
                    response -> {
                        assertThat(response.getProjects()).hasSize(2);
                        assertThat(response.getProjects().get(0).getProject().getName()).contains("Bridge");
                        assertThat(response.getProjects().get(1).getProject().getName()).contains("OnlyDust");
                    }, true
            );
            test_projects_stats("projectIds", onlyDustId.toString(),
                    response -> assertThat(response.getProjects())
                            .hasSize(1)
                            .extracting(BiProjectsPageItemResponse::getProject).extracting(ProjectLinkResponse::getId).contains(onlyDustId.value()), true
            );
            test_projects_stats("programIds", ethGrantingProgram.toString(),
                    response -> assertThat(response.getProjects())
                            .extracting(BiProjectsPageItemResponse::getPrograms)
                            .allMatch(programs -> programs.stream().map(ProgramLinkResponse::getId).toList().contains(ethGrantingProgram.value())), true
            );
            test_projects_stats("projectSlugs", onlyDustSlug,
                    response -> assertThat(response.getProjects())
                            .hasSize(1)
                            .extracting(BiProjectsPageItemResponse::getProject).extracting(ProjectLinkResponse::getId).contains(onlyDustId.value()), true
            );
            test_projects_stats("projectLeadIds", mehdi.userId().toString(),
                    response -> response.getProjects().forEach(project -> assertThat(project.getProjectLeads().stream().map(RegisteredUserResponse::getGithubUserId))
                            .contains(mehdi.githubUserId().value())), true
            );
            test_projects_stats("categoryIds", gaming.id().toString(),
                    response -> response.getProjects().forEach(project -> assertThat(project.getCategories().stream().map(ProjectCategoryResponse::getName).toList())
                            .contains("Gaming")), true
            );
            test_projects_stats("languageIds", "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                    response -> response.getProjects().forEach(project -> assertThat(project.getLanguages().stream().map(LanguageResponse::getName).toList())
                            .contains("Cairo")), true
            );
            test_projects_stats("ecosystemIds", starknet.toString(),
                    response -> response.getProjects().forEach(project -> assertThat(project.getEcosystems().stream().map(EcosystemLinkResponse::getName).toList())
                            .contains("Starknet ecosystem")), true
            );
            test_projects_stats(Map.of("totalGrantedUsdAmount.gte", "1800", "totalGrantedUsdAmount.lte", "2200"),
                    response -> response.getProjects().forEach(project -> assertThat(project.getTotalGrantedUsdAmount().getValue())
                            .isEqualTo(BigDecimal.valueOf(2000))), true
            );
            test_projects_stats(Map.of("availableBudgetUsdAmount.gte", "90", "availableBudgetUsdAmount.lte", "100"),
                    response -> response.getProjects().forEach(project -> assertThat(project.getAvailableBudget().getTotalUsdEquivalent())
                            .isEqualTo(BigDecimal.valueOf(96.25))), true
            );
            test_projects_stats(Map.of("percentUsedBudget.gte", "0.001"),
                    response -> response.getProjects().forEach(project -> assertThat(project.getPercentUsedBudget())
                            .isEqualTo(BigDecimal.valueOf(0.04))), true
            );
            test_projects_stats(Map.of("totalGrantedUsdAmount.eq", "2000"),
                    response -> response.getProjects().forEach(project -> assertThat(project.getTotalGrantedUsdAmount().getValue())
                            .isEqualTo(BigDecimal.valueOf(2000))), true
            );
            test_projects_stats(Map.of("totalRewardedUsdAmount.lte", "5"),
                    response -> response.getProjects().forEach(project -> assertThat(project.getTotalRewardedUsdAmount().getValue())
                            .isEqualTo(BigDecimal.valueOf(5.0))), true
            );
            test_projects_stats(Map.of("averageRewardUsdAmount.gte", "1.25", "averageRewardUsdAmount.lte", "2"),
                    response -> response.getProjects().forEach(project -> assertThat(project.getAverageRewardUsdAmount().getValue())
                            .isEqualTo(BigDecimal.valueOf(1.25))), true
            );
            test_projects_stats(Map.of("onboardedContributorCount.eq", "3"),
                    response -> response.getProjects().forEach(project -> assertThat(project.getOnboardedContributorCount().getValue())
                            .isEqualTo(3)), true
            );
            test_projects_stats(Map.of("activeContributorCount.eq", "1"),
                    response -> response.getProjects().forEach(project -> assertThat(project.getActiveContributorCount().getValue())
                            .isEqualTo(1)), true
            );
            test_projects_stats(Map.of("contributionCount.eq", "1", "contributionCount.types", "ISSUE"),
                    response -> response.getProjects().forEach(project -> assertThat(project.getIssueCount().getValue())
                            .isEqualTo(1)), true
            );
            test_projects_stats(Map.of("contributionCount.eq", "6", "contributionCount.types", "PULL_REQUEST"),
                    response -> response.getProjects().forEach(project -> assertThat(project.getPrCount().getValue())
                            .isEqualTo(6)), true
            );
            test_projects_stats(Map.of("contributionCount.eq", "1", "contributionCount.types", "CODE_REVIEW"),
                    response -> response.getProjects().forEach(project -> assertThat(project.getCodeReviewCount().getValue())
                            .isEqualTo(1)), true
            );
            test_projects_stats(Map.of("rewardCount.eq", "4"),
                    response -> response.getProjects().forEach(project -> assertThat(project.getRewardCount().getValue())
                            .isEqualTo(4)), true
            );
            test_projects_stats(Map.of("contributionCount.eq", "8", "contributionCount.types", "ISSUE,PULL_REQUEST,CODE_REVIEW"),
                    response -> response.getProjects().forEach(project -> assertThat(project.getContributionCount().getValue())
                            .isEqualTo(8)), true
            );
            test_projects_stats(Map.of("languageIds", "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                            "showFilteredKpis", "true"),
                    response -> assertThat(response.getProjects())
                            .extracting(BiProjectsPageItemResponse::getContributionCount)
                            .extracting(NumberKpi::getValue)
                            .contains(1), true
            );
            test_projects_stats(Map.of("languageIds", "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                            "showFilteredKpis", "false"),
                    response -> assertThat(response.getProjects())
                            .extracting(BiProjectsPageItemResponse::getContributionCount)
                            .extracting(NumberKpi::getValue)
                            .contains(4), true
            );
            test_projects_stats(Map.of("engagementStatuses", "NEW"),
                    response -> response.getProjects().forEach(project -> assertThat(project.getEngagementStatus())
                            .isEqualTo(NEW)), true
            );
        }

        @Test
        public void should_get_projects_stats_ordered() {
            test_projects_stats(Map.of("sort", ProjectKpiSortEnum.PROJECT_NAME.name(), "sortDirection", "ASC"),
                    response -> assertThat(response.getProjects()).isSortedAccordingTo(Comparator.comparing(p -> p.getProject().getName())), true
            );
            test_projects_stats(Map.of("sort", ProjectKpiSortEnum.ACTIVE_CONTRIBUTOR_COUNT.name(), "sortDirection", "ASC"),
                    response -> assertThat(response.getProjects()).isSortedAccordingTo(Comparator.comparing(p -> p.getActiveContributorCount().getValue())),
                    true
            );
            test_projects_stats(Map.of("sort", ProjectKpiSortEnum.PERCENT_USED_BUDGET.name(), "sortDirection", "ASC"),
                    response -> assertThat(response.getProjects()).isSortedAccordingTo(Comparator.comparing(p -> p.getPercentUsedBudget())), true
            );
            test_projects_stats(Map.of("sort", ProjectKpiSortEnum.TOTAL_GRANTED_USD_AMOUNT.name(), "sortDirection", "ASC"),
                    response -> assertThat(response.getProjects()).isSortedAccordingTo(Comparator.comparing(p -> p.getTotalGrantedUsdAmount().getValue())), true
            );
            test_projects_stats(Map.of("sort", ProjectKpiSortEnum.AVERAGE_REWARD_USD_AMOUNT.name(), "sortDirection", "ASC"),
                    response -> assertThat(response.getProjects()).isSortedAccordingTo(Comparator.comparing(p -> p.getAverageRewardUsdAmount().getValue())),
                    true
            );
            test_projects_stats(Map.of("sort", ProjectKpiSortEnum.TOTAL_REWARDED_USD_AMOUNT.name(), "sortDirection", "ASC"),
                    response -> assertThat(response.getProjects()).isSortedAccordingTo(Comparator.comparing(p -> p.getTotalRewardedUsdAmount().getValue())),
                    true
            );
            test_projects_stats(Map.of("sort", ProjectKpiSortEnum.ONBOARDED_CONTRIBUTOR_COUNT.name(), "sortDirection", "ASC"),
                    response -> assertThat(response.getProjects()).isSortedAccordingTo(Comparator.comparing(p -> p.getOnboardedContributorCount().getValue())), true
            );
            test_projects_stats(Map.of("sort", ProjectKpiSortEnum.ACTIVE_CONTRIBUTOR_COUNT.name(), "sortDirection", "ASC"),
                    response -> assertThat(response.getProjects()).isSortedAccordingTo(Comparator.comparing(p -> p.getActiveContributorCount().getValue())),
                    true
            );
            test_projects_stats(Map.of("sort", ProjectKpiSortEnum.REWARD_COUNT.name(), "sortDirection", "ASC"),
                    response -> assertThat(response.getProjects()).isSortedAccordingTo(Comparator.comparing(p -> p.getRewardCount().getValue())), true
            );
            test_projects_stats(Map.of("sort", ProjectKpiSortEnum.ISSUE_COUNT.name(), "sortDirection", "ASC"),
                    response -> assertThat(response.getProjects()).isSortedAccordingTo(Comparator.comparing(p -> p.getIssueCount().getValue())), true
            );
            test_projects_stats(Map.of("sort", ProjectKpiSortEnum.PR_COUNT.name(), "sortDirection", "ASC"),
                    response -> assertThat(response.getProjects()).isSortedAccordingTo(Comparator.comparing(p -> p.getPrCount().getValue())), true
            );
            test_projects_stats(Map.of("sort", ProjectKpiSortEnum.CODE_REVIEW_COUNT.name(), "sortDirection", "ASC"),
                    response -> assertThat(response.getProjects()).isSortedAccordingTo(Comparator.comparing(p -> p.getCodeReviewCount().getValue())), true
            );
            test_projects_stats(Map.of("sort", ProjectKpiSortEnum.CONTRIBUTION_COUNT.name(), "sortDirection", "ASC"),
                    response -> assertThat(response.getProjects()).isSortedAccordingTo(Comparator.comparing(p -> p.getContributionCount().getValue())), true
            );
        }
    }
}
