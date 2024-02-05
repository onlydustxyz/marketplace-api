package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.backoffice.api.contract.model.AccountResponse;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class BackOfficeAccountingApiIT extends AbstractMarketplaceBackOfficeApiIT {
    static final SponsorId COCA_COLAX = SponsorId.of("44c6807c-48d1-4987-a0a6-ac63f958bdae");
    static final SponsorId THEODO = SponsorId.of("2639563e-4437-4bde-a4f4-654977c0cb39");

    static final ProjectId BRETZEL = ProjectId.of("7d04163c-4187-4313-8066-61504d34fc56");
    static final ProjectId KAAPER = ProjectId.of("298a547f-ecb6-4ab2-8975-68f4e9bf7b39");

    static final Currency.Id BTC = Currency.Id.of("3f6e1c98-8659-493a-b941-943a803bd91f");

    @Test
    void should_allocate_budget_to_project_and_get_refunded_of_unspent_budget() {
        // When
        final var response = client.post()
                .uri(getApiURI(POST_SPONSORS_ACCOUNTS.formatted(COCA_COLAX)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "currencyId": "%s",
                            "allowance": 100
                        }
                        """.formatted(BTC))
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AccountResponse.class)
                .returnResult()
                .getResponseBody();

        final var accountId = response.getId();
        assertThat(accountId).isNotNull();
        assertThat(response.getSponsorId()).isEqualTo(COCA_COLAX.value());
        assertThat(response.getCurrencyId()).isEqualTo(BTC.value());
        assertThat(response.getAllowance()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(response.getBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.getLockedUntil()).isNull();
        assertThat(response.getAwaitingPaymentAmount()).isEqualTo(BigDecimal.ZERO);

        // When
        client.post()
                .uri(getApiURI(POST_SPONSOR_ACCOUNTS_RECEIPTS.formatted(accountId)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "receipt": {
                                "reference": "0x01",
                                "amount": 100,
                                "network": "ETHEREUM",
                                "thirdPartyName": "Coca Cola LTD",
                                "thirdPartyAccountNumber": "coca.cola.eth"
                            }
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(100)
                .jsonPath("$.allowance").isEqualTo(100)
                .jsonPath("$.receipts[0].reference").isEqualTo("0x01")
                .jsonPath("$.receipts[0].amount").isEqualTo(100)
                .jsonPath("$.receipts[0].network").isEqualTo("ETHEREUM")
                .jsonPath("$.receipts[0].thirdPartyName").isEqualTo("Coca Cola LTD")
                .jsonPath("$.receipts[0].thirdPartyAccountNumber").isEqualTo("coca.cola.eth");
        ;

        // When
        client.post()
                .uri(getApiURI(POST_PROJECTS_BUDGETS_ALLOCATE.formatted(BRETZEL)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "sponsorAccountId": "%s",
                            "amount": 90
                        }
                        """.formatted(accountId))
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();


        // When
        client.post()
                .uri(getApiURI(POST_PROJECTS_BUDGETS_UNALLOCATE.formatted(BRETZEL)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "sponsorAccountId": "%s",
                            "amount": 50
                        }
                        """.formatted(accountId))
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        // When
        client.post()
                .uri(getApiURI(POST_SPONSOR_ACCOUNTS_RECEIPTS.formatted(accountId)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "receipt": {
                                "reference": "0x02",
                                "amount": -60,
                                "network": "ETHEREUM",
                                "thirdPartyName": "Coca Cola LTD",
                                "thirdPartyAccountNumber": "coca.cola.eth"
                            }
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(40)
                .jsonPath("$.allowance").isEqualTo(60)
                .jsonPath("$.receipts[0].reference").isEqualTo("0x01")
                .jsonPath("$.receipts[0].amount").isEqualTo(100)
                .jsonPath("$.receipts[0].network").isEqualTo("ETHEREUM")
                .jsonPath("$.receipts[0].thirdPartyName").isEqualTo("Coca Cola LTD")
                .jsonPath("$.receipts[0].thirdPartyAccountNumber").isEqualTo("coca.cola.eth")
                .jsonPath("$.receipts[1].reference").isEqualTo("0x02")
                .jsonPath("$.receipts[1].amount").isEqualTo(-60)
                .jsonPath("$.receipts[1].network").isEqualTo("ETHEREUM")
                .jsonPath("$.receipts[1].thirdPartyName").isEqualTo("Coca Cola LTD")
                .jsonPath("$.receipts[1].thirdPartyAccountNumber").isEqualTo("coca.cola.eth")
        ;


        // When
        client.put()
                .uri(getApiURI(POST_SPONSOR_ACCOUNTS_ALLOWANCE.formatted(accountId)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "allowance": -40
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(40)
                .jsonPath("$.allowance").isEqualTo(20)
                .jsonPath("$.awaitingPaymentAmount").isEqualTo(0)
        ;
    }

    @Test
    void should_get_list_of_sponsor_accounts() {
        // Given
        client.post()
                .uri(getApiURI(POST_SPONSORS_ACCOUNTS.formatted(THEODO)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "currencyId": "%s",
                            "allowance": 100,
                             "receipt": {
                                "reference": "0x01",
                                "amount": 100,
                                "network": "ETHEREUM",
                                "thirdPartyName": "Coca Cola LTD",
                                "thirdPartyAccountNumber": "coca.cola.eth"
                            }
                        }
                        """.formatted(BTC))
                // Then
                .exchange()
                .expectStatus()
                .isOk();

        client.post()
                .uri(getApiURI(POST_SPONSORS_ACCOUNTS.formatted(THEODO)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "currencyId": "%s",
                            "allowance": 200
                        }
                        """.formatted(BTC))
                // Then
                .exchange()
                .expectStatus()
                .isOk();

        // When
        client.get()
                .uri(getApiURI(GET_SPONSORS_ACCOUNTS.formatted(THEODO)))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "accounts": [
                            {
                              "sponsorId": "2639563e-4437-4bde-a4f4-654977c0cb39",
                              "currencyId": "3f6e1c98-8659-493a-b941-943a803bd91f",
                              "balance": 100,
                              "allowance": 100,
                              "awaitingPaymentAmount": 0,
                              "lockedUntil": null,
                              "receipts": [
                                {
                                  "reference": "0x01",
                                  "amount": 100,
                                  "network": "ETHEREUM",
                                  "thirdPartyName": "Coca Cola LTD",
                                  "thirdPartyAccountNumber": "coca.cola.eth"
                                }
                              ]
                            },
                            {
                              "sponsorId": "2639563e-4437-4bde-a4f4-654977c0cb39",
                              "currencyId": "3f6e1c98-8659-493a-b941-943a803bd91f",
                              "balance": 0,
                              "allowance": 200,
                              "awaitingPaymentAmount": 0,
                              "lockedUntil": null,
                              "receipts": []
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_delete_transaction_registered_by_mistake() {
        // Given
        final var accountId = client.post()
                .uri(getApiURI(POST_SPONSORS_ACCOUNTS.formatted(COCA_COLAX)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "currencyId": "%s",
                            "allowance": 100,
                            "receipt": {
                                "amount": 100,
                                "network": "ETHEREUM",
                                "reference": "0x01",
                                "thirdPartyName": "Coca Cola LTD",
                                "thirdPartyAccountNumber": "coca.cola.eth"
                            }
                        }
                        """.formatted(BTC))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AccountResponse.class)
                .returnResult().getResponseBody().getId();

        // When
        client.delete()
                .uri(getApiURI(DELETE_SPONSOR_ACCOUNTS_RECEIPTS.formatted(accountId, "0x01")))
                .header("Api-Key", apiKey())
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(0)
                .jsonPath("$.allowance").isEqualTo(100)
                .jsonPath("$.receipts.size()").isEqualTo(0)
        ;
    }
}
