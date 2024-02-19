package onlydust.com.marketplace.api.bootstrap.it.api;

import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.contract.model.MyBillingProfilesResponse;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;

import java.net.URL;
import java.util.Map;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.BodyInserters.fromResource;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InvoicesApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    PdfStoragePort pdfStoragePort;

    UserAuthHelper.AuthenticatedUser antho;
    UUID billingProfileId;

    @BeforeEach
    void setUp() {
        antho = userAuthHelper.authenticateAnthony();

        billingProfileId = client.get()
                .uri(ME_BILLING_PROFILES)
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MyBillingProfilesResponse.class)
                .returnResult()
                .getResponseBody().getBillingProfiles().get(0).getId();
    }

    @SneakyThrows
    @Test
    @Order(1)
    void preview_and_upload_invoice() {
        // When
        final var invoiceId = new MutableObject<String>();
        client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE_PREVIEW.formatted(billingProfileId), Map.of(
                        "rewardIds", "ee28315c-7a84-4052-9308-c2236eeafda1,79209029-c488-4284-aa3f-bce8870d3a66,966cd55c-7de8-45c4-8bba-b388c38ca15d"
                )))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").value(invoiceId::setValue)
                .jsonPath("$.createdAt").isNotEmpty()
                .jsonPath("$.dueAt").isNotEmpty()
                .json("""
                        {
                          "name": "OD-BUISSET-ANTHONY-001",
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
        when(pdfStoragePort.upload(any(), any())).then(invocation -> {
            final var fileName = invocation.getArgument(0, String.class);
            return new URL("https://s3.storage.com/%s".formatted(fileName));
        });

        client.post()
                .uri(getApiURI(BILLING_PROFILE_INVOICE.formatted(billingProfileId, invoiceId.getValue())))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .body(fromResource(new FileSystemResource(getClass().getResource("/invoices/invoice-sample.pdf").getFile())))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();
    }

    @Test
    @Order(2)
    void invoice_name_should_be_incremented_only_when_submitted() {
        // When
        // TODO: Should reject as same rewards as above and an invoice is already uploaded
        client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE_PREVIEW.formatted(billingProfileId), Map.of(
                        "rewardIds", "ee28315c-7a84-4052-9308-c2236eeafda1,79209029-c488-4284-aa3f-bce8870d3a66,966cd55c-7de8-45c4-8bba-b388c38ca15d"
                )))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("OD-BUISSET-ANTHONY-002");

        client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE_PREVIEW.formatted(billingProfileId), Map.of(
                        "rewardIds", "ee28315c-7a84-4052-9308-c2236eeafda1,79209029-c488-4284-aa3f-bce8870d3a66,966cd55c-7de8-45c4-8bba-b388c38ca15d"
                )))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("OD-BUISSET-ANTHONY-002");
    }

    @Test
    @Order(99)
    void list_invoice() {
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
                .json("""
                        {
                          "invoices": [
                            {
                              "name": "OD-BUISSET-ANTHONY-001",
                              "totalAfterTax": {
                                "amount": 1784757.50,
                                "currency": "USD"
                              },
                              "status": "PROCESSING"
                            }
                          ],
                          "hasMore": false,
                          "totalPageNumber": 1,
                          "totalItemNumber": 1,
                          "nextPageIndex": 0
                        }
                        """)
        ;
    }
}
