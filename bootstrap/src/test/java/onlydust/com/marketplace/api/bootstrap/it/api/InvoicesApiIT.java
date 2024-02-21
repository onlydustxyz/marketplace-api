package onlydust.com.marketplace.api.bootstrap.it.api;

import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.contract.model.MyBillingProfilesResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.VerificationStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.GlobalSettingsRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.IndividualBillingProfileRepository;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.BodyInserters.fromResource;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InvoicesApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    PdfStoragePort pdfStoragePort;
    @Autowired
    IndividualBillingProfileRepository individualBillingProfileRepository;
    @Autowired
    GlobalSettingsRepository globalSettingsRepository;

    UserAuthHelper.AuthenticatedUser antho;
    UserAuthHelper.AuthenticatedUser olivier;
    UUID billingProfileId;

    @BeforeEach
    void setUp() {
        antho = userAuthHelper.authenticateAnthony();
        olivier = userAuthHelper.authenticateOlivier();

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
                .consumeWith(System.out::println)
                .jsonPath("$.rewards.size()").isEqualTo(11)
                .jsonPath("$.rewards[?(@.id == '966cd55c-7de8-45c4-8bba-b388c38ca15d')]").exists()
                .jsonPath("$.rewards[?(@.id == '79209029-c488-4284-aa3f-bce8870d3a66')]").exists()
                .jsonPath("$.rewards[?(@.id == 'd22f75ab-d9f5-4dc6-9a85-60dcd7452028')]").exists()
        ;
    }

    @SneakyThrows
    @Test
    @Order(1)
    void preview_and_upload_external_invoice() {
        // When
        final var invoiceId = new MutableObject<String>();
        client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE_PREVIEW.formatted(billingProfileId), Map.of(
                        "rewardIds", "966cd55c-7de8-45c4-8bba-b388c38ca15d"
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
                         "oldBillingProfileType": "INDIVIDUAL",
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
                           "amount": 1781980.00,
                           "currency": "USD"
                         },
                         "taxRate": 0,
                         "totalTax": {
                           "amount": 0.00,
                           "currency": "USD"
                         },
                         "totalAfterTax": {
                           "amount": 1781980.00,
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
                .jsonPath("$.rewards[?(@.id == '966cd55c-7de8-45c4-8bba-b388c38ca15d')]").exists();

        // When
        when(pdfStoragePort.upload(eq(invoiceId.getValue() + ".pdf"), any())).then(invocation -> {
            final var fileName = invocation.getArgument(0, String.class);
            return new URL("https://s3.storage.com/%s".formatted(fileName));
        });

        // Uploading a generated invoice is forbidden when the mandate has not been accepted
        client.post()
                .uri(getApiURI(BILLING_PROFILE_INVOICE.formatted(billingProfileId, invoiceId.getValue())))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .body(fromResource(new FileSystemResource(getClass().getResource("/invoices/invoice-sample.pdf").getFile())))
                .exchange()
                // Then
                .expectStatus()
                .isForbidden();

        // Uploading an external invoice is allowed when the mandate has not been accepted
        client.post()
                .uri(getApiURI(BILLING_PROFILE_INVOICE.formatted(billingProfileId, invoiceId.getValue()), "fileName", "invoice-sample.pdf"))
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
                .jsonPath("$.rewards.size()").isEqualTo(10)
                .jsonPath("$.rewards[?(@.id == '966cd55c-7de8-45c4-8bba-b388c38ca15d')]").doesNotExist()
        ;

        // When
        final var pdfData = faker.lorem().paragraph().getBytes();
        when(pdfStoragePort.download(eq(invoiceId.getValue() + ".pdf"))).then(invocation -> new ByteArrayInputStream(pdfData));

        final var data = client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE.formatted(billingProfileId, invoiceId.getValue())))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectHeader()
                .contentDisposition(ContentDisposition.attachment().filename("OD-BUISSET-ANTHONY-001.pdf").build())
                .expectBody().returnResult().getResponseBody();

        assertThat(data).isEqualTo(pdfData);
    }

    @SneakyThrows
    @Test
    @Order(2)
    void preview_and_upload_generated_invoice() {
        // When
        final var invoiceId = new MutableObject<String>();
        client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE_PREVIEW.formatted(billingProfileId), Map.of(
                        "rewardIds", "79209029-c488-4284-aa3f-bce8870d3a66,d22f75ab-d9f5-4dc6-9a85-60dcd7452028"
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
                          "number": "OD-BUISSET-ANTHONY-002",
                          "oldBillingProfileType": "INDIVIDUAL",
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
                            }
                          ],
                          "totalBeforeTax": {
                            "amount": 2020.00,
                            "currency": "USD"
                          },
                          "taxRate": 0,
                          "totalTax": {
                            "amount": 0.00,
                            "currency": "USD"
                          },
                          "totalAfterTax": {
                            "amount": 2020.00,
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
                .jsonPath("$.rewards.size()").isEqualTo(10)
                .jsonPath("$.rewards[?(@.id == '79209029-c488-4284-aa3f-bce8870d3a66')]").exists()
                .jsonPath("$.rewards[?(@.id == 'd22f75ab-d9f5-4dc6-9a85-60dcd7452028')]").exists()
        ;

        // When
        when(pdfStoragePort.upload(eq(invoiceId.getValue() + ".pdf"), any())).then(invocation -> {
            final var fileName = invocation.getArgument(0, String.class);
            return new URL("https://s3.storage.com/%s".formatted(fileName));
        });
        // Accept the mandate
        client.put()
                .uri(getApiURI(BILLING_PROFILE_INVOICES_MANDATE.formatted(billingProfileId)))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "hasAcceptedInvoiceMandate": true
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();


        // Uploading an external invoice is forbidden when the mandate has been accepted
        client.post()
                .uri(getApiURI(BILLING_PROFILE_INVOICE.formatted(billingProfileId, invoiceId.getValue()), "fileName", "invoice-sample.pdf"))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .body(fromResource(new FileSystemResource(getClass().getResource("/invoices/invoice-sample.pdf").getFile())))
                .exchange()
                // Then
                .expectStatus()
                .isForbidden();

        // Uploading a generated invoice is forbidden when the mandate has been accepted
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
                .jsonPath("$.rewards[?(@.id == '79209029-c488-4284-aa3f-bce8870d3a66')]").doesNotExist()
                .jsonPath("$.rewards[?(@.id == 'd22f75ab-d9f5-4dc6-9a85-60dcd7452028')]").doesNotExist()
        ;

        // When
        final var pdfData = faker.lorem().paragraph().getBytes();
        when(pdfStoragePort.download(eq(invoiceId.getValue() + ".pdf"))).then(invocation -> new ByteArrayInputStream(pdfData));

        final var data = client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE.formatted(billingProfileId, invoiceId.getValue())))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectHeader()
                .contentDisposition(ContentDisposition.attachment().filename("OD-BUISSET-ANTHONY-002.pdf").build())
                .expectBody().returnResult().getResponseBody();

        assertThat(data).isEqualTo(pdfData);
    }

    @Test
    @Order(3)
    void invoice_name_should_be_incremented_only_when_submitted() {
        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE_PREVIEW.formatted(billingProfileId), Map.of(
                        "rewardIds", "dd7d445f-6915-4955-9bae-078173627b05"
                )))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.number").isEqualTo("OD-BUISSET-ANTHONY-003");

        client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE_PREVIEW.formatted(billingProfileId), Map.of(
                        "rewardIds", "dd7d445f-6915-4955-9bae-078173627b05"
                )))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.number").isEqualTo("OD-BUISSET-ANTHONY-003");
    }

    @Test
    @Order(2)
    void should_prevent_invoice_preview_on_invoiced_rewards() {
        // Given
        final var rewardAlreadyInvoiced = "966cd55c-7de8-45c4-8bba-b388c38ca15d";
        final var otherReward = "dd7d445f-6915-4955-9bae-078173627b05";

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE_PREVIEW.formatted(billingProfileId), Map.of(
                        "rewardIds", "%s,%s".formatted(rewardAlreadyInvoiced, otherReward)
                )))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Some rewards are already invoiced");
    }

    @Test
    @Order(4)
    void should_accept_mandate() {
        // Given
        final var billingProfiles = client.get()
                .uri(ME_BILLING_PROFILES)
                .header("Authorization", BEARER_PREFIX + olivier.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MyBillingProfilesResponse.class)
                .returnResult().getResponseBody();

        assertThat(billingProfiles.getBillingProfiles()).hasSize(1);
        assertThat(billingProfiles.getBillingProfiles().get(0).getInvoiceMandateAccepted()).isFalse();
        final var billingProfileId = billingProfiles.getBillingProfiles().get(0).getId();

        // When
        client.put()
                .uri(getApiURI(BILLING_PROFILE_INVOICES_MANDATE.formatted(billingProfileId)))
                .header("Authorization", BEARER_PREFIX + olivier.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "hasAcceptedInvoiceMandate": true
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        // Then
        client.get()
                .uri(ME_BILLING_PROFILES)
                .header("Authorization", BEARER_PREFIX + olivier.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.billingProfiles.length()").isEqualTo(1)
                .jsonPath("$.billingProfiles[0].id").isNotEmpty()
                .jsonPath("$.billingProfiles[0].invoiceMandateAccepted").isEqualTo(true);

        // When
        final var settings = globalSettingsRepository.get();
        settings.setInvoiceMandateLatestVersionDate(new Date());
        globalSettingsRepository.save(settings);

        // Then
        client.get()
                .uri(ME_BILLING_PROFILES)
                .header("Authorization", BEARER_PREFIX + olivier.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.billingProfiles.length()").isEqualTo(1)
                .jsonPath("$.billingProfiles[0].id").isNotEmpty()
                .jsonPath("$.billingProfiles[0].invoiceMandateAccepted").isEqualTo(false);
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
                                "amount": 1781980.00,
                                "currency": "USD"
                              },
                              "status": "PROCESSING"
                            },
                            {
                              "number": "OD-BUISSET-ANTHONY-002",
                              "totalAfterTax": {
                                "amount": 2020.00,
                                "currency": "USD"
                              },
                              "status": "PROCESSING"
                            }
                          ],
                          "hasMore": false,
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
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
