package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.api.contract.model.DetailedTotalMoney;
import onlydust.com.marketplace.api.contract.model.ProgramPageResponse;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagMe;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toSet;
import static onlydust.com.marketplace.api.helper.CurrencyHelper.ETH;
import static onlydust.com.marketplace.api.helper.CurrencyHelper.USDC;
import static org.assertj.core.api.Assertions.assertThat;

@TagMe
public class MeReadProgramsApiIT extends AbstractMarketplaceApiIT {
    UserAuthHelper.AuthenticatedUser caller;

    @BeforeEach
    void setUp() {
        caller = userAuthHelper.create();
    }

    @Nested
    class GivenNoPrograms {
        @Test
        void should_get_my_programs_with_no_result() {
            // When
            client.get()
                    .uri(getApiURI(ME_PROGRAMS))
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
                              "programs": []
                            }
                            """);
        }
    }

    @Nested
    class GivenMyPrograms {
        Set<Sponsor> programs;

        @BeforeEach
        void setUp() {
            programs = IntStream.range(0, 13).mapToObj(i -> programHelper.create(caller)).collect(toSet());
        }

        @Test
        void should_get_my_programs() {
            // When
            final var response = client.get()
                    .uri(getApiURI(ME_PROGRAMS))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                    .expectBody(ProgramPageResponse.class)
                    .consumeWith(System.out::println)
                    .returnResult().getResponseBody();

            assertThat(response).isNotNull();
            assertThat(response.getTotalItemNumber()).isEqualTo(13);
            assertThat(response.getTotalPageNumber()).isEqualTo(3);
            assertThat(response.getHasMore()).isTrue();
            assertThat(response.getNextPageIndex()).isEqualTo(1);
            assertThat(response.getPrograms()).hasSize(5);
            assertThat(response.getPrograms().get(0).getName().compareTo(response.getPrograms().get(4).getName())).isLessThan(0);
            assertThat(response.getPrograms()).allMatch(p -> programs.stream().anyMatch(p1 -> p1.id().equals(p.getId()) && p1.name().equals(p.getName())));
            assertThat(response.getPrograms()).extracting("leads", List.class).allMatch(leads -> leads.size() == 1);
            assertThat(response.getPrograms()).extracting("projectCount", Integer.class).allMatch(count -> count == 0);
            assertThat(response.getPrograms()).extracting("totalAvailable", DetailedTotalMoney.class).allMatch(t -> t.getTotalUsdEquivalent().compareTo(ZERO) == 0);
            assertThat(response.getPrograms()).extracting("totalGranted", DetailedTotalMoney.class).allMatch(t -> t.getTotalUsdEquivalent().compareTo(ZERO) == 0);
            assertThat(response.getPrograms()).extracting("totalRewarded", DetailedTotalMoney.class).allMatch(t -> t.getTotalUsdEquivalent().compareTo(ZERO) == 0);
        }
    }

    @Nested
    class GivenOneProgramWithTransactions {
        Sponsor program;

        @BeforeEach
        void setUp() {
            program = programHelper.create(caller);

            final var projectLead = userAuthHelper.create();
            final var project1Id = projectHelper.create(projectLead);
            final var project2Id = projectHelper.create(projectLead);
            final var project3Id = projectHelper.create(projectLead);
            final var recipient = userAuthHelper.create();
            final var recipientId = GithubUserId.of(recipient.user().getGithubUserId());

            final var accountId = accountingHelper.createSponsorAccount(SponsorId.of(program.id()), 2_200, USDC);
            accountingHelper.increaseAllowance(accountId, -700);
            accountingHelper.createSponsorAccount(SponsorId.of(program.id()), 12, ETH);

            accountingHelper.grant(SponsorId.of(program.id()), project1Id, 500, USDC);
            accountingHelper.refund(project1Id, SponsorId.of(program.id()), 200, USDC);
            accountingHelper.grant(SponsorId.of(program.id()), project1Id, 2, ETH);

            accountingHelper.grant(SponsorId.of(program.id()), project2Id, 200, USDC);
            accountingHelper.grant(SponsorId.of(program.id()), project2Id, 3, ETH);

            accountingHelper.grant(SponsorId.of(program.id()), project3Id, 3, ETH);
            accountingHelper.refund(project3Id, SponsorId.of(program.id()), 3, ETH);

            final var reward1 = rewardHelper.create(project1Id, projectLead, recipientId, 200, USDC);
            final var reward2 = rewardHelper.create(project1Id, projectLead, recipientId, 1, ETH);

            final var reward4 = rewardHelper.create(project2Id, projectLead, recipientId, 100, USDC);
            final var reward5 = rewardHelper.create(project2Id, projectLead, recipientId, 1, ETH);

            final var reward6 = rewardHelper.create(project1Id, projectLead, recipientId, 1, ETH);
            rewardHelper.cancel(project1Id, projectLead, reward6);
        }

        @Test
        void should_get_my_programs() {
            // When
            client.get()
                    .uri(getApiURI(ME_PROGRAMS))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.programs.size()").isEqualTo(1)
                    .jsonPath("$.programs[0].leads.size()").isEqualTo(1)
                    .jsonPath("$.programs[0].leads[0].login").isEqualTo(caller.user().getGithubLogin())
                    .json("""
                            {
                              "totalPageNumber": 1,
                              "totalItemNumber": 1,
                              "hasMore": false,
                              "nextPageIndex": 0,
                              "programs": [
                                {
                                  "projectCount": 2,
                                  "totalAvailable": {
                                    "totalUsdEquivalent": 13483.86,
                                    "totalPerCurrency": [
                                      {
                                        "amount": 7,
                                        "prettyAmount": 7,
                                        "currency": {
                                          "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                          "code": "ETH",
                                          "name": "Ether",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 12473.86,
                                        "usdConversionRate": 1781.98
                                      },
                                      {
                                        "amount": 1000,
                                        "prettyAmount": 1000,
                                        "currency": {
                                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                          "code": "USDC",
                                          "name": "USD Coin",
                                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                          "decimals": 6
                                        },
                                        "usdEquivalent": 1010.00,
                                        "usdConversionRate": 1.01
                                      }
                                    ]
                                  },
                                  "totalGranted": {
                                    "totalUsdEquivalent": 9414.90,
                                    "totalPerCurrency": [
                                      {
                                        "amount": 5,
                                        "prettyAmount": 5,
                                        "currency": {
                                          "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                          "code": "ETH",
                                          "name": "Ether",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 8909.90,
                                        "usdConversionRate": 1781.98
                                      },
                                      {
                                        "amount": 500,
                                        "prettyAmount": 500,
                                        "currency": {
                                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                          "code": "USDC",
                                          "name": "USD Coin",
                                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                          "decimals": 6
                                        },
                                        "usdEquivalent": 505.00,
                                        "usdConversionRate": 1.01
                                      }
                                    ]
                                  },
                                  "totalRewarded": {
                                    "totalUsdEquivalent": 3866.96,
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
                                        "usdEquivalent": 3563.96,
                                        "usdConversionRate": 1781.98
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
                                        "usdConversionRate": 1.01
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
