package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.accounting.domain.model.ProgramId;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.api.contract.model.DetailedTotalMoney;
import onlydust.com.marketplace.api.contract.model.ProgramPageResponse;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagMe;
import onlydust.com.marketplace.project.domain.model.Program;
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
import static onlydust.com.marketplace.api.helper.CurrencyHelper.*;
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
        Set<Program> programs;

        @BeforeEach
        void setUp() {
            final var sponsor = sponsorHelper.create();
            final var sponsorId = SponsorId.of(sponsor.id().value());

            programs = LongStream.range(1, 14).mapToObj(i -> {
                final var program = programHelper.create(caller);
                final var programId = ProgramId.of(program.id().value());

                accountingHelper.createSponsorAccount(sponsorId, i * 100, USDC);
                accountingHelper.allocate(sponsorId, programId, i * 100, USDC);
                accountingHelper.createSponsorAccount(sponsorId, 3 * i, ETH);
                accountingHelper.allocate(sponsorId, programId, 3 * i, ETH);
                accountingHelper.createSponsorAccount(sponsorId, i, BTC);
                accountingHelper.allocate(sponsorId, programId, i, BTC);
                return program;
            }).collect(toSet());
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
            assertThat(response.getPrograms()).extracting("totalAvailable", DetailedTotalMoney.class).allMatch(t -> t.getTotalUsdEquivalent().compareTo(ZERO) > 0);
            assertThat(response.getPrograms()).extracting("totalGranted", DetailedTotalMoney.class).allMatch(t -> t.getTotalUsdEquivalent().compareTo(ZERO) == 0);
            assertThat(response.getPrograms()).extracting("totalRewarded", DetailedTotalMoney.class).allMatch(t -> t.getTotalUsdEquivalent().compareTo(ZERO) == 0);
        }
    }

    @Nested
    class GivenOneProgramWithTransactions {
        Program program;

        @BeforeEach
        void setUp() {
            final var sponsor = sponsorHelper.create();
            final var sponsorId = SponsorId.of(sponsor.id().value());

            program = programHelper.create(caller);
            final var programId = ProgramId.of(program.id().value());

            final var projectLead = userAuthHelper.create();
            final var project1Id = projectHelper.create(projectLead);
            final var project2Id = projectHelper.create(projectLead);
            final var project3Id = projectHelper.create(projectLead);
            final var recipient = userAuthHelper.create();
            final var recipientId = GithubUserId.of(recipient.user().getGithubUserId());

            accountingHelper.createSponsorAccount(sponsorId, 2_200, USDC);
            accountingHelper.allocate(sponsorId, programId, 1500, USDC);

            accountingHelper.createSponsorAccount(sponsorId, 12, ETH);
            accountingHelper.allocate(sponsorId, programId, 12, ETH);

            accountingHelper.grant(programId, project1Id, 500, USDC);
            accountingHelper.refund(project1Id, programId, 200, USDC);
            accountingHelper.grant(programId, project1Id, 2, ETH);

            accountingHelper.grant(programId, project2Id, 200, USDC);
            accountingHelper.grant(programId, project2Id, 3, ETH);

            accountingHelper.grant(programId, project3Id, 3, ETH);
            accountingHelper.refund(project3Id, programId, 3, ETH);

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
                                    "totalUsdEquivalent": 13483.89,
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
                                        "usdEquivalent": 12473.89,
                                        "usdConversionRate": 1781.983987,
                                        "ratio": 93
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
                                        "usdConversionRate": 1.010001,
                                        "ratio": 7
                                      }
                                    ]
                                  },
                                  "totalGranted": {
                                    "totalUsdEquivalent": 9414.92,
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
                                        "usdEquivalent": 8909.92,
                                        "usdConversionRate": 1781.983987,
                                        "ratio": 95
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
                                        "usdConversionRate": 1.010001,
                                        "ratio": 5
                                      }
                                    ]
                                  },
                                  "totalRewarded": {
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
                                  }
                                }
                              ]
                            }
                            """);
        }
    }
}
