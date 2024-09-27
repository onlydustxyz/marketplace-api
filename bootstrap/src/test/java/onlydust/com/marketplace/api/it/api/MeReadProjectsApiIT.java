package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.service.CurrentDateProvider;
import onlydust.com.marketplace.api.contract.model.DetailedTotalMoney;
import onlydust.com.marketplace.api.contract.model.MyProjectsPageResponse;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagMe;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.SponsorId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Set;
import java.util.stream.LongStream;

import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toSet;
import static onlydust.com.marketplace.api.helper.CurrencyHelper.ETH;
import static onlydust.com.marketplace.api.helper.CurrencyHelper.USDC;
import static org.assertj.core.api.Assertions.assertThat;

@TagMe
public class MeReadProjectsApiIT extends AbstractMarketplaceApiIT {
    UserAuthHelper.AuthenticatedUser caller;

    @BeforeEach
    void setUp() {
        caller = userAuthHelper.create();
    }

    @Nested
    class GivenNoProjects {
        @Test
        void should_get_my_projects_with_no_result() {
            // When
            client.get()
                    .uri(getApiURI(ME_PROJECTS))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .json("""
                            {
                              "totalPageNumber": 0,
                              "totalItemNumber": 0,
                              "hasMore": false,
                              "nextPageIndex": 0,
                              "projects": []
                            }
                            """);
        }
    }

    @Nested
    class GivenMyProjects {
        Set<ProjectId> projectIds;

        @BeforeEach
        void setUp() {
            projectIds = LongStream.range(1, 14).mapToObj(i -> projectHelper.create(caller).getLeft()).collect(toSet());
        }

        @Test
        void should_get_my_projects() {
            // When
            final var response = client.get()
                    .uri(getApiURI(ME_PROJECTS))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                    .expectBody(MyProjectsPageResponse.class)
                    .returnResult().getResponseBody();

            assertThat(response).isNotNull();
            assertThat(response.getTotalItemNumber()).isEqualTo(13);
            assertThat(response.getTotalPageNumber()).isEqualTo(3);
            assertThat(response.getHasMore()).isTrue();
            assertThat(response.getNextPageIndex()).isEqualTo(1);
            assertThat(response.getProjects()).hasSize(5);
            assertThat(response.getProjects().get(0).getName().compareTo(response.getProjects().get(4).getName())).isLessThan(0);
            assertThat(response.getProjects()).allMatch(p -> projectIds.stream().anyMatch(p1 -> p1.value().equals(p.getId())));
            assertThat(response.getProjects()).extracting("leads", List.class).allMatch(leads -> leads.size() == 1);
            assertThat(response.getProjects()).extracting("contributorCount", Integer.class).allMatch(count -> count == 0);
            assertThat(response.getProjects()).extracting("totalAvailable", DetailedTotalMoney.class).allMatch(t -> t.getTotalUsdEquivalent().compareTo(ZERO) == 0);
            assertThat(response.getProjects()).extracting("totalGranted", DetailedTotalMoney.class).allMatch(t -> t.getTotalUsdEquivalent().compareTo(ZERO) == 0);
            assertThat(response.getProjects()).extracting("totalRewarded", DetailedTotalMoney.class).allMatch(t -> t.getTotalUsdEquivalent().compareTo(ZERO) == 0);
        }
    }

    @Nested
    class GivenOneProgramWithTransactions {
        ProjectId projectId;

