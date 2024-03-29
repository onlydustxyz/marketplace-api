package onlydust.com.marketplace.api.bootstrap.it.bo;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Payment;
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
    static Payment.Id sepaBatchPaymentId;
    static Payment.Id ethBatchPaymentId;

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

        newAnthonyApprovedInvoice(List.of(
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

    private void newAnthonyApprovedInvoice(List<RewardId> rewardIds) throws IOException {
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
    void should_create_batch_payments_given_list_of_invoice_ids() throws IOException {
        // When
        final var network1 = new MutableObject<String>();
        final var batchPaymentId1 = new MutableObject<String>();
        final var network2 = new MutableObject<String>();
        final var batchPaymentId2 = new MutableObject<String>();

        client
                .put()
                .uri(getApiURI(PUT_INVOICES_STATUS.formatted(olivierInvoiceIds.get(0))))
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
                .jsonPath("$.batchPayments[0].network").value(network1::setValue)
                .jsonPath("$.batchPayments[1].id").value(batchPaymentId2::setValue)
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

        ethBatchPaymentId = Payment.Id.of(network1.getValue().equals("ETHEREUM") ? batchPaymentId1.getValue() : batchPaymentId2.getValue());
        sepaBatchPaymentId = Payment.Id.of(network1.getValue().equals("SEPA") ? batchPaymentId1.getValue() : batchPaymentId2.getValue());
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
                                "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                "decimals": 6
                              },
                              "dollarsEquivalent": 3030.00
                            }
                          ],
                          "transactionHash": null,
                          "rewards": [
                            {
                              "id": "4ac9d6ac-f2ca-43d5-901a-ac7f5b149d72",
                              "project": {
                                "name": "Cairo foundry",
                                "logoUrl": null
                              },
                              "status": "PENDING_SIGNUP",
                              "money": {
                                "amount": 2000,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "dollarsEquivalent": 2020.00,
                                "conversionRate": 1.0100000000000000
                              }
                            },
                            {
                              "id": "d22f75ab-d9f5-4dc6-9a85-60dcd7452028",
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
                .uri(getApiURI(REWARDS_BATCH_PAYMENTS.formatted(ethBatchPaymentId)))
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
                .header("Api-Key", apiKey())
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
                .header("Api-Key", apiKey())
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
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        client.get()
                .uri(getApiURI(GET_REWARDS_BATCH_PAYMENTS_BY_ID.formatted(sepaBatchPaymentId)))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .isNotFound();
    }
}
