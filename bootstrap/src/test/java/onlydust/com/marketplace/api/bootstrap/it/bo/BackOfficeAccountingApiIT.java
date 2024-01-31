package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.backoffice.api.contract.model.TransactionResponse;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import org.junit.jupiter.api.Test;

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
        client.post()
                .uri(getApiURI(POST_SPONSORS_BUDGETS_ALLOCATE.formatted(COCA_COLAX)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "amount": 100,
                            "currencyId": "%s"
                        }
                        """.formatted(BTC))
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        // When
        client.post()
                .uri(getApiURI(POST_SPONSORS_ACCOUNTING_TRANSACTIONS.formatted(COCA_COLAX)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "type": "CREDIT",
                            "amount": 100,
                            "currencyId": "%s",
                            "receipt": {
                                "network": "ETHEREUM",
                                "reference": "0x0",
                                "thirdPartyName": "Coca Cola LTD",
                                "thirdPartyAccountNumber": "coca.cola.eth"
                            }
                        }
                        """.formatted(BTC))
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody().consumeWith(System.out::println)
                .jsonPath("$.id").isNotEmpty();

        // When
        client.post()
                .uri(getApiURI(POST_PROJECTS_BUDGETS_ALLOCATE.formatted(BRETZEL)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "sponsorId": "%s",
                            "amount": 90,
                            "currencyId": "%s"
                        }
                        """.formatted(COCA_COLAX, BTC))
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
                            "sponsorId": "%s",
                            "amount": 50,
                            "currencyId": "%s"
                        }
                        """.formatted(COCA_COLAX, BTC))
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        // When
        client.post()
                .uri(getApiURI(POST_SPONSORS_ACCOUNTING_TRANSACTIONS.formatted(COCA_COLAX)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "type": "DEBIT",
                            "amount": 60,
                            "currencyId": "%s",
                            "receipt": {
                                "network": "ETHEREUM",
                                "reference": "0x0",
                                "thirdPartyName": "Coca Cola LTD",
                                "thirdPartyAccountNumber": "coca.cola.eth"
                            }
                        }
                        """.formatted(BTC))
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isNotEmpty();


        // When
        client.post()
                .uri(getApiURI(POST_SPONSORS_BUDGETS_UNALLOCATE.formatted(COCA_COLAX)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "amount": 60,
                            "currencyId": "%s"
                        }
                        """.formatted(BTC))
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();
    }


    @Test
    void should_delete_transaction_registered_by_mistake() {

        // When
        final var transactionId = client.post()
                .uri(getApiURI(POST_SPONSORS_ACCOUNTING_TRANSACTIONS.formatted(COCA_COLAX)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "type": "CREDIT",
                            "amount": 100,
                            "currencyId": "%s",
                            "receipt": {
                                "network": "ETHEREUM",
                                "reference": "0x0",
                                "thirdPartyName": "Coca Cola LTD",
                                "thirdPartyAccountNumber": "coca.cola.eth"
                            }
                        }
                        """.formatted(BTC))
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(TransactionResponse.class)
                .returnResult().getResponseBody().getId();

        // When
        client.delete()
                .uri(getApiURI(DELETE_SPONSORS_ACCOUNTING_TRANSACTIONS.formatted(COCA_COLAX, transactionId)))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}
