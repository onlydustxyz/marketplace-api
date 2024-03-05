package onlydust.com.marketplace.api.bootstrap.it.bo;

import com.github.javafaker.Faker;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.PostgresOldBillingProfileAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceRewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.InvoiceRewardRepository;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.service.UserService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeInvoicingApiIT extends AbstractMarketplaceBackOfficeApiIT {
    @Autowired
    private InvoiceRewardRepository invoiceRewardRepository;
    @Autowired
    PdfStoragePort pdfStoragePort;
    @Autowired
    BillingProfileService billingProfileService;
    @Autowired
    UserService userService;
    @Autowired
    PostgresOldBillingProfileAdapter postgresOldBillingProfileAdapter;
    private final Faker faker = new Faker();

    UserId userId;
    OldCompanyBillingProfile companyBillingProfile;
    BillingProfile.Id companyBillingProfileId;
    OldIndividualBillingProfile individualBillingProfile;
    BillingProfile.Id individualBillingProfileId;

    static final List<Invoice.Id> companyBillingProfileToReviewInvoices = new ArrayList<>();

    void setUp() throws IOException {
        // Given
        final UserAuthHelper.AuthenticatedUser olivier = userAuthHelper.authenticateOlivier();
        userId = UserId.of(olivier.user().getId());

        companyBillingProfile = userService.getCompanyBillingProfile(userId.value());
        postgresOldBillingProfileAdapter.saveCompanyProfile(companyBillingProfile.toBuilder()
                .name("Mr. Needful")
                .address(faker.address().fullAddress())
                .euVATNumber("111")
                .subjectToEuropeVAT(false)
                .oldCountry(OldCountry.fromIso3("FRA"))
                .usEntity(false)
                .status(OldVerificationStatus.VERIFIED)
                .build()
        );

        companyBillingProfileId = BillingProfile.Id.of(companyBillingProfile.getId());

        individualBillingProfile = userService.getIndividualBillingProfile(userId.value());
        postgresOldBillingProfileAdapter.saveIndividualProfile(individualBillingProfile.toBuilder()
                .firstName("Olivier")
                .lastName("Fu")
                .address(faker.address().fullAddress())
                .oldCountry(OldCountry.fromIso3("FRA"))
                .usCitizen(false)
                .idDocumentType(OldIndividualBillingProfile.OldIdDocumentTypeEnum.PASSPORT)
                .idDocumentNumber(faker.idNumber().valid())
                .idDocumentCountryCode("FRA")
                .validUntil(Date.from(ZonedDateTime.now().plusYears(10).toInstant()))
                .status(OldVerificationStatus.VERIFIED)
                .build()
        );
        individualBillingProfileId = BillingProfile.Id.of(individualBillingProfile.getId());

        // Select COMPANY as active billing profile
        userService.updateBillingProfileType(userId.value(), OldBillingProfileType.COMPANY);


        // Given
        newCompanyToReviewInvoice(List.of(
                RewardId.of("061e2c7e-bda4-49a8-9914-2e76926f70c2")));
        newCompanyToReviewInvoice(List.of(
                RewardId.of("ee28315c-7a84-4052-9308-c2236eeafda1"),
                RewardId.of("d067b24d-115a-45e9-92de-94dd1d01b184")));
        newCompanyToReviewInvoice(List.of(
                RewardId.of("d506a05d-3739-452f-928d-45ea81d33079"),
                RewardId.of("5083ac1f-4325-4d47-9760-cbc9ab82f25c"),
                RewardId.of("e6ee79ae-b3f0-4f4e-b7e3-9e643bc27236")));
    }

    private void newCompanyToReviewInvoice(List<RewardId> rewardIds) throws IOException {
        final Invoice.Id invoiceId = billingProfileService.previewInvoice(userId, companyBillingProfileId, rewardIds).id();
        billingProfileService.uploadExternalInvoice(userId, companyBillingProfileId, invoiceId, "foo.pdf",
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
                              "amount": 4765.00,
                              "currencyId": "f35155b5-6107-4677-85ac-23f8c2a63193",
                              "rewardIds": [
                                "d506a05d-3739-452f-928d-45ea81d33079",
                                "5083ac1f-4325-4d47-9760-cbc9ab82f25c",
                                "e6ee79ae-b3f0-4f4e-b7e3-9e643bc27236"
                              ]
                            },
                            {
                              "status": "PROCESSING",
                              "internalStatus": "TO_REVIEW",
                              "amount": 2777.50,
                              "currencyId": "f35155b5-6107-4677-85ac-23f8c2a63193",
                              "rewardIds": [
                                "d067b24d-115a-45e9-92de-94dd1d01b184",
                                "ee28315c-7a84-4052-9308-c2236eeafda1"
                              ]
                            },
                            {
                              "status": "PROCESSING",
                              "internalStatus": "TO_REVIEW",
                              "amount": 1010.00,
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
                                "name": "Mr. Needful",
                                "type": "COMPANY",
                                "admins": null
                              },
                              "rewardCount": 3,
                              "totalEquivalent": {
                                "amount": 4765.00,
                                "dollarsEquivalent": 4765.00,
                                "conversionRate": null,
                                "currencyCode": "USD",
                                "currencyName": "US Dollar",
                                "currencyLogoUrl": null
                              },
                              "totalPerCurrency": [
                                {
                                  "amount": 3250,
                                  "dollarsEquivalent": 3250,
                                  "conversionRate": null,
                                  "currencyCode": "USD",
                                  "currencyName": "US Dollar",
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
                                  "amount": 500,
                                  "dollarsEquivalent": 505.00,
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
                                "name": "Mr. Needful",
                                "type": "COMPANY",
                                "admins": null
                              },
                              "rewardCount": 2,
                              "totalEquivalent": {
                                "amount": 2777.50,
                                "dollarsEquivalent": 2777.50,
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
                                "name": "Mr. Needful",
                                "type": "COMPANY",
                                "admins": null
                              },
                              "rewardCount": 1,
                              "totalEquivalent": {
                                "amount": 1010.00,
                                "dollarsEquivalent": 1010.00,
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
                          "status": "TO_REVIEW",
                          "billingProfile": {
                            "name": "Mr. Needful",
                            "type": "COMPANY",
                            "admins": []
                          },
                          "totalEquivalent": {
                            "amount": 1010.00,
                            "currencyCode": "USD",
                            "currencyName": "US Dollar",
                            "currencyLogoUrl": null
                          },
                          "rewardsPerNetwork": [
                            {
                              "network": "ETHEREUM",
                              "billingAccountNumber": null,
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
                                  "transactionHash": "0x0000000000000000000000000000000000000000000000000000000000000000"
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
                              "rewardCount": 3,
                              "totalEquivalent": {
                                "amount": 4765.00,
                                "dollarsEquivalent": 4765.00,
                                "conversionRate": null,
                                "currencyCode": "USD",
                                "currencyName": "US Dollar",
                                "currencyLogoUrl": null
                              }
                            },
                            {
                              "status": "TO_REVIEW",
                              "rewardCount": 2,
                              "totalEquivalent": {
                                "amount": 2777.50,
                                "dollarsEquivalent": 2777.50,
                                "conversionRate": null,
                                "currencyCode": "USD",
                                "currencyName": "US Dollar",
                                "currencyLogoUrl": null
                              }
                            },
                            {
                              "status": "APPROVED",
                              "rewardCount": 1,
                              "totalEquivalent": {
                                "amount": 1010.00,
                                "dollarsEquivalent": 1010.00,
                                "conversionRate": null,
                                "currencyCode": "USD",
                                "currencyName": "US Dollar",
                                "currencyLogoUrl": null
                              }
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
                              "rewardCount": 3,
                              "totalEquivalent": {
                                "amount": 4765.00,
                                "dollarsEquivalent": 4765.00,
                                "conversionRate": null,
                                "currencyCode": "USD",
                                "currencyName": "US Dollar",
                                "currencyLogoUrl": null
                              }
                            },
                            {
                              "status": "REJECTED",
                              "rewardCount": 2,
                              "totalEquivalent": {
                                "amount": 2777.50,
                                "dollarsEquivalent": 2777.50,
                                "conversionRate": null,
                                "currencyCode": "USD",
                                "currencyName": "US Dollar",
                                "currencyLogoUrl": null
                              }
                            },
                            {
                              "status": "APPROVED",
                              "rewardCount": 1,
                              "totalEquivalent": {
                                "amount": 1010.00,
                                "dollarsEquivalent": 1010.00,
                                "conversionRate": null,
                                "currencyCode": "USD",
                                "currencyName": "US Dollar",
                                "currencyLogoUrl": null
                              }
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
                              "rewardCount": 3,
                              "totalEquivalent": {
                                "amount": 4765.00,
                                "dollarsEquivalent": 4765.00,
                                "conversionRate": null,
                                "currencyCode": "USD",
                                "currencyName": "US Dollar",
                                "currencyLogoUrl": null
                              }
                            },
                            {
                              "status": "REJECTED",
                              "rewardCount": 2,
                              "totalEquivalent": {
                                "amount": 2777.50,
                                "dollarsEquivalent": 2777.50,
                                "conversionRate": null,
                                "currencyCode": "USD",
                                "currencyName": "US Dollar",
                                "currencyLogoUrl": null
                              }
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

    @SneakyThrows
    @Transactional
    InvoiceEntity fakeInvoice(UUID id, List<UUID> rewardIds) {
        final var firstName = faker.name().firstName();
        final var lastName = faker.name().lastName();

        final var rewards = invoiceRewardRepository.findAll(rewardIds);

        return new InvoiceEntity(
                id,
                UUID.randomUUID(),
                Invoice.Number.of(12, lastName, firstName).toString(),
                ZonedDateTime.now().minusDays(1),
                InvoiceEntity.Status.TO_REVIEW,
                rewards.stream().map(InvoiceRewardEntity::baseAmount).reduce(BigDecimal.ZERO, BigDecimal::add),
                rewards.get(0).targetCurrency(),
                new URL("https://s3.storage.com/invoice.pdf"),
                null,
                new InvoiceEntity.Data(
                        ZonedDateTime.now().plusDays(9),
                        BigDecimal.ZERO,
                        new Invoice.PersonalInfo(
                                firstName,
                                lastName,
                                faker.address().fullAddress(),
                                faker.address().countryCode()
                        ),
                        null,
                        null,
                        List.of(new Invoice.Wallet(Network.ETHEREUM, "vitalik.eth")),
                        rewards
                ), null
        );
    }
}
