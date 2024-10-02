package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.api.contract.model.BiFinancialsStatsListResponse;
import onlydust.com.marketplace.api.contract.model.FinancialTransactionType;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagBI;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.Program;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import onlydust.com.marketplace.project.domain.port.input.ProjectFacadePort;
import org.apache.commons.lang3.mutable.MutableObject;
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

@TagBI
public class ReadBiFinancialStatsApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    ProjectFacadePort projectFacadePort;
    private static Sponsor mySponsor;
    private static Program myProgram;
    private static Project myProject;
    private static final AtomicBoolean setupDone = new AtomicBoolean();
    private static String recipientLogin;

    UserAuthHelper.AuthenticatedUser caller;

    @BeforeEach
    synchronized void setUp() {
        caller = userAuthHelper.authenticateAntho();

        if (setupDone.compareAndExchange(false, true)) return;

        recipientLogin = faker.lordOfTheRings().character();
        mySponsor = sponsorHelper.create(caller);
        myProgram = programHelper.create(mySponsor.id(), caller);
        final var project1Id = projectHelper.create(caller, "p1").getLeft();
        myProject = projectHelper.get(project1Id);
    }

    @Test
    void should_get_sponsor_bi_financial_stats() {
        // When
        client.get()
                .uri(getApiURI(BI_STATS_FINANCIALS, Map.of("sponsorId", mySponsor.id().toString())))
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
    void should_get_program_bi_financial_stats() {
        // When
        client.get()
                .uri(getApiURI(BI_STATS_FINANCIALS, Map.of("programId", myProgram.id().toString())))
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
        private static Program anotherProgram;
        private static ProjectId project2Id;

        private static final AtomicBoolean setupDone = new AtomicBoolean();

        @BeforeEach
        void setUp() {
            if (setupDone.compareAndExchange(false, true)) return;

            project2Id = projectHelper.create(caller, "p2").getLeft();
            projectHelper.addRepo(project2Id, 498695724L);
            anotherProgram = programHelper.create(mySponsor.id());
            final var recipient = userAuthHelper.create(recipientLogin);
            final var recipientId = GithubUserId.of(recipient.user().getGithubUserId());

            at("2023-12-31T00:00:00Z", () -> {
                depositHelper.create(mySponsor.id(), Network.ETHEREUM, USDC, BigDecimal.valueOf(1_000_000));
                depositHelper.create(mySponsor.id(), Network.ETHEREUM, ETH, BigDecimal.valueOf(100));
                depositHelper.create(mySponsor.id(), Network.ETHEREUM, ETH, BigDecimal.valueOf(200), Deposit.Status.REJECTED);
            });

            at("2024-01-01T00:00:00Z", () -> {
                accountingHelper.createSponsorAccount(mySponsor.id(), 2_200, USDC);
                accountingHelper.allocate(mySponsor.id(), myProgram.id(), 2_200, USDC);
            });

            at("2024-01-15T00:00:00Z", () -> {
                accountingHelper.unallocate(myProgram.id(), mySponsor.id(), 700, USDC);
            });

            at("2024-02-01T00:00:00Z", () -> {
                accountingHelper.createSponsorAccount(mySponsor.id(), 12, ETH);
                accountingHelper.allocate(mySponsor.id(), myProgram.id(), 12, ETH);
            });

            at("2024-02-04T00:00:00Z", () -> {
                accountingHelper.createSponsorAccount(mySponsor.id(), 2_000, USDC);
                accountingHelper.allocate(mySponsor.id(), anotherProgram.id(), 1_500, USDC);
            });

            at("2024-03-12T00:00:00Z", () -> {
                accountingHelper.createSponsorAccount(mySponsor.id(), 1, BTC);
                accountingHelper.allocate(mySponsor.id(), anotherProgram.id(), 1, BTC);
            });

            at("2024-04-23T00:00:00Z", () -> {
                accountingHelper.grant(myProgram.id(), myProject.getId(), 500, USDC);
                accountingHelper.grant(myProgram.id(), myProject.getId(), 2, ETH);
            });

            at("2024-04-24T00:00:00Z", () -> accountingHelper.grant(myProgram.id(), project2Id, 200, USDC));

            at("2024-05-23T00:00:00Z", () -> {
                accountingHelper.grant(myProgram.id(), project2Id, 3, ETH);
                accountingHelper.grant(anotherProgram.id(), myProject.getId(), 500, USDC);
                accountingHelper.grant(anotherProgram.id(), myProject.getId(), 1, BTC);
            });

            at("2024-06-23T00:00:00Z", () -> {
                accountingHelper.grant(anotherProgram.id(), project2Id, 400, USDC);
                accountingHelper.ungrant(myProject.getId(), myProgram.id(), 200, USDC);
            });

            final var reward1 = at("2024-07-11T00:00:00Z", () -> rewardHelper.create(myProject.getId(), caller, recipientId, 400, USDC));
            final var reward2 = at("2024-07-11T00:00:00Z", () -> rewardHelper.create(myProject.getId(), caller, recipientId, 1, ETH));
            final var reward3 = at("2024-07-11T00:00:00Z", () -> rewardHelper.create(myProject.getId(), caller, recipientId, 1, BTC));

            final var reward4 = at("2024-08-02T00:00:00Z", () -> rewardHelper.create(project2Id, caller, recipientId, 100, USDC));
            final var reward5 = at("2024-08-02T00:00:00Z", () -> rewardHelper.create(project2Id, caller, recipientId, 2, ETH));

            final var reward6 = at("2024-08-02T00:00:00Z", () -> rewardHelper.create(myProject.getId(), caller, recipientId, 1, ETH));
            at("2024-08-03T00:00:00Z", () -> rewardHelper.cancel(myProject.getId(), caller, reward6));

            at("2024-08-15T00:00:00Z", () -> accountingHelper.pay(reward1, reward2, reward3, reward4, reward5));

            projectFacadePort.refreshStats();
        }

        @Test
        void should_get_sponsor_bi_financial_stats() {
            // When
            client.get()
                    .uri(getApiURI(BI_STATS_FINANCIALS, Map.of("sponsorId", mySponsor.id().toString())))
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
                                  "transactionCount": 0
                                }
                            """))
            ;

            // When
            client.get()
                    .uri(getApiURI(BI_STATS_FINANCIALS, Map.of("sponsorId", mySponsor.id().toString(), "showEmpty", "false")))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.stats[?(@.date == '2024-04-01')]").doesNotExist();
        }

        @Test
        void should_get_sponsor_bi_financial_stats_filtered_by_date() {
            // When
            client.get()
                    .uri(getApiURI(BI_STATS_FINANCIALS, Map.of(
                            "sponsorId", mySponsor.id().toString(),
                            "fromDate", "2024-04-01",
                            "toDate", "2024-06-01")))
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
                    .uri(getApiURI(BI_STATS_FINANCIALS, Map.of(
                            "sponsorId", mySponsor.id().toString(),
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
        void should_get_sponsor_bi_financial_stats_filtered_by_search() {
            final var search = URLEncoder.encode(myProgram.name().split(" ")[0], StandardCharsets.UTF_8);

            // When
            client.get()
                    .uri(getApiURI(BI_STATS_FINANCIALS, Map.of(
                            "sponsorId", mySponsor.id().toString(),
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
        void should_get_sponsor_bi_financial_stats_filtered_by_types(FinancialTransactionType type) {
            // When
            final var stats = client.get()
                    .uri(getApiURI(BI_STATS_FINANCIALS, Map.of(
                            "sponsorId", mySponsor.id().toString(),
                            "types", type.name()
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
        void should_get_program_bi_financial_stats() {
            // When
            client.get()
                    .uri(getApiURI(BI_STATS_FINANCIALS, Map.of(
                            "programId", myProgram.id().toString()
                    )))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody()
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
                                    "totalUsdEquivalent": 21383.81,
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
                                        "ratio": 100
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
                                    "totalUsdEquivalent": 4270.97,
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
                                        "ratio": 83
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
                                        "usdConversionRate": 1.010001,
                                        "ratio": 17
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
                                    "totalUsdEquivalent": 5345.95,
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
                                        "usdEquivalent": 5345.95,
                                        "usdConversionRate": 1781.983987,
                                        "ratio": 100
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
                                        "ratio": 0
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
                                    "totalUsdEquivalent": -202.00,
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
                                        "usdConversionRate": 1781.983987,
                                        "ratio": 0
                                      },
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
                                        "usdConversionRate": 1.010001,
                                        "ratio": 100
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

            client.get()
                    .uri(getApiURI(PROGRAM_STATS_TRANSACTIONS.formatted(myProgram.id()), Map.of("showEmpty", "false")))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.stats[?(@.date == '2024-03-01')]").doesNotExist()
            ;
        }

        @Test
        void should_get_program_bi_financial_stats_filtered_by_date() {
            // When
            client.get()
                    .uri(getApiURI(BI_STATS_FINANCIALS, Map.of(
                            "programId", myProgram.id().toString(),
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

            // When
            client.get()
                    .uri(getApiURI(BI_STATS_FINANCIALS, Map.of(
                            "programId", myProgram.id().toString(),
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
                    .jsonPath("$.stats[0].transactionCount").isEqualTo(1)
                    .jsonPath("$.stats[1].date").isEqualTo("2024-05-01")
                    .jsonPath("$.stats[1].transactionCount").isEqualTo(1)
                    .jsonPath("$.stats[2].date").isEqualTo("2024-04-01")
                    .jsonPath("$.stats[2].transactionCount").isEqualTo(3)
            ;
        }

        @Test
        void should_get_program_bi_financial_stats_filtered_by_search() {
            final var search = myProject.getName();

            // When
            client.get()
                    .uri(getApiURI(BI_STATS_FINANCIALS, Map.of(
                            "programId", myProgram.id().toString(),
                            "search", search
                    )))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.stats[0].date").isEqualTo("2024-04-01")
                    .jsonPath("$.stats[0].transactionCount").isEqualTo(2)
                    .jsonPath("$.stats[1].date").isEqualTo("2024-06-01")
                    .jsonPath("$.stats[1].transactionCount").isEqualTo(1)
            ;
        }

        @ParameterizedTest
        @EnumSource(value = FinancialTransactionType.class, names = {"GRANTED", "UNGRANTED", "ALLOCATED", "UNALLOCATED"})
        void should_get_program_bi_financial_stats_filtered_by_types(FinancialTransactionType type) {
            // When
            final var stats = client.get()
                    .uri(getApiURI(BI_STATS_FINANCIALS, Map.of(
                            "programId", myProgram.id().toString(),
                            "types", type.name()
                    )))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody(BiFinancialsStatsListResponse.class)
                    .returnResult().getResponseBody().getStats();

            switch (type) {
                case GRANTED -> {
                    assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.APRIL).findFirst().orElseThrow().getTransactionCount()).isEqualTo(3);
                    assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.MAY).findFirst().orElseThrow().getTransactionCount()).isEqualTo(1);
                    assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.JUNE).findFirst().orElseThrow().getTransactionCount()).isEqualTo(0);
                }
                case UNGRANTED -> {
                    assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.APRIL).findFirst().orElseThrow().getTransactionCount()).isEqualTo(0);
                    assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.MAY).findFirst().orElseThrow().getTransactionCount()).isEqualTo(0);
                    assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.JUNE).findFirst().orElseThrow().getTransactionCount()).isEqualTo(1);
                }
                case ALLOCATED -> {
                    assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.JANUARY).findFirst().orElseThrow().getTransactionCount()).isEqualTo(1);
                    assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.FEBRUARY).findFirst().orElseThrow().getTransactionCount()).isEqualTo(1);
                    assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.MARCH).findFirst().orElseThrow().getTransactionCount()).isEqualTo(0);
                }
                case UNALLOCATED -> {
                    assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.JANUARY).findFirst().orElseThrow().getTransactionCount()).isEqualTo(1);
                    assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.FEBRUARY).findFirst().orElseThrow().getTransactionCount()).isEqualTo(0);
                    assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.MARCH).findFirst().orElseThrow().getTransactionCount()).isEqualTo(0);
                }
            }
        }

        @Test
        void should_get_project_bi_financial_stats() {
            // Given
            final var responseBody = new MutableObject<String>();

            // When
            client.get()
                    .uri(getApiURI(BI_STATS_FINANCIALS, Map.of(
                            "projectId", myProject.getId().toString()
                    )))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .consumeWith(b -> responseBody.setValue(new String(b.getResponseBody(), StandardCharsets.UTF_8)))
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
                                    "totalUsdEquivalent": 4068.97,
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
                                        "ratio": 88
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
                                        "ratio": 12
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
                                    "totalUsdEquivalent": 505.00,
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
                                  "transactionCount": 2
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
                                    "totalUsdEquivalent": -202.00,
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
                                        "ratio": 0
                                      },
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
                                        "usdConversionRate": 1.010001,
                                        "ratio": 100
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
                                  "transactionCount": 1
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
                                    "totalUsdEquivalent": 2185.98,
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
                                        "ratio": 82
                                      },
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
                                        "usdConversionRate": 1.010001,
                                        "ratio": 18
                                      }
                                    ]
                                  },
                                  "transactionCount": 4
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
                                    "totalUsdEquivalent": 1781.98,
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
                                        "ratio": 100
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
                                        "ratio": 0
                                      }
                                    ]
                                  },
                                  "transactionCount": 1
                                }
                            """))
            ;

            client.get()
                    .uri(getApiURI(BI_STATS_FINANCIALS, Map.of(
                            "projectSlug", myProject.getSlug()
                    )))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .json(responseBody.getValue());

            client.get()
                    .uri(getApiURI(PROGRAM_STATS_TRANSACTIONS.formatted(myProgram.id()), Map.of("showEmpty", "false")))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.stats[?(@.date == '2024-03-01')]").doesNotExist()
            ;
        }

        @Test
        void should_get_project_bi_financial_stats_filtered_by_date() {
            // When
            client.get()
                    .uri(getApiURI(BI_STATS_FINANCIALS, Map.of(
                            "projectId", myProject.getId().toString(),
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
                    .jsonPath("$.stats[0].transactionCount").isEqualTo(2)
                    .jsonPath("$.stats[1].date").isEqualTo("2024-05-01")
                    .jsonPath("$.stats[1].transactionCount").isEqualTo(2)
                    .jsonPath("$.stats[2].date").isEqualTo("2024-06-01")
                    .jsonPath("$.stats[2].transactionCount").isEqualTo(1)
            ;

            // When
            client.get()
                    .uri(getApiURI(BI_STATS_FINANCIALS, Map.of(
                            "projectId", myProject.getId().toString(),
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
                    .jsonPath("$.stats[0].transactionCount").isEqualTo(1)
                    .jsonPath("$.stats[1].date").isEqualTo("2024-05-01")
                    .jsonPath("$.stats[1].transactionCount").isEqualTo(2)
                    .jsonPath("$.stats[2].date").isEqualTo("2024-04-01")
                    .jsonPath("$.stats[2].transactionCount").isEqualTo(2)
            ;
        }

        @Test
        void should_get_project_bi_financial_stats_filtered_by_search() {
            final var search = recipientLogin;

            // When
            client.get()
                    .uri(getApiURI(BI_STATS_FINANCIALS, Map.of(
                            "projectId", myProject.getId().toString(),
                            "search", search
                    )))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.stats[0].date").isEqualTo("2024-07-01")
                    .jsonPath("$.stats[0].transactionCount").isEqualTo(4)
            ;
        }

        @ParameterizedTest
        @EnumSource(value = FinancialTransactionType.class, names = {"GRANTED", "UNGRANTED", "REWARDED"})
        void should_get_project_bi_financial_stats_filtered_by_types(FinancialTransactionType type) {
            // When
            final var stats = client.get()
                    .uri(getApiURI(BI_STATS_FINANCIALS, Map.of(
                            "projectId", myProject.getId().toString(),
                            "types", type.name()
                    )))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody(BiFinancialsStatsListResponse.class)
                    .returnResult().getResponseBody().getStats();

            switch (type) {
                case GRANTED -> {
                    assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.APRIL).findFirst().orElseThrow().getTransactionCount()).isEqualTo(2);
                    assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.MAY).findFirst().orElseThrow().getTransactionCount()).isEqualTo(2);
                    assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.JUNE).findFirst().orElseThrow().getTransactionCount()).isEqualTo(0);
                }
                case UNGRANTED -> {
                    assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.APRIL).findFirst().orElseThrow().getTransactionCount()).isEqualTo(0);
                    assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.MAY).findFirst().orElseThrow().getTransactionCount()).isEqualTo(0);
                    assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.JUNE).findFirst().orElseThrow().getTransactionCount()).isEqualTo(1);
                }
                case REWARDED -> {
                    assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.JUNE).findFirst().orElseThrow().getTransactionCount()).isEqualTo(0);
                    assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.JULY).findFirst().orElseThrow().getTransactionCount()).isEqualTo(4);
                    assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.AUGUST).findFirst().orElseThrow().getTransactionCount()).isEqualTo(1);
                }
            }
        }
    }

    @Test
    void should_be_unauthorized_getting_another_sponsor_bi_financial_stats() {
        // When
        client.get()
                .uri(getApiURI(BI_STATS_FINANCIALS, Map.of("sponsorId", sponsorHelper.create().id().toString())))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void should_be_unauthorized_getting_another_program_bi_financial_stats() {
        // When
        client.get()
                .uri(getApiURI(BI_STATS_FINANCIALS, Map.of(
                        "programId", programHelper.create(sponsorHelper.create().id()).id().toString()
                )))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void should_be_unauthorized_getting_another_project_bi_financial_stats() {
        // When
        client.get()
                .uri(getApiURI(BI_STATS_FINANCIALS, Map.of(
                        "projectId", projectHelper.create(userAuthHelper.create()).getLeft().toString()
                )))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void should_be_unauthorized_getting_another_project_bi_financial_stats_by_slug() {
        // When
        client.get()
                .uri(getApiURI(BI_STATS_FINANCIALS, Map.of(
                        "projectSlug", projectHelper.create(userAuthHelper.create()).getRight()
                )))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void should_be_unauthorized_getting_bi_financial_stats_without_id() {
        // When
        client.get()
                .uri(getApiURI(BI_STATS_FINANCIALS))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isBadRequest();
    }
}
