package onlydust.com.marketplace.api.it.api.bi;

import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.accounting.domain.service.CurrentDateProvider;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.it.api.AbstractMarketplaceApiIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ApplicationRepository;
import onlydust.com.marketplace.api.suites.tags.TagBI;
import onlydust.com.marketplace.kernel.model.ContributionUUID;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
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

import static java.util.Comparator.comparing;
import static onlydust.com.marketplace.api.helper.CurrencyHelper.*;
import static onlydust.com.marketplace.api.helper.DateHelper.at;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

@TagBI
public class ContributorDeepKpisApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    ProjectFacadePort projectFacadePort;
    @Autowired
    ApplicationRepository applicationRepository;

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
        private static ProjectId onlyDust;
        private static String onlyDustSlug;
        private static ProjectId madara;
        private static ContributionUUID prId;

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
            billingProfileHelper.verify(antho, Country.fromIso3("FRA"));
            pierre = userAuthHelper.create("pierre");
            mehdi = userAuthHelper.create("mehdi");
            billingProfileHelper.verify(mehdi, Country.fromIso3("MAR"));
            hayden = userAuthHelper.create("hayden");
            billingProfileHelper.verify(hayden, Country.fromIso3("GBR"));
            abdel = userAuthHelper.create("abdel");
            billingProfileHelper.verify(abdel, Country.fromIso3("MAR"));
            emma = userAuthHelper.create("emma");
            james = userAuthHelper.create("james");
            billingProfileHelper.verify(james, Country.fromIso3("GBR"));

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

            final var od = projectHelper.create(pierre, "OnlyDust", List.of(universe));
            onlyDust = od.getLeft();
            onlyDustSlug = od.getRight();
            projectHelper.addCategory(onlyDust, defi.id());
            at("2021-01-01T00:00:00Z", () -> accountingHelper.grant(nethermind, onlyDust, 100, STRK));
            at("2021-01-05T00:00:00Z", () -> accountingHelper.grant(ethGrantingProgram, onlyDust, 25, ETH));

            final var marketplace_api = githubHelper.createRepo(onlyDust);
            final var marketplace_frontend = githubHelper.createRepo(onlyDust);

            final var bridge = projectHelper.create(mehdi, "Bridge", List.of(universe, starknet, ethereum)).getLeft();
            projectHelper.addCategory(bridge, gaming.id());
            at("2021-01-01T00:00:00Z", () -> accountingHelper.grant(ethGrantingProgram, bridge, 1_000, ETH));
            at("2021-02-05T00:00:00Z", () -> accountingHelper.grant(explorationTeam, bridge, 10, STRK));

            final var bridge_api = githubHelper.createRepo(bridge);
            final var bridge_frontend = githubHelper.createRepo(bridge);

            madara = projectHelper.create(hayden, "Madara", List.of(universe, starknet)).getLeft();
            at("2021-01-06T00:00:00Z", () -> accountingHelper.grant(explorationTeam, madara, 120, STRK));

            final var madara_contracts = githubHelper.createRepo(madara);
            final var madara_app = githubHelper.createRepo(madara);


            at("2021-01-01T00:00:00Z", () -> githubHelper.createPullRequest(marketplace_api, antho, List.of("java")));

            final var issueId1 = at("2021-01-01T00:00:03Z", () -> githubHelper.createIssue(marketplace_frontend, mehdi));
            at("2021-01-01T02:00:03Z", () -> applicationHelper.create(onlyDust, GithubIssue.Id.of(issueId1), james.githubUserId()));

            final var issueId2 = at("2021-01-01T00:00:04Z", () -> githubHelper.createIssue(marketplace_frontend.getId(), CurrentDateProvider.now(),
                    null, "OPEN", mehdi));
            at("2021-01-01T02:00:04Z", () -> applicationHelper.create(onlyDust, GithubIssue.Id.of(issueId2), james.githubUserId())); // SHELVED
            githubHelper.assignIssueToContributor(issueId2, mehdi.user().getGithubUserId());

            at("2021-01-01T00:00:04Z", () -> githubHelper.createPullRequest(marketplace_frontend, mehdi, List.of("ts")));
            at("2021-01-01T00:00:05Z", () -> githubHelper.createPullRequest(marketplace_frontend, hayden, List.of("ts")));
            at("2021-01-01T00:00:07Z", () -> githubHelper.createPullRequest(bridge_frontend, emma, List.of("cairo")));
            at("2021-01-01T00:00:09Z", () -> githubHelper.createPullRequest(bridge_frontend, emma));


            at("2021-01-02T00:00:00Z", () -> githubHelper.createPullRequest(marketplace_api, antho, List.of("rs")));
            prId = at("2021-01-05T00:00:05Z", () -> githubHelper.createPullRequest(marketplace_frontend, hayden, List.of("ts")));
            at("2021-01-03T00:00:03Z", () -> githubHelper.createCodeReview(marketplace_frontend, prId, mehdi));
            at("2021-01-04T00:00:04Z", () -> githubHelper.createPullRequest(marketplace_frontend, mehdi, List.of("ts")));
            at("2021-01-06T00:00:07Z", () -> githubHelper.createPullRequest(bridge_frontend, emma));
            at("2021-01-07T00:00:09Z", () -> githubHelper.createPullRequest(bridge_frontend, emma));

            at("2021-02-01T00:00:00Z", () -> githubHelper.createPullRequest(marketplace_api, null, antho, null));
            at("2021-02-02T00:00:02Z", () -> githubHelper.createPullRequest(marketplace_api, pierre));
            at("2021-02-03T00:00:03Z", () -> githubHelper.createPullRequest(marketplace_frontend, mehdi));
            at("2021-02-05T00:00:04Z", () -> githubHelper.createPullRequest(marketplace_frontend, mehdi));
            at("2021-02-08T00:00:06Z", () -> githubHelper.createPullRequest(madara_contracts, abdel));
            at("2021-02-13T00:00:06Z", () -> githubHelper.createPullRequest(madara_app, emma));
            at("2021-02-21T00:00:08Z", () -> githubHelper.createPullRequest(bridge_frontend, james));
            at("2021-02-28T00:00:08Z", () -> githubHelper.createPullRequest(bridge_api, james));

            projectFacadePort.refreshStats();
            // BI regarding the following commands should be refreshed in real-time

            at("2021-01-01T00:00:00Z", () -> rewardHelper.create(onlyDust, pierre, antho.githubUserId(), 1, STRK));
            at("2021-01-01T00:00:00Z", () -> rewardHelper.create(onlyDust, pierre, antho.githubUserId(), 2, STRK));
            at("2021-01-01T00:00:00Z", () -> rewardHelper.create(onlyDust, pierre, james.githubUserId(), 3, STRK));
            at("2021-01-03T00:00:00Z", () -> rewardHelper.create(onlyDust, pierre, james.githubUserId(), 4, STRK));

            at("2021-02-10T00:00:00Z", () -> rewardHelper.create(onlyDust, pierre, abdel.githubUserId(), 5, STRK));

            allProgramOrEcosystemIds = String.join(",", Stream.of(universe).map(UUID::toString).toList());
        }

        @Test
        public void should_get_contributors_stats_between_dates() {
            // When
            client.get()
                    .uri(getApiURI(BI_CONTRIBUTORS, Map.of("pageIndex", "0",
                            "pageSize", "100",
                            "fromDate", "2021-01-01",
                            "toDate", "2021-01-07")))
                    .header("Authorization", BEARER_PREFIX + userAuthHelper.signInUser(caller).jwt())
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .consumeWith(System.out::println)
                    .jsonPath("$.contributors[0].projects[0].name").<String>value(name -> assertThat(name).contains("OnlyDust"))
                    .jsonPath("$.contributors[1].projects[0].name").<String>value(name -> assertThat(name).contains("Bridge"))
                    .jsonPath("$.contributors[1].projects[1].name").<String>value(name -> assertThat(name).contains("Madara"))
                    .jsonPath("$.contributors[2].projects[0].name").<String>value(name -> assertThat(name).contains("OnlyDust"))
                    .jsonPath("$.contributors[3].projects[0].name").<String>value(name -> assertThat(name).contains("Bridge"))
                    .jsonPath("$.contributors[4].projects[0].name").<String>value(name -> assertThat(name).contains("OnlyDust"))
                    .json("""
                            {
                              "totalPageNumber": 1,
                              "totalItemNumber": 5,
                              "hasMore": false,
                              "nextPageIndex": 0,
                              "contributors": [
                                {
                                  "contributor": {
                                    "login": "antho"
                                  },
                                  "categories": [
                                    {
                                      "slug": "defi"
                                    }
                                  ],
                                  "languages": [
                                    {
                                      "slug": "java"
                                    },
                                    {
                                      "slug": "rust"
                                    }
                                  ],
                                  "ecosystems": [
                                    {
                                      "slug": "universe-ecosystem"
                                    }
                                  ],
                                  "projectContributorLabels": null,
                                  "countryCode": "FR",
                                  "totalRewardedUsdAmount": {
                                    "value": 1.5,
                                    "trend": "UP"
                                  },
                                  "rewardCount": {
                                    "value": 2,
                                    "trend": "UP"
                                  },
                                  "issueCount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "prCount": {
                                    "value": 2,
                                    "trend": "UP"
                                  },
                                  "codeReviewCount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "contributionCount": {
                                    "value": 2,
                                    "trend": "UP"
                                  },
                                  "maintainedProjectCount": 0,
                                  "inProgressIssueCount": 0,
                                  "pendingApplicationCount": 0
                                },
                                {
                                  "contributor": {
                                    "login": "emma"
                                  },
                                  "categories": [
                                    {
                                      "slug": "gaming"
                                    }
                                  ],
                                  "languages": [
                                    {
                                      "slug": "cairo"
                                    }
                                  ],
                                  "ecosystems": [
                                    {
                                      "slug": "ethereum-ecosystem"
                                    },
                                    {
                                      "slug": "starknet-ecosystem"
                                    },
                                    {
                                      "slug": "universe-ecosystem"
                                    }
                                  ],
                                  "projectContributorLabels": null,
                                  "countryCode": null,
                                  "totalRewardedUsdAmount": {
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
                                  },
                                  "maintainedProjectCount": 0,
                                  "inProgressIssueCount": 0,
                                  "pendingApplicationCount": 0
                                },
                                {
                                  "contributor": {
                                    "login": "hayden"
                                  },
                                  "categories": [
                                    {
                                      "slug": "defi"
                                    }
                                  ],
                                  "languages": [
                                    {
                                      "slug": "typescript"
                                    }
                                  ],
                                  "ecosystems": [
                                    {
                                      "slug": "universe-ecosystem"
                                    }
                                  ],
                                  "projectContributorLabels": null,
                                  "countryCode": "GB",
                                  "totalRewardedUsdAmount": {
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
                                    "value": 2,
                                    "trend": "UP"
                                  },
                                  "codeReviewCount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "contributionCount": {
                                    "value": 2,
                                    "trend": "UP"
                                  },
                                  "maintainedProjectCount": 1,
                                  "inProgressIssueCount": 0,
                                  "pendingApplicationCount": 0
                                },
                                {
                                  "contributor": {
                                    "login": "james"
                                  },
                                  "categories": [
                                    {
                                      "slug": "gaming"
                                    }
                                  ],
                                  "languages": null,
                                  "ecosystems": [
                                    {
                                      "slug": "ethereum-ecosystem"
                                    },
                                    {
                                      "slug": "starknet-ecosystem"
                                    },
                                    {
                                      "slug": "universe-ecosystem"
                                    }
                                  ],
                                  "projectContributorLabels": null,
                                  "countryCode": "GB",
                                  "totalRewardedUsdAmount": {
                                    "value": 3.5,
                                    "trend": "UP"
                                  },
                                  "rewardCount": {
                                    "value": 2,
                                    "trend": "UP"
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
                                  },
                                  "maintainedProjectCount": 0,
                                  "inProgressIssueCount": 0,
                                  "pendingApplicationCount": 0
                                },
                                {
                                  "contributor": {
                                    "login": "mehdi"
                                  },
                                  "categories": [
                                    {
                                      "slug": "defi"
                                    }
                                  ],
                                  "languages": [
                                    {
                                      "slug": "typescript"
                                    }
                                  ],
                                  "ecosystems": [
                                    {
                                      "slug": "universe-ecosystem"
                                    }
                                  ],
                                  "projectContributorLabels": null,
                                  "countryCode": "MA",
                                  "totalRewardedUsdAmount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "rewardCount": {
                                    "value": 0,
                                    "trend": "STABLE"
                                  },
                                  "issueCount": {
                                    "value": 1,
                                    "trend": "UP"
                                  },
                                  "prCount": {
                                    "value": 2,
                                    "trend": "UP"
                                  },
                                  "codeReviewCount": {
                                    "value": 1,
                                    "trend": "UP"
                                  },
                                  "contributionCount": {
                                    "value": 4,
                                    "trend": "UP"
                                  },
                                  "maintainedProjectCount": 1,
                                  "inProgressIssueCount": 0,
                                  "pendingApplicationCount": 0
                                }
                              ]
                            }
                            """);
        }

        @Test
        public void should_get_contributors_stats_with_pagination() {
            // When
            client.get()
                    .uri(getApiURI(BI_CONTRIBUTORS, Map.of("pageIndex", "0",
                            "pageSize", "2",
                            "fromDate", "2021-01-01",
                            "toDate", "2021-01-10")))
                    .header("Authorization", BEARER_PREFIX + userAuthHelper.signInUser(caller).jwt())
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .json("""
                            {
                              "totalPageNumber": 3,
                              "totalItemNumber": 5,
                              "hasMore": true,
                              "nextPageIndex": 1
                            }
                            """);
        }

        @Test
        public void should_export_projects_stats_between_dates() {
            // When
            final var csv = client.get()
                    .uri(getApiURI(BI_CONTRIBUTORS, Map.of("pageIndex", "0",
                            "pageSize", "100",
                            "fromDate", "2021-01-01",
                            "toDate", "2021-01-10")))
                    .header("Authorization", BEARER_PREFIX + userAuthHelper.signInUser(caller).jwt())
                    .accept(MediaType.valueOf("text/csv"))
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody(String.class)
                    .returnResult().getResponseBody();

            final var lines = Objects.requireNonNull(csv).split("\\R");
            assertThat(lines).hasSize(6);
            assertThat(lines[0]).isEqualTo("contributor;projects;categories;languages;ecosystems;country;total_rewarded_usd_amount;" +
                                           "reward_count;issue_count;pr_count;code_review_count;contribution_count");
        }

        @Test
        public void should_get_contributors_stats_of_project() {
            // When
            final var response = client.get()
                    .uri(getApiURI(BI_CONTRIBUTORS, Map.of("pageIndex", "0",
                            "pageSize", "100",
                            "fromDate", "2021-01-01",
                            "toDate", "2021-03-01",
                            "dataSourceIds", madara.toString())))
                    .header("Authorization", BEARER_PREFIX + userAuthHelper.signInUser(hayden).jwt())
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody(BiContributorsPageResponse.class).returnResult().getResponseBody();

            assertThat(response.getContributors()).isNotEmpty();
            response.getContributors().forEach(contributor -> assertThat(contributor.getProjects())
                    .extracting(ProjectLinkResponse::getName)
                    .filteredOn(name -> name.contains("Madara")).isNotEmpty());
        }

        private void test_contributors_stats(String queryParam, String value, Consumer<BiContributorsPageResponse> asserter, boolean assertNotEmpty) {
            test_contributors_stats(Map.of(queryParam, value), asserter, assertNotEmpty);
        }

        private void test_contributors_stats(Map<String, String> queryParamsWithValues, Consumer<BiContributorsPageResponse> asserter, boolean assertNotEmpty) {
            final var queryParams = new HashMap<String, String>();
            queryParams.put("pageIndex", "0");
            queryParams.put("pageSize", "100");
            queryParams.put("fromDate", "2021-01-01");
            queryParams.put("toDate", "2021-01-10");
            queryParams.put("dataSourceIds", allProgramOrEcosystemIds);
            queryParams.putAll(queryParamsWithValues);
            final var response = client.get()
                    .uri(getApiURI(BI_CONTRIBUTORS, queryParams))
                    .header("Authorization", BEARER_PREFIX + userAuthHelper.signInUser(caller).jwt())
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody(BiContributorsPageResponse.class).returnResult().getResponseBody();
            if (assertNotEmpty)
                assertThat(response.getContributors()).isNotEmpty();
            asserter.accept(response);
        }

        @Test
        public void should_get_contributors_stats_with_filters() {
            test_contributors_stats("search", "gaming",
                    response -> response.getContributors().forEach(contributor -> assertThat(contributor.getCategories().stream().map(ProjectCategoryResponse::getName).toList())
                            .contains("Gaming")), true
            );
            test_contributors_stats("search", "typescript",
                    response -> {
                        assertThat(response.getContributors()).hasSize(2);
                        assertThat(response.getContributors().get(0).getContributor().getLogin()).contains("hayden");
                        assertThat(response.getContributors().get(1).getContributor().getLogin()).contains("mehdi");
                    }, true
            );
            test_contributors_stats("contributorIds", mehdi.githubUserId().toString(),
                    response -> assertThat(response.getContributors())
                            .hasSize(1)
                            .extracting(BiContributorsPageItemResponse::getContributor)
                            .extracting(ContributorOverviewResponse::getGithubUserId)
                            .contains(mehdi.githubUserId().value()), true);

            test_contributors_stats(Map.of("contributedTo", prId.value().toString()),
                    response -> assertThat(response.getContributors())
                            .hasSize(1)
                            .extracting(BiContributorsPageItemResponse::getContributor)
                            .extracting(ContributorOverviewResponse::getGithubUserId)
                            .contains(hayden.githubUserId().value()), true);
            test_contributors_stats("projectIds", onlyDust.toString(),
                    response -> response.getContributors().forEach(contributor -> assertThat(contributor.getProjects())
                            .extracting(ProjectLinkResponse::getName)
                            .filteredOn(name -> name.contains("OnlyDust")).isNotEmpty()), true
            );
            test_contributors_stats("projectSlugs", onlyDustSlug,
                    response -> response.getContributors().forEach(contributor -> assertThat(contributor.getProjects())
                            .extracting(ProjectLinkResponse::getName)
                            .filteredOn(name -> name.contains("OnlyDust")).isNotEmpty()), true
            );
            test_contributors_stats("categoryIds", gaming.id().toString(),
                    response -> response.getContributors().forEach(contributor -> assertThat(contributor.getCategories().stream().map(ProjectCategoryResponse::getName).toList())
                            .contains("Gaming")), true
            );
            test_contributors_stats("languageIds", "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                    response -> response.getContributors().forEach(contributor -> assertThat(contributor.getLanguages().stream().map(LanguageResponse::getName).toList())
                            .contains("Cairo")), true
            );
            test_contributors_stats("ecosystemIds", starknet.toString(),
                    response -> response.getContributors().forEach(contributor -> assertThat(contributor.getEcosystems().stream().map(EcosystemLinkResponse::getName).toList())
                            .contains("Starknet ecosystem")), true
            );
            test_contributors_stats("countryCodes", "GB",
                    response -> response.getContributors().forEach(contributor -> assertThat(contributor.getCountryCode())
                            .isEqualTo("GB")), true
            );
            test_contributors_stats(Map.of("totalRewardedUsdAmount.lte", "3"),
                    response -> response.getContributors().forEach(contributor -> assertThat(contributor.getTotalRewardedUsdAmount().getValue())
                            .isLessThanOrEqualTo(BigDecimal.valueOf(3.0))), true
            );
            test_contributors_stats(Map.of("contributionCount.eq", "1", "contributionCount.types", "ISSUE"),
                    response -> response.getContributors().forEach(project -> assertThat(project.getIssueCount().getValue())
                            .isEqualTo(1)), true
            );
            test_contributors_stats(Map.of("contributionCount.eq", "2", "contributionCount.types", "PULL_REQUEST"),
                    response -> response.getContributors().forEach(project -> assertThat(project.getPrCount().getValue())
                            .isEqualTo(2)), true
            );
            test_contributors_stats(Map.of("contributionCount.eq", "1", "contributionCount.types", "CODE_REVIEW"),
                    response -> response.getContributors().forEach(project -> assertThat(project.getCodeReviewCount().getValue())
                            .isEqualTo(1)), true
            );
            test_contributors_stats(Map.of("rewardCount.eq", "2"),
                    response -> response.getContributors().forEach(contributor -> assertThat(contributor.getRewardCount().getValue())
                            .isEqualTo(2)), true
            );
            test_contributors_stats(Map.of("contributionCount.eq", "4", "contributionCount.types", "ISSUE,PULL_REQUEST,CODE_REVIEW"),
                    response -> response.getContributors().forEach(contributor -> assertThat(contributor.getContributionCount().getValue())
                            .isEqualTo(4)), true
            );
            test_contributors_stats(Map.of("contributorIds", mehdi.githubUserId().toString(),
                            "languageIds", "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                            "showFilteredKpis", "true"),
                    response -> assertThat(response.getContributors())
                            .hasSize(1)
                            .extracting(BiContributorsPageItemResponse::getContributionCount)
                            .extracting(NumberKpi::getValue)
                            .contains(2), true);
            test_contributors_stats(Map.of("contributorIds", mehdi.githubUserId().toString(),
                            "languageIds", "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                            "showFilteredKpis", "false"),
                    response -> assertThat(response.getContributors())
                            .hasSize(1)
                            .extracting(BiContributorsPageItemResponse::getContributionCount)
                            .extracting(NumberKpi::getValue)
                            .contains(4), true);
            test_contributors_stats(Map.of("includeApplicants", "true", "projectIds", onlyDust.value().toString()),
                    response -> assertThat(response.getContributors())
                            .extracting(c -> c.getContributor().getLogin())
                            .containsExactlyInAnyOrder("antho", "hayden", "mehdi", "james"), true);
            test_contributors_stats(Map.of("includeApplicants", "true", "projectSlugs", onlyDustSlug),
                    response -> assertThat(response.getContributors())
                            .extracting(c -> c.getContributor().getLogin())
                            .containsExactlyInAnyOrder("antho", "hayden", "mehdi", "james"), true);
        }

        @Test
        public void should_get_contributors_stats_ordered() {
            test_contributors_stats(Map.of("sort", "PR_COUNT", "sortDirection", "ASC"),
                    response -> assertThat(response.getContributors()).isSortedAccordingTo(comparing(c -> c.getPrCount().getValue())), true
            );
        }

        @Test
        public void should_get_antho_by_id() {
            // When
            client.get()
                    .uri(getApiURI(BI_CONTRIBUTORS_BY_ID.formatted(antho.githubUserId().toString())))
                    .header("Authorization", BEARER_PREFIX + userAuthHelper.signInUser(hayden).jwt())
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .json("""
                            {
                              "contributor": {
                                "bio": null,
                                "signedUpOnGithubAt": null,
                                "contacts": null,
                                "login": "antho",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/antho",
                                "isRegistered": true,
                                "globalRank": null,
                                "globalRankPercentile": null,
                                "globalRankCategory": "F"
                              },
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
                                }
                              ],
                              "ecosystems": [
                                {
                                  "name": "Universe ecosystem"
                                }
                              ],
                              "projectContributorLabels": null,
                              "countryCode": "FR",
                              "totalRewardedUsdAmount": {
                                "value": 1.5,
                                "trend": "UP"
                              },
                              "rewardCount": {
                                "value": 2,
                                "trend": "UP"
                              },
                              "issueCount": {
                                "value": 0,
                                "trend": "STABLE"
                              },
                              "prCount": {
                                "value": 3,
                                "trend": "UP"
                              },
                              "codeReviewCount": {
                                "value": 0,
                                "trend": "STABLE"
                              },
                              "contributionCount": {
                                "value": 3,
                                "trend": "UP"
                              },
                              "maintainedProjectCount": 0,
                              "inProgressIssueCount": 0,
                              "pendingApplicationCount": 0
                            }
                            """);
        }

        @Test
        public void should_get_pierre_by_id() {
            // When
            client.get()
                    .uri(getApiURI(BI_CONTRIBUTORS_BY_ID.formatted(pierre.githubUserId().toString())))
                    .header("Authorization", BEARER_PREFIX + userAuthHelper.signInUser(hayden).jwt())
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .json("""
                            {
                              "contributor": {
                                "login": "pierre"
                              },
                              "maintainedProjectCount": 1,
                              "inProgressIssueCount": 0,
                              "pendingApplicationCount": 0
                            }
                            """);
        }

        @Test
        public void should_get_mehdi_by_id() {
            // When
            client.get()
                    .uri(getApiURI(BI_CONTRIBUTORS_BY_ID.formatted(mehdi.githubUserId().toString())))
                    .header("Authorization", BEARER_PREFIX + userAuthHelper.signInUser(hayden).jwt())
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .json("""
                            {
                              "contributor": {
                                "login": "mehdi"
                              },
                              "maintainedProjectCount": 1,
                              "inProgressIssueCount": 1,
                              "pendingApplicationCount": 0
                            }
                            """);
        }

        @Test
        public void should_get_james_by_id() {
            // When
            client.get()
                    .uri(getApiURI(BI_CONTRIBUTORS_BY_ID.formatted(james.githubUserId().toString())))
                    .header("Authorization", BEARER_PREFIX + userAuthHelper.signInUser(hayden).jwt())
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .json("""
                            {
                              "contributor": {
                                "login": "james"
                              },
                              "maintainedProjectCount": 0,
                              "inProgressIssueCount": 0,
                              "pendingApplicationCount": 1
                            }
                            """);
        }
    }
}
