package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.backoffice.api.contract.model.CurrencyResponse;
import onlydust.com.marketplace.accounting.domain.model.ContributorId;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.springframework.http.MediaType.APPLICATION_JSON;

public class BackOfficeAccountingApiIT extends AbstractMarketplaceBackOfficeApiIT {
    static final SponsorId COCA_COLAX = SponsorId.of("44c6807c-48d1-4987-a0a6-ac63f958bdae");
    static final SponsorId THEODO = SponsorId.of("2639563e-4437-4bde-a4f4-654977c0cb39");

    static final ProjectId BRETZEL = ProjectId.of("7d04163c-4187-4313-8066-61504d34fc56");
    static final ProjectId KAAPER = ProjectId.of("298a547f-ecb6-4ab2-8975-68f4e9bf7b39");

    static final ContributorId ANTHO = ContributorId.of(43467246L);
    static final ContributorId OFUX = ContributorId.of(595505L);

    Currency.Id currency;

    @BeforeEach
    void setup() {
        currency = Currency.Id.of(client
                .post()
                .uri(getApiURI(POST_CURRENCIES))
                .contentType(APPLICATION_JSON)
                .header("Api-Key", apiKey())
                .bodyValue("""
                        {
                            "type": "CRYPTO",
                            "standard": "ERC20",
                            "blockchain": "ETHEREUM",
                            "address": "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CurrencyResponse.class)
                .returnResult()
                .getResponseBody()
                .getId()
        );
    }

    @Test
    void should_allocate_budget_to_project_and_get_refunded_of_unspent_budget() {
        // When
        client.post()
                .uri(getApiURI(POST_SPONSOR_FUNDS.formatted(COCA_COLAX)))
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
                        """.formatted(currency))
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        // When
        client.post()
                .uri(getApiURI(POST_PROJECT_ALLOCATIONS.formatted(BRETZEL)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "sponsorId": "%s",
                            "amount": 90,
                            "currencyId": "%s"
                        }
                        """.formatted(COCA_COLAX, currency))
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();


        // When
        client.post()
                .uri(getApiURI(POST_PROJECT_REFUNDS.formatted(BRETZEL)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "sponsorId": "%s",
                            "amount": 50,
                            "currencyId": "%s"
                        }
                        """.formatted(COCA_COLAX, currency))
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        // When
        client.post()
                .uri(getApiURI(POST_SPONSOR_TRANSACTIONS.formatted(COCA_COLAX)))
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
                        """.formatted(currency))
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}
