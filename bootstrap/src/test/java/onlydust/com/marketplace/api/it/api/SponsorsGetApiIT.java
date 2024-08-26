package onlydust.com.marketplace.api.it.api;

import com.google.common.collect.Streams;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.api.contract.model.SponsorAccountTransactionType;
import onlydust.com.marketplace.api.contract.model.SponsorTransactionPageResponse;
import onlydust.com.marketplace.api.helper.CurrencyHelper;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.kernel.model.SponsorId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

@TagProject
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SponsorsGetApiIT extends AbstractMarketplaceApiIT {
    private final static UUID sponsorId = UUID.fromString("0980c5ab-befc-4314-acab-777fbf970cbb");
    private UserAuthHelper.AuthenticatedUser user;

    @Autowired
    private AccountingFacadePort accountingFacadePort;

    @BeforeEach
    void setup() {
        user = userAuthHelper.authenticateAntho();
    }

    @Test
    @Order(1)
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
    @Order(2)
    void should_return_sponsor_by_id() {
        // Given
        addSponsorFor(user, sponsorId);

        // create multiple sponsor accounts on the same currency to assert that they are well aggregated in available budgets
        accountingFacadePort.createSponsorAccountWithInitialAllowance(SponsorId.of(sponsorId), CurrencyHelper.USDC, null, PositiveAmount.of(1000L));
        accountingFacadePort.createSponsorAccountWithInitialAllowance(SponsorId.of(sponsorId), CurrencyHelper.USDC, null,
                PositiveAmount.of(BigDecimal.valueOf(8000.123456789D)));

        // When
        getSponsor(sponsorId)
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                          "name": "Coca Cola",
                          "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj",
                          "url": null
                        }
                        """);
    }

    @Test
    @Order(3)
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
                          "totalPageNumber": 3,
                          "totalItemNumber": 18,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "transactions": [
                            {
                              "date": "2023-02-21T09:15:10.603693Z",
                              "type": "DEPOSIT",
                              "program": null,
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
                              "date": "2023-02-21T09:15:11.603693Z",
                              "type": "ALLOCATION",
                              "program": {
                                "name": "Coca Cola",
                                "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                              },
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
                              "date": "2023-02-22T12:23:03.403824Z",
                              "type": "DEPOSIT",
                              "program": null,
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
                                "usdEquivalent": 20132794.33,
                                "usdConversionRate": 1.010001
                              }
                            },
                            {
                              "date": "2023-02-22T12:23:04.403824Z",
                              "type": "ALLOCATION",
                              "program": {
                                "name": "Coca Cola",
                                "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                              },
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
                                "usdEquivalent": 20132794.33,
                                "usdConversionRate": 1.010001
                              }
                            },
                            {
                              "date": "2023-09-28T14:20:35.595046Z",
                              "type": "DEPOSIT",
                              "program": null,
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
                                "usdEquivalent": 5345951.96,
                                "usdConversionRate": 1781.983987
                              }
                            },
                            {
                              "date": "2023-09-28T14:20:36.595046Z",
                              "type": "ALLOCATION",
                              "program": {
                                "name": "Coca Cola",
                                "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                              },
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
                                "usdEquivalent": 5345951.96,
                                "usdConversionRate": 1781.983987
                              }
                            }
                          ]
                        }
                        """);
    }

    @ParameterizedTest
    @EnumSource(value = SponsorAccountTransactionType.class)
    @Order(10)
    void should_filter_sponsor_transactions_by_type(@NonNull SponsorAccountTransactionType type) {
        // Given
        addSponsorFor(user, sponsorId);

        // When
        final var transactions = getSponsorTransactions(sponsorId, 0, 3, Map.of("types", type.toString()))
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(SponsorTransactionPageResponse.class)
                .returnResult()
                .getResponseBody().getTransactions();

        assertThat(transactions).allMatch(t -> t.getType() == type);
    }

    @Test
    @Order(10)
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
    @Order(10)
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

    @ParameterizedTest
    @CsvSource({
            "DATE, DESC",
            "DATE, ASC",
            "TYPE, DESC",
            "TYPE, ASC",
            "AMOUNT, DESC",
            "AMOUNT, ASC",
            "PROGRAM, DESC",
            "PROGRAM, ASC"
    })
    @Order(10)
    void should_order_sponsor_transactions(String sort, String direction) {
        // Given
        addSponsorFor(user, sponsorId);

        // When
        final var transactions = getSponsorTransactions(sponsorId, 0, 100, Map.of("sort", sort, "direction", direction))
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(SponsorTransactionPageResponse.class)
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
            case "PROGRAM":
                final var transactionsWithProjects = transactions.stream().filter(t -> t.getProgram() != null).toList();
                final var firstWithProject = direction.equals("ASC") ? transactionsWithProjects.get(0) :
                        transactionsWithProjects.get(transactionsWithProjects.size() - 1);
                final var lastWithProject = direction.equals("ASC") ? transactionsWithProjects.get(transactionsWithProjects.size() - 1) :
                        transactionsWithProjects.get(0);
                assertThat(firstWithProject.getProgram().getName().compareTo(lastWithProject.getProgram().getName())).isLessThanOrEqualTo(0);
                break;
        }
    }

    @NonNull
    private WebTestClient.ResponseSpec getSponsor(UUID id) {
        return client.get()
                .uri(SPONSOR.formatted(id))
                .header("Authorization", "Bearer " + user.jwt())
                .exchange();
    }

    @NonNull
    private WebTestClient.ResponseSpec getSponsorTransactions(UUID id, Integer pageIndex, Integer pageSize) {
        return getSponsorTransactions(id, pageIndex, pageSize, Map.of());
    }

    @NonNull
    private WebTestClient.ResponseSpec getSponsorTransactions(UUID id, Integer pageIndex, Integer pageSize, Map<String, String> otherParams) {
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
