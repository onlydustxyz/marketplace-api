package onlydust.com.marketplace.api.bootstrap.it.api;

import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil;
import lombok.NonNull;
import lombok.SneakyThrows;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.PostgresUserAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.IndividualBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.OldVerificationStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserBillingProfileTypeEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserBillingProfileTypeEntity.BillingProfileTypeEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.CryptoUsdQuotesEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentRequestEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CompanyBillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.IndividualBillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserBillingProfileTypeRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.CryptoUsdQuotesRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRequestRepository;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.project.domain.model.UserPayoutSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;


public class MeGetRewardsApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    PaymentRequestRepository paymentRequestRepository;
    @Autowired
    CryptoUsdQuotesRepository cryptoUsdQuotesRepository;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    PostgresUserAdapter postgresUserAdapter;
    @Autowired
    UserBillingProfileTypeRepository userBillingProfileTypeRepository;
    @Autowired
    CompanyBillingProfileRepository companyBillingProfileRepository;
    @Autowired
    IndividualBillingProfileRepository individualBillingProfileRepository;

    UserAuthHelper.AuthenticatedUser pierre;

    @BeforeEach
    void setup() {
        pierre = userAuthHelper.authenticatePierre();

        patchBillingProfile(pierre, BillingProfileTypeEntity.COMPANY, OldVerificationStatusEntity.VERIFIED);

        addQuote(CurrencyEnumEntity.eth, 1500);
        addQuote(CurrencyEnumEntity.apt, 200);

        patchReward("40fda3c6-2a3f-4cdd-ba12-0499dd232d53", 10, CurrencyEnumEntity.eth, null, "2023-07-12");
        patchReward("e1498a17-5090-4071-a88a-6f0b0c337c3a", 50, CurrencyEnumEntity.eth, null, "2023-08-12");
        patchReward("2ac80cc6-7e83-4eef-bc0c-932b58f683c0", 500, CurrencyEnumEntity.apt, null, null);
        patchReward("8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0", 30, CurrencyEnumEntity.op, "2023-08-14", null);
        patchReward("5b96ca1e-4ad2-41c1-8819-520b885d9223", 9511147, CurrencyEnumEntity.strk, null, null);

        postgresUserAdapter.savePayoutSettingsForUserId(pierre.user().getId(),
                UserPayoutSettings.builder()
                        .ethWallet(Ethereum.wallet("vitalik.eth"))
                        .aptosAddress(Aptos.accountAddress("0x" + faker.random().hex(40)))
                        .build());
    }

    @Test
    void should_list_my_rewards() {
        // When
        client.get()
                .uri(getApiURI(String.format(ME_GET_REWARDS), Map.of(
                        "pageIndex", "0",
                        "pageSize", "20",
                        "sort", "AMOUNT",
                        "direction", "DESC")
                ))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody()
                .json("""
                        {
                          "rewards": [
                            {
                              "requestedAt": "2023-09-19T07:38:22.018458Z",
                              "processedAt": null,
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "status": "PENDING_INVOICE",
                              "unlockDate": null,
                              "amount": {
                                "total": 500,
                                "currency": "APT",
                                "dollarsEquivalent": 100000
                              },
                              "numberOfRewardedContributions": 25,
                              "rewardedOnProjectName": "QA new contributions",
                              "rewardedOnProjectLogoUrl": null,
                              "id": "2ac80cc6-7e83-4eef-bc0c-932b58f683c0"
                            },
                            {
                              "requestedAt": "2023-09-20T08:46:52.77875Z",
                              "processedAt": "2023-08-12T00:00:00Z",
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "status": "COMPLETE",
                              "unlockDate": null,
                              "amount": {
                                "total": 50,
                                "currency": "ETH",
                                "dollarsEquivalent": 75000
                              },
                              "numberOfRewardedContributions": 1,
                              "rewardedOnProjectName": "QA new contributions",
                              "rewardedOnProjectLogoUrl": null,
                              "id": "e1498a17-5090-4071-a88a-6f0b0c337c3a"
                            },
                            {
                              "requestedAt": "2023-09-19T07:40:26.971981Z",
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "status": "COMPLETE",
                              "unlockDate": null,
                              "amount": {
                                "total": 10,
                                "currency": "ETH",
                                "dollarsEquivalent": 15000
                              },
                              "numberOfRewardedContributions": 25,
                              "rewardedOnProjectName": "QA new contributions",
                              "rewardedOnProjectLogoUrl": null,
                              "id": "40fda3c6-2a3f-4cdd-ba12-0499dd232d53"
                            },
                            {
                              "requestedAt": "2023-09-19T07:38:52.590518Z",
                              "processedAt": null,
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "status": "PENDING_INVOICE",
                              "unlockDate": null,
                              "amount": {
                                "total": 1000,
                                "currency": "USDC",
                                "dollarsEquivalent": 1010.00
                              },
                              "numberOfRewardedContributions": 25,
                              "rewardedOnProjectName": "QA new contributions",
                              "rewardedOnProjectLogoUrl": null,
                              "id": "85f8358c-5339-42ac-a577-16d7760d1e28"
                            },
                            {
                              "requestedAt": "2023-09-19T07:39:54.45638Z",
                              "processedAt": null,
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "status": "MISSING_PAYOUT_INFO",
                              "unlockDate": "2024-08-23T00:00:00Z",
                              "amount": {
                                "total": 30,
                                "currency": "OP",
                                "dollarsEquivalent": null
                              },
                              "numberOfRewardedContributions": 25,
                              "rewardedOnProjectName": "QA new contributions",
                              "rewardedOnProjectLogoUrl": null,
                              "id": "8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0"
                            },
                            {
                              "requestedAt": "2023-09-19T07:39:23.730967Z",
                              "processedAt": null,
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "status": "MISSING_PAYOUT_INFO",
                              "unlockDate": null,
                              "amount": {
                                "total": 9511147,
                                "currency": "STRK",
                                "dollarsEquivalent": null
                              },
                              "numberOfRewardedContributions": 25,
                              "rewardedOnProjectName": "QA new contributions",
                              "rewardedOnProjectLogoUrl": null,
                              "id": "5b96ca1e-4ad2-41c1-8819-520b885d9223"
                            }
                          ],
                          "hasMore": false,
                          "totalPageNumber": 1,
                          "totalItemNumber": 6,
                          "nextPageIndex": 0,
                          "rewardedAmount": {
                            "amount": null,
                            "currency": null,
                            "usdEquivalent": 191010.00
                          },
                          "pendingAmount": {
                            "amount": null,
                            "currency": null,
                            "usdEquivalent": 101010.00
                          },
                          "receivedRewardsCount": 6,
                          "rewardedContributionsCount": 26,
                          "rewardingProjectsCount": 1
                        }
                        """);
    }

    @Test
    void should_get_my_total_reward_amounts() {
        // When
        client.get()
                .uri(getApiURI(ME_GET_REWARD_TOTAL_AMOUNTS))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalAmount": 191010.00,
                          "details": [
                            {
                              "totalAmount": 60,
                              "totalDollarsEquivalent": 90000,
                              "currency": "ETH"
                            },
                            {
                              "totalAmount": 30,
                              "totalDollarsEquivalent": null,
                              "currency": "OP"
                            },
                            {
                              "totalAmount": 500,
                              "totalDollarsEquivalent": 100000,
                              "currency": "APT"
                            },
                            {
                              "totalAmount": 9511147,
                              "totalDollarsEquivalent": null,
                              "currency": "STRK"
                            },
                            {
                              "totalAmount": 1000,
                              "totalDollarsEquivalent": 1010.00,
                              "currency": "USDC"
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_get_my_rewards_with_pending_invoice() {
        // When
        client.get()
                .uri(getApiURI(ME_REWARDS_PENDING_INVOICE))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "rewards": [
                            {
                              "requestedAt": "2023-09-19T07:38:22.018458Z",
                              "processedAt": null,
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "status": "PENDING_INVOICE",
                              "unlockDate": null,
                              "amount": {
                                "total": 500,
                                "currency": "APT",
                                "dollarsEquivalent": 100000
                              },
                              "numberOfRewardedContributions": 25,
                              "rewardedOnProjectName": "QA new contributions",
                              "rewardedOnProjectLogoUrl": null,
                              "id": "2ac80cc6-7e83-4eef-bc0c-932b58f683c0"
                            },
                            {
                              "requestedAt": "2023-09-19T07:38:52.590518Z",
                              "processedAt": null,
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "status": "PENDING_INVOICE",
                              "unlockDate": null,
                              "amount": {
                                "total": 1000,
                                "currency": "USDC",
                                "dollarsEquivalent": 1010.00
                              },
                              "numberOfRewardedContributions": 25,
                              "rewardedOnProjectName": "QA new contributions",
                              "rewardedOnProjectLogoUrl": null,
                              "id": "85f8358c-5339-42ac-a577-16d7760d1e28"
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_return_pending_verification_info_then_missing_payout_info_given_first_authenticated_user_with_pending_reward() {
        // Given
        final long githubUserId = faker.number().randomNumber();
        final UUID userId = UUID.randomUUID();
        final String jwt = userAuthHelper.newFakeUser(userId, githubUserId,
                faker.rickAndMorty().character(), faker.internet().url(), false).jwt();
        paymentRequestRepository.save(new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), githubUserId,
                new Date(), BigDecimal.ONE, null, 1, UUID.fromString("c66b929a-664d-40b9-96c4-90d3efd32a3c"),
                CurrencyEnumEntity.usd, BigDecimal.ONE));

        // When
        client.get()
                .uri(getApiURI(ME_GET_REWARDS, Map.of("pageIndex", "0", "pageSize", "100")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards[0].status").isEqualTo("PENDING_VERIFICATION");

        userBillingProfileTypeRepository.save(UserBillingProfileTypeEntity.builder()
                .userId(userId)
                .billingProfileType(BillingProfileTypeEntity.INDIVIDUAL)
                .build());
        individualBillingProfileRepository.save(IndividualBillingProfileEntity.builder()
                .verificationStatus(OldVerificationStatusEntity.VERIFIED)
                .userId(userId)
                .id(UUID.randomUUID())
                .build());

        // When
        client.get()
                .uri(getApiURI(ME_GET_REWARDS, Map.of("pageIndex", "0", "pageSize", "100")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards[0].status").isEqualTo("MISSING_PAYOUT_INFO");
    }

    @Test
    void should_filter_by_date() {
        // When
        client.get()
                .uri(getApiURI(ME_GET_REWARDS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "100",
                        "fromDate", "2023-09-20",
                        "toDate", "2023-09-20"
                )))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                // we have at least one correct date
                .jsonPath("$.rewards[?(@.requestedAt >= '2023-09-20')]").exists()
                .jsonPath("$.rewards[?(@.requestedAt < '2023-09-21')]").exists()
                // we do not have any incorrect date
                .jsonPath("$.rewards[?(@.requestedAt < '2023-09-20')]").doesNotExist()
                .jsonPath("$.rewards[?(@.requestedAt > '2023-09-21')]").doesNotExist()
                .jsonPath("$.rewardedAmount.amount").isEqualTo(50)
                .jsonPath("$.rewardedAmount.currency").isEqualTo("ETH")
                .jsonPath("$.rewardedAmount.usdEquivalent").isEqualTo(75000)
                .jsonPath("$.pendingAmount.amount").isEqualTo(0)
                .jsonPath("$.pendingAmount.currency").isEqualTo("ETH")
                .jsonPath("$.pendingAmount.usdEquivalent").isEqualTo(0)
                .jsonPath("$.receivedRewardsCount").isEqualTo(1)
                .jsonPath("$.rewardedContributionsCount").isEqualTo(1)
                .jsonPath("$.rewardingProjectsCount").isEqualTo(1)
        ;
    }

    @Test
    void should_filter_by_currency() {
        // When
        client.get()
                .uri(getApiURI(ME_GET_REWARDS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "100",
                        "currencies", "ETH"
                )))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards[?(@.amount.currency == 'ETH')]").exists()
                .jsonPath("$.rewards[?(@.amount.currency != 'ETH')]").doesNotExist()
                .jsonPath("$.rewardedAmount.amount").isEqualTo(60)
                .jsonPath("$.rewardedAmount.currency").isEqualTo("ETH")
                .jsonPath("$.rewardedAmount.usdEquivalent").isEqualTo(90000.00)
                .jsonPath("$.pendingAmount.amount").isEqualTo(0)
                .jsonPath("$.pendingAmount.currency").isEqualTo("ETH")
                .jsonPath("$.pendingAmount.usdEquivalent").isEqualTo(0.00)
                .jsonPath("$.receivedRewardsCount").isEqualTo(2)
                .jsonPath("$.rewardedContributionsCount").isEqualTo(26)
                .jsonPath("$.rewardingProjectsCount").isEqualTo(1)
        ;
    }

    @Test
    void should_filter_by_projects() {
        // Given
        final String jwt = userAuthHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(ME_GET_REWARDS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "100",
                        "projects", "5aabf0f1-7495-4bff-8de2-4396837ce6b4,298a547f-ecb6-4ab2-8975-68f4e9bf7b39"
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards[?(@.projectId in ['5aabf0f1-7495-4bff-8de2-4396837ce6b4'," +
                          "'298a547f-ecb6-4ab2-8975-68f4e9bf7b39'])]").exists()
                .jsonPath("$.rewards[?(@.projectId nin ['5aabf0f1-7495-4bff-8de2-4396837ce6b4'," +
                          "'298a547f-ecb6-4ab2-8975-68f4e9bf7b39'])]").doesNotExist()
                .jsonPath("$.rewardedAmount.amount").doesNotExist()
                .jsonPath("$.rewardedAmount.currency").doesNotExist()
                .jsonPath("$.rewardedAmount.usdEquivalent").isEqualTo(2260100)
                .jsonPath("$.pendingAmount.amount").doesNotExist()
                .jsonPath("$.pendingAmount.currency").doesNotExist()
                .jsonPath("$.pendingAmount.usdEquivalent").isEqualTo(1510100.0)
                .jsonPath("$.receivedRewardsCount").isEqualTo(13)
                .jsonPath("$.rewardedContributionsCount").isEqualTo(88)
                .jsonPath("$.rewardingProjectsCount").isEqualTo(2)
        ;
    }

    @Test
    void should_get_rewards_when_no_usd_equivalent() {
        // When
        client.get()
                .uri(getApiURI(ME_GET_REWARDS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "10"
                )))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards[?(@.amount.currency == 'STRK' && @.amount.dollarsEquivalent == null)]").exists()
                .jsonPath("$.rewards[?(@.amount.currency == 'STRK' && @.amount.dollarsEquivalent != null)]").doesNotExist()
                .jsonPath("$.rewardedAmount.usdEquivalent").isEqualTo(191010)
                .jsonPath("$.pendingAmount.usdEquivalent").isEqualTo(101010)
        ;
    }

    @SneakyThrows
    private void patchReward(@NonNull String id, Number amount, CurrencyEnumEntity currency, String invoiceReceivedAt, String paidAt) {
        final var paymentRequestEntity = paymentRequestRepository.findById(UUID.fromString(id)).orElseThrow();
        if (amount != null) paymentRequestEntity.setAmount(BigDecimal.valueOf(amount.doubleValue()));
        if (currency != null) paymentRequestEntity.setCurrency(currency);
        if (invoiceReceivedAt != null) paymentRequestEntity.setInvoiceReceivedAt(new SimpleDateFormat("yyyy-MM-dd").parse("2023-08-14"));
        paymentRequestRepository.save(paymentRequestEntity);

        if (paidAt != null) {
            final var paymentEntity = paymentRepository.findByRequestId(UUID.fromString(id)).orElse(new PaymentEntity(UUID.randomUUID(),
                    paymentRequestEntity.getAmount(), currency.name().toUpperCase(),
                    JacksonUtil.toJsonNode("{}"), paymentRequestEntity.getId(), new SimpleDateFormat("yyyy-MM-dd").parse(paidAt)));
            paymentEntity.setProcessedAt(new SimpleDateFormat("yyyy-MM-dd").parse(paidAt));
            paymentRepository.save(paymentEntity);
        }
    }

    private void patchBillingProfile(@NonNull UserAuthHelper.AuthenticatedUser user, BillingProfileTypeEntity type, OldVerificationStatusEntity status) {
        if (type != null) {
            final var entity = userBillingProfileTypeRepository.findById(user.user().getId()).orElseThrow();
            entity.setBillingProfileType(type);
            userBillingProfileTypeRepository.save(entity);
        }

        if (status != null) {
            final var entity = companyBillingProfileRepository.findByUserId(user.user().getId()).orElseThrow();
            entity.setVerificationStatus(status);
            companyBillingProfileRepository.save(entity);
        }
    }

    private void addQuote(@NonNull CurrencyEnumEntity currency, @NonNull Number price) {
        cryptoUsdQuotesRepository.save(CryptoUsdQuotesEntity.builder()
                .currency(currency)
                .price(BigDecimal.valueOf(price.doubleValue()))
                .updatedAt(new Date())
                .build());
    }
}
