package onlydust.com.marketplace.api.bootstrap.it;

import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil;
import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.CryptoUsdQuotesEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentRequestEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.CryptoUsdQuotesRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRequestRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@ActiveProfiles({"hasura_auth"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MeGetRewardsApiIT extends AbstractMarketplaceApiIT {
    private static final String GET_MY_REWARD_AMOUNTS_JSON_RESPONSE = """
            {
              "totalAmount": 197000,
              "details": [
                {
                  "totalAmount": 1000,
                  "totalDollarsEquivalent": 1000,
                  "currency": "USD"
                },
                {
                  "totalAmount": 60,
                  "totalDollarsEquivalent": 90000,
                  "currency": "ETH"
                },
                {
                  "totalAmount": 530,
                  "totalDollarsEquivalent": 106000,
                  "currency": "APT"
                },
                {
                  "totalAmount": 9511147,
                  "totalDollarsEquivalent": null,
                  "currency": "STARK"
                }
              ]
            }
            """;
    private static final String GET_USER_REWARDS_WITH_MULTI_CURRENCIES_RESPONSE_JSON = """
            {
              "rewards": [
                {
                  "requestedAt": "2023-09-19T05:38:22.018458Z",
                  "status": "MISSING_PAYOUT_INFO",
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
                  "requestedAt": "2023-09-20T06:46:52.77875Z",
                  "status": "MISSING_PAYOUT_INFO",
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
                  "requestedAt": "2023-09-19T05:40:26.971981Z",
                  "status": "MISSING_PAYOUT_INFO",
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
                  "requestedAt": "2023-09-19T05:39:54.45638Z",
                  "status": "MISSING_PAYOUT_INFO",
                  "amount": {
                    "total": 30,
                    "currency": "APT",
                    "dollarsEquivalent": 6000
                  },
                  "numberOfRewardedContributions": 25,
                  "rewardedOnProjectName": "QA new contributions",
                  "rewardedOnProjectLogoUrl": null,
                  "id": "8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0"
                },
                {
                  "requestedAt": "2023-09-19T05:38:52.590518Z",
                  "status": "MISSING_PAYOUT_INFO",
                  "amount": {
                    "total": 1000,
                    "currency": "USD",
                    "dollarsEquivalent": 1000
                  },
                  "numberOfRewardedContributions": 25,
                  "rewardedOnProjectName": "QA new contributions",
                  "rewardedOnProjectLogoUrl": null,
                  "id": "85f8358c-5339-42ac-a577-16d7760d1e28"
                },
                {
                  "requestedAt": "2023-09-19T05:39:23.730967Z",
                  "status": "MISSING_PAYOUT_INFO",
                  "amount": {
                    "total": 9511147,
                    "currency": "STARK",
                    "dollarsEquivalent": 0
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
              "nextPageIndex": 0
            }
            """;
    private static final String ME_GET_REWARDS_RESPONSE_JSON = """
            {
                "rewards": [
                    {
                        "requestedAt": "2023-09-19T05:40:26.971981Z",
                        "status": "PENDING_INVOICE",
                        "amount": {
                            "total": 1000,
                            "currency": "USD",
                            "dollarsEquivalent": 1000
                        },
                        "numberOfRewardedContributions": 25,
                        "rewardedOnProjectName": "QA new contributions",
                        "rewardedOnProjectLogoUrl": null,
                        "id": "40fda3c6-2a3f-4cdd-ba12-0499dd232d53"
                    },
                    {
                        "requestedAt": "2023-09-19T05:39:54.45638Z",
                        "status": "PENDING_INVOICE",
                        "amount": {
                            "total": 1000,
                            "currency": "USD",
                            "dollarsEquivalent": 1000
                        },
                        "numberOfRewardedContributions": 25,
                        "rewardedOnProjectName": "QA new contributions",
                        "rewardedOnProjectLogoUrl": null,
                        "id": "8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0"
                    },
                    {
                        "requestedAt": "2023-09-19T05:39:23.730967Z",
                        "status": "PENDING_INVOICE",
                        "amount": {
                            "total": 1000,
                            "currency": "USD",
                            "dollarsEquivalent": 1000
                        },
                        "numberOfRewardedContributions": 25,
                        "rewardedOnProjectName": "QA new contributions",
                        "rewardedOnProjectLogoUrl": null,
                        "id": "5b96ca1e-4ad2-41c1-8819-520b885d9223"
                    },
                    {
                        "requestedAt": "2023-09-19T05:38:52.590518Z",
                        "status": "PENDING_INVOICE",
                        "amount": {
                            "total": 1000,
                            "currency": "USD",
                            "dollarsEquivalent": 1000
                        },
                        "numberOfRewardedContributions": 25,
                        "rewardedOnProjectName": "QA new contributions",
                        "rewardedOnProjectLogoUrl": null,
                        "id": "85f8358c-5339-42ac-a577-16d7760d1e28"
                    },
                    {
                        "requestedAt": "2023-09-19T05:38:22.018458Z",
                        "status": "PENDING_INVOICE",
                        "amount": {
                            "total": 1000,
                            "currency": "USD",
                            "dollarsEquivalent": 1000
                        },
                        "numberOfRewardedContributions": 25,
                        "rewardedOnProjectName": "QA new contributions",
                        "rewardedOnProjectLogoUrl": null,
                        "id": "2ac80cc6-7e83-4eef-bc0c-932b58f683c0"
                    },
                    {
                        "requestedAt": "2023-09-20T06:46:52.77875Z",
                        "status": "PENDING_INVOICE",
                        "amount": {
                            "total": 1000,
                            "currency": "USD",
                            "dollarsEquivalent": 1000
                        },
                        "numberOfRewardedContributions": 1,
                        "rewardedOnProjectName": "QA new contributions",
                        "rewardedOnProjectLogoUrl": null,
                        "id": "e1498a17-5090-4071-a88a-6f0b0c337c3a"
                    }
                ],
                "hasMore": false,
                "totalPageNumber": 1,
                "totalItemNumber": 6,
                "nextPageIndex": 0
            }""";
    @Autowired
    HasuraUserHelper userHelper;
    @Autowired
    PaymentRequestRepository paymentRequestRepository;
    @Autowired
    CryptoUsdQuotesRepository cryptoUsdQuotesRepository;
    @Autowired
    PaymentRepository paymentRepository;

    @Test
    @Order(1)
    void should_get_my_rewards() {
        // Given
        final String jwt = userHelper.authenticatePierre().jwt();

        // When
        client.get()
                .uri(getApiURI(ME_GET_REWARDS, Map.of("page_index", "0", "page_size", "100000", "sort", "CONTRIBUTION"
                        , "direction", "DESC")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(ME_GET_REWARDS_RESPONSE_JSON);
    }

    @Order(2)
    @Test
    void should_get_my_rewards_given_multi_currencies() {
        final String jwt = userHelper.authenticatePierre().jwt();

        cryptoUsdQuotesRepository.save(CryptoUsdQuotesEntity.builder()
                .currency(CurrencyEnumEntity.eth)
                .price(BigDecimal.valueOf(1500L))
                .updatedAt(new Date())
                .build());
        cryptoUsdQuotesRepository.save(CryptoUsdQuotesEntity.builder()
                .currency(CurrencyEnumEntity.apt)
                .price(BigDecimal.valueOf(200))
                .updatedAt(new Date())
                .build());

        final PaymentRequestEntity paymentRequestEntity = paymentRequestRepository.findById(
                        UUID.fromString("40fda3c6-2a3f-4cdd-ba12-0499dd232d53"))
                .orElseThrow();
        paymentRequestEntity.setCurrency(CurrencyEnumEntity.eth);
        paymentRequestEntity.setAmount(BigDecimal.valueOf(10));
        paymentRequestRepository.save(paymentRequestEntity);
        paymentRepository.save(new PaymentEntity(UUID.randomUUID(), paymentRequestEntity.getAmount(), "ETH",
                JacksonUtil.toJsonNode("{}"), paymentRequestEntity.getId(), new Date()));

        final PaymentRequestEntity paymentRequestEntity2 = paymentRequestRepository.findById(
                        UUID.fromString("e1498a17-5090-4071-a88a-6f0b0c337c3a"))
                .orElseThrow();
        paymentRequestEntity2.setCurrency(CurrencyEnumEntity.eth);
        paymentRequestEntity2.setAmount(BigDecimal.valueOf(50));
        paymentRequestRepository.save(paymentRequestEntity2);

        final PaymentRequestEntity paymentRequestEntity3 = paymentRequestRepository.findById(
                        UUID.fromString("2ac80cc6-7e83-4eef-bc0c-932b58f683c0"))
                .orElseThrow();
        paymentRequestEntity3.setCurrency(CurrencyEnumEntity.apt);
        paymentRequestEntity3.setAmount(BigDecimal.valueOf(500));
        paymentRequestRepository.save(paymentRequestEntity3);

        final PaymentRequestEntity paymentRequestEntity4 = paymentRequestRepository.findById(
                        UUID.fromString("8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0"))
                .orElseThrow();
        paymentRequestEntity4.setCurrency(CurrencyEnumEntity.apt);
        paymentRequestEntity4.setAmount(BigDecimal.valueOf(30));
        paymentRequestRepository.save(paymentRequestEntity4);

        final PaymentRequestEntity paymentRequestEntity5 = paymentRequestRepository.findById(
                        UUID.fromString("5b96ca1e-4ad2-41c1-8819-520b885d9223"))
                .orElseThrow();
        paymentRequestEntity5.setCurrency(CurrencyEnumEntity.stark);
        paymentRequestEntity5.setAmount(BigDecimal.valueOf(9511147));
        paymentRequestRepository.save(paymentRequestEntity5);


        // When
        client.get()
                .uri(getApiURI(String.format(ME_GET_REWARDS), Map.of("page_index", "0", "page_size",
                        "20", "sort", "AMOUNT", "direction", "DESC")))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody()
                .json(GET_USER_REWARDS_WITH_MULTI_CURRENCIES_RESPONSE_JSON);

    }

    @Test
    @Order(3)
    void should_get_my_total_reward_amounts_given_multi_currencies() {
        // Given
        final String jwt = userHelper.authenticatePierre().jwt();

        // When
        client.get()
                .uri(getApiURI(ME_GET_REWARD_TOTAL_AMOUNTS))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_MY_REWARD_AMOUNTS_JSON_RESPONSE);
    }

    @Test
    @Order(4)
    void should_get_my_rewards_with_pending_invoice() throws ParseException {
        // Given
        final String jwt = userHelper.authenticatePierre().jwt();
        paymentRepository.save(
                PaymentEntity.builder().id(UUID.randomUUID()).amount(BigDecimal.ONE)
                        .processedAt(new Date()).currencyCode("FAKE").requestId(UUID.fromString("40fda3c6-2a3f-4cdd-ba12-0499dd232d53"))
                        .receipt(JacksonUtil.toJsonNode("{}")).build());

        paymentRepository.save(
                PaymentEntity.builder().id(UUID.randomUUID()).amount(BigDecimal.ONE)
                        .processedAt(new Date()).currencyCode("FAKE").requestId(UUID.fromString("8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0"))
                        .receipt(JacksonUtil.toJsonNode("{}")).build());

        paymentRepository.save(
                PaymentEntity.builder().id(UUID.randomUUID()).amount(BigDecimal.ONE)
                        .processedAt(new Date()).currencyCode("FAKE").requestId(UUID.fromString("2ac80cc6-7e83-4eef-bc0c-932b58f683c0"))
                        .receipt(JacksonUtil.toJsonNode("{}")).build());

        paymentRepository.save(
                PaymentEntity.builder().id(UUID.randomUUID()).amount(BigDecimal.ONE)
                        .processedAt(new Date()).currencyCode("FAKE").requestId(UUID.fromString("5b96ca1e-4ad2-41c1-8819-520b885d9223"))
                        .receipt(JacksonUtil.toJsonNode("{}")).build());

        paymentRepository.save(
                PaymentEntity.builder().id(UUID.randomUUID()).amount(BigDecimal.ONE)
                        .processedAt(new Date()).currencyCode("FAKE").requestId(UUID.fromString("e1498a17-5090-4071-a88a-6f0b0c337c3a"))
                        .receipt(JacksonUtil.toJsonNode("{}")).build());

        paymentRepository.save(
                PaymentEntity.builder().id(UUID.randomUUID()).amount(BigDecimal.ONE)
                        .processedAt(new Date()).currencyCode("FAKE").requestId(UUID.fromString("85f8358c-5339-42ac-a577-16d7760d1e28"))
                        .receipt(JacksonUtil.toJsonNode("{}")).build());

        // When
        client.get()
                .uri(getApiURI(ME_REWARDS_PENDING_INVOICE))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "rewards": [
                            {
                              "requestedAt": "2023-09-19T05:38:52.590518Z",
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "status": "PENDING_INVOICE",
                              "amount": {
                                "total": 1000,
                                "currency": "USD",
                                "dollarsEquivalent": 1000
                              },
                              "numberOfRewardedContributions": 25,
                              "rewardedOnProjectName": "QA new contributions",
                              "rewardedOnProjectLogoUrl": null,
                              "id": "85f8358c-5339-42ac-a577-16d7760d1e28"
                            },
                            {
                              "requestedAt": "2023-09-20T06:46:52.77875Z",
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "status": "PENDING_INVOICE",
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
                              "requestedAt": "2023-09-19T05:39:23.730967Z",
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "status": "PENDING_INVOICE",
                              "amount": {
                                "total": 9511147,
                                "currency": "STARK",
                                "dollarsEquivalent": 0
                              },
                              "numberOfRewardedContributions": 25,
                              "rewardedOnProjectName": "QA new contributions",
                              "rewardedOnProjectLogoUrl": null,
                              "id": "5b96ca1e-4ad2-41c1-8819-520b885d9223"
                            },
                            {
                              "requestedAt": "2023-09-19T05:38:22.018458Z",
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "status": "PENDING_INVOICE",
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
                              "requestedAt": "2023-09-19T05:39:54.45638Z",
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "status": "PENDING_INVOICE",
                              "amount": {
                                "total": 30,
                                "currency": "APT",
                                "dollarsEquivalent": 6000
                              },
                              "numberOfRewardedContributions": 25,
                              "rewardedOnProjectName": "QA new contributions",
                              "rewardedOnProjectLogoUrl": null,
                              "id": "8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0"
                            },
                            {
                              "requestedAt": "2023-09-19T05:40:26.971981Z",
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "status": "PENDING_INVOICE",
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
                              "requestedAt": "2023-09-19T05:40:26.971981Z",
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "status": "PENDING_INVOICE",
                              "amount": {
                                "total": 10,
                                "currency": "ETH",
                                "dollarsEquivalent": 15000
                              },
                              "numberOfRewardedContributions": 25,
                              "rewardedOnProjectName": "QA new contributions",
                              "rewardedOnProjectLogoUrl": null,
                              "id": "40fda3c6-2a3f-4cdd-ba12-0499dd232d53"
                            }
                          ]
                        }
                         """);

        final PaymentRequestEntity reward1 = paymentRequestRepository.findById(UUID.fromString("40fda3c6-2a3f-4cdd" +
                                                                                               "-ba12-0499dd232d53")).orElseThrow();
        reward1.setInvoiceReceivedAt(new SimpleDateFormat("yyyy-MM-dd").parse("2023-08-13"));
        paymentRequestRepository.save(reward1);

        final PaymentRequestEntity reward2 = paymentRequestRepository.findById(UUID.fromString("2ac80cc6-7e83-4eef" +
                                                                                               "-bc0c-932b58f683c0")).orElseThrow();
        reward2.setInvoiceReceivedAt(new SimpleDateFormat("yyyy-MM-dd").parse("2023-08-14"));
        paymentRequestRepository.save(reward2);

        final PaymentRequestEntity reward3 = paymentRequestRepository.findById(UUID.fromString("5b96ca1e-4ad2-41c1" +
                                                                                               "-8819-520b885d9223")).orElseThrow();
        reward3.setInvoiceReceivedAt(new SimpleDateFormat("yyyy-MM-dd").parse("2023-08-15"));
        paymentRequestRepository.save(reward3);
        paymentRepository.save(new PaymentEntity(UUID.randomUUID(), BigDecimal.ONE, "OP",
                JacksonUtil.toJsonNode("{}"), reward3.getId(), new SimpleDateFormat("yyyy-MM-dd").parse("2023-08-12")));


        client.get()
                .uri(getApiURI(ME_REWARDS_PENDING_INVOICE))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                            "rewards": [
                              {
                                "requestedAt": "2023-09-19T05:39:54.45638Z",
                                "status": "PENDING_INVOICE",
                                "amount": {
                                  "total": 30,
                                  "currency": "APT",
                                  "dollarsEquivalent": 6000
                                },
                                "numberOfRewardedContributions": 25,
                                "rewardedOnProjectName": "QA new contributions",
                                "rewardedOnProjectLogoUrl": null,
                                "id": "8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0"
                              },
                              {
                                "requestedAt": "2023-09-19T05:38:52.590518Z",
                                "status": "PENDING_INVOICE",
                                "amount": {
                                  "total": 1000,
                                  "currency": "USD",
                                  "dollarsEquivalent": 1000
                                },
                                "numberOfRewardedContributions": 25,
                                "rewardedOnProjectName": "QA new contributions",
                                "rewardedOnProjectLogoUrl": null,
                                "id": "85f8358c-5339-42ac-a577-16d7760d1e28"
                              },
                              {
                                "requestedAt": "2023-09-20T06:46:52.77875Z",
                                "status": "PENDING_INVOICE",
                                "amount": {
                                  "total": 50,
                                  "currency": "ETH",
                                  "dollarsEquivalent": 75000
                                },
                                "numberOfRewardedContributions": 1,
                                "rewardedOnProjectName": "QA new contributions",
                                "rewardedOnProjectLogoUrl": null,
                                "id": "e1498a17-5090-4071-a88a-6f0b0c337c3a"
                              }
                            ]
                          }
                         """);
    }
}
