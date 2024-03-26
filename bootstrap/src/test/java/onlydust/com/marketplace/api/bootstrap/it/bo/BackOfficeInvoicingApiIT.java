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
import onlydust.com.marketplace.api.bootstrap.helper.AccountingHelper;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.VerificationStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.KybRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.KycRepository;
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
    AccountingHelper accountingHelper;
    @Autowired
    UserService userService;
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
        accountingHelper.patchBillingProfile(companyBillingProfile.id().value(), null, VerificationStatusEntity.VERIFIED);

        selfEmployedBillingProfile = billingProfileService.createSelfEmployedBillingProfile(userId, "Olivier SASU", null);
        billingProfileService.updatePayoutInfo(selfEmployedBillingProfile.id(), userId,
                PayoutInfo.builder().ethWallet(new WalletLocator(new Name(userId + ".eth"))).build());
        accountingHelper.patchBillingProfile(selfEmployedBillingProfile.id().value(), null, VerificationStatusEntity.VERIFIED);

        individualBillingProfile = billingProfileService.createIndividualBillingProfile(userId, "Olivier", null);
        billingProfileService.updatePayoutInfo(individualBillingProfile.id(), userId,
                PayoutInfo.builder().ethWallet(new WalletLocator(new Name(userId + ".eth"))).build());
        accountingHelper.patchBillingProfile(individualBillingProfile.id().value(), null, VerificationStatusEntity.VERIFIED);

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
                        .birthdate(faker.date().birthday())
                        .usCitizen(false)
                        .verificationStatus(VerificationStatusEntity.VERIFIED).build()));

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
                                "kyc": null,
                                "admins": null
                              },
                              "rewardCount": 3,
                              "totalUsdEquivalent": 3636.000,
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
                                },
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
                                },
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
                            },
                            {
                              "status": "TO_REVIEW",
                              "billingProfile": {
                                "name": "Apple Inc.",
                                "type": "COMPANY",
                                "verificationStatus": null,
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
                                "kyc": null,
                                "admins": null
                              },
                              "rewardCount": 2,
                              "totalUsdEquivalent": 2424.000,
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
                                },
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
                            },
                            {
                              "status": "TO_REVIEW",
                              "billingProfile": {
                                "name": "Apple Inc.",
                                "type": "COMPANY",
                                "verificationStatus": null,
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
                                "kyc": null,
                                "admins": null
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
                          "number": "OD-APPLE-INC--001",
                          "status": "TO_REVIEW",
                          "billingProfile": {
                            "name": "Apple Inc.",
                            "type": "COMPANY",
                            "verificationStatus": null,
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
                            "kyc": null,
                            "admins": null
                          },
                          "rejectionReason": null,
                          "createdBy": {
                            "githubUserId": 595505,
                            "githubLogin": "ofux",
                            "githubAvatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4",
                            "email": "olivier.fuxet@gmail.com",
                            "id": "e461c019-ba23-4671-9b6c-3a5a18748af9",
                            "name": "Olivier Fuxet"
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
                          "rewardsPerNetwork": [
                            {
                              "network": "ETHEREUM",
                              "billingAccountNumber": "e461c019-ba23-4671-9b6c-3a5a18748af9.eth",
                              "totalUsdEquivalent": 1010.00,
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
                              ],
                              "rewards": [
                                {
                                  "id": "6587511b-3791-47c6-8430-8f793606c63a",
                                  "paymentId": null,
                                  "requestedAt": "2023-09-20T08:01:47.616674Z",
                                  "processedAt": null,
                                  "githubUrls": [
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1026",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1037",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1038",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1039",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1040",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1041",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1042",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1043",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1044",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1045",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1048",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1049",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1052",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1053",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1054",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1056",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1059",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1063",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1064",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1065",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1067",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1068",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1070",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1071",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1073",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1075",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1076",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1077",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1079",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1080",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1081",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1082",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1084",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1085",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1087",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1088",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1090",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1091",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1100",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1103",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1104",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1105",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1107",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1108",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1112",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1113",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1114",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1115",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1117",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1118",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1121",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1122",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1124",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1129",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1131",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1132",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1133",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1137",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1143",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1148",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1150",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1151",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1152",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1160",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1161",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1162",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1163",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1164",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1165",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1167",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1168",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1169",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1172",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1174",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1175",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1204",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1212",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1217",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1235",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1237",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1239",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1240",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1241",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/1247",
                                    "https://github.com/onlydustxyz/marketplace-frontend/pull/62"
                                  ],
                                  "project": {
                                    "name": "kaaper",
                                    "logoUrl": null
                                  },
                                  "sponsors": [
                                    {
                                      "name": "No Sponsor",
                                      "avatarUrl": "https://app.onlydust.com/_next/static/media/onlydust-logo.68e14357.webp"
                                    }
                                  ],
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
                        .withRequestBody(matchingJsonPath("$.totalUsdAmount", equalTo("2020.0")))
                        .withRequestBody(
                                matchingJsonPath("$.rejectionReason", equalTo(rejectionReason))
                        )
                        .withRequestBody(matchingJsonPath("$.rewardNames", containing("#303F2 - kaaper - USDC - 1000")))
                        .withRequestBody(matchingJsonPath("$.rewardNames", containing("#79209 - kaaper - USDC - 1000")))
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
