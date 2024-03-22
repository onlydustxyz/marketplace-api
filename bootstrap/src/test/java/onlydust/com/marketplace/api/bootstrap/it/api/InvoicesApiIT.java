package onlydust.com.marketplace.api.bootstrap.it.api;

import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.CompanyBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.PayoutPreferenceFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.accounting.domain.view.ShortBillingProfileView;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.contract.model.BillingProfileInvoicesPageResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.GlobalSettingsRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.InvoiceRepository;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;

import javax.persistence.EntityManagerFactory;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.Set;
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
    GlobalSettingsRepository globalSettingsRepository;
    @Autowired
    InvoiceRepository invoiceRepository;
    @Autowired
    EntityManagerFactory entityManagerFactory;
    @Autowired
    BillingProfileFacadePort billingProfileFacadePort;
    @Autowired
    BillingProfileStoragePort billingProfileStoragePort;
    @Autowired
    PayoutPreferenceFacadePort payoutPreferenceFacadePort;

    UserAuthHelper.AuthenticatedUser antho;
    UUID companyBillingProfileId;

    private static final ProjectId PROJECT_ID = ProjectId.of("298a547f-ecb6-4ab2-8975-68f4e9bf7b39");

    @BeforeEach
    void setUp() {
        antho = userAuthHelper.authenticateAnthony();
        companyBillingProfileId = initBillingProfile(antho).value();

        payoutPreferenceFacadePort.setPayoutPreference(PROJECT_ID, BillingProfile.Id.of(companyBillingProfileId), UserId.of(antho.user().getId()));
    }

    private BillingProfile.Id initBillingProfile(UserAuthHelper.AuthenticatedUser owner) {
        final var ownerId = UserId.of(owner.user().getId());

        return billingProfileStoragePort.findAllBillingProfilesForUser(ownerId).stream()
                .filter(bp -> bp.getType() == BillingProfile.Type.COMPANY)
                .findFirst()
                .map(ShortBillingProfileView::getId)
                .orElseGet(() -> createCompanyBillingProfileFor(ownerId).id());
    }

    private CompanyBillingProfile createCompanyBillingProfileFor(UserId ownerId) {
        final var billingProfile = billingProfileFacadePort.createCompanyBillingProfile(ownerId, "My billing profile", Set.of(PROJECT_ID));

        billingProfileStoragePort.savePayoutInfoForBillingProfile(PayoutInfo.builder()
                .ethWallet(Ethereum.wallet("abuisset.eth"))
                .build(), billingProfile.id());

        billingProfileStoragePort.saveKyb(billingProfile.kyb().toBuilder()
                .name("My company")
                .country(Country.fromIso3("FRA"))
                .address("My address")
                .registrationNumber("123456")
                .subjectToEuropeVAT(true)
                .usEntity(false)
                .status(VerificationStatus.VERIFIED)
                .build());

        billingProfileStoragePort.updateBillingProfileStatus(billingProfile.id(), VerificationStatus.VERIFIED);

        return billingProfile;
    }

    @Test
    @Order(0)
    void list_pending_invoices_before() {
        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_INVOICEABLE_REWARDS.formatted(companyBillingProfileId)))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards.size()").isEqualTo(12)
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
                .uri(getApiURI(BILLING_PROFILE_INVOICE_PREVIEW.formatted(companyBillingProfileId), Map.of(
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
                          "number": "OD-MY-COMPANY-001",
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
                                "network": "ETHEREUM"
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
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "target": {
                                  "amount": 1010.00,
                                  "currency": {
                                    "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                    "code": "USD",
                                    "name": "US Dollar",
                                    "logoUrl": null,
                                    "decimals": 2
                                  },
                                  "conversionRate": 1.01
                                }
                              }
                            }
                          ],
                          "totalBeforeTax": {
                            "amount": 1010.00,
                            "currency": {
                              "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                              "code": "USD",
                              "name": "US Dollar",
                              "logoUrl": null,
                              "decimals": 2
                            }
                          },
                          "taxRate": 0.2,
                          "totalTax": {
                            "amount": 202.000,
                            "currency": {
                              "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                              "code": "USD",
                              "name": "US Dollar",
                              "logoUrl": null,
                              "decimals": 2
                            }
                          },
                          "totalAfterTax": {
                            "amount": 1212.000,
                            "currency": {
                              "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                              "code": "USD",
                              "name": "US Dollar",
                              "logoUrl": null,
                              "decimals": 2
                            }
                          },
                          "usdToEurConversionRate": 0.92
                        }
                        """
                );

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_INVOICEABLE_REWARDS.formatted(companyBillingProfileId)))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards.size()").isEqualTo(12)
                .jsonPath("$.rewards[?(@.id == '966cd55c-7de8-45c4-8bba-b388c38ca15d')]").exists();

        // When
        when(pdfStoragePort.upload(eq(invoiceId.getValue() + ".pdf"), any())).then(invocation -> {
            final var fileName = invocation.getArgument(0, String.class);
            return new URL("https://s3.storage.com/%s".formatted(fileName));
        });

        // Uploading a generated invoice is forbidden when the mandate has not been accepted
        client.post()
                .uri(getApiURI(BILLING_PROFILE_INVOICE.formatted(companyBillingProfileId, invoiceId.getValue())))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .body(fromResource(new FileSystemResource(getClass().getResource("/invoices/invoice-sample.pdf").getFile())))
                .exchange()
                // Then
                .expectStatus()
                .isForbidden();

        // Uploading an external invoice is allowed when the mandate has not been accepted
        client.post()
                .uri(getApiURI(BILLING_PROFILE_INVOICE.formatted(companyBillingProfileId, invoiceId.getValue()), "fileName", "invoice-sample.pdf"))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .body(fromResource(new FileSystemResource(getClass().getResource("/invoices/invoice-sample.pdf").getFile())))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_INVOICEABLE_REWARDS.formatted(companyBillingProfileId)))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards.size()").isEqualTo(11)
                .jsonPath("$.rewards[?(@.id == '966cd55c-7de8-45c4-8bba-b388c38ca15d')]").doesNotExist()
        ;

        notificationOutboxJob.run();
        webhookWireMockServer.verify(1, postRequestedFor(urlEqualTo("/"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.aggregate_name", equalTo("BillingProfile")))
                .withRequestBody(matchingJsonPath("$.event_name", equalTo("InvoiceUploaded")))
                .withRequestBody(matchingJsonPath("$.environment", equalTo("local-it")))
                .withRequestBody(matchingJsonPath("$.payload.billing_profile_id", equalTo(companyBillingProfileId.toString())))
                .withRequestBody(matchingJsonPath("$.payload.invoice_id", equalTo(invoiceId.getValue())))
                .withRequestBody(matchingJsonPath("$.payload.is_external", equalTo("true")))
        );


        // When
        final var pdfData = faker.lorem().paragraph().getBytes();
        when(pdfStoragePort.download(eq(invoiceId.getValue() + ".pdf"))).then(invocation -> new ByteArrayInputStream(pdfData));

        final var data = client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE.formatted(companyBillingProfileId, invoiceId.getValue())))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectHeader()
                .contentDisposition(ContentDisposition.attachment().filename("OD-MY-COMPANY-001.pdf").build())
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
                .uri(getApiURI(BILLING_PROFILE_INVOICE_PREVIEW.formatted(companyBillingProfileId), Map.of(
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
                          "number": "OD-MY-COMPANY-002",
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
                                "network": "ETHEREUM"
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
                                "currency": {"id":"562bbf65-8a71-4d30-ad63-520c0d68ba27","code":"USDC","name":"USD Coin","logoUrl":"https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png","decimals":6},
                                "target": {
                                  "amount": 1010.00,
                                  "currency": {"id":"f35155b5-6107-4677-85ac-23f8c2a63193","code":"USD","name":"US Dollar","logoUrl":null,"decimals":2},
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
                                "currency": {"id":"562bbf65-8a71-4d30-ad63-520c0d68ba27","code":"USDC","name":"USD Coin","logoUrl":"https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png","decimals":6},
                                "target": {
                                  "amount": 1010.00,
                                  "currency": {"id":"f35155b5-6107-4677-85ac-23f8c2a63193","code":"USD","name":"US Dollar","logoUrl":null,"decimals":2},
                                  "conversionRate": 1.01
                                }
                              }
                            }
                          ],
                          "totalBeforeTax": {
                            "amount": 2020.00,
                            "currency": {"id":"f35155b5-6107-4677-85ac-23f8c2a63193","code":"USD","name":"US Dollar","logoUrl":null,"decimals":2}
                          },
                          "taxRate": 0.2,
                          "totalTax": {
                            "amount": 404.000,
                            "currency": {"id":"f35155b5-6107-4677-85ac-23f8c2a63193","code":"USD","name":"US Dollar","logoUrl":null,"decimals":2}
                          },
                          "totalAfterTax": {
                            "amount": 2424.000,
                            "currency": {"id":"f35155b5-6107-4677-85ac-23f8c2a63193","code":"USD","name":"US Dollar","logoUrl":null,"decimals":2}
                          },
                          "usdToEurConversionRate": 0.92
                        }
                        """
                );

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_INVOICEABLE_REWARDS.formatted(companyBillingProfileId)))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards.size()").isEqualTo(11)
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
                .uri(getApiURI(BILLING_PROFILE_INVOICES_MANDATE.formatted(companyBillingProfileId)))
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
                .uri(getApiURI(BILLING_PROFILE_INVOICE.formatted(companyBillingProfileId, invoiceId.getValue()), "fileName", "invoice-sample.pdf"))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .body(fromResource(new FileSystemResource(getClass().getResource("/invoices/invoice-sample.pdf").getFile())))
                .exchange()
                // Then
                .expectStatus()
                .isForbidden();

        // Uploading a generated invoice is allowed when the mandate has been accepted
        client.post()
                .uri(getApiURI(BILLING_PROFILE_INVOICE.formatted(companyBillingProfileId, invoiceId.getValue())))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .body(fromResource(new FileSystemResource(getClass().getResource("/invoices/invoice-sample.pdf").getFile())))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_INVOICEABLE_REWARDS.formatted(companyBillingProfileId)))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards.size()").isEqualTo(9)
                .jsonPath("$.rewards[?(@.id == '79209029-c488-4284-aa3f-bce8870d3a66')]").doesNotExist()
                .jsonPath("$.rewards[?(@.id == 'd22f75ab-d9f5-4dc6-9a85-60dcd7452028')]").doesNotExist()
        ;

        notificationOutboxJob.run();
        webhookWireMockServer.verify(1, postRequestedFor(urlEqualTo("/"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.aggregate_name", equalTo("BillingProfile")))
                .withRequestBody(matchingJsonPath("$.event_name", equalTo("InvoiceUploaded")))
                .withRequestBody(matchingJsonPath("$.environment", equalTo("local-it")))
                .withRequestBody(matchingJsonPath("$.payload.billing_profile_id", equalTo(companyBillingProfileId.toString())))
                .withRequestBody(matchingJsonPath("$.payload.invoice_id", equalTo(invoiceId.getValue())))
                .withRequestBody(matchingJsonPath("$.payload.is_external", equalTo("false")))
        );

        // When
        final var pdfData = faker.lorem().paragraph().getBytes();
        when(pdfStoragePort.download(eq(invoiceId.getValue() + ".pdf"))).then(invocation -> new ByteArrayInputStream(pdfData));

        final var data = client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE.formatted(companyBillingProfileId, invoiceId.getValue())))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectHeader()
                .contentDisposition(ContentDisposition.attachment().filename("OD-MY-COMPANY-002.pdf").build())
                .expectBody().returnResult().getResponseBody();

        assertThat(data).isEqualTo(pdfData);
    }

    @Test
    @Order(3)
    void invoice_name_should_be_incremented_only_when_submitted() {
        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE_PREVIEW.formatted(companyBillingProfileId), Map.of(
                        "rewardIds", "dd7d445f-6915-4955-9bae-078173627b05"
                )))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.number").isEqualTo("OD-MY-COMPANY-003");

        client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE_PREVIEW.formatted(companyBillingProfileId), Map.of(
                        "rewardIds", "dd7d445f-6915-4955-9bae-078173627b05"
                )))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.number").isEqualTo("OD-MY-COMPANY-003");
    }

    @Test
    @Order(4)
    void should_prevent_invoice_preview_on_invoiced_rewards() {
        // Given
        final var rewardAlreadyInvoiced = "966cd55c-7de8-45c4-8bba-b388c38ca15d";
        final var otherReward = "dd7d445f-6915-4955-9bae-078173627b05";

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE_PREVIEW.formatted(companyBillingProfileId), Map.of(
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
        resetInvoiceMandateLatestVersionDate();

        client.get()
                .uri(ME_BILLING_PROFILES)
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.billingProfiles[?(@.id == '%s')].invoiceMandateAccepted".formatted(companyBillingProfileId)).isEqualTo(false);

        // When
        client.put()
                .uri(getApiURI(BILLING_PROFILE_INVOICES_MANDATE.formatted(companyBillingProfileId)))
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

        // Then
        client.get()
                .uri(ME_BILLING_PROFILES)
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.billingProfiles[?(@.id == '%s')].invoiceMandateAccepted".formatted(companyBillingProfileId)).isEqualTo(true);

        // When
        resetInvoiceMandateLatestVersionDate();

        // Then
        client.get()
                .uri(ME_BILLING_PROFILES)
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.billingProfiles[?(@.id == '%s')].invoiceMandateAccepted".formatted(companyBillingProfileId)).isEqualTo(false);
    }

    private void resetInvoiceMandateLatestVersionDate() {
        final var settings = globalSettingsRepository.get();
        settings.setInvoiceMandateLatestVersionDate(new Date());
        globalSettingsRepository.save(settings);
    }


    @Test
    @Order(99)
    void list_invoices() {
        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICES.formatted(companyBillingProfileId), Map.of(
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
                               "number": "OD-MY-COMPANY-002",
                               "totalAfterTax": {
                                 "amount": 2424.000,
                                 "currency": {"id":"f35155b5-6107-4677-85ac-23f8c2a63193","code":"USD","name":"US Dollar","logoUrl":null,"decimals":2}
                               },
                               "status": "PROCESSING"
                             },
                             {
                               "number": "OD-MY-COMPANY-001",
                               "totalAfterTax": {
                                 "amount": 1212.000,
                                 "currency": {"id":"f35155b5-6107-4677-85ac-23f8c2a63193","code":"USD","name":"US Dollar","logoUrl":null,"decimals":2}
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
                    .uri(getApiURI(BILLING_PROFILE_INVOICES.formatted(companyBillingProfileId), Map.of(
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
                    .uri(getApiURI(BILLING_PROFILE_INVOICES.formatted(companyBillingProfileId), Map.of(
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
                    .uri(getApiURI(BILLING_PROFILE_INVOICES.formatted(companyBillingProfileId), Map.of(
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
                    .uri(getApiURI(BILLING_PROFILE_INVOICES.formatted(companyBillingProfileId), Map.of(
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
                    .uri(getApiURI(BILLING_PROFILE_INVOICES.formatted(companyBillingProfileId), Map.of(
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
                    .uri(getApiURI(BILLING_PROFILE_INVOICES.formatted(companyBillingProfileId), Map.of(
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
        final var invoice = invoiceRepository.findAll().stream().filter(i -> i.billingProfileId().equals(companyBillingProfileId)).findFirst().orElseThrow();
        invoiceRepository.save(invoice.status(InvoiceEntity.Status.PAID));

        {
            // When
            final var invoices = client.get()
                    .uri(getApiURI(BILLING_PROFILE_INVOICES.formatted(companyBillingProfileId), Map.of(
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
                    .uri(getApiURI(BILLING_PROFILE_INVOICES.formatted(companyBillingProfileId), Map.of(
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

    @SneakyThrows
    @Test
    @Order(104)
    void preview_with_both_billing_profile_types() {
        final var rewardId = "fa097fab-9c01-4afa-bf1f-8d07029e03af";
        final var em = entityManagerFactory.createEntityManager();

        // First, generate an invoice preview with the company billing profile
        final var invoiceOnCompanyId = new MutableObject<String>();
        client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE_PREVIEW.formatted(companyBillingProfileId), Map.of(
                        "rewardIds", rewardId
                )))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").value(invoiceOnCompanyId::setValue);

        var invoiceOnCompany = em.find(InvoiceEntity.class, UUID.fromString(invoiceOnCompanyId.getValue()));
        assertThat(invoiceOnCompany.status()).isEqualTo(InvoiceEntity.Status.DRAFT);
        assertThat(invoiceOnCompany.data().rewards().stream().anyMatch(r -> r.id().equals(UUID.fromString(rewardId)))).isTrue();
        var reward = entityManagerFactory.createEntityManager().find(RewardEntity.class, UUID.fromString(rewardId));
        assertThat(reward.invoice().id().toString()).isEqualTo(invoiceOnCompanyId.getValue());

        // Then switch billing profile type
        final var ownerId = UserId.of(antho.user().getId());
        final var individualBillingProfileId = billingProfileStoragePort.findIndividualBillingProfileForUser(ownerId).orElseThrow().getId();
        final var individualBillingProfile = billingProfileStoragePort.findById(individualBillingProfileId).orElseThrow();
        billingProfileStoragePort.saveKyc(individualBillingProfile.getKyc().toBuilder().usCitizen(false).build());
        billingProfileStoragePort.updateBillingProfileStatus(individualBillingProfileId, VerificationStatus.VERIFIED);
        billingProfileStoragePort.savePayoutInfoForBillingProfile(PayoutInfo.builder()
                .ethWallet(Ethereum.wallet("abuisset.eth"))
                .build(), individualBillingProfileId);

        payoutPreferenceFacadePort.setPayoutPreference(PROJECT_ID, individualBillingProfileId, ownerId);

        // Generate another preview for the same rewards
        final var invoiceId = new MutableObject<String>();
        client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE_PREVIEW.formatted(individualBillingProfileId), Map.of(
                        "rewardIds", rewardId
                )))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").value(invoiceId::setValue);

        // ...and approve it
        client.post()
                .uri(getApiURI(BILLING_PROFILE_INVOICE.formatted(individualBillingProfileId, invoiceId.getValue())))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .body(fromResource(new FileSystemResource(getClass().getResource("/invoices/invoice-sample.pdf").getFile())))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        final var invoice = em.find(InvoiceEntity.class, UUID.fromString(invoiceId.getValue()));
        assertThat(invoice.status()).isEqualTo(InvoiceEntity.Status.APPROVED);

        // Check that the reward is part of the approved invoice
        reward = em.find(RewardEntity.class, UUID.fromString(rewardId));
        assertThat(reward.invoice().id().toString()).isEqualTo(invoiceId.getValue());

        // Switch back to company billing profile type
        payoutPreferenceFacadePort.setPayoutPreference(PROJECT_ID, BillingProfile.Id.of(companyBillingProfileId), ownerId);

        // And generate another preview for another reward
        client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE_PREVIEW.formatted(companyBillingProfileId), Map.of(
                        "rewardIds", "b917e003-2880-4958-995b-06805fb0e928"
                )))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody();

        // Now check that the reward is still part of the approved invoice
        reward = em.find(RewardEntity.class, UUID.fromString(rewardId));
        assertThat(reward.invoice().id().toString()).isEqualTo(invoiceId.getValue());
        em.close();
    }

    @Test
    @Order(105)
    void should_prevent_invoice_preview_on_disabled_billing_profiles() {
        // Given
        final var otherReward = "dd7d445f-6915-4955-9bae-078173627b05";

        // When
        client.put()
                .uri(getApiURI(BILLING_PROFILES_ENABLE_BY_ID.formatted(companyBillingProfileId)))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "enable": false
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE_PREVIEW.formatted(companyBillingProfileId), Map.of(
                        "rewardIds", "%s".formatted(otherReward)
                )))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isUnauthorized()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Cannot generate invoice on a disabled billing profile");
    }
}
