package onlydust.com.marketplace.api.it.bo;

import com.github.javafaker.Faker;
import jakarta.persistence.EntityManagerFactory;
import onlydust.com.backoffice.api.contract.model.PayRewardRequest;
import onlydust.com.backoffice.api.contract.model.PostBatchPaymentRequest;
import onlydust.com.backoffice.api.contract.model.TransactionNetwork;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.api.helper.AccountingHelper;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.repository.BillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.KybRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.KycRepository;
import onlydust.com.marketplace.api.read.entities.billing_profile.BillingProfileReadEntity;
import onlydust.com.marketplace.api.read.repositories.BillingProfileReadRepository;
import onlydust.com.marketplace.api.rest.api.adapter.BackofficeAccountingManagementRestApi;
import onlydust.com.marketplace.api.suites.tags.TagBO;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.*;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.it.api.AbstractMarketplaceApiIT.ME_PUT_PAYOUT_PREFERENCES;
import static org.assertj.core.api.Assertions.assertThat;

@TagBO
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeBatchPaymentApiIT extends AbstractMarketplaceBackOfficeApiIT {
    static final List<Invoice.Id> anthonyInvoiceIds = new ArrayList<>();
    static final List<Invoice.Id> olivierInvoiceIds = new ArrayList<>();
    static final List<Invoice.Id> pierreInvoiceIds = new ArrayList<>();
    static Payment.Id sepaBatchPaymentId;
    static Payment.Id ethBatchPaymentId;
    private final Faker faker = new Faker();
    @Autowired
    BillingProfileService billingProfileService;
    @Autowired
    BillingProfileRepository billingProfileRepository;
    @Autowired
    BillingProfileReadRepository billingProfileReadRepository;
    @Autowired
    AccountingHelper accountingHelper;
    @Autowired
    KycRepository kycRepository;
    @Autowired
    KybRepository kybRepository;
    @Autowired
    EntityManagerFactory entityManagerFactory;
    @Autowired
    BackofficeAccountingManagementRestApi backofficeAccountingManagementRestApi;
    UserAuthHelper.AuthenticatedBackofficeUser camille;
    UserId anthony;
    UserId olivier;
    UserId pierre;
    CompanyBillingProfile olivierBillingProfile;
    BillingProfileReadEntity anthonyBillingProfile;
    SelfEmployedBillingProfile pierreBillingProfile;

    @BeforeEach
    void login() {
        camille = userAuthHelper.authenticateCamille();
    }

    void setUp() throws IOException {
        // Given
        this.anthony = UserId.of(userAuthHelper.authenticateAntho().user().getId());
        this.olivier = UserId.of(userAuthHelper.authenticateOlivier().user().getId());
        this.pierre = UserId.of(userAuthHelper.authenticatePierre().user().getId());

        olivierBillingProfile = billingProfileService.createCompanyBillingProfile(this.olivier, "Olive Company", null);
        billingProfileService.updatePayoutInfo(olivierBillingProfile.id(), this.olivier,
                PayoutInfo.builder().ethWallet(new WalletLocator(new Name(this.olivier + ".eth")))
                        .bankAccount(new BankAccount("BIC", "FR76000111222333334444")).build());
        accountingHelper.patchBillingProfile(olivierBillingProfile.id().value(), null, VerificationStatus.VERIFIED);

        anthonyBillingProfile = billingProfileReadRepository.findByUserId(this.anthony.value()).get(0);
        billingProfileService.updatePayoutInfo(BillingProfile.Id.of(anthonyBillingProfile.id()), this.anthony,
                PayoutInfo.builder().ethWallet(new WalletLocator(new Name(this.anthony + ".eth"))).build());
        accountingHelper.patchBillingProfile(anthonyBillingProfile.id(), null, VerificationStatus.VERIFIED);

        pierreBillingProfile = billingProfileService.createSelfEmployedBillingProfile(this.pierre, "Pierre", null);
        billingProfileService.acceptInvoiceMandate(this.pierre, pierreBillingProfile.id());
        billingProfileService.updatePayoutInfo(pierreBillingProfile.id(), this.pierre,
                PayoutInfo.builder().ethWallet(new WalletLocator(new Name(this.pierre + ".eth"))).build());
        accountingHelper.patchBillingProfile(pierreBillingProfile.id().value(), null, VerificationStatus.VERIFIED);

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
                        .verificationStatus(VerificationStatus.VERIFIED).build()))
                .orElseThrow();
        kycRepository.findByBillingProfileId(anthonyBillingProfile.id())
                .map(kyb -> kycRepository.saveAndFlush(kyb.toBuilder()
                        .country("FRA")
                        .address("2 Infinite Loop, Cupertino, CA 95014, United States")
                        .firstName("Antho")
                        .lastName("Arbuste")
                        .consideredUsPersonQuestionnaire(false)
                        .idDocumentCountryCode("FRA")
                        .usCitizen(false)
                        .verificationStatus(VerificationStatus.VERIFIED).build()))
                .orElseThrow();
        kybRepository.findByBillingProfileId(pierreBillingProfile.id().value())
                .map(kyb -> kybRepository.saveAndFlush(kyb.toBuilder()
                        .country("FRA")
                        .address("1 Infinite Loop, Cupertino, CA 95014, United States")
                        .euVATNumber("FR12345678901")
                        .name("Pierre Inc.")
                        .registrationDate(faker.date().birthday())
                        .registrationNumber("123456789")
                        .usEntity(false)
                        .subjectToEuVAT(true)
                        .verificationStatus(VerificationStatus.VERIFIED).build()))
                .orElseThrow();

        updatePayoutPreferences(595505L, olivierBillingProfile.id(), UUID.fromString("e41f44a2-464c-4c96-817f-81acb06b2523"));
        updatePayoutPreferences(43467246L, BillingProfile.Id.of(anthonyBillingProfile.id()), UUID.fromString("298a547f-ecb6-4ab2-8975-68f4e9bf7b39"));
        updatePayoutPreferences(16590657L, pierreBillingProfile.id(), UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723"));

        // Given
        olivierInvoiceIds.add(newInvoiceToReview(olivier, olivierBillingProfile.id(), List.of(
                RewardId.of("5c668b61-e42c-4f0e-b31f-44c4e50dc2f4"),    //SEPA
                RewardId.of("1fad9f3b-67ab-4499-a320-d719a986d933")))); //SEPA

        anthonyInvoiceIds.add(newApprovedInvoice(anthony, BillingProfile.Id.of(anthonyBillingProfile.id()), List.of(
                RewardId.of("d22f75ab-d9f5-4dc6-9a85-60dcd7452028"),    //ETHEREUM (USDC)
                RewardId.of("e33ea956-d2f5-496b-acf9-e2350faddb16")))); //ETHEREUM (USDC)

        pierreInvoiceIds.add(newApprovedInvoice(pierre, pierreBillingProfile.id(), List.of(
                RewardId.of("8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0")))); //ETHEREUM (USDC)

        // Already paid invoice
        final var paidRewardId = RewardId.of("79209029-c488-4284-aa3f-bce8870d3a66");
        anthonyInvoiceIds.add(newApprovedInvoice(anthony, BillingProfile.Id.of(anthonyBillingProfile.id()), List.of(paidRewardId)));
        backofficeAccountingManagementRestApi.payReward(paidRewardId.value(),
                new PayRewardRequest().network(TransactionNetwork.ETHEREUM).reference(
                        "0xb1c3579ffbe3eabe6f88c58a037367dee7de6c06262cfecc3bd2e8c013cc5156"));
    }

    private Invoice.Id newInvoiceToReview(UserId userId, BillingProfile.Id billingProfileId, List<RewardId> rewardIds) throws IOException {
        final Invoice.Id invoiceId = billingProfileService.previewInvoice(userId, billingProfileId, rewardIds).id();
        billingProfileService.uploadExternalInvoice(userId, billingProfileId, invoiceId, "foo.pdf",
                new FileSystemResource(Objects.requireNonNull(getClass().getResource("/invoices/invoice-sample.pdf")).getFile()).getInputStream());
        return invoiceId;
    }

    private Invoice.Id newApprovedInvoice(UserId userId, BillingProfile.Id billingProfileId, List<RewardId> rewardIds) throws IOException {
        final Invoice.Id invoiceId = billingProfileService.previewInvoice(userId, billingProfileId, rewardIds).id();
        billingProfileService.uploadGeneratedInvoice(userId, billingProfileId, invoiceId,
                new FileSystemResource(Objects.requireNonNull(getClass().getResource("/invoices/invoice-sample.pdf")).getFile()).getInputStream());
        return invoiceId;
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
                        anthonyInvoiceIds.get(1)))
                .header("Authorization", "Bearer " + camille.jwt())
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
    void should_create_batch_payments_given_list_of_invoice_ids() throws IOException {
        // When
        final var network1 = new MutableObject<String>();
        final var batchPaymentId1 = new MutableObject<String>();
        final var network2 = new MutableObject<String>();
        final var batchPaymentId2 = new MutableObject<String>();

        client
                .put()
                .uri(getApiURI(PUT_INVOICES_STATUS.formatted(olivierInvoiceIds.get(0))))
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

        client.post()
                .uri(getApiURI(POST_REWARDS_BATCH_PAYMENTS))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PostBatchPaymentRequest()
                        .invoiceIds(List.of(
                                olivierInvoiceIds.get(0).value(),
                                anthonyInvoiceIds.get(0).value(),
                                pierreInvoiceIds.get(0).value())
                        ))
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.batchPayments.length()").isEqualTo(2)
                .jsonPath("$.batchPayments[0].id").value(batchPaymentId1::setValue)
                .jsonPath("$.batchPayments[0].network").value(network1::setValue)
                .jsonPath("$.batchPayments[1].id").value(batchPaymentId2::setValue)
                .jsonPath("$.batchPayments[1].network").value(network2::setValue)
                .json("""
                        {
                          "batchPayments": [
                            {
                              "status": "TO_PAY",
                              "network": "ETHEREUM",
                              "rewardCount": 3
                            },
                            {
                              "status": "TO_PAY",
                              "network": "SEPA",
                              "rewardCount": 2
                            }
                          ]
                        }
                        """);

        ethBatchPaymentId = Payment.Id.of(network1.getValue().equals("ETHEREUM") ? batchPaymentId1.getValue() : batchPaymentId2.getValue());
        sepaBatchPaymentId = Payment.Id.of(network1.getValue().equals("SEPA") ? batchPaymentId1.getValue() : batchPaymentId2.getValue());
    }

    @Test
    @Order(3)
    void should_get_batch_payment() {
        final var csv = new MutableObject<String>();
        client.get()
                .uri(getApiURI(GET_REWARDS_BATCH_PAYMENTS_BY_ID.formatted(ethBatchPaymentId)))
                .header("Authorization", "Bearer " + camille.jwt())
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
                          "status": "TO_PAY",
                          "network": "ETHEREUM",
                          "rewardCount": 3,
                          "totalUsdEquivalent": 3232.000,
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
                          ],
                          "transactionHash": null,
                          "rewards": [
                            {
                              "id": "e33ea956-d2f5-496b-acf9-e2350faddb16",
                              "project": {
                                "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                "slug": "kaaper",
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
                                "conversionRate": 1.01
                              }
                            },
                            {
                              "id": "8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0",
                              "project": {
                                "id": "f39b827f-df73-498c-8853-99bc3f562723",
                                "slug": "qa-new-contributions",
                                "name": "QA new contributions",
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
                                "conversionRate": 1.01
                              }
                            },
                            {
                              "id": "d22f75ab-d9f5-4dc6-9a85-60dcd7452028",
                              "project": {
                                "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                "slug": "kaaper",
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
                                "conversionRate": 1.01
                              }
                            }
                          ]
                        }
                        """);

        assertThat(csv.getValue().split("\\R")).containsExactlyInAnyOrder(
                "erc20,0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48,fc92397c-3431-4a84-8054-845376b630a0.eth,1200.0,",
                "erc20,0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48,747e663f-4e68-4b42-965b-b5aebedcd4c4.eth,1000,",
                "erc20,0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48,747e663f-4e68-4b42-965b-b5aebedcd4c4.eth,1000,"
        );
    }

    @Test
    @Order(4)
    void should_get_batch_payments() {
        client.get()
                .uri(getApiURI(GET_REWARDS_BATCH_PAYMENTS, Map.of("pageIndex", "0", "pageSize", "10")))
                .header("Authorization", "Bearer " + camille.jwt())
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
                              "network": "ETHEREUM",
                              "rewardCount": 3,
                              "totalUsdEquivalent": 3030.00,
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
                              "status": "TO_PAY",
                              "network": "SEPA",
                              "rewardCount": 2,
                              "totalUsdEquivalent": 2750,
                              "totalsPerCurrency": [
                                {
                                  "amount": 2750,
                                  "currency": {
                                    "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                    "code": "USD",
                                    "name": "US Dollar",
                                    "logoUrl": null,
                                    "decimals": 2
                                  },
                                  "dollarsEquivalent": 2750
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
                .uri(getApiURI(REWARDS_BATCH_PAYMENTS.formatted(ethBatchPaymentId)))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "transactionHash": "0xfoobar"
                        }
                        """)
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    @Order(6)
    void should_mark_batch_payment_as_paid() {
        client.put()
                .uri(getApiURI(REWARDS_BATCH_PAYMENTS.formatted(ethBatchPaymentId)))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "transactionHash": "0x313d09b7aa7d113ebd99cd58a59741d9e547813989d94ece7725b841a776b47e"
                        }
                        """)
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        client.get()
                .uri(getApiURI(GET_REWARDS_BATCH_PAYMENTS_BY_ID.formatted(ethBatchPaymentId)))
                .header("Authorization", "Bearer " + camille.jwt())
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
                          "rewardCount": 3,
                          "totalUsdEquivalent": 3232.0,
                          "transactionHash": "0x313d09b7aa7d113ebd99cd58a59741d9e547813989d94ece7725b841a776b47e"
                        }
                        """);

        client.get()
                .uri(getApiURI(GET_REWARDS_BATCH_PAYMENTS, Map.of("pageIndex", "0", "pageSize", "10", "statuses", "PAID")))
                .header("Authorization", "Bearer " + camille.jwt())
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
                              "rewardCount": 3,
                              "totalUsdEquivalent": 3030.00,
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
                            }
                          ]
                        }
                        """);

        client
                .get()
                .uri(getApiURI(INVOICE.formatted(pierreInvoiceIds.get(0))))
                .header("Authorization", "Bearer " + camille.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "number": "OD-PIERRE-INC--001",
                          "status": "PAID",
                          "billingProfile": {
                            "subject": "Pierre Inc.",
                            "type": "SELF_EMPLOYED",
                            "name": null,
                            "verificationStatus": "VERIFIED",
                            "kyb": {
                              "name": "Pierre Inc.",
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
                            "githubUserId": 16590657,
                            "githubLogin": "PierreOucif",
                            "githubAvatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                            "email": "pierre.oucif@gadz.org",
                            "id": "fc92397c-3431-4a84-8054-845376b630a0",
                            "name": "Pierre Oucif"
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
                              "id": "8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0",
                              "project": {
                                "id": "f39b827f-df73-498c-8853-99bc3f562723",
                                "slug": "qa-new-contributions",
                                "name": "QA new contributions",
                                "logoUrl": null
                              },
                              "status": "COMPLETE",
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
                                "conversionRate": 1.01
                              }
                            }
                          ]
                        }
                        """)
        ;

        client
                .get()
                .uri(getApiURI(INVOICE.formatted(anthonyInvoiceIds.get(0))))
                .header("Authorization", "Bearer " + camille.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "number": "OD-ARBUSTE-ANTHO-001",
                          "status": "PAID",
                          "billingProfile": {
                            "subject": "Antho Arbuste",
                            "type": "INDIVIDUAL",
                            "name": null,
                            "verificationStatus": "VERIFIED",
                            "kyb": null,
                            "kyc": {
                              "firstName": "Antho",
                              "lastName": "Arbuste",
                              "birthdate": null,
                              "address": "2 Infinite Loop, Cupertino, CA 95014, United States",
                              "country": "France",
                              "countryCode": "FRA",
                              "usCitizen": false,
                              "idDocumentType": null,
                              "idDocumentNumber": null,
                              "validUntil": null,
                              "idDocumentCountryCode": null,
                              "sumsubUrl": null
                            }
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
                            "amount": 2020.00,
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
                              "id": "e33ea956-d2f5-496b-acf9-e2350faddb16",
                              "project": {
                                "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                "slug": "kaaper",
                                "name": "kaaper",
                                "logoUrl": null
                              },
                              "status": "COMPLETE",
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
                                "conversionRate": 1.01
                              }
                            },
                            {
                              "id": "d22f75ab-d9f5-4dc6-9a85-60dcd7452028",
                              "project": {
                                "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                "slug": "kaaper",
                                "name": "kaaper",
                                "logoUrl": null
                              },
                              "status": "COMPLETE",
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
                                "conversionRate": 1.01
                              }
                            }
                          ]
                        }
                        """)
        ;
    }

    @Test
    @Order(7)
    void should_fail_to_mark_batch_payment_as_paid_when_it_is_already_paid() {
        client.put()
                .uri(getApiURI(REWARDS_BATCH_PAYMENTS.formatted(ethBatchPaymentId)))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "transactionHash": "0x313d09b7aa7d113ebd99cd58a59741d9e547813989d94ece7725b841a776b47e"
                        }
                        """)
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    @Order(7)
    void should_fail_to_delete_batch_payment_when_it_is_already_paid() {
        client.delete()
                .uri(getApiURI(REWARDS_BATCH_PAYMENTS.formatted(ethBatchPaymentId)))
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    @Order(7)
    void should_delete_batch_payment_when_it_is_not_already_paid() {
        client.delete()
                .uri(getApiURI(REWARDS_BATCH_PAYMENTS.formatted(sepaBatchPaymentId)))
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        client.get()
                .uri(getApiURI(GET_REWARDS_BATCH_PAYMENTS_BY_ID.formatted(sepaBatchPaymentId)))
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isNotFound();
    }
}
