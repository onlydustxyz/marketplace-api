package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagAccounting;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static onlydust.com.marketplace.api.helper.CurrencyHelper.*;
import static onlydust.com.marketplace.api.helper.DateHelper.at;

@TagAccounting
public class SponsorsApiIT extends AbstractMarketplaceApiIT {
    UserAuthHelper.AuthenticatedUser caller;

    @BeforeEach
    void setUp() {
        caller = userAuthHelper.create();
    }

    @Nested
    class GivenMySponsor {
        Sponsor sponsor;

        @BeforeEach
        void setUp() {
            sponsor = sponsorHelper.create(caller);
        }

        @Test
        void should_get_sponsor_by_id() {
            // When
            client.get()
                    .uri(getApiURI(SPONSOR.formatted(sponsor.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(sponsor.id().toString())
                    .jsonPath("$.name").isEqualTo(sponsor.name())
                    .jsonPath("$.totalDeposited.totalUsdEquivalent").isEqualTo(0)
                    .jsonPath("$.totalDeposited.totalPerCurrency").isEmpty()
                    .jsonPath("$.totalAvailable.totalUsdEquivalent").isEqualTo(0)
                    .jsonPath("$.totalAvailable.totalPerCurrency").isEmpty()
                    .jsonPath("$.totalGranted.totalUsdEquivalent").isEqualTo(0)
                    .jsonPath("$.totalGranted.totalPerCurrency").isEmpty()
                    .jsonPath("$.totalRewarded.totalUsdEquivalent").isEqualTo(0)
                    .jsonPath("$.totalRewarded.totalPerCurrency").isEmpty()
            ;
        }

        @Nested
        class GivenSomeTransactions {
            Project project1;
            ProjectId project2Id;

            @BeforeEach
            void setUp() {
                final var program = programHelper.create();
                final var projectLead = userAuthHelper.create();
                final var project1Id = projectHelper.create(projectLead, "p1");
                project1 = projectHelper.get(project1Id);
                project2Id = projectHelper.create(projectLead, "p2");
                final var anotherProgram = programHelper.create();
                final var recipient = userAuthHelper.create();
                final var recipientId = GithubUserId.of(recipient.user().getGithubUserId());

                at("2024-01-01T00:00:00Z", () -> {
                    accountingHelper.createSponsorAccount(sponsor.id(), 2_200, USDC);
                    accountingHelper.allocate(sponsor.id(), program.id(), 2_200, USDC);
                });

                at("2024-01-15T00:00:00Z", () -> {
                    accountingHelper.unallocate(program.id(), sponsor.id(), 700, USDC);
                });

                at("2024-02-03T00:00:00Z", () -> {
                    accountingHelper.createSponsorAccount(sponsor.id(), 12, ETH);
                    accountingHelper.allocate(sponsor.id(), program.id(), 12, ETH);
                });

                at("2024-02-03T00:00:00Z", () -> {
                    accountingHelper.createSponsorAccount(sponsor.id(), 2_000, USDC);
                    accountingHelper.allocate(sponsor.id(), anotherProgram.id(), 1_500, USDC);
                });

                at("2024-03-12T00:00:00Z", () -> {
                    accountingHelper.createSponsorAccount(sponsor.id(), 1, BTC);
                    accountingHelper.allocate(sponsor.id(), anotherProgram.id(), 1, BTC);
                });

                at("2024-04-23T00:00:00Z", () -> {
                    accountingHelper.grant(program.id(), project1Id, 500, USDC);
                    accountingHelper.grant(program.id(), project1Id, 2, ETH);
                });

                at("2024-04-24T00:00:00Z", () -> accountingHelper.grant(program.id(), project2Id, 200, USDC));

                at("2024-05-23T00:00:00Z", () -> {
                    accountingHelper.grant(program.id(), project2Id, 3, ETH);
                    accountingHelper.grant(anotherProgram.id(), project1Id, 500, USDC);
                    accountingHelper.grant(anotherProgram.id(), project1Id, 1, BTC);
                });

                at("2024-06-23T00:00:00Z", () -> {
                    accountingHelper.grant(anotherProgram.id(), project2Id, 400, USDC);
                    accountingHelper.refund(project1Id, program.id(), 200, USDC);
                });

                final var reward1 = at("2024-07-11T00:00:00Z", () -> rewardHelper.create(project1Id, projectLead, recipientId, 400, USDC));
                final var reward2 = at("2024-07-11T00:00:00Z", () -> rewardHelper.create(project1Id, projectLead, recipientId, 1, ETH));
                final var reward3 = at("2024-07-11T00:00:00Z", () -> rewardHelper.create(project1Id, projectLead, recipientId, 1, BTC));

                final var reward4 = at("2024-08-02T00:00:00Z", () -> rewardHelper.create(project2Id, projectLead, recipientId, 100, USDC));
                final var reward5 = at("2024-08-02T00:00:00Z", () -> rewardHelper.create(project2Id, projectLead, recipientId, 2, ETH));

                final var reward6 = at("2024-08-02T00:00:00Z", () -> rewardHelper.create(project1Id, projectLead, recipientId, 1, ETH));
                at("2024-08-03T00:00:00Z", () -> rewardHelper.cancel(project1Id, projectLead, reward6));

                at("2024-08-15T00:00:00Z", () -> accountingHelper.pay(reward1, reward2, reward3, reward4, reward5));
            }

            @Test
            void should_get_sponsor_by_id() {
                // When
                client.get()
                        .uri(getApiURI(SPONSOR.formatted(sponsor.id())))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.id").isEqualTo(sponsor.id().toString())
                        .jsonPath("$.name").isEqualTo(sponsor.name())
                        .jsonPath("$.logoUrl").isEqualTo(sponsor.logoUrl().toString())
                        .jsonPath("$.url").isEqualTo(sponsor.url().toString())
                        .jsonPath("$.totalDeposited.totalPerCurrency[0].currency.code").isEqualTo("BTC")
                        .jsonPath("$.totalAvailable.totalPerCurrency[0].currency.code").isEqualTo("BTC")
                        .jsonPath("$.totalGranted.totalPerCurrency[0].currency.code").isEqualTo("BTC")
                        .jsonPath("$.totalRewarded.totalPerCurrency[0].currency.code").isEqualTo("BTC")
                        .json("""
                                {
                                  "totalDeposited": {
                                    "totalUsdEquivalent": 25625.81,
                                    "totalPerCurrency": [
                                      {
                                        "amount": 1,
                                        "prettyAmount": 1,
                                        "currency": {
                                          "id": "3f6e1c98-8659-493a-b941-943a803bd91f",
                                          "code": "BTC",
                                          "name": "Bitcoin",
                                          "logoUrl": null,
                                          "decimals": 8
                                        },
                                        "usdEquivalent": null,
                                        "usdConversionRate": null,
                                        "ratio": null
                                      },
                                      {
                                        "amount": 12,
                                        "prettyAmount": 12,
                                        "currency": {
                                          "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                          "code": "ETH",
                                          "name": "Ether",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 21383.81,
                                        "usdConversionRate": 1781.983987,
                                        "ratio": 83
                                      },
                                      {
                                        "amount": 4200,
                                        "prettyAmount": 4200,
                                        "currency": {
                                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                          "code": "USDC",
                                          "name": "USD Coin",
                                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                          "decimals": 6
                                        },
                                        "usdEquivalent": 4242.00,
                                        "usdConversionRate": 1.010001,
                                        "ratio": 17
                                      }
                                    ]
                                  },
                                  "totalAvailable": {
                                    "totalUsdEquivalent": 1212.00,
                                    "totalPerCurrency": [
                                      {
                                        "amount": 0,
                                        "prettyAmount": 0,
                                        "currency": {
                                          "id": "3f6e1c98-8659-493a-b941-943a803bd91f",
                                          "code": "BTC",
                                          "name": "Bitcoin",
                                          "logoUrl": null,
                                          "decimals": 8
                                        },
                                        "usdEquivalent": null,
                                        "usdConversionRate": null,
                                        "ratio": null
                                      },
                                      {
                                        "amount": 1200,
                                        "prettyAmount": 1200,
                                        "currency": {
                                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                          "code": "USDC",
                                          "name": "USD Coin",
                                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                          "decimals": 6
                                        },
                                        "usdEquivalent": 1212.00,
                                        "usdConversionRate": 1.010001,
                                        "ratio": 100
                                      },
                                      {
                                        "amount": 0,
                                        "prettyAmount": 0,
                                        "currency": {
                                          "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                          "code": "ETH",
                                          "name": "Ether",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 0.00,
                                        "usdConversionRate": 1781.983987,
                                        "ratio": 0
                                      }
                                    ]
                                  },
                                  "totalGranted": {
                                    "totalUsdEquivalent": 10323.92,
                                    "totalPerCurrency": [
                                      {
                                        "amount": 1,
                                        "prettyAmount": 1,
                                        "currency": {
                                          "id": "3f6e1c98-8659-493a-b941-943a803bd91f",
                                          "code": "BTC",
                                          "name": "Bitcoin",
                                          "logoUrl": null,
                                          "decimals": 8
                                        },
                                        "usdEquivalent": null,
                                        "usdConversionRate": null,
                                        "ratio": null
                                      },
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
                                        "ratio": 86
                                      },
                                      {
                                        "amount": 1400,
                                        "prettyAmount": 1400,
                                        "currency": {
                                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                          "code": "USDC",
                                          "name": "USD Coin",
                                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                          "decimals": 6
                                        },
                                        "usdEquivalent": 1414.00,
                                        "usdConversionRate": 1.010001,
                                        "ratio": 14
                                      }
                                    ]
                                  },
                                  "totalRewarded": {
                                    "totalUsdEquivalent": 5850.95,
                                    "totalPerCurrency": [
                                      {
                                        "amount": 1,
                                        "prettyAmount": 1,
                                        "currency": {
                                          "id": "3f6e1c98-8659-493a-b941-943a803bd91f",
                                          "code": "BTC",
                                          "name": "Bitcoin",
                                          "logoUrl": null,
                                          "decimals": 8
                                        },
                                        "usdEquivalent": null,
                                        "usdConversionRate": null,
                                        "ratio": null
                                      },
                                      {
                                        "amount": 3,
                                        "prettyAmount": 3,
                                        "currency": {
                                          "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                          "code": "ETH",
                                          "name": "Ether",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 5345.95,
                                        "usdConversionRate": 1781.983987,
                                        "ratio": 91
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
                                        "ratio": 9
                                      }
                                    ]
                                  }
                                }
                                """)
                ;
            }
        }
    }

    @Nested
    class GivenNotMySponsor {
        Sponsor sponsor;

        @BeforeEach
        void setUp() {
            sponsor = sponsorHelper.create();
        }

        @Test
        void should_be_unauthorized_getting_sponsor_by_id() {
            // When
            client.get()
                    .uri(getApiURI(SPONSOR.formatted(sponsor.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isUnauthorized();
        }
    }
}
