package onlydust.com.marketplace.api.bootstrap.it.api;

import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.contract.model.BillingProfileInvoicesPageResponse;
import onlydust.com.marketplace.api.contract.model.MyBillingProfilesResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.OldVerificationStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CompanyBillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.GlobalSettingsRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.IndividualBillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.InvoiceRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRepository;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;

import javax.persistence.EntityManagerFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
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
    CompanyBillingProfileRepository companyBillingProfileRepository;
    @Autowired
    GlobalSettingsRepository globalSettingsRepository;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    InvoiceRepository invoiceRepository;
    @Autowired
    EntityManagerFactory entityManagerFactory;

    UserAuthHelper.AuthenticatedUser antho;
    UserAuthHelper.AuthenticatedUser olivier;
    UUID billingProfileId;

    @BeforeEach
    void setUp() {
        antho = userAuthHelper.authenticateAnthony();
        olivier = userAuthHelper.authenticateOlivier();

        client.patch()
                .uri(getApiURI(ME_PATCH_BILLING_PROFILE_TYPE))
                .header("Authorization", "Bearer " + antho.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "type": "COMPANY"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        final var companyBillingProfile = companyBillingProfileRepository.findByUserId(antho.user().getId()).orElseThrow();
        companyBillingProfile.setName("My company");
        companyBillingProfile.setCountry("FRA");
        companyBillingProfile.setAddress("My address");
        companyBillingProfile.setRegistrationNumber("123456");
        companyBillingProfile.setSubjectToEuVAT(true);
        companyBillingProfile.setVerificationStatus(OldVerificationStatusEntity.VERIFIED);
        companyBillingProfileRepository.save(companyBillingProfile);

        billingProfileId = companyBillingProfile.getId();
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
                .jsonPath("$.rewards[?(@.id == 'dd7d445f-6915-4955-9bae-078173627b05')]").exists()
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
                          "number": "OD-MYCOMPANY-001",
                          "billingProfileType": "COMPANY",
                          "individualBillingProfile": null,
                          "companyBillingProfile": {
                            "registrationNumber": "123456",
                            "name": "My company",
                            "address": "My address",
                            "vatRegulationState": "VAT_APPLICABLE",
                            "euVATNumber": null,
                            "country": "France",
                            "countryCode": "FRA"
                          },
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
                                "target": {
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
                          "taxRate": 0.2,
                          "totalTax": {
                            "amount": 356396.000,
                            "currency": "USD"
                          },
                          "totalAfterTax": {
                            "amount": 2138376.000,
                            "currency": "USD"
                          },
                          "usdToEurConversionRate": 0.92
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

        notificationOutboxJob.run();
        webhookWireMockServer.verify(1, postRequestedFor(urlEqualTo("/"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.aggregate_name", equalTo("BillingProfile")))
                .withRequestBody(matchingJsonPath("$.event_name", equalTo("InvoiceUploaded")))
                .withRequestBody(matchingJsonPath("$.environment", equalTo("local-it")))
                .withRequestBody(matchingJsonPath("$.payload.billing_profile_id", equalTo(billingProfileId.toString())))
                .withRequestBody(matchingJsonPath("$.payload.invoice_id", equalTo(invoiceId.getValue())))
                .withRequestBody(matchingJsonPath("$.payload.is_external", equalTo("true")))
        );


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
                .contentDisposition(ContentDisposition.attachment().filename("OD-MYCOMPANY-001.pdf").build())
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
                          "number": "OD-MYCOMPANY-002",
                          "billingProfileType": "COMPANY",
                          "individualBillingProfile": null,
                          "companyBillingProfile": {
                            "registrationNumber": "123456",
                            "name": "My company",
                            "address": "My address",
                            "vatRegulationState": "VAT_APPLICABLE",
                            "euVATNumber": null,
                            "country": "France",
                            "countryCode": "FRA"
                          },
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
                                "target": {
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
                                "target": {
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
                          "taxRate": 0.2,
                          "totalTax": {
                            "amount": 404.000,
                            "currency": "USD"
                          },
                          "totalAfterTax": {
                            "amount": 2424.000,
                            "currency": "USD"
                          },
                          "usdToEurConversionRate": 0.92
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

        notificationOutboxJob.run();
        webhookWireMockServer.verify(1, postRequestedFor(urlEqualTo("/"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.aggregate_name", equalTo("BillingProfile")))
                .withRequestBody(matchingJsonPath("$.event_name", equalTo("InvoiceUploaded")))
                .withRequestBody(matchingJsonPath("$.environment", equalTo("local-it")))
                .withRequestBody(matchingJsonPath("$.payload.billing_profile_id", equalTo(billingProfileId.toString())))
                .withRequestBody(matchingJsonPath("$.payload.invoice_id", equalTo(invoiceId.getValue())))
                .withRequestBody(matchingJsonPath("$.payload.is_external", equalTo("false")))
        );

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
                .contentDisposition(ContentDisposition.attachment().filename("OD-MYCOMPANY-002.pdf").build())
                .expectBody().returnResult().getResponseBody();

        assertThat(data).isEqualTo(pdfData);

        testInvoiceStatus(UUID.fromString(invoiceId.getValue()));
    }

    protected void testInvoiceStatus(UUID invoiceId) {
        {
            final var invoice = entityManagerFactory.createEntityManager().find(InvoiceEntity.class, invoiceId);
            assertThat(invoice.status()).isEqualTo(InvoiceEntity.Status.APPROVED);

            final var em = entityManagerFactory.createEntityManager();
            em.getTransaction().begin();
            em.persist(PaymentEntity.builder()
                    .id(UUID.randomUUID())
                    .amount(BigDecimal.valueOf(1000))
                    .requestId(UUID.fromString("79209029-c488-4284-aa3f-bce8870d3a66"))
                    .processedAt(new Date())
                    .currencyCode("USDC")
                    .receipt(JacksonUtil.toJsonNode("""
                            {"Sepa": {"recipient_iban": "FR7640618802650004034616521", "transaction_reference": "IBAN OK"}}"""))
                    .build());
            em.flush();
            em.getTransaction().commit();
            em.close();
        }

        {
            final var invoice = entityManagerFactory.createEntityManager().find(InvoiceEntity.class, invoiceId);
            assertThat(invoice.status()).isEqualTo(InvoiceEntity.Status.APPROVED);

            final var em = entityManagerFactory.createEntityManager();
            em.getTransaction().begin();
            em.persist(PaymentEntity.builder()
                    .id(UUID.randomUUID())
                    .amount(BigDecimal.valueOf(1000))
                    .requestId(UUID.fromString("d22f75ab-d9f5-4dc6-9a85-60dcd7452028"))
                    .processedAt(new Date())
                    .currencyCode("USDC")
                    .receipt(JacksonUtil.toJsonNode("""
                            {"Sepa": {"recipient_iban": "FR7640618802650004034616521", "transaction_reference": "IBAN OK"}}"""))
                    .build());
            em.flush();
            em.getTransaction().commit();
            em.close();
        }

        {
            final var invoice = entityManagerFactory.createEntityManager().find(InvoiceEntity.class, invoiceId);
            assertThat(invoice.status()).isEqualTo(InvoiceEntity.Status.PAID);
        }
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
                .jsonPath("$.number").isEqualTo("OD-MYCOMPANY-003");

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
                .jsonPath("$.number").isEqualTo("OD-MYCOMPANY-003");
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


    @SneakyThrows
    @Test
    @Order(5)
    void preview_with_both_billing_profile_types() {
        // First, generate an invoice preview with the individual billing profile
        client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE_PREVIEW.formatted(billingProfileId), Map.of(
                        "rewardIds", "dd7d445f-6915-4955-9bae-078173627b05"
                )))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        // Then switch billing profile type
        client.patch()
                .uri(getApiURI(ME_PATCH_BILLING_PROFILE_TYPE))
                .header("Authorization", "Bearer " + antho.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "type": "INDIVIDUAL"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        final var individualBillingProfileId = client.get()
                .uri(ME_BILLING_PROFILES)
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MyBillingProfilesResponse.class)
                .returnResult()
                .getResponseBody().getBillingProfiles().get(0).getId();


        // Generate another preview for the same rewards
        client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE_PREVIEW.formatted(individualBillingProfileId), Map.of(
                        "rewardIds", "dd7d445f-6915-4955-9bae-078173627b05"
                )))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();
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
                               "number": "OD-MYCOMPANY-002",
                               "totalAfterTax": {
                                 "amount": 2424.000,
                                 "currency": "USD"
                               },
                               "status": "COMPLETE"
                             },
                             {
                               "number": "OD-MYCOMPANY-001",
                               "totalAfterTax": {
                                 "amount": 2138376.000,
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
        ;
    }

    @Test
    @Order(101)
    void should_order_invoices_by_date() {
        {
            // When
            final var invoices = client.get()
                    .uri(getApiURI(BILLING_PROFILE_INVOICES.formatted(billingProfileId), Map.of(
                            "pageIndex", "0",
                            "pageSize", "10",
                            "sort", "CREATED_AT",
                            "direction", "ASC"
                    )))
                    .header("Authorization", BEARER_PREFIX + antho.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody(BillingProfileInvoicesPageResponse.class)
                    .returnResult().getResponseBody().getInvoices();

            assertThat(invoices).hasSize(2);
            assertThat(invoices.get(0).getCreatedAt()).isBefore(invoices.get(1).getCreatedAt());
        }

        {
            // When
            final var invoices = client.get()
                    .uri(getApiURI(BILLING_PROFILE_INVOICES.formatted(billingProfileId), Map.of(
                            "pageIndex", "0",
                            "pageSize", "10",
                            "sort", "CREATED_AT",
                            "direction", "DESC"
                    )))
                    .header("Authorization", BEARER_PREFIX + antho.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody(BillingProfileInvoicesPageResponse.class)
                    .returnResult().getResponseBody().getInvoices();
            ;

            assertThat(invoices).hasSize(2);
            assertThat(invoices.get(1).getCreatedAt()).isBefore(invoices.get(0).getCreatedAt());
        }
    }

    @Test
    @Order(101)
    void should_order_invoices_by_number() {
        {
            // When
            final var invoices = client.get()
                    .uri(getApiURI(BILLING_PROFILE_INVOICES.formatted(billingProfileId), Map.of(
                            "pageIndex", "0",
                            "pageSize", "10",
                            "sort", "INVOICE_NUMBER",
                            "direction", "ASC"
                    )))
                    .header("Authorization", BEARER_PREFIX + antho.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody(BillingProfileInvoicesPageResponse.class)
                    .returnResult().getResponseBody().getInvoices();

            assertThat(invoices).hasSize(2);
            assertThat(invoices.get(0).getNumber().compareTo(invoices.get(1).getNumber())).isLessThan(0);
        }

        {
            // When
            final var invoices = client.get()
                    .uri(getApiURI(BILLING_PROFILE_INVOICES.formatted(billingProfileId), Map.of(
                            "pageIndex", "0",
                            "pageSize", "10",
                            "sort", "INVOICE_NUMBER",
                            "direction", "DESC"
                    )))
                    .header("Authorization", BEARER_PREFIX + antho.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody(BillingProfileInvoicesPageResponse.class)
                    .returnResult().getResponseBody().getInvoices();
            ;

            assertThat(invoices).hasSize(2);
            assertThat(invoices.get(1).getNumber().compareTo(invoices.get(0).getNumber())).isLessThan(0);
        }
    }

    @Test
    @Order(102)
    void should_order_invoices_by_amount() {
        {
            // When
            final var invoices = client.get()
                    .uri(getApiURI(BILLING_PROFILE_INVOICES.formatted(billingProfileId), Map.of(
                            "pageIndex", "0",
                            "pageSize", "10",
                            "sort", "AMOUNT",
                            "direction", "ASC"
                    )))
                    .header("Authorization", BEARER_PREFIX + antho.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody(BillingProfileInvoicesPageResponse.class)
                    .returnResult().getResponseBody().getInvoices();

            assertThat(invoices).hasSize(2);
            assertThat(invoices.get(0).getTotalAfterTax().getAmount()).isLessThan(invoices.get(1).getTotalAfterTax().getAmount());
        }

        {
            // When
            final var invoices = client.get()
                    .uri(getApiURI(BILLING_PROFILE_INVOICES.formatted(billingProfileId), Map.of(
                            "pageIndex", "0",
                            "pageSize", "10",
                            "sort", "AMOUNT",
                            "direction", "DESC"
                    )))
                    .header("Authorization", BEARER_PREFIX + antho.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody(BillingProfileInvoicesPageResponse.class)
                    .returnResult().getResponseBody().getInvoices();
            ;

            assertThat(invoices).hasSize(2);
            assertThat(invoices.get(1).getTotalAfterTax().getAmount()).isLessThan(invoices.get(0).getTotalAfterTax().getAmount());
        }
    }

    @Test
    @Order(103)
    void should_order_invoices_by_status() {
        final var invoice = invoiceRepository.findAll().stream().filter(i -> i.billingProfileId().equals(billingProfileId)).findFirst().orElseThrow();
        invoiceRepository.save(invoice.status(InvoiceEntity.Status.PAID));

        {
            // When
            final var invoices = client.get()
                    .uri(getApiURI(BILLING_PROFILE_INVOICES.formatted(billingProfileId), Map.of(
                            "pageIndex", "0",
                            "pageSize", "10",
                            "sort", "STATUS",
                            "direction", "ASC"
                    )))
                    .header("Authorization", BEARER_PREFIX + antho.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody(BillingProfileInvoicesPageResponse.class)
                    .returnResult().getResponseBody().getInvoices();

            assertThat(invoices).hasSize(2);
            assertThat(invoices.get(0).getStatus()).isLessThan(invoices.get(1).getStatus());
        }

        {
            // When
            final var invoices = client.get()
                    .uri(getApiURI(BILLING_PROFILE_INVOICES.formatted(billingProfileId), Map.of(
                            "pageIndex", "0",
                            "pageSize", "10",
                            "sort", "STATUS",
                            "direction", "DESC"
                    )))
                    .header("Authorization", BEARER_PREFIX + antho.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody(BillingProfileInvoicesPageResponse.class)
                    .returnResult().getResponseBody().getInvoices();
            ;

            assertThat(invoices).hasSize(2);
            assertThat(invoices.get(1).getStatus()).isLessThan(invoices.get(0).getStatus());
        }
    }
}
