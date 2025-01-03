package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.api.contract.model.BiFinancialsStatsListResponse;
import onlydust.com.marketplace.api.contract.model.FinancialTransactionType;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagAccounting;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.Program;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import onlydust.com.marketplace.project.domain.port.input.ProjectFacadePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Month;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static onlydust.com.marketplace.api.helper.CurrencyHelper.*;
import static onlydust.com.marketplace.api.helper.DateHelper.at;
import static onlydust.com.marketplace.api.helper.JSONPathAssertion.jsonObjectEquals;
import static org.assertj.core.api.Assertions.assertThat;

@TagAccounting
public class SponsorsApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    ProjectFacadePort projectFacadePort;

    UserAuthHelper.AuthenticatedUser caller;

    @BeforeEach
    synchronized void setUp() {
        caller = userAuthHelper.authenticateAntho();
    }

    @Nested
    class GivenMySponsor {
        private static Sponsor sponsor;
        private static final AtomicBoolean setupDone = new AtomicBoolean();

        @BeforeEach
        void setUp() {
            if (setupDone.compareAndExchange(false, true)) return;

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
                    .jsonPath("$.totalAvailable.totalUsdEquivalent").isEqualTo(0)
                    .jsonPath("$.totalAvailable.totalPerCurrency").isEmpty()
                    .jsonPath("$.totalAllocated.totalUsdEquivalent").isEqualTo(0)
                    .jsonPath("$.totalAllocated.totalPerCurrency").isEmpty()
                    .jsonPath("$.totalGranted.totalUsdEquivalent").isEqualTo(0)
                    .jsonPath("$.totalGranted.totalPerCurrency").isEmpty()
                    .jsonPath("$.totalRewarded.totalUsdEquivalent").isEqualTo(0)
                    .jsonPath("$.totalRewarded.totalPerCurrency").isEmpty()
            ;
        }

        @Test
        void should_get_sponsor_monthly_transactions_stats() {
            // When
            client.get()
                    .uri(getApiURI(SPONSOR_STATS_TRANSACTIONS.formatted(sponsor.id())))
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

        @Test
        void should_get_sponsor_programs() {
            // Given
            final var program1 = programHelper.create(sponsor.id());
            final var program2 = programHelper.create(sponsor.id());
            final var program3 = programHelper.create(sponsor.id());
            final var otherSponsor = sponsorHelper.create(caller);
            programHelper.create(otherSponsor.id());

            // When
            client.get()
                    .uri(getApiURI(SPONSOR_PROGRAMS.formatted(sponsor.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.totalItemNumber").isEqualTo(3)
                    .jsonPath("$.programs[?(@.id == '%s')].name".formatted(program1.id())).isEqualTo(program1.name())
                    .jsonPath("$.programs[?(@.id == '%s')].name".formatted(program2.id())).isEqualTo(program2.name())
                    .jsonPath("$.programs[?(@.id == '%s')].name".formatted(program3.id())).isEqualTo(program3.name())
            ;
        }

        @Test
        void should_get_sponsor_transactions() {
            // When
            client.get()
                    .uri(getApiURI(SPONSOR_TRANSACTIONS.formatted(sponsor.id())))
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
                              "transactions": []
                            }
                            """);
        }

        @Test
        void should_get_sponsor_transactions_in_csv() {
            // When
            final var csv = client.get()
                    .uri(getApiURI(SPONSOR_TRANSACTIONS.formatted(sponsor.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .header(HttpHeaders.ACCEPT, "text/csv")
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody(String.class)
                    .returnResult().getResponseBody();

            final var lines = new String(csv).split("\\R");
            assertThat(lines.length).isEqualTo(1);
            assertThat(lines[0]).isEqualTo("id,timestamp,transaction_type,deposit_status,program_id,amount,currency,usd_amount");
        }

        @Nested
        class GivenSomeTransactions {
            private static Program program;
            private static Program anotherProgram;
            private static ProjectId project2Id;

            private static final AtomicBoolean setupDone = new AtomicBoolean();

            @BeforeEach
            void setUp() {
                if (setupDone.compareAndExchange(false, true)) return;

                sponsor = sponsorHelper.create(caller);
                program = programHelper.create(sponsor.id(), "My program " + faker.random().nextLong());
                final var projectLead = userAuthHelper.create();
                final var project1Id = projectHelper.create(projectLead, "p1").getLeft();
                final var project1 = projectHelper.get(project1Id);
                project2Id = projectHelper.create(projectLead, "p2").getLeft();
                projectHelper.addRepo(project2Id, 498695724L);
                anotherProgram = programHelper.create(sponsor.id());
                final var recipient = userAuthHelper.create();
                final var recipientId = GithubUserId.of(recipient.user().getGithubUserId());

                at("2023-12-31T00:00:00Z", () -> {
                    depositHelper.create(sponsor.id(), Network.ETHEREUM, USDC, BigDecimal.valueOf(1_000_000));
                    depositHelper.create(sponsor.id(), Network.ETHEREUM, ETH, BigDecimal.valueOf(100));
                    depositHelper.create(sponsor.id(), Network.ETHEREUM, ETH, BigDecimal.valueOf(200), Deposit.Status.REJECTED);
                });

                at("2024-01-01T00:00:00Z", () -> {
                    accountingHelper.createSponsorAccount(sponsor.id(), 2_200, USDC);
                    accountingHelper.allocate(sponsor.id(), program.id(), 2_200, USDC);
                });

                at("2024-01-15T00:00:00Z", () -> {
                    accountingHelper.unallocate(program.id(), sponsor.id(), 700, USDC);
                });

                at("2024-02-01T00:00:00Z", () -> {
                    accountingHelper.createSponsorAccount(sponsor.id(), 12, ETH);
                    accountingHelper.allocate(sponsor.id(), program.id(), 12, ETH);
                });

                at("2024-02-04T00:00:00Z", () -> {
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
                    accountingHelper.ungrant(project1Id, program.id(), 200, USDC);
                });

                final var reward1 = at("2024-07-11T00:00:00Z", () -> rewardHelper.create(project1Id, projectLead, recipientId, 400, USDC));
                final var reward2 = at("2024-07-11T00:00:00Z", () -> rewardHelper.create(project1Id, projectLead, recipientId, 1, ETH));
                final var reward3 = at("2024-07-11T00:00:00Z", () -> rewardHelper.create(project1Id, projectLead, recipientId, 1, BTC));

                final var reward4 = at("2024-08-02T00:00:00Z", () -> rewardHelper.create(project2Id, projectLead, recipientId, 100, USDC));
                final var reward5 = at("2024-08-02T00:00:00Z", () -> rewardHelper.create(project2Id, projectLead, recipientId, 2, ETH));

                final var reward6 = at("2024-08-02T00:00:00Z", () -> rewardHelper.create(project1Id, projectLead, recipientId, 1, ETH));
                at("2024-08-03T00:00:00Z", () -> rewardHelper.cancel(project1Id, projectLead, reward6));

                at("2024-08-15T00:00:00Z", () -> accountingHelper.pay(reward1, reward2, reward3, reward4, reward5));

                projectFacadePort.refreshStats();
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
                        .jsonPath("$.totalAvailable.totalPerCurrency[0].currency.code").isEqualTo("BTC")
                        .jsonPath("$.totalAllocated.totalPerCurrency[0].currency.code").isEqualTo("BTC")
                        .jsonPath("$.totalGranted.totalPerCurrency[0].currency.code").isEqualTo("BTC")
                        .jsonPath("$.totalRewarded.totalPerCurrency[0].currency.code").isEqualTo("BTC")
                        .json("""
                                {
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
                                  "totalAllocated": {
                                    "totalUsdEquivalent": 24413.81,
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
                                        "ratio": 88
                                      },
                                      {
                                        "amount": 3000,
                                        "prettyAmount": 3000,
                                        "currency": {
                                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                          "code": "USDC",
                                          "name": "USD Coin",
                                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                          "decimals": 6
                                        },
                                        "usdEquivalent": 3030.00,
                                        "usdConversionRate": 1.010001,
                                        "ratio": 12
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

            @Test
            void should_get_sponsor_monthly_transactions_stats() {
                // When
                client.get()
                        .uri(getApiURI(SPONSOR_STATS_TRANSACTIONS.formatted(sponsor.id())))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.stats[?(@.date == '2023-12-01')]").value(jsonObjectEquals("""
                                {
                                      "date": "2023-12-01",
                                      "totalDeposited": {
                                        "totalUsdEquivalent": 1188199.40,
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
                                            "amount": 1000000,
                                            "prettyAmount": 1000000,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 1010001.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": 85
                                          },
                                          {
                                            "amount": 100,
                                            "prettyAmount": 100,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 178198.40,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": 15
                                          }
                                        ]
                                      },
                                      "totalAllocated": {
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalPaid": {
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "transactionCount": 3
                                    }
                                """))
                        .jsonPath("$.stats[?(@.date == '2024-01-01')]").value(jsonObjectEquals("""
                                {
                                      "date": "2024-01-01",
                                      "totalDeposited": {
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalAllocated": {
                                        "totalUsdEquivalent": 1515.00,
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
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalPaid": {
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "transactionCount": 2
                                    }
                                """))
                        .jsonPath("$.stats[?(@.date == '2024-02-01')]").value(jsonObjectEquals("""
                                {
                                      "date": "2024-02-01",
                                      "totalDeposited": {
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalAllocated": {
                                        "totalUsdEquivalent": 22898.81,
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
                                            "ratio": 93
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": 7
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalPaid": {
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "transactionCount": 2
                                    }
                                """))
                        .jsonPath("$.stats[?(@.date == '2024-03-01')]").value(jsonObjectEquals("""
                                {
                                      "date": "2024-03-01",
                                      "totalDeposited": {
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalAllocated": {
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalPaid": {
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "transactionCount": 1
                                    }
                                """))
                        .jsonPath("$.stats[?(@.date == '2024-04-01')]").value(jsonObjectEquals("""
                                {
                                      "date": "2024-04-01",
                                      "totalDeposited": {
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalAllocated": {
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalPaid": {
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "transactionCount": 0
                                    }
                                """))
                        .jsonPath("$.stats[?(@.date == '2024-05-01')]").value(jsonObjectEquals("""
                                {
                                      "date": "2024-05-01",
                                      "totalDeposited": {
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalAllocated": {
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalPaid": {
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "transactionCount": 0
                                    }
                                """))
                        .jsonPath("$.stats[?(@.date == '2024-06-01')]").value(jsonObjectEquals("""
                                {
                                      "date": "2024-06-01",
                                      "totalDeposited": {
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalAllocated": {
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalPaid": {
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "transactionCount": 0
                                    }
                                """))
                        .jsonPath("$.stats[?(@.date == '2024-07-01')]").value(jsonObjectEquals("""
                                {
                                      "date": "2024-07-01",
                                      "totalDeposited": {
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalAllocated": {
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalPaid": {
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "transactionCount": 0
                                    }
                                """))
                        .jsonPath("$.stats[?(@.date == '2024-08-01')]").value(jsonObjectEquals("""
                                {
                                      "date": "2024-08-01",
                                      "totalDeposited": {
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalAllocated": {
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalPaid": {
                                        "totalUsdEquivalent": 0.00,
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
                                            "ratio": null
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "transactionCount": 0
                                    }
                                """))
                ;

                // When
                client.get()
                        .uri(getApiURI(SPONSOR_STATS_TRANSACTIONS.formatted(sponsor.id()), Map.of("showEmpty", "false")))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.stats[?(@.date == '2024-04-01')]").doesNotExist();
            }

            @Test
            void should_get_sponsor_monthly_transactions_stats_filtered_by_date() {
                // When
                client.get()
                        .uri(getApiURI(SPONSOR_STATS_TRANSACTIONS.formatted(sponsor.id()), Map.of(
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
                        .jsonPath("$.stats[0].transactionCount").isEqualTo(0)
                        .jsonPath("$.stats[1].date").isEqualTo("2024-05-01")
                        .jsonPath("$.stats[1].transactionCount").isEqualTo(0)
                        .jsonPath("$.stats[2].date").isEqualTo("2024-06-01")
                        .jsonPath("$.stats[2].transactionCount").isEqualTo(0)
                ;

                // When
                client.get()
                        .uri(getApiURI(SPONSOR_STATS_TRANSACTIONS.formatted(sponsor.id()), Map.of(
                                "fromDate", "2024-04-01",
                                "toDate", "2024-06-01",
                                "sortDirection", "DESC"
                        )))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.stats.size()").isEqualTo(3)
                        .jsonPath("$.stats[0].date").isEqualTo("2024-06-01")
                        .jsonPath("$.stats[0].transactionCount").isEqualTo(0)
                        .jsonPath("$.stats[1].date").isEqualTo("2024-05-01")
                        .jsonPath("$.stats[1].transactionCount").isEqualTo(0)
                        .jsonPath("$.stats[2].date").isEqualTo("2024-04-01")
                        .jsonPath("$.stats[2].transactionCount").isEqualTo(0)
                ;
            }

            @Test
            void should_get_sponsor_monthly_transactions_stats_filtered_by_search() {
                final var search = URLEncoder.encode(program.name().split(" ")[0], StandardCharsets.UTF_8);

                // When
                client.get()
                        .uri(getApiURI(SPONSOR_STATS_TRANSACTIONS.formatted(sponsor.id()), Map.of(
                                "search", search
                        )))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.stats[0].date").isEqualTo("2024-01-01")
                        .jsonPath("$.stats[0].transactionCount").isEqualTo(2)
                        .jsonPath("$.stats[1].date").isEqualTo("2024-02-01")
                        .jsonPath("$.stats[1].transactionCount").isEqualTo(1)
                ;
            }

            @ParameterizedTest
            @EnumSource(value = FinancialTransactionType.class, names = {"DEPOSITED", "ALLOCATED", "UNALLOCATED"})
            void should_get_sponsor_monthly_transactions_stats_filtered_by_types(FinancialTransactionType type) {
                // When
                final var stats = client.get()
                        .uri(getApiURI(SPONSOR_STATS_TRANSACTIONS.formatted(sponsor.id()), Map.of(
                                "types", type.name(),
                                "fromDate", "2023-12-01",
                                "toDate", "2024-12-01"
                        )))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody(BiFinancialsStatsListResponse.class)
                        .returnResult().getResponseBody().getStats();

                switch (type) {
                    case DEPOSITED -> {
                        assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.DECEMBER).findFirst().orElseThrow().getTransactionCount()).isEqualTo(3);
                        assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.JANUARY).findFirst().orElseThrow().getTransactionCount()).isEqualTo(0);
                        assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.FEBRUARY).findFirst().orElseThrow().getTransactionCount()).isEqualTo(0);
                        assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.MARCH).findFirst().orElseThrow().getTransactionCount()).isEqualTo(0);
                    }
                    case ALLOCATED -> {
                        assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.DECEMBER).findFirst().orElseThrow().getTransactionCount()).isEqualTo(0);
                        assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.JANUARY).findFirst().orElseThrow().getTransactionCount()).isEqualTo(1);
                        assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.FEBRUARY).findFirst().orElseThrow().getTransactionCount()).isEqualTo(2);
                        assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.MARCH).findFirst().orElseThrow().getTransactionCount()).isEqualTo(1);
                    }
                    case UNALLOCATED -> {
                        assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.DECEMBER).findFirst().orElseThrow().getTransactionCount()).isEqualTo(0);
                        assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.JANUARY).findFirst().orElseThrow().getTransactionCount()).isEqualTo(1);
                        assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.FEBRUARY).findFirst().orElseThrow().getTransactionCount()).isEqualTo(0);
                        assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.MARCH).findFirst().orElseThrow().getTransactionCount()).isEqualTo(0);
                    }
                }
            }

            @Test
            void should_get_sponsor_programs() {
                // When
                client.get()
                        .uri(getApiURI(SPONSOR_PROGRAMS.formatted(sponsor.id())))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .json("""
                                {
                                  "totalPageNumber": 1,
                                  "totalItemNumber": 2,
                                  "hasMore": false,
                                  "nextPageIndex": 0,
                                  "programs": [
                                    {
                                      "leads": [],
                                      "projectCount": 2,
                                      "userCount": 19,
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
                                      "totalAllocated": {
                                        "totalUsdEquivalent": 22898.81,
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
                                            "usdEquivalent": 21383.81,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": 93
                                          },
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": 7
                                          }
                                        ]
                                      }
                                    },
                                    {
                                      "leads": [],
                                      "projectCount": 2,
                                      "userCount": 19,
                                      "totalAvailable": {
                                        "totalUsdEquivalent": 606.00,
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
                                            "amount": 600,
                                            "prettyAmount": 600,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 606.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": 100
                                          }
                                        ]
                                      },
                                      "totalGranted": {
                                        "totalUsdEquivalent": 909.00,
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
                                            "amount": 900,
                                            "prettyAmount": 900,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 909.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": 100
                                          }
                                        ]
                                      },
                                      "totalAllocated": {
                                        "totalUsdEquivalent": 1515.00,
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
                                            "usdConversionRate": 1.010001,
                                            "ratio": 100
                                          }
                                        ]
                                      }
                                    }
                                  ]
                                }
                                """)
                ;
            }

            @Test
            void should_get_sponsor_transactions() {
                // When
                client.get()
                        .uri(getApiURI(SPONSOR_TRANSACTIONS.formatted(sponsor.id()), Map.of(
                                "pageIndex", "0",
                                "pageSize", "10"
                        )))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .is2xxSuccessful()
                        .expectBody()
                        .jsonPath("$.transactions[0].program.id").isEqualTo(anotherProgram.id().toString())
                        .jsonPath("$.transactions[1].program.id").isEqualTo(anotherProgram.id().toString())
                        .jsonPath("$.transactions[2].program.id").isEqualTo(program.id().toString())
                        .jsonPath("$.transactions[3].program.id").isEqualTo(program.id().toString())
                        .jsonPath("$.transactions[4].program.id").isEqualTo(program.id().toString())
                        .json("""
                                {
                                  "totalPageNumber": 1,
                                  "totalItemNumber": 8,
                                  "hasMore": false,
                                  "nextPageIndex": 0,
                                  "transactions": [
                                    {
                                      "date": "2024-03-12T00:00:00Z",
                                      "type": "ALLOCATED",
                                      "amount": {
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
                                        "usdConversionRate": null
                                      },
                                      "depositStatus": null
                                    },
                                    {
                                      "date": "2024-02-04T00:00:00Z",
                                      "type": "ALLOCATED",
                                      "amount": {
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
                                        "usdConversionRate": 1.010001
                                      },
                                      "depositStatus": null
                                    },
                                    {
                                      "date": "2024-02-01T00:00:00Z",
                                      "type": "ALLOCATED",
                                      "amount": {
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
                                        "usdConversionRate": 1781.983987
                                      },
                                      "depositStatus": null
                                    },
                                    {
                                      "date": "2024-01-15T00:00:00Z",
                                      "type": "UNALLOCATED",
                                      "amount": {
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
                                        "usdConversionRate": 1.010001
                                      },
                                      "depositStatus": null
                                    },
                                    {
                                      "date": "2024-01-01T00:00:00Z",
                                      "type": "ALLOCATED",
                                      "amount": {
                                        "amount": 2200,
                                        "prettyAmount": 2200,
                                        "currency": {
                                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                          "code": "USDC",
                                          "name": "USD Coin",
                                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                          "decimals": 6
                                        },
                                        "usdEquivalent": 2222.00,
                                        "usdConversionRate": 1.010001
                                      },
                                      "depositStatus": null
                                    },
                                    {
                                      "date": "2023-12-31T00:00:00Z",
                                      "type": "DEPOSITED",
                                      "program": null,
                                      "amount": {
                                        "amount": 1000000,
                                        "prettyAmount": 1000000,
                                        "currency": {
                                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                          "code": "USDC",
                                          "name": "USD Coin",
                                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                          "decimals": 6
                                        },
                                        "usdEquivalent": 1010001.00,
                                        "usdConversionRate": 1.010001
                                      },
                                      "depositStatus": "COMPLETED"
                                    },
                                    {
                                      "date": "2023-12-31T00:00:00Z",
                                      "type": "DEPOSITED",
                                      "program": null,
                                      "amount": {
                                        "amount": 100,
                                        "prettyAmount": 100,
                                        "currency": {
                                          "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                          "code": "ETH",
                                          "name": "Ether",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 178198.40,
                                        "usdConversionRate": 1781.983987
                                      },
                                      "depositStatus": "COMPLETED"
                                    },
                                    {
                                      "date": "2023-12-31T00:00:00Z",
                                      "type": "DEPOSITED",
                                      "program": null,
                                      "amount": {
                                        "amount": 200,
                                        "prettyAmount": 200,
                                        "currency": {
                                          "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                          "code": "ETH",
                                          "name": "Ether",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 356396.80,
                                        "usdConversionRate": 1781.983987
                                      },
                                      "depositStatus": "REJECTED"
                                    }
                                  ]
                                }
                                """);
            }

            @Test
            void should_get_sponsor_transactions_filtered_by_date() {
                // When
                client.get()
                        .uri(getApiURI(SPONSOR_TRANSACTIONS.formatted(sponsor.id()), Map.of(
                                "pageIndex", "0",
                                "pageSize", "5",
                                "fromDate", "2024-01-01",
                                "toDate", "2024-01-31"
                        )))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.transactions.size()").isEqualTo(2)
                        .jsonPath("$.transactions[?(@.date < '2024-01-01')]").doesNotExist()
                        .jsonPath("$.transactions[?(@.date > '2024-02-01')]").doesNotExist();
            }

            @Test
            void should_get_sponsor_transactions_filtered_by_search() {
                // Given
                final var search = program.name();

                // When
                client.get()
                        .uri(getApiURI(SPONSOR_TRANSACTIONS.formatted(sponsor.id()), Map.of(
                                "pageIndex", "0",
                                "pageSize", "5",
                                "search", search
                        )))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.transactions.size()").isEqualTo(3)
                        .jsonPath("$.transactions[?(@.program.id != '%s')]".formatted(program.id())).doesNotExist();
            }

            @ParameterizedTest
            @EnumSource(value = FinancialTransactionType.class, names = {"DEPOSITED", "ALLOCATED", "UNALLOCATED"})
            void should_get_sponsor_transactions_filtered_by_types(FinancialTransactionType type) {
                // When
                client.get()
                        .uri(getApiURI(SPONSOR_TRANSACTIONS.formatted(sponsor.id()), Map.of(
                                "types", type.name()
                        )))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.transactions.size()").isEqualTo(switch (type) {
                            case DEPOSITED -> 3;
                            case ALLOCATED -> 4;
                            case UNALLOCATED -> 1;
                            default -> throw new IllegalStateException();
                        })
                        .jsonPath("$.transactions[?(@.type != '%s')]".formatted(type.name())).doesNotExist();
            }

            @Test
            void should_get_sponsor_transactions_in_csv() {
                // When
                final var csv = client.get()
                        .uri(getApiURI(SPONSOR_TRANSACTIONS.formatted(sponsor.id())))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .header(HttpHeaders.ACCEPT, "text/csv")
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody(String.class)
                        .returnResult().getResponseBody();

                final var lines = csv.split("\\R");
                assertThat(lines.length).isEqualTo(9);
                assertThat(lines[0]).isEqualTo("id,timestamp,transaction_type,deposit_status,program_id,amount,currency,usd_amount");
            }
        }
    }

    @Nested
    class GivenNotMySponsor {
        private static Sponsor sponsor;
        private static final AtomicBoolean setupDone = new AtomicBoolean();

        @BeforeEach
        void setUp() {
            if (setupDone.compareAndExchange(false, true)) return;

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
                    .isForbidden();
        }

        @Test
        void should_be_unauthorized_getting_sponsor_transactions_stats() {
            // When
            client.get()
                    .uri(getApiURI(SPONSOR_STATS_TRANSACTIONS.formatted(sponsor.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isForbidden();
        }

        @Test
        void should_be_unauthorized_getting_sponsor_transactions() {
            // When
            client.get()
                    .uri(getApiURI(SPONSOR_TRANSACTIONS.formatted(sponsor.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isForbidden();
        }
    }
}
