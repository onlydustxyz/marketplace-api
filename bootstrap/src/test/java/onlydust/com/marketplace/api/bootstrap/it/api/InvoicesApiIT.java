package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.contract.model.MyBillingProfilesResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

public class InvoicesApiIT extends AbstractMarketplaceApiIT {

    @Test
    void should_upload_and_list_invoices() {
        // Given
        final var antho = userAuthHelper.authenticateAnthony();

        final var myBillingProfiles = client.get()
                .uri(ME_BILLING_PROFILES)
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MyBillingProfilesResponse.class)
                .returnResult()
                .getResponseBody();

        final var billingProfileId = myBillingProfiles.getBillingProfiles().get(0).getId();

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE_PREVIEW.formatted(billingProfileId), Map.of(
                        "pageIndex", "0",
                        "pageSize", "10",
                        "rewardIds", "ee28315c-7a84-4052-9308-c2236eeafda1,79209029-c488-4284-aa3f-bce8870d3a66,966cd55c-7de8-45c4-8bba-b388c38ca15d"
                )))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.createdAt").isNotEmpty()
                .jsonPath("$.dueAt").isNotEmpty()
                .json("""
                        {
                          "id": "OD-BUISSET-ANTHONY-001",
                          "billingProfileType": "INDIVIDUAL",
                          "individualBillingProfile": {
                            "firstName": "Anthony",
                            "lastName": "BUISSET",
                            "address": "771 chemin de la sine, 06140, Vence, France"
                          },
                          "companyBillingProfile": null,
                          "destinationAccounts": {
                            "bankAccount": null,
                            "wallets": [
                              {
                                "address": "abuisset.eth",
                                "network": "ethereum"
                              }
                            ]
                          },
                          "rewards": [
                            {
                              "id": "79209029-c488-4284-aa3f-bce8870d3a66",
                              "date": "2023-06-02T08:49:08.444047Z",
                              "projectName": "kaaper",
                              "amount": {
                                "amount": 1000,
                                "currency": "USDC",
                                "base": {
                                  "amount": 1010.00,
                                  "currency": "USD",
                                  "conversionRate": 1.01
                                }
                              }
                            },
                            {
                              "id": "ee28315c-7a84-4052-9308-c2236eeafda1",
                              "date": "2023-06-22T08:47:12.915468Z",
                              "projectName": "Aldébaran du Taureau",
                              "amount": {
                                "amount": 1750,
                                "currency": "USDC",
                                "base": {
                                  "amount": 1767.50,
                                  "currency": "USD",
                                  "conversionRate": 1.01
                                }
                              }
                            },
                            {
                              "id": "966cd55c-7de8-45c4-8bba-b388c38ca15d",
                              "date": "2023-06-02T08:48:04.697886Z",
                              "projectName": "kaaper",
                              "amount": {
                                "amount": 1000,
                                "currency": "ETH",
                                "base": {
                                  "amount": 1781980.00,
                                  "currency": "USD",
                                  "conversionRate": 1781.98
                                }
                              }
                            }
                          ],
                          "totalBeforeTax": {
                            "amount": 1784757.50,
                            "currency": "USD"
                          },
                          "taxRate": 0,
                          "totalTax": {
                            "amount": 0.00,
                            "currency": "USD"
                          },
                          "totalAfterTax": {
                            "amount": 1784757.50,
                            "currency": "USD"
                          }
                        }
                        """
                );

        // When
        client.post()
                .uri(getApiURI(BILLING_PROFILE_INVOICES.formatted(billingProfileId), Map.of(
                        "filename", "OD-BUISSET-ANTHONY-001.pdf",
                        "totalAfterTax", "1784757.50",
                        "currency", "USD"
                )))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICES.formatted(billingProfileId), Map.of(
                        "pageIndex", "0",
                        "pageSize", "10"
                )))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.invoices.length()").isEqualTo(0)
                .jsonPath("$.hasMore").isEqualTo(false)
                .jsonPath("$.totalPageNumber").isEqualTo(0)
                .jsonPath("$.totalItemNumber").isEqualTo(0)
                .jsonPath("$.nextPageIndex").isEqualTo(0)
        ;
    }
}