        @BeforeEach
        void setUp() {
            final var sponsor = sponsorHelper.create();
            final var sponsorId = SponsorId.of(sponsor.id().value());

            final var program = programHelper.create(sponsorId);
            final var programId = ProgramId.of(program.id().value());

            final var projectLead = caller;
            projectId = projectHelper.create(projectLead).getLeft();
            final var recipient = userAuthHelper.create();
            final var recipientId = GithubUserId.of(recipient.user().getGithubUserId());

            accountingHelper.createSponsorAccount(sponsorId, 2_200, USDC);
            accountingHelper.allocate(sponsorId, programId, 1500, USDC);

            accountingHelper.createSponsorAccount(sponsorId, 12, ETH);
            accountingHelper.allocate(sponsorId, programId, 12, ETH);

            accountingHelper.grant(programId, projectId, 500, USDC);
            accountingHelper.ungrant(projectId, programId, 200, USDC);
            accountingHelper.grant(programId, projectId, 2, ETH);

            rewardHelper.create(projectId, projectLead, recipientId, 200, USDC);
            rewardHelper.create(projectId, projectLead, recipientId, 1, ETH);

            final var cancelledReward = rewardHelper.create(projectId, projectLead, recipientId, 1, ETH);
            rewardHelper.cancel(projectId, projectLead, cancelledReward);

            final var repo1 = githubHelper.createRepo(projectId);
            final var issue1 = githubHelper.createIssue(repo1, userAuthHelper.create());
            githubHelper.assignIssueToContributor(issue1, userAuthHelper.create().user().getGithubUserId());
            githubHelper.assignIssueToContributor(issue1, userAuthHelper.create().user().getGithubUserId());
            githubHelper.createPullRequest(repo1, userAuthHelper.create());

            final var repo2 = githubHelper.createRepo(projectId);
            final var issue2 = githubHelper.createIssue(repo2.getId(), CurrentDateProvider.now(), null, "OPEN", userAuthHelper.create());
            githubHelper.assignIssueToContributor(issue2, userAuthHelper.create().user().getGithubUserId());
            githubHelper.assignIssueToContributor(issue2, userAuthHelper.create().user().getGithubUserId());
            final var issue3 = githubHelper.createIssue(repo2, userAuthHelper.create());
            githubHelper.assignIssueToContributor(issue3, userAuthHelper.create().user().getGithubUserId());

            githubHelper.createPullRequest(repo2, userAuthHelper.create());
        }

        @Test
        void should_get_my_projects() {
            // When
            client.get()
                    .uri(getApiURI(ME_PROJECTS))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.projects.size()").isEqualTo(1)
                    .jsonPath("$.projects[0].leads.size()").isEqualTo(1)
                    .jsonPath("$.projects[0].leads[0].login").isEqualTo(caller.user().getGithubLogin())
                    .json("""
                            {
                              "totalPageNumber": 1,
                              "totalItemNumber": 1,
                              "hasMore": false,
                              "nextPageIndex": 0,
                              "projects": [
                                {
                                  "contributorCount": 7,
                                  "totalAvailable": {
                                    "totalUsdEquivalent": 1882.98,
                                    "totalPerCurrency": [
                                      {
                                        "amount": 1,
                                        "prettyAmount": 1,
                                        "currency": {
                                          "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                          "code": "ETH",
                                          "name": "Ether",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 1781.98,
                                        "usdConversionRate": 1781.983987,
                                        "ratio": 95
                                      },
                                      {
                                        "amount": 100,
                                        "prettyAmount": 100,
                                        "currency": {
                                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                          "code": "USDC",
                                          "name": "USD Coin",
                                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                          "decimals": 6
                                        },
                                        "usdEquivalent": 101.00,
                                        "usdConversionRate": 1.010001,
                                        "ratio": 5
                                      }
                                    ]
                                  },
                                  "totalGranted": {
                                    "totalUsdEquivalent": 3866.97,
                                    "totalPerCurrency": [
                                      {
                                        "amount": 2,
                                        "prettyAmount": 2,
                                        "currency": {
                                          "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                          "code": "ETH",
                                          "name": "Ether",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 3563.97,
                                        "usdConversionRate": 1781.983987,
                                        "ratio": 92
                                      },
                                      {
                                        "amount": 300,
                                        "prettyAmount": 300,
                                        "currency": {
                                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                          "code": "USDC",
                                          "name": "USD Coin",
                                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                          "decimals": 6
                                        },
                                        "usdEquivalent": 303.00,
                                        "usdConversionRate": 1.010001,
                                        "ratio": 8
                                      }
                                    ]
                                  },
                                  "totalRewarded": {
                                    "totalUsdEquivalent": 1983.98,
                                    "totalPerCurrency": [
                                      {
                                        "amount": 1,
                                        "prettyAmount": 1,
                                        "currency": {
                                          "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                          "code": "ETH",
                                          "name": "Ether",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 1781.98,
                                        "usdConversionRate": 1781.983987,
                                        "ratio": 90
                                      },
                                      {
                                        "amount": 200,
                                        "prettyAmount": 200,
                                        "currency": {
                                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                          "code": "USDC",
                                          "name": "USD Coin",
                                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                          "decimals": 6
                                        },
                                        "usdEquivalent": 202.00,
                                        "usdConversionRate": 1.010001,
                                        "ratio": 10
                                      }
                                    ]
                                  }
                                }
                              ]
                            }
                            """);
        }
    }
}
