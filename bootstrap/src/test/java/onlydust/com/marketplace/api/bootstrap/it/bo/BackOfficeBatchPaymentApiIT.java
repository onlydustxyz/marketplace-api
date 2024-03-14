package onlydust.com.marketplace.api.bootstrap.it.bo;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.CompanyBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.SelfEmployedBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.api.bootstrap.helper.AccountingHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BatchPaymentEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NetworkEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.VerificationStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.KybRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.KycRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BatchPaymentRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key.ApiKeyAuthenticationService;
import onlydust.com.marketplace.api.webhook.Config;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeBatchPaymentApiIT extends AbstractMarketplaceBackOfficeApiIT {

    @Autowired
    BillingProfileService billingProfileService;
    @Autowired
    AccountingHelper accountingHelper;
    @Autowired
    Config webhookHttpClientProperties;
    @Autowired
    KycRepository kycRepository;
    @Autowired
    KybRepository kybRepository;
    private final Faker faker = new Faker();

    UserId anthony;
    UserId olivier;
    CompanyBillingProfile olivierBillingProfile;
    SelfEmployedBillingProfile anthonyBillingProfile;

    static final List<Invoice.Id> anthonyInvoiceIds = new ArrayList<>();
    static final List<Invoice.Id> olivierInvoiceIds = new ArrayList<>();

    void setUp() throws IOException {
        // Given
        this.anthony = UserId.of(userAuthHelper.authenticateAnthony().user().getId());
        this.olivier = UserId.of(userAuthHelper.authenticateOlivier().user().getId());

        olivierBillingProfile = billingProfileService.createCompanyBillingProfile(this.olivier, "Apple Inc.", null);
        billingProfileService.updatePayoutInfo(olivierBillingProfile.id(), this.olivier,
                PayoutInfo.builder().ethWallet(new WalletLocator(new Name(this.olivier + ".eth")))
                        .bankAccount(new BankAccount("BIC", "FR76000111222333334444")).build());

        anthonyBillingProfile = billingProfileService.createSelfEmployedBillingProfile(this.anthony, "Olivier SASU", null);
        billingProfileService.updatePayoutInfo(anthonyBillingProfile.id(), this.anthony,
                PayoutInfo.builder().ethWallet(new WalletLocator(new Name(this.anthony + ".eth"))).build());

        kybRepository.findByBillingProfileId(olivierBillingProfile.id().value())
                .ifPresent(kyb -> kybRepository.save(kyb.toBuilder()
                        .country("FRA")
                        .address("1 Infinite Loop, Cupertino, CA 95014, United States")
                        .euVATNumber("FR12345678901")
                        .name("Olivier Inc.")
                        .registrationDate(faker.date().birthday())
                        .registrationNumber("123456789")
                        .usEntity(false)
                        .subjectToEuVAT(true)
                        .verificationStatus(VerificationStatusEntity.VERIFIED).build()));
        kybRepository.findByBillingProfileId(anthonyBillingProfile.id().value())
                .ifPresent(kyb -> kybRepository.save(kyb.toBuilder()
                        .country("FRA")
                        .address("2 Infinite Loop, Cupertino, CA 95014, United States")
                        .euVATNumber("FR0987654321")
                        .name("Antho SASU")
                        .registrationDate(faker.date().birthday())
                        .registrationNumber("ABC123456789")
                        .usEntity(false)
                        .subjectToEuVAT(true)
                        .verificationStatus(VerificationStatusEntity.VERIFIED).build()));

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

    private Invoice.Id newOlivierInvoiceToReview(List<RewardId> rewardIds) throws IOException {
        final Invoice.Id invoiceId = billingProfileService.previewInvoice(olivier, olivierBillingProfile.id(), rewardIds).id();
        billingProfileService.uploadExternalInvoice(olivier, olivierBillingProfile.id(), invoiceId, "foo.pdf",
                new FileSystemResource(Objects.requireNonNull(getClass().getResource("/invoices/invoice-sample.pdf")).getFile()).getInputStream());
        olivierInvoiceIds.add(invoiceId);
        return invoiceId;
    }

    private Invoice.Id newAnthonyInvoiceToReview(List<RewardId> rewardIds) throws IOException {
        final Invoice.Id invoiceId = billingProfileService.previewInvoice(anthony, anthonyBillingProfile.id(), rewardIds).id();
        billingProfileService.uploadExternalInvoice(anthony, anthonyBillingProfile.id(), invoiceId, "foo.pdf",
                new FileSystemResource(Objects.requireNonNull(getClass().getResource("/invoices/invoice-sample.pdf")).getFile()).getInputStream());
        anthonyInvoiceIds.add(invoiceId);
        return invoiceId;
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
                .json("""
                    {
                      "batchPayments": [
                        {
                          "csv": "erc20,0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48,747e663f-4e68-4b42-965b-b5aebedcd4c4.eth,1000\\r\\nerc20,0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48,e461c019-ba23-4671-9b6c-3a5a18748af9.eth,2000\\r\\n",
                          "network": "ETHEREUM",
                          "rewardCount": null,
                          "totalAmounts": []
                        },
                        {
                          "csv": "iso4217,,FR76000111222333334444,1000\\r\\n",
                          "network": "SEPA",
                          "rewardCount": null,
                          "totalAmounts": []
                        }
                      ]
                    }
                    """);
    }

    @Autowired
    BatchPaymentRepository batchPaymentRepository;
    @Autowired
    ApiKeyAuthenticationService.Config backOfficeApiKeyAuthenticationConfig;


    //TODO
//    @Test
//    @Order(103)
//    void should_pay_strk_batch_payment() {
//        // Given
//        final var toto = batchPaymentRepository.findAll();
//        final BatchPaymentEntity starknetBatchPaymentEntity = batchPaymentRepository.findAll().stream()
//                .filter(batchPaymentEntity -> batchPaymentEntity.getNetwork().equals(NetworkEnumEntity.starknet))
//                .findFirst()
//                .orElseThrow();
//        final String transactionHash = "0x" + faker.random().hex();
//
//        final PaymentRequestEntity r1 = paymentRequestRepository.findById(starknetBatchPaymentEntity.getRewardIds().get(0)).orElseThrow();
//        final PaymentRequestEntity r2 = paymentRequestRepository.findById(starknetBatchPaymentEntity.getRewardIds().get(1)).orElseThrow();
//
//
//        rustApiWireMockServer.stubFor(
//                WireMock.post("/api/payments/%s/receipts".formatted(r1.getId()))
//                        .withHeader("Api-Key", WireMock.equalTo(odRustApiHttpClientProperties.getApiKey()))
//                        .withRequestBody(WireMock.equalToJson(
//                                """
//                                        {
//                                           "amount": %s,
//                                           "currency": "%s",
//                                           "recipientWallet": "%s",
//                                           "recipientIban" : null,
//                                           "transactionReference" : "%s"
//                                        }
//                                            """.formatted(r1.getAmount().toString(), r1.getCurrency().toDomain().name(), anthoStarknetAddress,
//                                            transactionHash)
//                        ))
//                        .willReturn(ResponseDefinitionBuilder.okForJson("""
//                                {
//                                    "receipt_id": "%s"
//                                }""".formatted(UUID.randomUUID()))));
//
//        rustApiWireMockServer.stubFor(
//                WireMock.post("/api/payments/%s/receipts".formatted(r2.getId()))
//                        .withHeader("Api-Key", WireMock.equalTo(odRustApiHttpClientProperties.getApiKey()))
//                        .withRequestBody(WireMock.equalToJson(
//                                """
//                                        {
//                                           "amount": %s,
//                                           "currency": "%s",
//                                           "recipientWallet": "%s",
//                                           "recipientIban" : null,
//                                           "transactionReference" : "%s"
//                                        }
//                                        """.formatted(r2.getAmount().toString(), r2.getCurrency().toDomain().name(), olivierStarknetAddress,
//                                        transactionHash)
//                        ))
//                        .willReturn(ResponseDefinitionBuilder.okForJson("""
//                                {
//                                    "receipt_id": "%s"
//                                }""".formatted(UUID.randomUUID()))));
//
//
//        // When
//        client.put()
//                .uri(getApiURI(PUT_REWARDS_BATCH_PAYMENTS.formatted(starknetBatchPaymentEntity.getId())))
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Api-Key", apiKey())
//                .bodyValue("""
//                                          {
//                                            "transactionHash": "%s"
//                                          }
//                        """.formatted(transactionHash))
//                // Then
//                .exchange()
//                .expectStatus()
//                .is2xxSuccessful();
//
//        final BatchPayment batchPayment = batchPaymentRepository.findById(starknetBatchPaymentEntity.getId()).orElseThrow().toDomain();
//        assertEquals(BatchPayment.Status.PAID, batchPayment.status());
//        assertTrue(batchPayment.rewardIds().contains(RewardId.of(r1.getId())));
//        assertTrue(batchPayment.rewardIds().contains(RewardId.of(r2.getId())));
//    }


    @Test
    @Order(104)
    void should_get_page_of_payment_batch_and_get_payment_batch_by_id() {
        // Given
        final BatchPaymentEntity starknetBatchPaymentEntity = batchPaymentRepository.findAll().stream()
                .filter(batchPaymentEntity -> batchPaymentEntity.getNetwork().equals(NetworkEnumEntity.starknet))
                .findFirst()
                .orElseThrow();

        // When
        client.get()
                .uri(getApiURI(GET_REWARDS_BATCH_PAYMENTS, Map.of("pageIndex", "0", "pageSize", "20")))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_BATCH_PAYMENTS_PAGE_JSON_RESPONSE);

        // When
        client.get()
                .uri(getApiURI(GET_REWARDS_BATCH_PAYMENTS_BY_ID.formatted(starknetBatchPaymentEntity.getId())))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.blockchain").isEqualTo("STARKNET")
                .jsonPath("$.rewardCount").isEqualTo(2)
                .jsonPath("$.totalAmountUsd").isEqualTo(544)
                .jsonPath("$.totalAmounts[0].amount").isEqualTo(11533.222)
                .jsonPath("$.totalAmounts.length()").isEqualTo(1)
                .jsonPath("$.csv").isNotEmpty()
                .jsonPath("$.transactionHash").isNotEmpty()
                .jsonPath("$.rewards.length()").isEqualTo(2);
    }

//    @Autowired
//    PaymentRepository paymentRepository;
//    @Autowired
//    Config webhookHttpClientProperties;
//
//    @Test
//    @Order(110)
//    void should_notify_new_rewards_paid() {
//        // Given
//        final List<PaymentRequestEntity> rewardsToPay = paymentRequestRepository.findAllById(
//                batchPaymentRepository.findAll().stream()
//                        .flatMap(batchPaymentEntity -> batchPaymentEntity.getRewardIds().stream())
//                        .toList());
//        paymentRepository.save(new PaymentEntity(UUID.randomUUID(), BigDecimal.ONE, "ETH", JacksonUtil.toJsonNode("{}"), rewardsToPay.get(0).getId(),
//                new Date()));
//        paymentRepository.save(new PaymentEntity(UUID.randomUUID(), BigDecimal.ONE, "ETH", JacksonUtil.toJsonNode("{}"), rewardsToPay.get(1).getId(),
//                new Date()));
//
//        makeWebhookSendRewardsPaidMailWireMockServer.stubFor(
//                post("/?api-key=%s".formatted(webhookHttpClientProperties.getApiKey()))
//                        .willReturn(ok()));
//
//        // When
//        client.put()
//                .uri(getApiURI(PUT_REWARDS_NOTIFY_PAYMENTS))
//                .header("Api-Key", apiKey())
//                // Then
//                .exchange()
//                .expectStatus()
//                .is2xxSuccessful();
//
//        makeWebhookSendRewardsPaidMailWireMockServer.verify(1,
//                postRequestedFor(urlEqualTo("/?api-key=%s".formatted(webhookHttpClientProperties.getApiKey())))
//                        .withHeader("Content-Type", equalTo("application/json"))
//                        .withRequestBody(matchingJsonPath("$.recipientEmail", equalTo("abuisset@gmail.com")))
//                        .withRequestBody(matchingJsonPath("$.recipientName", equalTo("Anthony BUISSET"))));
//    }

    private static final String GET_BATCH_PAYMENTS_PAGE_JSON_RESPONSE = """
            {
              "totalPageNumber": 1,
              "totalItemNumber": 1,
              "hasMore": false,
              "nextPageIndex": 0,
              "batchPayments": [
                {
                  "blockchain": "STARKNET",
                  "rewardCount": 2,
                  "totalAmountUsd": 544,
                  "totalAmounts": [
                    {
                      "amount": 11533.222,
                      "dollarsEquivalent": 544,
                      "conversionRate": null,
                      "currencyCode": "STRK",
                      "currencyName": "StarkNet Token",
                      "currencyLogoUrl": null
                    }
                  ]
                }
              ]
            }
            """;
}
