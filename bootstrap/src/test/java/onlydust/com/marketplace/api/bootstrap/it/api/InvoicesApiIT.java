package onlydust.com.marketplace.api.bootstrap.it.api;

import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.contract.model.MyBillingProfilesResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.VerificationStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.IndividualBillingProfileRepository;
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
    @Autowired
    IndividualBillingProfileRepository individualBillingProfileRepository;

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

        final var kyc = individualBillingProfileRepository.findById(billingProfileId).orElseThrow();
        kyc.setVerificationStatus(VerificationStatusEntity.VERIFIED);
        individualBillingProfileRepository.save(kyc);
    }


    @Test
    @Order(0)
    void list_pending_invoices_before() {
        // When
        client.get()
                .uri(getApiURI(ME_REWARDS_PENDING_INVOICE))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards.size()").isEqualTo(11)
                .jsonPath("$.rewards[?(@.id == '966cd55c-7de8-45c4-8bba-b388c38ca15d')]").exists()
                .jsonPath("$.rewards[?(@.id == '79209029-c488-4284-aa3f-bce8870d3a66')]").exists()
                .jsonPath("$.rewards[?(@.id == 'd22f75ab-d9f5-4dc6-9a85-60dcd7452028')]").exists()
        ;
    }

    @SneakyThrows
    @Test
    @Order(1)
    void preview_and_upload_invoice() {
        // When
        final var invoiceId = new MutableObject<String>();
        client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE_PREVIEW.formatted(billingProfileId), Map.of(
                        "rewardIds", "966cd55c-7de8-45c4-8bba-b388c38ca15d,79209029-c488-4284-aa3f-bce8870d3a66,d22f75ab-d9f5-4dc6-9a85-60dcd7452028"
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
                          "number": "OD-BUISSET-ANTHONY-001",
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
                              "id": "d22f75ab-d9f5-4dc6-9a85-60dcd7452028",
                              "date": "2023-09-20T07:59:16.657487Z",
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
                            "amount": 1784000.00,
                            "currency": "USD"
                          },
                          "taxRate": 0,
                          "totalTax": {
                            "amount": 0.00,
                            "currency": "USD"
                          },
                          "totalAfterTax": {
                            "amount": 1784000.00,
                            "currency": "USD"
                          }
                        }
                        """
                );

        // When
        client.get()
                .uri(getApiURI(ME_REWARDS_PENDING_INVOICE))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards.size()").isEqualTo(11)
                .jsonPath("$.rewards[?(@.id == '966cd55c-7de8-45c4-8bba-b388c38ca15d')]").exists()
                .jsonPath("$.rewards[?(@.id == '79209029-c488-4284-aa3f-bce8870d3a66')]").exists()
                .jsonPath("$.rewards[?(@.id == 'd22f75ab-d9f5-4dc6-9a85-60dcd7452028')]").exists()
        ;

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

        // When
        client.get()
                .uri(getApiURI(ME_REWARDS_PENDING_INVOICE))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards.size()").isEqualTo(8)
                .jsonPath("$.rewards[?(@.id == '966cd55c-7de8-45c4-8bba-b388c38ca15d')]").doesNotExist()
                .jsonPath("$.rewards[?(@.id == '79209029-c488-4284-aa3f-bce8870d3a66')]").doesNotExist()
                .jsonPath("$.rewards[?(@.id == 'd22f75ab-d9f5-4dc6-9a85-60dcd7452028')]").doesNotExist()
        ;
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
                .jsonPath("$.number").isEqualTo("OD-BUISSET-ANTHONY-002");

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
                .jsonPath("$.number").isEqualTo("OD-BUISSET-ANTHONY-002");
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
                              "number": "OD-BUISSET-ANTHONY-001",
                              "totalAfterTax": {
                                "amount": 1784000.00,
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

    @Test
    @Order(100)
    void list_rewards() {
        // When
        client.get()
                .uri(getApiURI(ME_GET_REWARDS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "10"
                )))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
        ;
    }
}
