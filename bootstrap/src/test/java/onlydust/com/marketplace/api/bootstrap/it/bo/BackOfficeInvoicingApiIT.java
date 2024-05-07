package onlydust.com.marketplace.api.bootstrap.it.bo;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.CompanyBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.IndividualBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.api.bootstrap.helper.AccountingHelper;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.VerificationStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.KybRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.KycRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.UserProfileInfoRepository;
import onlydust.com.marketplace.kernel.jobs.OutboxConsumerJob;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import onlydust.com.marketplace.project.domain.service.UserService;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.bootstrap.it.api.AbstractMarketplaceApiIT.ME_PUT_PAYOUT_PREFERENCES;
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
    AccountingHelper accountingHelper;
    @Autowired
    UserService userService;
    @Autowired
    InvoiceStoragePort invoiceStoragePort;
    @Autowired
    KycRepository kycRepository;
    @Autowired
    KybRepository kybRepository;
    @Autowired
    UserProfileInfoRepository userProfileInfoRepository;
    @Autowired
    OutboxConsumerJob accountingMailOutboxJob;

    private final Faker faker = new Faker();

    UserId antho;
    UserAuthHelper.AuthenticatedBackofficeUser camille;
    static CompanyBillingProfile companyBillingProfile;

    static final List<Invoice.Id> companyBillingProfileToReviewInvoices = new ArrayList<>();

    @BeforeEach
    void setupAll() {
        antho = UserId.of(userAuthHelper.authenticateAnthony().user().getId());
        camille = userAuthHelper.authenticateCamille();
    }

    void setUp() throws IOException {
        // Given
        companyBillingProfile = createCompanyBillingProfileFor(antho);

        updatePayoutPreferences(43467246L, companyBillingProfile.id(), UUID.fromString("298a547f-ecb6-4ab2-8975-68f4e9bf7b39"));

        // Given
        newCompanyInvoiceToReview(List.of(
                RewardId.of("6587511b-3791-47c6-8430-8f793606c63a")));
        newCompanyInvoiceToReview(List.of(
                RewardId.of("79209029-c488-4284-aa3f-bce8870d3a66"),
                RewardId.of("303f26b1-63f0-41f1-ab11-e70b54ef4a2a")));
        newCompanyInvoiceToReview(List.of(
                RewardId.of("0b275f04-bdb1-4d4f-8cd1-76fe135ccbdf"),
                RewardId.of("335e45a5-7f59-4519-8a12-1addc530214c"),
                RewardId.of("e9ebbe59-fb74-4a6c-9a51-6d9050412977")));
    }

    private CompanyBillingProfile createCompanyBillingProfileFor(UserId ownerId) {
        final var billingProfile = billingProfileService.createCompanyBillingProfile(ownerId, "Apple Inc.", null);

        billingProfileService.updatePayoutInfo(billingProfile.id(), ownerId,
                PayoutInfo.builder().ethWallet(new WalletLocator(new Name(ownerId + ".eth"))).build());
        accountingHelper.patchBillingProfile(billingProfile.id().value(), null, VerificationStatusEntity.VERIFIED);

        kybRepository.findByBillingProfileId(billingProfile.id().value())
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

        return billingProfile;
    }

    private void updatePayoutPreferences(final Long githubUserId, BillingProfile.Id billingProfileId, final UUID projectId) {
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.authenticateUser(githubUserId);
        client.put()
                .uri(getApiURI(ME_PUT_PAYOUT_PREFERENCES))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "billingProfileId": "%s",
                          "projectId": "%s"
                        }
                        """.formatted(isNull(billingProfileId) ? null : billingProfileId.value(), projectId))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }

    private void newCompanyInvoiceToReview(List<RewardId> rewardIds) throws IOException {
        final Invoice.Id invoiceId = billingProfileService.previewInvoice(antho, companyBillingProfile.id(), rewardIds).id();
        billingProfileService.uploadExternalInvoice(antho, companyBillingProfile.id(), invoiceId, "foo.pdf",
                new FileSystemResource(Objects.requireNonNull(getClass().getResource("/invoices/invoice-sample.pdf")).getFile()).getInputStream());
        companyBillingProfileToReviewInvoices.add(invoiceId);
    }

    private IndividualBillingProfile createIndividualBillingProfileFor(UserId ownerId, ProjectId projectId) {
        final var billingProfile = billingProfileService.createIndividualBillingProfile(ownerId, "My billing profile", Set.of(projectId));

        accountingHelper.patchBillingProfile(billingProfile.id().value(), null, VerificationStatusEntity.VERIFIED);

        billingProfileService.updatePayoutInfo(billingProfile.id(), ownerId, PayoutInfo.builder()
                .ethWallet(Ethereum.wallet("abuisset.eth"))
                .bankAccount(new BankAccount("BOUSFRPPXXX", "NL32INGB9127357228"))
                .build());

        kycRepository.findByBillingProfileId(billingProfile.id().value())
                .ifPresent(kyc -> kycRepository.save(kyc.toBuilder()
                        .firstName("John")
                        .country("FRA")
                        .address("My address")
                        .consideredUsPersonQuestionnaire(false)
                        .idDocumentCountryCode("FRA")
                        .usCitizen(false)
                        .verificationStatus(VerificationStatusEntity.VERIFIED)
                        .build()));

        return billingProfile;
    }

    @Test
    @Order(1)
    void should_list_invoices() throws IOException {
        setUp();
        final String jwt = userAuthHelper.authenticateBackofficeUser("pierre.oucif@gadz.org", List.of(BackofficeUser.Role.BO_READER)).jwt();

        // When
        client
                .get()
                .uri(getApiURI(INVOICES, Map.of(
                        "pageIndex", "0",
                        "pageSize", "10",
                        "invoiceIds", companyBillingProfileToReviewInvoices.stream().map(Invoice.Id::toString).collect(Collectors.joining(",")))))
                .header("Authorization", "Bearer " + camille.jwt())
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
                              "amount": 3636.000,
                              "currency": {
                                "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                "code": "USD",
                                "name": "US Dollar",
                                "logoUrl": null,
                                "decimals": 2
                              },
                              "rewardIds": [
                                "e9ebbe59-fb74-4a6c-9a51-6d9050412977",
                                "335e45a5-7f59-4519-8a12-1addc530214c",
                                "0b275f04-bdb1-4d4f-8cd1-76fe135ccbdf"
                              ]
                            },
                            {
                              "status": "PROCESSING",
                              "internalStatus": "TO_REVIEW",
                              "amount": 2424.000,
                              "currency": {
                                "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                "code": "USD",
                                "name": "US Dollar",
                                "logoUrl": null,
                                "decimals": 2
                              },
                              "rewardIds": [
                                "303f26b1-63f0-41f1-ab11-e70b54ef4a2a",
                                "79209029-c488-4284-aa3f-bce8870d3a66"
                              ]
                            },
                            {
                              "status": "PROCESSING",
                              "internalStatus": "TO_REVIEW",
                              "amount": 1212.000,
                              "currency": {
                                "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                "code": "USD",
                                "name": "US Dollar",
                                "logoUrl": null,
                                "decimals": 2
                              },
                              "rewardIds": [
                                "6587511b-3791-47c6-8430-8f793606c63a"
                              ]
                            }
                          ]
                        }
                        """)
        ;
    }

    @Test
    @Order(2)
    void should_list_invoices_v2() {

        // When
        client
                .get()
                .uri(getApiURI(V2_INVOICES, Map.of(
                        "pageIndex", "0",
                        "pageSize", "10",
                        "invoiceIds", companyBillingProfileToReviewInvoices.stream().map(Invoice.Id::toString).collect(Collectors.joining(",")))))
                .header("Authorization", "Bearer " + camille.jwt())
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
                                "subject": "Apple Inc.",
                                "type": "COMPANY"
                              },
                              "rewardCount": 3,
                              "totalUsdEquivalent": 3636.000,
                              "totalsPerCurrency": [
                                {
                                  "amount": 3000,
                                  "currency": {
                                    "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                    "code": "USDC",
                                    "name": "USD Coin",
                                    "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                    "decimals": 6
                                  },
                                  "dollarsEquivalent": 3030.00
                                }
                              ]
                            },
                            {
                              "status": "TO_REVIEW",
                              "billingProfile": {
                                "subject": "Apple Inc.",
                                "type": "COMPANY"
                              },
                              "rewardCount": 2,
                              "totalUsdEquivalent": 2424.000,
                              "totalsPerCurrency": [
                                {
                                  "amount": 2000,
                                  "currency": {
                                    "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                    "code": "USDC",
                                    "name": "USD Coin",
                                    "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                    "decimals": 6
                                  },
                                  "dollarsEquivalent": 2020.00
                                }
                              ]
                            },
                            {
                              "status": "TO_REVIEW",
                              "billingProfile": {
                                "subject": "Apple Inc.",
                                "type": "COMPANY"
                              },
                              "rewardCount": 1,
                              "totalUsdEquivalent": 1212.000,
                              "totalsPerCurrency": [
                                {
                                  "amount": 1000,
                                  "currency": {
                                    "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                    "code": "USDC",
                                    "name": "USD Coin",
                                    "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                    "decimals": 6
                                  },
                                  "dollarsEquivalent": 1010.00
                                }
                              ]
                            }
                          ]
                        }
                        """)
        ;
    }

    @Test
    @Order(2)
    void should_list_invoices_with_kyc_without_lastname() throws IOException {
        // Given
        final var ofux = UserId.of(userAuthHelper.authenticateOlivier().user().getId());
        final String jwt = userAuthHelper.authenticateBackofficeUser("pierre.oucif@gadz.org", List.of(BackofficeUser.Role.BO_READER)).jwt();
        final var individualBillingProfile = createIndividualBillingProfileFor(ofux, ProjectId.of("e41f44a2-464c-4c96-817f-81acb06b2523"));
        final Invoice.Id invoiceId = billingProfileService.previewInvoice(ofux, individualBillingProfile.id(), List.of(
                RewardId.of("5c668b61-e42c-4f0e-b31f-44c4e50dc2f4")
        )).id();
        billingProfileService.uploadGeneratedInvoice(ofux, individualBillingProfile.id(), invoiceId,
                new FileSystemResource(Objects.requireNonNull(getClass().getResource("/invoices/invoice-sample.pdf")).getFile()).getInputStream());

        // When
        client
                .get()
                .uri(getApiURI(V2_INVOICES, Map.of(
                        "pageIndex", "0",
                        "pageSize", "10",
                        "statuses", "APPROVED,TO_REVIEW")))
                .header("Authorization", "Bearer " + camille.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "totalItemNumber": 4
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
                .header("Authorization", "Bearer " + camille.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "number": "OD-APPLE-INC--001",
                          "status": "TO_REVIEW",
                          "billingProfile": {
                            "subject": "Apple Inc.",
                            "type": "COMPANY",
                            "verificationStatus": "VERIFIED",
                            "kyb": {
                              "name": "Apple Inc.",
                              "registrationNumber": "123456789",
                              "registrationDate": null,
                              "address": "1 Infinite Loop, Cupertino, CA 95014, United States",
                              "country": "France",
                              "countryCode": "FRA",
                              "usEntity": false,
                              "subjectToEuropeVAT": true,
                              "euVATNumber": "FR12345678901",
                              "sumsubUrl": null
                            },
                            "kyc": null
                          },
                          "rejectionReason": null,
                          "createdBy": {
                            "githubUserId": 43467246,
                            "githubLogin": "AnthonyBuisset",
                            "githubAvatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                            "email": "abuisset@gmail.com",
                            "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                            "name": "Anthony BUISSET"
                          },
                          "totalEquivalent": {
                            "amount": 1212.000,
                            "currency": {
                              "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                              "code": "USD",
                              "name": "US Dollar",
                              "logoUrl": null,
                              "decimals": 2
                            }
                          },
                          "rewards": [
                            {
                              "id": "6587511b-3791-47c6-8430-8f793606c63a",
                              "project": {
                                "name": "kaaper",
                                "logoUrl": null
                              },
                              "status": "PROCESSING",
                              "money": {
                                "amount": 1000,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "dollarsEquivalent": 1010.00,
                                "conversionRate": 1.0100000000000000
                              }
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
                .header("Authorization", "Bearer " + camille.jwt())
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
                .header("Authorization", "Bearer " + camille.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 4,
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
    void should_reject_invoices() throws InterruptedException {
        // Delete AnthonyBuisset user profile to check fallback of "createdBy.name" field in invoice details
        userProfileInfoRepository.deleteById(antho.value());

        final String rejectionReason = faker.rickAndMorty().character();
        client
                .put()
                .uri(getApiURI(PUT_INVOICES_STATUS.formatted(companyBillingProfileToReviewInvoices.get(1))))
                .header("Authorization", "Bearer " + camille.jwt())
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

        accountingMailOutboxJob.run();
        final Invoice invoice = invoiceStoragePort.get(companyBillingProfileToReviewInvoices.get(1)).orElseThrow();
        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(matchingJsonPath("$.transactional_message_id", equalTo(customerIOProperties.getInvoiceRejectedEmailId().toString())))
                        .withRequestBody(matchingJsonPath("$.identifiers.id", equalTo("747e663f-4e68-4b42-965b-b5aebedcd4c4")))
                        .withRequestBody(matchingJsonPath("$.message_data.username", equalTo("AnthonyBuisset")))
                        .withRequestBody(matchingJsonPath("$.message_data.rewardsNumber", equalTo(String.valueOf(invoice.rewards().size()))))
                        .withRequestBody(matchingJsonPath("$.message_data.invoiceName", equalTo(invoice.number().value())))
                        .withRequestBody(matchingJsonPath("$.message_data.totalUsdAmount", equalTo("2020.0")))
                        .withRequestBody(matchingJsonPath("$.message_data.reason", equalTo(rejectionReason)))
                        .withRequestBody(matchingJsonPath("$.message_data.rewardsDetails", containing("#303F2 - kaaper - USDC - 1000")))
                        .withRequestBody(matchingJsonPath("$.message_data.rewardsDetails", containing("#79209 - kaaper - USDC - 1000")))
                        .withRequestBody(matchingJsonPath("$.to", equalTo("abuisset@gmail.com")))
                        .withRequestBody(matchingJsonPath("$.from", equalTo(customerIOProperties.getOnlyDustAdminEmail())))
                        .withRequestBody(matchingJsonPath("$.subject", equalTo("An invoice for 2 reward(s) got rejected")))
        );

        client
                .get()
                .uri(getApiURI(INVOICE.formatted(companyBillingProfileToReviewInvoices.get(1))))
                .header("Authorization", "Bearer " + camille.jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.createdBy.name").isEqualTo("AnthonyBuisset")
                .jsonPath("$.rejectionReason").isEqualTo(rejectionReason);

        client
                .get()
                .uri(getApiURI(V2_INVOICES, Map.of("pageIndex", "0", "pageSize", "10")))
                .header("Authorization", "Bearer " + camille.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 4,
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
                .header("Authorization", "Bearer " + camille.jwt())
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
    @Order(6)
    void should_filter_invoices_by_currencies() {

        // When
        client
                .get()
                .uri(getApiURI(V2_INVOICES, Map.of(
                        "pageIndex", "0",
                        "pageSize", "10",
                        "currencies", "562bbf65-8a71-4d30-ad63-520c0d68ba27")))
                .header("Authorization", "Bearer " + camille.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "invoices": [
                            {
                              "totalsPerCurrency": [
                                {
                                  "currency": { "code": "USDC" }
                                }
                              ]
                            },
                            {
                              "totalsPerCurrency": [
                                {
                                  "currency": { "code": "USDC" }
                                }
                              ]
                            },
                            {
                              "totalsPerCurrency": [
                                {
                                  "currency": { "code": "USDC" }
                                }
                              ]
                            }
                          ]
                        }
                        """)
        ;
    }

    @Test
    @Order(6)
    void should_filter_invoices_by_billing_profile_type() {

        // When
        client
                .get()
                .uri(getApiURI(V2_INVOICES, Map.of(
                        "pageIndex", "0",
                        "pageSize", "10",
                        "billingProfileTypes", "COMPANY,SELF_EMPLOYED")))
                .header("Authorization", "Bearer " + camille.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "invoices": [
                            {
                              "billingProfile": {
                                "type": "COMPANY"
                              }
                            },
                            {
                              "billingProfile": {
                                "type": "COMPANY"
                              }
                            },
                            {
                              "billingProfile": {
                                "type": "COMPANY"
                              }
                            }
                          ]
                        }
                        """)
        ;
    }

    @Test
    @Order(7)
    void should_filter_invoices_by_billing_profile_id() {

        // When
        client
                .get()
                .uri(getApiURI(V2_INVOICES, Map.of(
                        "pageIndex", "0",
                        "pageSize", "10",
                        "billingProfiles", companyBillingProfile.id().toString())))
                .header("Authorization", "Bearer " + camille.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.invoices[?(@.billingProfile.id == '%s')]".formatted(companyBillingProfile.id())).isArray()
                .jsonPath("$.invoices[?(@.billingProfile.id != '%s')]".formatted(companyBillingProfile.id())).doesNotExist()
        ;
    }

    @Test
    @Order(8)
    void should_search_invoices() {

        // When
        client
                .get()
                .uri(getApiURI(V2_INVOICES, Map.of(
                        "pageIndex", "0",
                        "pageSize", "10",
                        "search", "apple")))
                .header("Authorization", "Bearer " + camille.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "invoices": [
                            {
                              "billingProfile": {
                                "subject": "Apple Inc."
                              }
                            },
                            {
                              "billingProfile": {
                                "subject": "Apple Inc."
                              }
                            },
                            {
                              "billingProfile": {
                                "subject": "Apple Inc."
                              }
                            }
                          ]
                        }
                        """)
        ;
    }

    @Test
    @Order(20)
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
    @Order(21)
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
