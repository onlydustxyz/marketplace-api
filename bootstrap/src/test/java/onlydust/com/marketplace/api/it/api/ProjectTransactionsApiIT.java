package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.api.contract.model.ProjectTransactionType;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagAccounting;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
import onlydust.com.marketplace.project.domain.model.Program;
import onlydust.com.marketplace.project.domain.model.Reward;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import onlydust.com.marketplace.project.domain.port.input.ProjectFacadePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static onlydust.com.marketplace.api.helper.CurrencyHelper.ETH;
import static onlydust.com.marketplace.api.helper.CurrencyHelper.USDC;
import static onlydust.com.marketplace.api.helper.DateHelper.at;
import static org.assertj.core.api.Assertions.assertThat;

@TagAccounting
public class ProjectTransactionsApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    ProjectFacadePort projectFacadePort;

    @Autowired
    ImageStoragePort imageStoragePort;

    UserAuthHelper.AuthenticatedUser caller;

    @BeforeEach
    void setUp() {
        caller = userAuthHelper.authenticateAntho();
    }

    @Nested
    class GivenMyProject {
        private static Sponsor sponsor;
        private static Program program;
        private static ProjectId projectId;
        UserAuthHelper.AuthenticatedUser programLead;
        private static final AtomicBoolean setupDone = new AtomicBoolean();

        @BeforeEach
        synchronized void setUp() {
            programLead = userAuthHelper.authenticateOlivier();

            if (setupDone.compareAndExchange(false, true)) return;

            sponsor = sponsorHelper.create(programLead);
            program = programHelper.create(sponsor.id(), programLead);
            projectId = projectHelper.create(caller);
        }

        @Test
        void should_get_project_transactions() {
            // When
            client.get()
                    .uri(getApiURI(PROJECT_TRANSACTIONS.formatted(projectId.toString())))
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
        void should_get_project_transactions_in_csv() {
            // When
            final var csv = client.get()
                    .uri(getApiURI(PROJECT_TRANSACTIONS.formatted(projectId.toString())))
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
            assertThat(lines[0]).isEqualTo("id,timestamp,transaction_type,contributor_id,program_id,amount,currency,usd_amount");
        }

        @Nested
        class GivenSomeTransactions {
            private static UserAuthHelper.AuthenticatedUser contributor1;
            private static UserAuthHelper.AuthenticatedUser contributor2;
            private static Reward reward1;
            private static Reward reward2;
            private static Reward reward3;
            private static final AtomicBoolean setupDone = new AtomicBoolean();

            @BeforeEach
            void setUp() {
                if (setupDone.compareAndExchange(false, true)) return;

                contributor1 = userAuthHelper.create();
                contributor2 = userAuthHelper.create();

                final var anotherProject = projectHelper.create(caller);

                at("2024-01-01T00:00:00Z", () -> {
                    accountingHelper.createSponsorAccount(sponsor.id(), 2_200, USDC);
                    accountingHelper.allocate(sponsor.id(), program.id(), 2_200, USDC);
                });

                at("2024-01-15T00:00:00Z", () -> {
                    accountingHelper.unallocate(program.id(), sponsor.id(), 700, USDC);
                });

                at("2024-02-03T00:00:00Z", () -> {
                    accountingHelper.createSponsorAccount(sponsor.id(), 130, ETH);
                    accountingHelper.allocate(sponsor.id(), program.id(), 130, ETH);
                });

                at("2024-02-03T00:00:00Z", () -> {
                    accountingHelper.createSponsorAccount(sponsor.id(), 2_000, USDC);
                    accountingHelper.allocate(sponsor.id(), program.id(), 1_500, USDC);
                });

                at("2024-03-12T00:00:00Z", () -> {
                    accountingHelper.grant(program.id(), projectId, 500, USDC);
                    accountingHelper.grant(program.id(), projectId, 60, ETH);
                });

                at("2024-04-23T00:00:00Z", () -> {
                    accountingHelper.grant(program.id(), anotherProject, 400, USDC);
                });

                at("2024-04-24T00:00:00Z", () -> accountingHelper.ungrant(projectId, program.id(), 55, USDC));

                at("2024-05-23T00:00:00Z", () -> {
                    final var reward1Id = rewardHelper.create(projectId, caller, GithubUserId.of(contributor1.user().getGithubUserId()), 50, USDC);
                    reward1 = rewardHelper.get(reward1Id);
                });

                at("2024-05-24T00:00:00Z", () -> {
                    final var reward2Id = rewardHelper.create(projectId, caller, GithubUserId.of(contributor2.user().getGithubUserId()), 6, ETH);
                    reward2 = rewardHelper.get(reward2Id);
                });

                at("2024-06-23T00:00:00Z", () -> {
                    final var reward3Id = rewardHelper.create(projectId, caller, GithubUserId.of(contributor2.user().getGithubUserId()), 1, ETH);
                    reward3 = rewardHelper.get(reward3Id);
                });

                at("2024-08-03T00:00:00Z", () -> rewardHelper.cancel(projectId, caller, reward3.id()));

                at("2024-08-15T00:00:00Z", () -> accountingHelper.pay(reward2.id()));

                projectFacadePort.refreshStats();
            }

            @Test
            void should_get_project_transactions() {
                // When
                client.get()
                        .uri(getApiURI(PROJECT_TRANSACTIONS.formatted(projectId), Map.of(
                                "pageIndex", "0",
                                "pageSize", "5"
                        )))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .is2xxSuccessful()
                        .expectBody()
                        .consumeWith(System.out::println)
                        .jsonPath("$.transactions[0].thirdParty.contributor.id").isEqualTo(contributor2.userId().toString())
                        .jsonPath("$.transactions[1].thirdParty.contributor.id").isEqualTo(contributor1.userId().toString())
                        .jsonPath("$.transactions[2].thirdParty.program.id").isEqualTo(program.id().toString())
                        .jsonPath("$.transactions[3].thirdParty.program.id").isEqualTo(program.id().toString())
                        .jsonPath("$.transactions[4].thirdParty.program.id").isEqualTo(program.id().toString())
                        .json("""
                                {
                                  "totalPageNumber": 1,
                                  "totalItemNumber": 5,
                                  "hasMore": false,
                                  "nextPageIndex": 0,
                                  "transactions": [
                                    {
                                      "date": "2024-05-24T00:00:00Z",
                                      "type": "REWARDED",
                                      "thirdParty": {
                                        "program": null
                                      },
                                      "amount": {
                                        "amount": 6,
                                        "prettyAmount": 6,
                                        "currency": {
                                          "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                          "code": "ETH",
                                          "name": "Ether",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 10691.90,
                                        "usdConversionRate": 1781.983987
                                      }
                                    },
                                    {
                                      "date": "2024-05-23T00:00:00Z",
                                      "type": "REWARDED",
                                      "thirdParty": {
                                        "program": null
                                      },
                                      "amount": {
                                        "amount": 50,
                                        "prettyAmount": 50,
                                        "currency": {
                                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                          "code": "USDC",
                                          "name": "USD Coin",
                                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                          "decimals": 6
                                        },
                                        "usdEquivalent": 50.50,
                                        "usdConversionRate": 1.010001
                                      }
                                    },
                                    {
                                      "date": "2024-04-24T00:00:00Z",
                                      "type": "UNGRANTED",
                                      "thirdParty": {
                                        "contributor": null
                                      },
                                      "amount": {
                                        "amount": 55,
                                        "prettyAmount": 55,
                                        "currency": {
                                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                          "code": "USDC",
                                          "name": "USD Coin",
                                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                          "decimals": 6
                                        },
                                        "usdEquivalent": 55.55,
                                        "usdConversionRate": 1.010001
                                      }
                                    },
                                    {
                                      "date": "2024-03-12T00:00:00Z",
                                      "type": "GRANTED",
                                      "thirdParty": {
                                        "contributor": null
                                      },
                                      "amount": {
                                        "amount": 60,
                                        "prettyAmount": 60,
                                        "currency": {
                                          "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                          "code": "ETH",
                                          "name": "Ether",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 106919.04,
                                        "usdConversionRate": 1781.983987
                                      }
                                    },
                                    {
                                      "date": "2024-03-12T00:00:00Z",
                                      "type": "GRANTED",
                                      "thirdParty": {
                                        "contributor": null
                                      },
                                      "amount": {
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
                                        "usdConversionRate": 1.010001
                                      }
                                    }
                                  ]
                                }
                                """);
            }

            @Test
            void should_get_program_transactions_in_csv() {
                // When
                final var csv = client.get()
                        .uri(getApiURI(PROJECT_TRANSACTIONS.formatted(projectId)))
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
                assertThat(lines[0]).isEqualTo("id,timestamp,transaction_type,contributor_id,program_id,amount,currency,usd_amount");
            }

            @Test
            void should_get_program_transactions_filtered_by_date() {
                // When
                client.get()
                        .uri(getApiURI(PROJECT_TRANSACTIONS.formatted(projectId), Map.of(
                                "pageIndex", "0",
                                "pageSize", "5",
                                "fromDate", "2024-04-01",
                                "toDate", "2024-06-01"
                        )))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.transactions.size()").isEqualTo(3)
                        .jsonPath("$.transactions[?(@.date < '2024-04-01')]").doesNotExist()
                        .jsonPath("$.transactions[?(@.date > '2024-06-01')]").doesNotExist();
            }

            @Test
            void should_get_program_transactions_filtered_by_search() {
                // Given
                final var search = contributor1.user().getGithubLogin();

                // When
                client.get()
                        .uri(getApiURI(PROJECT_TRANSACTIONS.formatted(projectId), Map.of(
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
                        .jsonPath("$.transactions.size()").isEqualTo(1)
                        .jsonPath("$.transactions[?(@.thirdParty.contributor.id != '%s')]".formatted(contributor1.userId())).doesNotExist();
            }

            @ParameterizedTest
            @EnumSource(ProjectTransactionType.class)
            void should_get_program_transactions_filtered_by_types(ProjectTransactionType type) {
                // When
                client.get()
                        .uri(getApiURI(PROJECT_TRANSACTIONS.formatted(projectId), Map.of(
                                "types", type.name()
                        )))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .consumeWith(System.out::println)
                        .jsonPath("$.transactions.size()").isEqualTo(switch (type) {
                            case REWARDED -> 2;
                            case GRANTED -> 2;
                            case UNGRANTED -> 1;
                        })
                        .jsonPath("$.transactions[?(@.type != '%s')]".formatted(type.name())).doesNotExist();
            }
        }
    }

    @Nested
    class GivenNotMyProject {
        private static ProjectId projectId;
        private static final AtomicBoolean setupDone = new AtomicBoolean();

        @BeforeEach
        synchronized void setUp() {
            if (setupDone.compareAndExchange(false, true)) return;

            projectId = projectHelper.create(caller);
        }


        @Test
        void should_be_unauthorized_getting_project_transactions() {
            // When
            client.get()
                    .uri(getApiURI(PROJECT_TRANSACTIONS.formatted(projectId)))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.create().jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isUnauthorized();
        }

    }
}
