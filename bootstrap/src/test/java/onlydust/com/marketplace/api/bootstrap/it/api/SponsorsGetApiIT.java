package onlydust.com.marketplace.api.bootstrap.it.api;

import com.google.common.collect.Streams;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.contract.model.SponsorAccountTransactionType;
import onlydust.com.marketplace.api.contract.model.TransactionHistoryPageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;


public class SponsorsGetApiIT extends AbstractMarketplaceApiIT {
    private final static SponsorId sponsorId = SponsorId.of("0980c5ab-befc-4314-acab-777fbf970cbb");
    private UserAuthHelper.AuthenticatedUser user;

    @BeforeEach
    void setup() {
        user = userAuthHelper.authenticateAnthony();
    }

    @Test
    void should_return_forbidden_if_not_admin() {
        removeSponsorFor(user, sponsorId);

        getSponsor(sponsorId)
                .expectStatus()
                .isForbidden();

        getSponsorTransactions(sponsorId, 0, 1)
                .expectStatus()
                .isForbidden();
    }

    @Test
    void should_return_sponsor_by_id() {
        // Given
        addSponsorFor(user, sponsorId);

        // When
        getSponsor(sponsorId)
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                          "name": "Coca Cola",
                          "url": null,
                          "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj",
                          "availableBudgets": [
                            {
                              "amount": 0,
                              "prettyAmount": 0,
                              "currency": {
                                "id": "48388edb-fda2-4a32-b228-28152a147500",
                                "code": "APT",
                                "name": "Aptos Coin",
                                "logoUrl": null,
                                "decimals": 8
                              },
                              "usdEquivalent": 0.00,
                              "usdConversionRate": 0.30
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
                            },
                            {
                              "amount": 0,
                              "prettyAmount": 0,
                              "currency": {
                                "id": "00ca98a5-0197-4b76-a208-4bfc55ea8256",
                                "code": "OP",
                                "name": "Optimism",
                                "logoUrl": null,
                                "decimals": 18
                              },
                              "usdEquivalent": null,
                              "usdConversionRate": null
                            },
                            {
                              "amount": 0,
                              "prettyAmount": 0,
                              "currency": {
                                "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                "code": "USD",
                                "name": "US Dollar",
                                "logoUrl": null,
                                "decimals": 2
                              },
                              "usdEquivalent": 0,
                              "usdConversionRate": 1
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
                              "usdConversionRate": 1.01
                            }
                          ],
                          "projects": [
                            {
                              "id": "7d04163c-4187-4313-8066-61504d34fc56",
                              "slug": "bretzel",
                              "name": "Bretzel",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png",
                              "totalUsdBudget": 5566182.50,
                              "remainingBudgets": [
                                {
                                  "amount": 400000,
                                  "prettyAmount": 400000,
                                  "currency": {
                                    "id": "48388edb-fda2-4a32-b228-28152a147500",
                                    "code": "APT",
                                    "name": "Aptos Coin",
                                    "logoUrl": null,
                                    "decimals": 8
                                  },
                                  "usdEquivalent": 120000.00,
                                  "usdConversionRate": 0.30
                                },
                                {
                                  "amount": 3000,
                                  "prettyAmount": 3000,
                                  "currency": {
                                    "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                    "code": "ETH",
                                    "name": "Ether",
                                    "logoUrl": null,
                                    "decimals": 18
                                  },
                                  "usdEquivalent": 5345940.00,
                                  "usdConversionRate": 1781.98
                                },
                                {
                                  "amount": 17000,
                                  "prettyAmount": 17000,
                                  "currency": {
                                    "id": "00ca98a5-0197-4b76-a208-4bfc55ea8256",
                                    "code": "OP",
                                    "name": "Optimism",
                                    "logoUrl": null,
                                    "decimals": 18
                                  },
                                  "usdEquivalent": null,
                                  "usdConversionRate": null
                                },
                                {
                                  "amount": 99250.00,
                                  "prettyAmount": 99250.00,
                                  "currency": {
                                    "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                    "code": "USDC",
                                    "name": "USD Coin",
                                    "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                    "decimals": 6
                                  },
                                  "usdEquivalent": 100242.50,
                                  "usdConversionRate": 1.01
                                }
                              ]
                            },
                            {
                              "id": "98873240-31df-431a-81dc-7d6fe01143a0",
                              "slug": "aiolia-du-lion",
                              "name": "Aiolia du Lion",
                              "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aiolia_lion.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=11e0e551affa5a88cc8c6de7f352449c",
                              "totalUsdBudget": 20025335.65,
                              "remainingBudgets": [
                                {
                                  "amount": 19827065,
                                  "prettyAmount": 19827065,
                                  "currency": {
                                    "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                    "code": "USDC",
                                    "name": "USD Coin",
                                    "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                    "decimals": 6
                                  },
                                  "usdEquivalent": 20025335.65,
                                  "usdConversionRate": 1.01
                                }
                              ]
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_return_sponsor_transactions() {
        // Given
        addSponsorFor(user, sponsorId);

        // When
        getSponsorTransactions(sponsorId, 0, 6)
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 2,
                          "totalItemNumber": 11,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "transactions": [
                            {
                              "id": "2c8d1803-3e2e-49c9-9e41-30f59ab6b5cd",
                              "date": "2023-02-21T09:15:10.603693Z",
                              "type": "DEPOSIT",
                              "project": null,
                              "amount": {
                                "amount": 3000,
                                "prettyAmount": 3000,
                                "currency": {
                                  "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                  "code": "USD",
                                  "name": "US Dollar",
                                  "logoUrl": null,
                                  "decimals": 2
                                },
                                "usdEquivalent": 3000,
                                "usdConversionRate": 1
                              }
                            },
                            {
                              "id": "8039a520-648b-4eee-84ae-9d46049d56ba",
                              "date": "2023-02-22T12:23:03.403824Z",
                              "type": "DEPOSIT",
                              "project": null,
                              "amount": {
                                "amount": 19933440,
                                "prettyAmount": 19933440,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "usdEquivalent": 20132774.40,
                                "usdConversionRate": 1.01
                              }
                            },
                            {
                              "id": "0efb0d6d-9f23-4692-b723-cba724800b90",
                              "date": "2023-09-28T14:20:35.595046Z",
                              "type": "DEPOSIT",
                              "project": null,
                              "amount": {
                                "amount": 3000,
                                "prettyAmount": 3000,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 5345940.00,
                                "usdConversionRate": 1781.98
                              }
                            },
                            {
                              "id": "f5ee6afd-3e76-4754-a7ca-b30719c3cce8",
                              "date": "2023-09-28T14:33:13.489288Z",
                              "type": "DEPOSIT",
                              "project": null,
                              "amount": {
                                "amount": 17000,
                                "prettyAmount": 17000,
                                "currency": {
                                  "id": "00ca98a5-0197-4b76-a208-4bfc55ea8256",
                                  "code": "OP",
                                  "name": "Optimism",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": null,
                                "usdConversionRate": null
                              }
                            },
                            {
                              "id": "c7f8bc49-7d86-4111-ba41-f8789fcc9354",
                              "date": "2023-09-28T14:34:37.110547Z",
                              "type": "DEPOSIT",
                              "project": null,
                              "amount": {
                                "amount": 400000,
                                "prettyAmount": 400000,
                                "currency": {
                                  "id": "48388edb-fda2-4a32-b228-28152a147500",
                                  "code": "APT",
                                  "name": "Aptos Coin",
                                  "logoUrl": null,
                                  "decimals": 8
                                },
                                "usdEquivalent": 120000.00,
                                "usdConversionRate": 0.30
                              }
                            },
                            {
                              "id": "7ef01cf9-a1c1-45c6-bff6-b9e39a0b47b4",
                              "date": "2024-03-13T14:13:21.135944Z",
                              "type": "ALLOCATION",
                              "project": {
                                "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                "slug": "bretzel",
                                "name": "Bretzel",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                              },
                              "amount": {
                                "amount": 17000,
                                "prettyAmount": 17000,
                                "currency": {
                                  "id": "00ca98a5-0197-4b76-a208-4bfc55ea8256",
                                  "code": "OP",
                                  "name": "Optimism",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": null,
                                "usdConversionRate": null
                              }
                            }
                          ]
                        }
                        """);
    }

    @ParameterizedTest
    @EnumSource(value = SponsorAccountTransactionType.class)
    void should_filter_sponsor_transactions_by_type(@NonNull SponsorAccountTransactionType type) {
        // Given
        addSponsorFor(user, sponsorId);

        // When
        final var transactions = getSponsorTransactions(sponsorId, 0, 3, Map.of("types", type.toString()))
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(TransactionHistoryPageResponse.class)
                .returnResult()
                .getResponseBody().getTransactions();

        assertThat(transactions).allMatch(t -> t.getType() == type);
    }

    @Test
    void should_filter_sponsor_transactions_by_date() {
        // Given
        addSponsorFor(user, sponsorId);

        // When
        getSponsorTransactions(sponsorId, 0, 100, Map.of("fromDate", "2023-09-28", "toDate", "2023-09-28"))
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                // we have at least one correct date
                .jsonPath("$.transactions[?(@.date >= '2023-09-28')]").exists()
                .jsonPath("$.transactions[?(@.date < '2023-09-29')]").exists()
                // we do not have any incorrect date
                .jsonPath("$.transactions[?(@.date < '2023-09-28')]").doesNotExist()
                .jsonPath("$.transactions[?(@.date > '2023-09-29')]").doesNotExist();
    }

    @Test
    void should_filter_sponsor_transactions_by_currencies() {
        // Given
        addSponsorFor(user, sponsorId);

        // When
        getSponsorTransactions(sponsorId, 0, 100, Map.of("currencies", "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c"))
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                // we have at least one correct date
                .jsonPath("$.transactions[?(@.amount.currency.code == 'ETH')]").exists()
                .jsonPath("$.transactions[?(@.amount.currency.code != 'ETH')]").doesNotExist();
    }

    @Test
    void should_filter_sponsor_transactions_by_projects() {
        // Given
        addSponsorFor(user, sponsorId);

        // When
        getSponsorTransactions(sponsorId, 0, 100, Map.of("projects", "7d04163c-4187-4313-8066-61504d34fc56"))
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                // we have at least one correct date
                .jsonPath("$.transactions[?(@.project.name == 'Bretzel')]").exists()
                .jsonPath("$.transactions[?(@.project.name != 'Bretzel')]").doesNotExist();
    }

    @ParameterizedTest
    @CsvSource({
            "DATE, DESC",
            "DATE, ASC",
            "TYPE, DESC",
            "TYPE, ASC",
            "AMOUNT, DESC",
            "AMOUNT, ASC",
            "PROJECT, DESC",
            "PROJECT, ASC"
    })
    void should_order_sponsor_transactions(String sort, String direction) {
        // Given
        addSponsorFor(user, sponsorId);

        // When
        final var transactions = getSponsorTransactions(sponsorId, 0, 100, Map.of("sort", sort, "direction", direction))
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(TransactionHistoryPageResponse.class)
                .returnResult()
                .getResponseBody().getTransactions();

        final var first = direction.equals("ASC") ? transactions.get(0) : transactions.get(transactions.size() - 1);
        final var last = direction.equals("ASC") ? transactions.get(transactions.size() - 1) : transactions.get(0);

        switch (sort) {
            case "DATE":
                assertThat(first.getDate()).isBeforeOrEqualTo(last.getDate());
                break;
            case "TYPE":
                assertThat(first.getType()).isLessThanOrEqualTo(last.getType());
                break;
            case "AMOUNT":
                assertThat(first.getAmount().getCurrency().getCode().compareTo(last.getAmount().getCurrency().getCode())).isLessThanOrEqualTo(0);
                // amounts are ordered within the same currency
                transactions.stream().collect(groupingBy(t -> t.getAmount().getCurrency()))
                        .forEach((currency, currencyTransactions) -> {
                            final var firstAmount = direction.equals("ASC") ? currencyTransactions.get(0).getAmount().getAmount() :
                                    currencyTransactions.get(currencyTransactions.size() - 1).getAmount().getAmount();
                            final var lastAmount = direction.equals("ASC") ? currencyTransactions.get(currencyTransactions.size() - 1).getAmount().getAmount() :
                                    currencyTransactions.get(0).getAmount().getAmount();
                            assertThat(firstAmount).isLessThanOrEqualTo(lastAmount);
                        });
                break;
            case "PROJECT":
                final var transactionsWithProjects = transactions.stream().filter(t -> t.getProject() != null).toList();
                final var firstWithProject = direction.equals("ASC") ? transactionsWithProjects.get(0) :
                        transactionsWithProjects.get(transactionsWithProjects.size() - 1);
                final var lastWithProject = direction.equals("ASC") ? transactionsWithProjects.get(transactionsWithProjects.size() - 1) :
                        transactionsWithProjects.get(0);
                assertThat(firstWithProject.getProject().getName().compareTo(lastWithProject.getProject().getName())).isLessThanOrEqualTo(0);
                break;
        }
    }

    @NonNull
    private WebTestClient.ResponseSpec getSponsor(SponsorId id) {
        return client.get()
                .uri(SPONSOR.formatted(id))
                .header("Authorization", "Bearer " + user.jwt())
                .exchange();
    }

    @NonNull
    private WebTestClient.ResponseSpec getSponsorTransactions(SponsorId id, Integer pageIndex, Integer pageSize) {
        return getSponsorTransactions(id, pageIndex, pageSize, Map.of());
    }

    @NonNull
    private WebTestClient.ResponseSpec getSponsorTransactions(SponsorId id, Integer pageIndex, Integer pageSize, Map<String, String> otherParams) {
        final var params = Streams.concat(
                Map.of("pageIndex", pageIndex.toString(), "pageSize", pageSize.toString()).entrySet().stream(),
                otherParams.entrySet().stream()
        ).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        return client.get()
                .uri(getApiURI(SPONSOR_TRANSACTIONS.formatted(id), params))
                .header("Authorization", "Bearer " + user.jwt())
                .exchange();
    }
}
