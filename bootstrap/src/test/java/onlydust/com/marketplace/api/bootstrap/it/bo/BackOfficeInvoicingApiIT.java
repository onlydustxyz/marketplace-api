package onlydust.com.marketplace.api.bootstrap.it.bo;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.CompanyBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.IndividualBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.SelfEmployedBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.VerificationStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.KybRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.KycRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardRepository;
import onlydust.com.marketplace.api.webhook.Config;
import onlydust.com.marketplace.kernel.jobs.OutboxConsumerJob;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import onlydust.com.marketplace.project.domain.service.UserService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeInvoicingApiIT extends AbstractMarketplaceBackOfficeApiIT {
    @Autowired
    PdfStoragePort pdfStoragePort;
    @Autowired
    BillingProfileService billingProfileService;
    @Autowired
    UserService userService;
    @Autowired
    private RewardRepository rewardRepository;
    @Autowired
    OutboxConsumerJob notificationOutboxJob;
    @Autowired
    Config webhookHttpClientProperties;
    @Autowired
    InvoiceStoragePort invoiceStoragePort;
    @Autowired
    KycRepository kycRepository;
    @Autowired
    KybRepository kybRepository;
    private final Faker faker = new Faker();

    UserId userId;
    CompanyBillingProfile companyBillingProfile;
    SelfEmployedBillingProfile selfEmployedBillingProfile;
    IndividualBillingProfile individualBillingProfile;

    static final List<Invoice.Id> companyBillingProfileToReviewInvoices = new ArrayList<>();

    void setUp() throws IOException {
        // Given
        final UserAuthHelper.AuthenticatedUser olivier = userAuthHelper.authenticateOlivier();
        userId = UserId.of(olivier.user().getId());

        companyBillingProfile = billingProfileService.createCompanyBillingProfile(userId, "Apple Inc.", null);
        billingProfileService.updatePayoutInfo(companyBillingProfile.id(), userId,
                PayoutInfo.builder().ethWallet(new WalletLocator(new Name(userId + ".eth"))).build());

        selfEmployedBillingProfile = billingProfileService.createSelfEmployedBillingProfile(userId, "Olivier SASU", null);
        billingProfileService.updatePayoutInfo(selfEmployedBillingProfile.id(), userId,
                PayoutInfo.builder().ethWallet(new WalletLocator(new Name(userId + ".eth"))).build());

        individualBillingProfile = billingProfileService.createIndividualBillingProfile(userId, "Olivier", null);
        billingProfileService.updatePayoutInfo(individualBillingProfile.id(), userId,
                PayoutInfo.builder().ethWallet(new WalletLocator(new Name(userId + ".eth"))).build());

        kybRepository.findByBillingProfileId(companyBillingProfile.id().value())
                .ifPresent(kyb -> kybRepository.save(kyb.toBuilder()
                        .country("FRA")
                        .address("1 Infinite Loop, Cupertino, CA 95014, United States")
                        .euVATNumber("FR12345678901")
                        .name("Apple Inc.")
                        .registrationDate(faker.date().birthday())
                        .registrationNumber("123456789")
                        .usEntity(false)
                        .subjectToEuVAT(true)
                        .verificationStatus(VerificationStatusEntity.VERIFIED).build()));
        kybRepository.findByBillingProfileId(selfEmployedBillingProfile.id().value())
                .ifPresent(kyb -> kybRepository.save(kyb.toBuilder()
                        .country("FRA")
                        .address("2 Infinite Loop, Cupertino, CA 95014, United States")
                        .euVATNumber("FR0987654321")
                        .name("Olivier SASU")
                        .registrationDate(faker.date().birthday())
                        .registrationNumber("ABC123456789")
                        .usEntity(false)
                        .subjectToEuVAT(true)
                        .verificationStatus(VerificationStatusEntity.VERIFIED).build()));
        kycRepository.findByBillingProfileId(individualBillingProfile.id().value())
                .ifPresent(kyc -> kycRepository.save(kyc.toBuilder()
                        .country("FRA")
                        .address("3 Infinite Loop, Cupertino, CA 95014, United States")
                        .firstName("Olivier")
                        .lastName("Fuxet")
                        .birthdate(faker.date().birthday())
                        .usCitizen(false)
                        .verificationStatus(VerificationStatusEntity.VERIFIED).build()));

        // Given
        newCompanyInvoiceToReview(List.of(
                RewardId.of("061e2c7e-bda4-49a8-9914-2e76926f70c2")));
        newCompanyInvoiceToReview(List.of(
                RewardId.of("ee28315c-7a84-4052-9308-c2236eeafda1"),
                RewardId.of("d067b24d-115a-45e9-92de-94dd1d01b184")));
        newCompanyInvoiceToReview(List.of(
                RewardId.of("d506a05d-3739-452f-928d-45ea81d33079"),
                RewardId.of("5083ac1f-4325-4d47-9760-cbc9ab82f25c"),
                RewardId.of("e6ee79ae-b3f0-4f4e-b7e3-9e643bc27236")));
    }

    private void newCompanyInvoiceToReview(List<RewardId> rewardIds) throws IOException {
        final Invoice.Id invoiceId = billingProfileService.previewInvoice(userId, companyBillingProfile.id(), rewardIds).id();
        billingProfileService.uploadExternalInvoice(userId, companyBillingProfile.id(), invoiceId, "foo.pdf",
                new FileSystemResource(Objects.requireNonNull(getClass().getResource("/invoices/invoice-sample.pdf")).getFile()).getInputStream());
        companyBillingProfileToReviewInvoices.add(invoiceId);
    }

    @Test
    @Order(1)
    void should_list_invoices() throws IOException {
        setUp();

        // When
        client
                .get()
                .uri(getApiURI(INVOICES, Map.of(
                        "pageIndex", "0",
                        "pageSize", "10",
                        "invoiceIds", companyBillingProfileToReviewInvoices.stream().map(Invoice.Id::toString).collect(Collectors.joining(",")))))
                .header("Api-Key", apiKey())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.invoices[?(@.createdAt empty true)]").isEmpty()
                .jsonPath("$.invoices[?(@.dueAt empty true)]").isEmpty()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 3,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "invoices": [
                            {
                              "status": "PROCESSING",
                              "internalStatus": "TO_REVIEW",
                              "amount": 5718.000,
                              "currencyId": "f35155b5-6107-4677-85ac-23f8c2a63193",
                              "rewardIds": [
                                "e6ee79ae-b3f0-4f4e-b7e3-9e643bc27236",
                                "5083ac1f-4325-4d47-9760-cbc9ab82f25c",
                                "d506a05d-3739-452f-928d-45ea81d33079"
                              ]
                            },
                            {
                              "status": "PROCESSING",
                              "internalStatus": "TO_REVIEW",
                              "amount": 3333.000,
                              "currencyId": "f35155b5-6107-4677-85ac-23f8c2a63193",
                              "rewardIds": [
                                "d067b24d-115a-45e9-92de-94dd1d01b184",
                                "ee28315c-7a84-4052-9308-c2236eeafda1"
                              ]
                            },
                            {
                              "status": "PROCESSING",
                              "internalStatus": "TO_REVIEW",
                              "amount": 1212.000,
                              "currencyId": "f35155b5-6107-4677-85ac-23f8c2a63193",
                              "rewardIds": [
                                "061e2c7e-bda4-49a8-9914-2e76926f70c2"
                              ]
                            }
                          ]
                        }
                        """)
        ;
    }

    @Test
    @Order(2)
    void should_list_invoices_v2() throws IOException {

        // When
        client
                .get()
                .uri(getApiURI(V2_INVOICES, Map.of(
                        "pageIndex", "0",
                        "pageSize", "10",
                        "invoiceIds", companyBillingProfileToReviewInvoices.stream().map(Invoice.Id::toString).collect(Collectors.joining(",")))))
                .header("Api-Key", apiKey())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.invoices[?(@.createdAt empty true)]").isEmpty()
                .jsonPath("$.invoices[?(@.billingProfile.id empty true)]").isEmpty()
                .jsonPath("$.invoices[?(@.billingProfile.name empty true)]").isEmpty()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 3,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "invoices": [
                            {
                              "status": "TO_REVIEW",
                              "billingProfile": {
                                "name": "Apple Inc.",
                                "type": "COMPANY",
                                "verificationStatus": null,
                                "admins": null
                              },
                              "rewardCount": 3,
                              "totalEquivalent": {
                                "amount": 5718.000,
                                "dollarsEquivalent": 5718.000,
                                "conversionRate": null,
                                "currencyCode": "USD",
                                "currencyName": "US Dollar",
                                "currencyLogoUrl": null
                              },
                              "totalPerCurrency": [
                                {
                                  "amount": 500,
                                  "dollarsEquivalent": 505.00,
                                  "conversionRate": null,
                                  "currencyCode": "USDC",
                                  "currencyName": "USD Coin",
                                  "currencyLogoUrl": null
                                },
                                {
                                  "amount": 1000,
                                  "dollarsEquivalent": 1010.00,
                                  "conversionRate": null,
                                  "currencyCode": "USDC",
                                  "currencyName": "USD Coin",
                                  "currencyLogoUrl": null
                                },
                                {
                                  "amount": 3250,
                                  "dollarsEquivalent": 3250,
                                  "conversionRate": null,
                                  "currencyCode": "USD",
                                  "currencyName": "US Dollar",
                                  "currencyLogoUrl": null
                                }
                              ]
                            },
                            {
                              "status": "TO_REVIEW",
                              "billingProfile": {
                                "name": "Apple Inc.",
                                "type": "COMPANY",
                                "verificationStatus": null,
                                "admins": null
                              },
                              "rewardCount": 2,
                              "totalEquivalent": {
                                "amount": 3333.000,
                                "dollarsEquivalent": 3333.000,
                                "conversionRate": null,
                                "currencyCode": "USD",
                                "currencyName": "US Dollar",
                                "currencyLogoUrl": null
                              },
                              "totalPerCurrency": [
                                {
                                  "amount": 1000,
                                  "dollarsEquivalent": 1010.00,
                                  "conversionRate": null,
                                  "currencyCode": "USDC",
                                  "currencyName": "USD Coin",
                                  "currencyLogoUrl": null
                                },
                                {
                                  "amount": 1750,
                                  "dollarsEquivalent": 1767.50,
                                  "conversionRate": null,
                                  "currencyCode": "USDC",
                                  "currencyName": "USD Coin",
                                  "currencyLogoUrl": null
                                }
                              ]
                            },
                            {
                              "status": "TO_REVIEW",
                              "billingProfile": {
                                "name": "Apple Inc.",
                                "type": "COMPANY",
                                "verificationStatus": null,
                                "admins": null
                              },
                              "rewardCount": 1,
                              "totalEquivalent": {
                                "amount": 1212.000,
                                "dollarsEquivalent": 1212.000,
                                "conversionRate": null,
                                "currencyCode": "USD",
                                "currencyName": "US Dollar",
                                "currencyLogoUrl": null
                              },
                              "totalPerCurrency": [
                                {
                                  "amount": 1000,
                                  "dollarsEquivalent": 1010.00,
                                  "conversionRate": null,
                                  "currencyCode": "USDC",
                                  "currencyName": "USD Coin",
                                  "currencyLogoUrl": null
                                }
                              ]
                            }
                          ]
                        }
                        """)
        ;
    }

    @Test
    @Order(3)
    void should_get_invoice() {
        client
                .get()
                .uri(getApiURI(INVOICE.formatted(companyBillingProfileToReviewInvoices.get(0))))
                .header("Api-Key", apiKey())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "number": "OD-APPLEINC-001",
                          "status": "TO_REVIEW",
                          "billingProfile": {
                            "name": "Apple Inc.",
                            "type": "COMPANY",
                            "verificationStatus": null,
                            "admins": [
                              {
                                "login": null,
                                "name": "ofux",
                                "email": "olivier.fuxet@gmail.com",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp"
                              }
                            ]
                          },
                          "rejectionReason": null,
                          "totalEquivalent": {
                            "amount": 1212.000,
                            "currencyCode": "USD",
                            "currencyName": "US Dollar",
                            "currencyLogoUrl": null
                          },
                          "rewardsPerNetwork": [
                            {
                              "network": "ETHEREUM",
                              "billingAccountNumber": "e461c019-ba23-4671-9b6c-3a5a18748af9.eth",
                              "dollarsEquivalent": 1010.00,
                              "totalPerCurrency": [
                                {
                                  "amount": 1000,
                                  "dollarsEquivalent": 1010.00,
                                  "conversionRate": null,
                                  "currencyCode": "USDC",
                                  "currencyName": "USD Coin",
                                  "currencyLogoUrl": null
                                }
                              ],
                              "rewards": [
                                {
                                  "id": "061e2c7e-bda4-49a8-9914-2e76926f70c2",
                                  "requestedAt": "2023-05-15T12:15:54.25529Z",
                                  "processedAt": "2023-07-27T10:27:14.522708Z",
                                  "githubUrls": [
                                    "https://github.com/od-mocks/cool-repo-A/pull/397"
                                  ],
                                  "project": {
                                    "name": "Pizzeria Yoshi !",
                                    "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/14305950553200301786.png"
                                  },
                                  "sponsors": [],
                                  "money": {
                                    "amount": 1000,
                                    "dollarsEquivalent": 1010.00,
                                    "conversionRate": null,
                                    "currencyCode": "USDC",
                                    "currencyName": "USD Coin",
                                    "currencyLogoUrl": null
                                  },
                                  "transactionReferences": [
                                    "0x0000000000000000000000000000000000000000000000000000000000000000"
                                  ]
                                }
                              ]
                            }
                          ]
                        }
                        """)
        ;
    }

    @Test
    @Order(4)
    void should_approve_invoices() {
        client
                .put()
                .uri(getApiURI(PUT_INVOICES_STATUS.formatted(companyBillingProfileToReviewInvoices.get(0))))
                .header("Api-Key", apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "status": "APPROVED"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isNoContent()
        ;

        client
                .get()
                .uri(getApiURI(V2_INVOICES, Map.of("pageIndex", "0", "pageSize", "10")))
                .header("Api-Key", apiKey())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 3,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "invoices": [
                            {
                              "status": "TO_REVIEW",
                              "rewardCount": 3
                            },
                            {
                              "status": "TO_REVIEW",
                              "rewardCount": 2
                            },
                            {
                              "status": "APPROVED",
                              "rewardCount": 1
                            }
                          ]
                        }
                        """)
        ;
    }

    @Test
    @Order(5)
    void should_reject_invoices() {
        final String rejectionReason = faker.rickAndMorty().character();
        client
                .put()
                .uri(getApiURI(PUT_INVOICES_STATUS.formatted(companyBillingProfileToReviewInvoices.get(1))))
                .header("Api-Key", apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "status": "REJECTED",
                          "rejectionReason": "%s"
                        }
                        """.formatted(rejectionReason))
                .exchange()
                .expectStatus()
                .isNoContent()
        ;

        // To avoid error on post InvoiceUploaded event to old BO
        makeWebhookWireMockServer.stubFor(post("/").willReturn(ok()));
        makeWebhookSendRejectedInvoiceMailWireMockServer.stubFor(
                post("/?api-key=%s".formatted(webhookHttpClientProperties.getApiKey()))
                        .willReturn(ok()));

        notificationOutboxJob.run();


        final Invoice invoice = invoiceStoragePort.get(companyBillingProfileToReviewInvoices.get(1)).orElseThrow();
        makeWebhookSendRejectedInvoiceMailWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/?api-key=%s".formatted(webhookHttpClientProperties.getApiKey())))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withRequestBody(matchingJsonPath("$.recipientEmail", equalTo("olivier.fuxet@gmail.com")))
                        .withRequestBody(matchingJsonPath("$.recipientName", equalTo("Olivier")))
                        .withRequestBody(matchingJsonPath("$.rewardCount", equalTo(String.valueOf(invoice.rewards().size()))))
                        .withRequestBody(matchingJsonPath("$.invoiceName", equalTo(invoice.number().value())))
                        .withRequestBody(matchingJsonPath("$.totalUsdAmount", equalTo("2777.5")))
                        .withRequestBody(
                                matchingJsonPath("$.rejectionReason", equalTo(rejectionReason))
                        )
                        .withRequestBody(matchingJsonPath("$.rewardNames", containing("#D067B - oscar's awesome project - USDC - 1000")))
                        .withRequestBody(matchingJsonPath("$.rewardNames", containing("#EE283 - Aldébaran du Taureau - USDC - 1750")))
        );

        client
                .get()
                .uri(getApiURI(INVOICE.formatted(companyBillingProfileToReviewInvoices.get(1))))
                .header("Api-Key", apiKey())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rejectionReason").isEqualTo(rejectionReason);

        client
                .get()
                .uri(getApiURI(V2_INVOICES, Map.of("pageIndex", "0", "pageSize", "10")))
                .header("Api-Key", apiKey())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 3,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "invoices": [
                            {
                              "status": "TO_REVIEW",
                              "rewardCount": 3
                            },
                            {
                              "status": "REJECTED",
                              "rewardCount": 2
                            },
                            {
                              "status": "APPROVED",
                              "rewardCount": 1
                            }
                          ]
                        }
                        """)
        ;

    }


    @Test
    @Order(6)
    void should_filter_invoices_by_status() {

        client
                .get()
                .uri(getApiURI(V2_INVOICES, Map.of("pageIndex", "0", "pageSize", "10", "statuses", "TO_REVIEW,REJECTED")))
                .header("Api-Key", apiKey())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "invoices": [
                            {
                              "status": "TO_REVIEW",
                              "rewardCount": 3
                            },
                            {
                              "status": "REJECTED",
                              "rewardCount": 2
                            }
                          ]
                        }
                        """)
        ;
    }

    @Test
    @Order(7)
    void should_download_invoices() {
        final var invoiceId = companyBillingProfileToReviewInvoices.get(0);
        final var pdfData = faker.lorem().paragraph().getBytes();
        when(pdfStoragePort.download(eq(invoiceId + ".pdf"))).then(invocation -> new ByteArrayInputStream(pdfData));

        final var data = client.get()
                .uri(getApiURI(EXTERNAL_INVOICE.formatted(invoiceId), Map.of("token", "BO_TOKEN")))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody().returnResult().getResponseBody();

        assertThat(data).isEqualTo(pdfData);
    }

    @Test
    @Order(8)
    void should_reject_invoice_download_if_wrong_token() {
        final var invoiceId = companyBillingProfileToReviewInvoices.get(0);

        client.get()
                .uri(getApiURI(EXTERNAL_INVOICE.formatted(invoiceId), Map.of("token", "INVALID_TOKEN")))
                .exchange()
                // Then
                .expectStatus()
                .isUnauthorized();
    }
}
