package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.api.contract.model.DetailedTotalMoney;
import onlydust.com.marketplace.api.contract.model.ProgramTransactionStatListResponse;
import onlydust.com.marketplace.api.contract.model.ProgramTransactionType;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagMe;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpHeaders;

import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static onlydust.com.marketplace.api.helper.CurrencyHelper.*;
import static onlydust.com.marketplace.api.helper.DateHelper.at;
import static org.assertj.core.api.Assertions.assertThat;

@TagMe
public class ProgramsApiIT extends AbstractMarketplaceApiIT {
    UserAuthHelper.AuthenticatedUser caller;

    @BeforeEach
    void setUp() {
        caller = userAuthHelper.create();
    }

    @Nested
    class GivenMyProgram {
        Sponsor program;

        @BeforeEach
        void setUp() {
            program = programHelper.create(caller);
        }

        @Test
        void should_get_program_by_id() {
            // When
            client.get()
                    .uri(getApiURI(PROGRAM_BY_ID.formatted(program.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(program.id().toString())
                    .jsonPath("$.name").isEqualTo(program.name())
                    .jsonPath("$.totalAvailable.totalUsdEquivalent").doesNotExist()
                    .jsonPath("$.totalAvailable.totalPerCurrency").isEmpty()
                    .jsonPath("$.totalGranted.totalUsdEquivalent").doesNotExist()
                    .jsonPath("$.totalGranted.totalPerCurrency").isEmpty()
                    .jsonPath("$.totalRewarded.totalUsdEquivalent").doesNotExist()
                    .jsonPath("$.totalRewarded.totalPerCurrency").isEmpty()
            ;
        }

        @Test
        void should_get_program_monthly_transactions_stats() {
            // When
            client.get()
                    .uri(getApiURI(PROGRAM_STATS_TRANSACTIONS.formatted(program.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .json("""
                            {
                              "stats": []
                            }
                            """);
        }

        @Nested
        class GivenSomeTransactions {
            Project project1;

            @BeforeEach
            void setUp() {
                final var projectLead = userAuthHelper.create();
                final var project1Id = projectHelper.create(projectLead);
                project1 = projectHelper.get(project1Id);
                final var project2Id = projectHelper.create(projectLead);
                final var anotherProgram = programHelper.create();
                final var recipient = userAuthHelper.create();
                final var recipientId = GithubUserId.of(recipient.user().getGithubUserId());

                final var accountId = at("2024-01-01T00:00:00Z",
                        () -> accountingHelper.createSponsorAccount(SponsorId.of(program.id()), 2_200, USDC));
                at("2024-01-15T00:00:00Z", () -> accountingHelper.increaseAllowance(accountId, -700));
                at("2024-02-03T00:00:00Z", () -> accountingHelper.createSponsorAccount(SponsorId.of(program.id()), 12, ETH));

                at("2024-02-03T00:00:00Z", () -> accountingHelper.createSponsorAccount(SponsorId.of(anotherProgram.id()), 2_000, USDC));
                at("2024-03-12T00:00:00Z", () -> accountingHelper.createSponsorAccount(SponsorId.of(anotherProgram.id()), 1, BTC));

                at("2024-04-23T00:00:00Z", () -> accountingHelper.grant(SponsorId.of(program.id()), project1Id, 500, USDC));
                at("2024-04-23T00:00:00Z", () -> accountingHelper.grant(SponsorId.of(program.id()), project1Id, 2, ETH));
                at("2024-04-23T00:00:00Z", () -> accountingHelper.grant(SponsorId.of(program.id()), project2Id, 200, USDC));
                at("2024-05-23T00:00:00Z", () -> accountingHelper.grant(SponsorId.of(program.id()), project2Id, 3, ETH));
                at("2024-05-23T00:00:00Z", () -> accountingHelper.grant(SponsorId.of(anotherProgram.id()), project1Id, 500, USDC));
                at("2024-05-23T00:00:00Z", () -> accountingHelper.grant(SponsorId.of(anotherProgram.id()), project1Id, 1, BTC));
                at("2024-06-23T00:00:00Z", () -> accountingHelper.grant(SponsorId.of(anotherProgram.id()), project2Id, 400, USDC));
                at("2024-06-23T00:00:00Z", () -> accountingHelper.refund(project1Id, SponsorId.of(program.id()), 200, USDC));

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
            void should_get_program_by_id() {
                // When
                client.get()
                        .uri(getApiURI(PROGRAM_BY_ID.formatted(program.id())))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.id").isEqualTo(program.id().toString())
                        .jsonPath("$.name").isEqualTo(program.name())
                        .jsonPath("$.totalAvailable.totalPerCurrency[0].currency.code").isEqualTo("ETH")
                        .jsonPath("$.totalGranted.totalPerCurrency[0].currency.code").isEqualTo("ETH")
                        .jsonPath("$.totalRewarded.totalPerCurrency[0].currency.code").isEqualTo("ETH")
                        .json("""
                                {
                                  "totalAvailable": {
                                    "totalUsdEquivalent": 13483.86,
                                    "totalPerCurrency": [
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
                                      },
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
                                      }
                                    ]
                                  },
                                  "totalGranted": {
                                    "totalUsdEquivalent": 9414.9,
                                    "totalPerCurrency": [
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
                                        "usdEquivalent": 8909.90,
                                        "usdConversionRate": 1781.98
                                      }
                                    ]
                                  },
                                  "totalRewarded": {
                                    "totalUsdEquivalent": 5749.94,
                                    "totalPerCurrency": [
                                      {
                                        "amount": 400,
                                        "prettyAmount": 400,
                                        "currency": {
                                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                          "code": "USDC",
                                          "name": "USD Coin",
                                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                          "decimals": 6
                                        },
                                        "usdEquivalent": 404.00,
                                        "usdConversionRate": 1.01
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
                                        "usdEquivalent": 5345.94,
                                        "usdConversionRate": 1781.98
                                      }
                                    ]
                                  }
                                }
                                """)
                ;
            }

            @Test
            void should_get_program_monthly_transactions_stats() {
                // When
                client.get()
                        .uri(getApiURI(PROGRAM_STATS_TRANSACTIONS.formatted(program.id())))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .json("""
                                {
                                  "stats": [
                                    {
                                      "date": "2024-01-01",
                                      "totalAvailable": {
                                        "totalUsdEquivalent": 1515.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 1500,
                                            "prettyAmount": 1500,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 1515.00,
                                            "usdConversionRate": 1.01
                                          }
                                        ]
                                      },
                                      "totalGranted": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.01
                                          }
                                        ]
                                      },
                                      "totalRewarded": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.01
                                          }
                                        ]
                                      },
                                      "transactionCount": 2
                                    },
                                    {
                                      "date": "2024-02-01",
                                      "totalAvailable": {
                                        "totalUsdEquivalent": 21383.76,
                                        "totalPerCurrency": [
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
                                            "usdEquivalent": 21383.76,
                                            "usdConversionRate": 1781.98
                                          }
                                        ]
                                      },
                                      "totalGranted": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
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
                                            "usdConversionRate": 1781.98
                                          }
                                        ]
                                      },
                                      "totalRewarded": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
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
                                            "usdConversionRate": 1781.98
                                          }
                                        ]
                                      },
                                      "transactionCount": 1
                                    },
                                    {
                                      "date": "2024-04-01",
                                      "totalAvailable": {
                                        "totalUsdEquivalent": 18627.80,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 10,
                                            "prettyAmount": 10,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 17819.80,
                                            "usdConversionRate": 1781.98
                                          },
                                          {
                                            "amount": 800,
                                            "prettyAmount": 800,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 808.00,
                                            "usdConversionRate": 1.01
                                          }
                                        ]
                                      },
                                      "totalGranted": {
                                        "totalUsdEquivalent": 4270.96,
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
                                            "amount": 700,
                                            "prettyAmount": 700,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 707.00,
                                            "usdConversionRate": 1.01
                                          }
                                        ]
                                      },
                                      "totalRewarded": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.01
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
                                            "usdConversionRate": 1781.98
                                          }
                                        ]
                                      },
                                      "transactionCount": 3
                                    },
                                    {
                                      "date": "2024-05-01",
                                      "totalAvailable": {
                                        "totalUsdEquivalent": 12473.86,
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
                                          }
                                        ]
                                      },
                                      "totalGranted": {
                                        "totalUsdEquivalent": 5345.94,
                                        "totalPerCurrency": [
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
                                            "usdEquivalent": 5345.94,
                                            "usdConversionRate": 1781.98
                                          }
                                        ]
                                      },
                                      "totalRewarded": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
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
                                            "usdConversionRate": 1781.98
                                          }
                                        ]
                                      },
                                      "transactionCount": 1
                                    },
                                    {
                                      "date": "2024-06-01",
                                      "totalAvailable": {
                                        "totalUsdEquivalent": 1010.00,
                                        "totalPerCurrency": [
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
                                        "totalUsdEquivalent": -202.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": -200,
                                            "prettyAmount": -200,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": -202.00,
                                            "usdConversionRate": 1.01
                                          }
                                        ]
                                      },
                                      "totalRewarded": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.01
                                          }
                                        ]
                                      },
                                      "transactionCount": 1
                                    },
                                    {
                                      "date": "2024-07-01",
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
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.01
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
                                            "usdConversionRate": 1781.98
                                          }
                                        ]
                                      },
                                      "totalRewarded": {
                                        "totalUsdEquivalent": 2084.98,
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
                                      },
                                      "transactionCount": 2
                                    },
                                    {
                                      "date": "2024-08-01",
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
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.01
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
                                            "usdConversionRate": 1781.98
                                          }
                                        ]
                                      },
                                      "totalRewarded": {
                                        "totalUsdEquivalent": 3664.96,
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
                                            "usdConversionRate": 1.01
                                          }
                                        ]
                                      },
                                      "transactionCount": 4
                                    }
                                  ]
                                }
                                """)
                ;
            }

            @Test
            void should_get_program_monthly_transactions_stats_filtered_by_date() {
                // When
                client.get()
                        .uri(getApiURI(PROGRAM_STATS_TRANSACTIONS.formatted(program.id()), Map.of(
                                "fromDate", "2024-04-01",
                                "toDate", "2024-06-01"
                        )))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.stats.size()").isEqualTo(3)
                        .jsonPath("$.stats[0].date").isEqualTo("2024-04-01")
                        .jsonPath("$.stats[0].transactionCount").isEqualTo(3)
                        .jsonPath("$.stats[1].date").isEqualTo("2024-05-01")
                        .jsonPath("$.stats[1].transactionCount").isEqualTo(1)
                        .jsonPath("$.stats[2].date").isEqualTo("2024-06-01")
                        .jsonPath("$.stats[2].transactionCount").isEqualTo(1)
                ;
            }

            @Test
            void should_get_program_monthly_transactions_stats_filtered_by_search() {
                final var search = project1.getName();

                // When
                client.get()
                        .uri(getApiURI(PROGRAM_STATS_TRANSACTIONS.formatted(program.id()), Map.of(
                                "search", search
                        )))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.stats.size()").isEqualTo(4)
                        .jsonPath("$.stats[0].date").isEqualTo("2024-04-01")
                        .jsonPath("$.stats[0].transactionCount").isEqualTo(2)
                        .jsonPath("$.stats[1].date").isEqualTo("2024-06-01")
                        .jsonPath("$.stats[1].transactionCount").isEqualTo(1)
                        .jsonPath("$.stats[2].date").isEqualTo("2024-07-01")
                        .jsonPath("$.stats[2].transactionCount").isEqualTo(2)
                        .jsonPath("$.stats[3].date").isEqualTo("2024-08-01")
                        .jsonPath("$.stats[3].transactionCount").isEqualTo(2)
                ;
            }

            @ParameterizedTest
            @EnumSource(ProgramTransactionType.class)
            void should_get_program_monthly_transactions_stats_filtered_by_types(ProgramTransactionType type) {
                // When
                final var stats = client.get()
                        .uri(getApiURI(PROGRAM_STATS_TRANSACTIONS.formatted(program.id()), Map.of(
                                "types", type.name()
                        )))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody(ProgramTransactionStatListResponse.class)
                        .returnResult().getResponseBody().getStats();

                switch (type) {
                    case GRANTED -> assertThat(stats)
                            .extracting("totalGranted", DetailedTotalMoney.class)
                            .allMatch(stat -> stat.getTotalUsdEquivalent().compareTo(ZERO) != 0);
                    case RECEIVED -> assertThat(stats)
                            .extracting("totalAvailable", DetailedTotalMoney.class)
                            .allMatch(stat -> stat.getTotalUsdEquivalent().compareTo(ZERO) > 0);
                    case RETURNED -> assertThat(stats)
                            .extracting("totalAvailable", DetailedTotalMoney.class)
                            .allMatch(stat -> stat.getTotalUsdEquivalent().compareTo(ZERO) < 0);
                }
            }
        }
    }

    @Nested
    class GivenNotMyProgram {
        Sponsor program;

        @BeforeEach
        void setUp() {
            program = programHelper.create();
        }

        @Test
        void should_be_unauthorized_getting_program_by_id() {
            // When
            client.get()
                    .uri(getApiURI(PROGRAM_BY_ID.formatted(program.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isUnauthorized();
        }

        @Test
        void should_be_unauthorized_getting_program_transactions_stats() {
            // When
            client.get()
                    .uri(getApiURI(PROGRAM_STATS_TRANSACTIONS.formatted(program.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isUnauthorized();
        }
    }
}
