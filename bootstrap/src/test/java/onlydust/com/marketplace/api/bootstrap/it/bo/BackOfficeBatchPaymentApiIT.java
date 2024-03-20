package onlydust.com.marketplace.api.bootstrap.it.bo;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.BatchPayment;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.CompanyBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.accounting.domain.view.ShortBillingProfileView;
import onlydust.com.marketplace.api.bootstrap.helper.AccountingHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.VerificationStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.BillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.KybRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.KycRepository;
import onlydust.com.marketplace.api.webhook.Config;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;

import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeBatchPaymentApiIT extends AbstractMarketplaceBackOfficeApiIT {

    @Autowired
    BillingProfileService billingProfileService;
    @Autowired
    BillingProfileRepository billingProfileRepository;
    @Autowired
    AccountingHelper accountingHelper;
    @Autowired
    Config webhookHttpClientProperties;
    @Autowired
    KycRepository kycRepository;
    @Autowired
    KybRepository kybRepository;
    @Autowired
    EntityManagerFactory entityManagerFactory;
    private final Faker faker = new Faker();

    UserId anthony;
    UserId olivier;
    CompanyBillingProfile olivierBillingProfile;
    ShortBillingProfileView anthonyBillingProfile;

    static final List<Invoice.Id> anthonyInvoiceIds = new ArrayList<>();
    static final List<Invoice.Id> olivierInvoiceIds = new ArrayList<>();
    static BatchPayment.Id sepaBatchPaymentId;
    static BatchPayment.Id ethBatchPaymentId;

    void setUp() throws IOException {
        // Given
        this.anthony = UserId.of(userAuthHelper.authenticateAnthony().user().getId());
        this.olivier = UserId.of(userAuthHelper.authenticateOlivier().user().getId());

        olivierBillingProfile = billingProfileService.createCompanyBillingProfile(this.olivier, "Olive Company", null);
        billingProfileService.updatePayoutInfo(olivierBillingProfile.id(), this.olivier,
                PayoutInfo.builder().ethWallet(new WalletLocator(new Name(this.olivier + ".eth")))
                        .bankAccount(new BankAccount("BIC", "FR76000111222333334444")).build());
        accountingHelper.patchBillingProfile(olivierBillingProfile.id().value(), null, VerificationStatusEntity.VERIFIED);

        anthonyBillingProfile = billingProfileService.getBillingProfilesForUser(this.anthony).get(0);
        billingProfileService.updatePayoutInfo(anthonyBillingProfile.getId(), this.anthony,
                PayoutInfo.builder().ethWallet(new WalletLocator(new Name(this.anthony + ".eth"))).build());
        accountingHelper.patchBillingProfile(anthonyBillingProfile.getId().value(), null, VerificationStatusEntity.VERIFIED);

        kybRepository.findByBillingProfileId(olivierBillingProfile.id().value())
                .map(kyb -> kybRepository.saveAndFlush(kyb.toBuilder()
                        .country("FRA")
                        .address("1 Infinite Loop, Cupertino, CA 95014, United States")
                        .euVATNumber("FR12345678901")
                        .name("Olivier Inc.")
                        .registrationDate(faker.date().birthday())
                        .registrationNumber("123456789")
                        .usEntity(false)
                        .subjectToEuVAT(true)
                        .verificationStatus(VerificationStatusEntity.VERIFIED).build()))
                .orElseThrow();
        kycRepository.findByBillingProfileId(anthonyBillingProfile.getId().value())
                .map(kyb -> kycRepository.saveAndFlush(kyb.toBuilder()
                        .country("FRA")
                        .address("2 Infinite Loop, Cupertino, CA 95014, United States")
                        .firstName("Antho")
                        .lastName("Arbuste")
                        .usCitizen(false)
                        .verificationStatus(VerificationStatusEntity.VERIFIED).build()))
                .orElseThrow();

        // Given
        newOlivierInvoiceToReview(List.of(
                RewardId.of("4ac9d6ac-f2ca-43d5-901a-ac7f5b149d72"),
                RewardId.of("cdea7e15-a757-4aa1-a209-a0a535e9af94")));

        newAnthonyInvoiceToReview(List.of(
                RewardId.of("d22f75ab-d9f5-4dc6-9a85-60dcd7452028")));

        // Already paid invoice
        newOlivierInvoiceToReview(List.of(
                RewardId.of("061e2c7e-bda4-49a8-9914-2e76926f70c2")));
    }

    private void newOlivierInvoiceToReview(List<RewardId> rewardIds) throws IOException {
        final Invoice.Id invoiceId = billingProfileService.previewInvoice(olivier, olivierBillingProfile.id(), rewardIds).id();
        billingProfileService.uploadExternalInvoice(olivier, olivierBillingProfile.id(), invoiceId, "foo.pdf",
                new FileSystemResource(Objects.requireNonNull(getClass().getResource("/invoices/invoice-sample.pdf")).getFile()).getInputStream());
        olivierInvoiceIds.add(invoiceId);
    }

    private void newAnthonyInvoiceToReview(List<RewardId> rewardIds) throws IOException {
        final Invoice.Id invoiceId = billingProfileService.previewInvoice(anthony, anthonyBillingProfile.getId(), rewardIds).id();
        billingProfileService.uploadGeneratedInvoice(anthony, anthonyBillingProfile.getId(), invoiceId,
                new FileSystemResource(Objects.requireNonNull(getClass().getResource("/invoices/invoice-sample.pdf")).getFile()).getInputStream());
        anthonyInvoiceIds.add(invoiceId);
    }

    @Test
    @Order(1)
    void should_not_create_any_batch_payments_given_list_of_invoice_ids_already_paid() throws IOException {
        setUp();

        // When
        client.post()
                .uri(getApiURI(POST_REWARDS_BATCH_PAYMENTS))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                            {
                            "invoiceIds": ["%s"]
                            }
                        """.formatted(
                        olivierInvoiceIds.get(1)))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "batchPayments": []
                        }""");
    }

    @Test
    @Order(2)
    void should_create_batch_payments_given_list_of_invoice_ids() {

        // When
        final var csv1 = new MutableObject<String>();
        final var network1 = new MutableObject<String>();
        final var batchPaymentId1 = new MutableObject<String>();
        final var csv2 = new MutableObject<String>();
        final var network2 = new MutableObject<String>();
        final var batchPaymentId2 = new MutableObject<String>();
        client.post()
                .uri(getApiURI(POST_REWARDS_BATCH_PAYMENTS))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                            {
                            "invoiceIds": ["%s","%s"]
                            }
                        """.formatted(
                        olivierInvoiceIds.get(0),
                        anthonyInvoiceIds.get(0)))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.batchPayments.length()").isEqualTo(2)
                .jsonPath("$.batchPayments[0].id").value(batchPaymentId1::setValue)
                .jsonPath("$.batchPayments[0].csv").value(csv1::setValue)
                .jsonPath("$.batchPayments[0].network").value(network1::setValue)
                .jsonPath("$.batchPayments[1].id").value(batchPaymentId2::setValue)
                .jsonPath("$.batchPayments[1].csv").value(csv2::setValue)
                .jsonPath("$.batchPayments[1].network").value(network2::setValue)
                .json("""
                        {
                          "batchPayments": [
                            {
                              "network": "SEPA",
                              "rewardCount": 1,
                              "totalUsdEquivalent": 1000,
                              "totalsPerCurrency": [
                                {
                                  "amount": 1000,
                                  "currency": {
                                    "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                    "code": "USD",
                                    "name": "US Dollar",
                                    "logoUrl": null,
                                    "decimals": 2
                                  },
                                  "dollarsEquivalent": 1000
                                }
                              ]
                            },
                            {
                              "network": "ETHEREUM",
                              "rewardCount": 2,
                              "totalUsdEquivalent": 3030.00,
                              "totalsPerCurrency": [
                                {
                                  "amount": 3000,
                                  "currency": {
                                    "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                    "code": "USDC",
                                    "name": "USD Coin",
                                    "logoUrl": null,
                                    "decimals": 6
                                  },
                                  "dollarsEquivalent": 3030.00
                                }
                              ]
                            }
                          ]
                        }
                        """);
        final var ethCSV = network1.getValue().equals("ETHEREUM") ? csv1 : csv2;
        final var sepaCSV = network1.getValue().equals("SEPA") ? csv1 : csv2;
        assertThat(sepaCSV.getValue()).isEqualToIgnoringWhitespace("iso4217,,FR76000111222333334444,1000");
        assertThat(ethCSV.getValue()).isEqualToIgnoringWhitespace("""
                erc20,0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48,747e663f-4e68-4b42-965b-b5aebedcd4c4.eth,1000
                erc20,0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48,e461c019-ba23-4671-9b6c-3a5a18748af9.eth,2000
                """);
        ethBatchPaymentId = BatchPayment.Id.of(network1.getValue().equals("ETHEREUM") ? batchPaymentId1.getValue() : batchPaymentId2.getValue());
        sepaBatchPaymentId = BatchPayment.Id.of(network1.getValue().equals("SEPA") ? batchPaymentId1.getValue() : batchPaymentId2.getValue());
    }

    @Test
    @Order(3)
    void should_get_batch_payment() {
        final var csv = new MutableObject<String>();
        client.get()
                .uri(getApiURI(GET_REWARDS_BATCH_PAYMENTS_BY_ID.formatted(ethBatchPaymentId)))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(ethBatchPaymentId.toString())
                .jsonPath("$.createdAt").isNotEmpty()
                .jsonPath("$.csv").value(csv::setValue)
                .json("""
                        {
                          "network": "ETHEREUM",
                          "status": "TO_PAY",
                          "rewardCount": 2,
                          "totalUsdEquivalent": 3030.00,
                          "totalsPerCurrency": [
                            {
                              "amount": 3000,
                              "currency": {
                                "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                "code": "USDC",
                                "name": "USD Coin",
                                "logoUrl": null,
                                "decimals": 6
                              },
                              "dollarsEquivalent": 3030.00
                            }
                          ],
                          "rewards": [
                            {
                              "id": "4ac9d6ac-f2ca-43d5-901a-ac7f5b149d72",
                              "billingProfile": {
                                "name": "Olivier Inc.",
                                "type": "COMPANY",
                                "verificationStatus": "VERIFIED",
                                "admins": []
                              },
                              "requestedAt": "2022-12-23T12:43:50.356166Z",
                              "processedAt": null,
                              "githubUrls": [
                                "https://github.com/onlydustxyz/marketplace-frontend/pull/832"
                              ],
                              "project": {
                                "name": "Cairo foundry",
                                "logoUrl": null
                              },
                              "sponsors": [
                                {
                                  "name": "No Sponsor",
                                  "avatarUrl": "https://app.onlydust.com/_next/static/media/onlydust-logo.68e14357.webp"
                                }
                              ],
                              "money": {
                                "amount": 2000,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": null,
                                  "decimals": 6
                                },
                                "dollarsEquivalent": 2020.00,
                                "conversionRate": 1.0100000000000000
                              }
                            },
                            {
                              "id": "d22f75ab-d9f5-4dc6-9a85-60dcd7452028",
                              "billingProfile": {
                                "name": "Antho Arbuste",
                                "type": "INDIVIDUAL",
                                "verificationStatus": "VERIFIED",
                                "admins": [
                                  {
                                    "login": "AnthonyBuisset",
                                    "name": "Antho Arbuste",
                                    "email": "abuisset@gmail.com",
                                    "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                                  }
                                ]
                              },
                              "requestedAt": "2023-09-20T07:59:16.657487Z",
                              "processedAt": null,
                              "githubUrls": [
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
                                  "logoUrl": null,
                                  "decimals": 6
                                },
                                "dollarsEquivalent": 1010.00,
                                "conversionRate": 1.0100000000000000
                              }
                            }
                          ],
                          "transactionHash": null
                        }
                        """);

        assertThat(csv.getValue()).isEqualToIgnoringWhitespace("""
                erc20,0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48,747e663f-4e68-4b42-965b-b5aebedcd4c4.eth,1000
                erc20,0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48,e461c019-ba23-4671-9b6c-3a5a18748af9.eth,2000
                """);
    }

    @Test
    @Order(4)
    void should_get_batch_payments() {
        client.get()
                .uri(getApiURI(GET_REWARDS_BATCH_PAYMENTS, Map.of("pageIndex", "0", "pageSize", "10")))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "batchPayments": [
                            {
                              "status": "TO_PAY",
                              "network": "SEPA",
                              "rewardCount": 1,
                              "totalUsdEquivalent": 1000,
                              "totalsPerCurrency": [
                                {
                                  "amount": 1000,
                                  "currency": {
                                    "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                    "code": "USD",
                                    "name": "US Dollar",
                                    "logoUrl": null,
                                    "decimals": 2
                                  },
                                  "dollarsEquivalent": 1000
                                }
                              ]
                            },
                            {
                              "status": "TO_PAY",
                              "network": "ETHEREUM",
                              "rewardCount": 2,
                              "totalUsdEquivalent": 3030.00,
                              "totalsPerCurrency": [
                                {
                                  "amount": 3000,
                                  "currency": {
                                    "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                    "code": "USDC",
                                    "name": "USD Coin",
                                    "logoUrl": null,
                                    "decimals": 6
                                  },
                                  "dollarsEquivalent": 3030.00
                                }
                              ]
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(5)
    void should_fail_to_mark_batch_payment_as_paid_when_transaction_reference_is_invalid() {
        client.put()
                .uri(getApiURI(PUT_REWARDS_BATCH_PAYMENTS.formatted(ethBatchPaymentId)))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "transactionHash": "0xfoobar"
                        }
                        """)
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    @Order(6)
    void should_mark_batch_payment_as_paid() {
        client.put()
                .uri(getApiURI(PUT_REWARDS_BATCH_PAYMENTS.formatted(ethBatchPaymentId)))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "transactionHash": "0x313d09b7aa7d113ebd99cd58a59741d9e547813989d94ece7725b841a776b47e"
                        }
                        """)
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        client.get()
                .uri(getApiURI(GET_REWARDS_BATCH_PAYMENTS_BY_ID.formatted(ethBatchPaymentId)))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(ethBatchPaymentId.toString())
                .json("""
                        {
                          "network": "ETHEREUM",
                          "status": "PAID",
                          "rewardCount": 2,
                          "totalUsdEquivalent": 3030.00,
                          "transactionHash": "0x313d09b7aa7d113ebd99cd58a59741d9e547813989d94ece7725b841a776b47e"
                        }
                        """);

        client.get()
                .uri(getApiURI(GET_REWARDS_BATCH_PAYMENTS, Map.of("pageIndex", "0", "pageSize", "10", "statuses", "PAID")))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 1,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "batchPayments": [
                            {
                              "status": "PAID",
                              "network": "ETHEREUM",
                              "rewardCount": 2,
                              "totalUsdEquivalent": 3030.00,
                              "totalsPerCurrency": [
                                {
                                  "amount": 3000,
                                  "currency": {
                                    "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                    "code": "USDC",
                                    "name": "USD Coin",
                                    "logoUrl": null,
                                    "decimals": 6
                                  },
                                  "dollarsEquivalent": 3030.00
                                }
                              ]
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(7)
    void should_fail_to_mark_batch_payment_as_paid_when_it_is_already_paid() {
        client.put()
                .uri(getApiURI(PUT_REWARDS_BATCH_PAYMENTS.formatted(ethBatchPaymentId)))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "transactionHash": "0x313d09b7aa7d113ebd99cd58a59741d9e547813989d94ece7725b841a776b47e"
                        }
                        """)
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

}
