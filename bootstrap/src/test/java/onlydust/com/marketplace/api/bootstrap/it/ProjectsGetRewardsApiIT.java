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
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@ActiveProfiles({"hasura_auth"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectsGetRewardsApiIT extends AbstractMarketplaceApiIT {


    private static final String GET_PROJECT_REWARDS_JSON_RESPONSE_PAGE_1 = """
            {
                "rewards": [
                    {
                        "requestedAt": "2023-09-19T05:40:26.971981Z",
                        "status": "PROCESSING",
                        "amount": {
                            "total": 1000,
                            "currency": "USD",
                            "dollarsEquivalent": 1000
                        },
                        "numberOfRewardedContributions": 25,
                        "rewardedUserLogin": "PierreOucif",
                        "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/16590657?v=4",
                        "id": "40fda3c6-2a3f-4cdd-ba12-0499dd232d53"
                    },
                    {
                        "requestedAt": "2023-09-20T06:46:52.77875Z",
                        "status": "PROCESSING",
                        "amount": {
                            "total": 1000,
                            "currency": "USD",
                            "dollarsEquivalent": 1000
                        },
                        "numberOfRewardedContributions": 1,
                        "rewardedUserLogin": "PierreOucif",
                        "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/16590657?v=4",
                        "id": "e1498a17-5090-4071-a88a-6f0b0c337c3a"
                    }
                ],
                "hasMore": false,
                "totalPageNumber": 2,
                "totalItemNumber": 6,
                "nextPageIndex": 1
            }""";
    private static final String GET_PROJECT_REWARDS_JSON_RESPONSE_PAGE_0 = """
            {
                "rewards": [
                    {
                        "requestedAt": "2023-09-19T05:38:22.018458Z",
                        "status": "PROCESSING",
                        "amount": {
                            "total": 1000,
                            "currency": "USD",
                            "dollarsEquivalent": 1000
                        },
                        "numberOfRewardedContributions": 25,
                        "rewardedUserLogin": "PierreOucif",
                        "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/16590657?v=4",
                        "id": "2ac80cc6-7e83-4eef-bc0c-932b58f683c0"
                    },
                    {
                        "requestedAt": "2023-09-19T05:38:52.590518Z",
                        "status": "PROCESSING",
                        "amount": {
                            "total": 1000,
                            "currency": "USD",
                            "dollarsEquivalent": 1000
                        },
                        "numberOfRewardedContributions": 25,
                        "rewardedUserLogin": "PierreOucif",
                        "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/16590657?v=4",
                        "id": "85f8358c-5339-42ac-a577-16d7760d1e28"
                    },
                    {
                        "requestedAt": "2023-09-19T05:39:23.730967Z",
                        "status": "PROCESSING",
                        "amount": {
                            "total": 1000,
                            "currency": "USD",
                            "dollarsEquivalent": 1000
                        },
                        "numberOfRewardedContributions": 25,
                        "rewardedUserLogin": "PierreOucif",
                        "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/16590657?v=4",
                        "id": "5b96ca1e-4ad2-41c1-8819-520b885d9223"
                    },
                    {
                        "requestedAt": "2023-09-19T05:39:54.45638Z",
                        "status": "PROCESSING",
                        "amount": {
                            "total": 1000,
                            "currency": "USD",
                            "dollarsEquivalent": 1000
                        },
                        "numberOfRewardedContributions": 25,
                        "rewardedUserLogin": "PierreOucif",
                        "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/16590657?v=4",
                        "id": "8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0"
                    }
                ],
                "hasMore": true,
                "totalPageNumber": 2,
                "totalItemNumber": 6,
                "nextPageIndex": 1
            }""";
    private static final String GET_PROJECT_REWARDS_JSON_RESPONSE = """
            {
                 "rewards": [
                     {
                         "requestedAt": "2023-09-20T06:46:52.77875Z",
                         "status": "PROCESSING",
                         "amount": {
                             "total": 1000,
                             "currency": "USD",
                             "dollarsEquivalent": 1000
                         },
                         "numberOfRewardedContributions": 1,
                         "rewardedUserLogin": "PierreOucif",
                         "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/16590657?v=4",
                         "id": "e1498a17-5090-4071-a88a-6f0b0c337c3a"
                     },
                     {
                         "requestedAt": "2023-09-19T05:40:26.971981Z",
                         "status": "PROCESSING",
                         "amount": {
                             "total": 1000,
                             "currency": "USD",
                             "dollarsEquivalent": 1000
                         },
                         "numberOfRewardedContributions": 25,
                         "rewardedUserLogin": "PierreOucif",
                         "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/16590657?v=4",
                         "id": "40fda3c6-2a3f-4cdd-ba12-0499dd232d53"
                     },
                     {
                         "requestedAt": "2023-09-19T05:39:54.45638Z",
                         "status": "PROCESSING",
                         "amount": {
                             "total": 1000,
                             "currency": "USD",
                             "dollarsEquivalent": 1000
                         },
                         "numberOfRewardedContributions": 25,
                         "rewardedUserLogin": "PierreOucif",
                         "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/16590657?v=4",
                         "id": "8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0"
                     },
                     {
                         "requestedAt": "2023-09-19T05:39:23.730967Z",
                         "status": "PROCESSING",
                         "amount": {
                             "total": 1000,
                             "currency": "USD",
                             "dollarsEquivalent": 1000
                         },
                         "numberOfRewardedContributions": 25,
                         "rewardedUserLogin": "PierreOucif",
                         "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/16590657?v=4",
                         "id": "5b96ca1e-4ad2-41c1-8819-520b885d9223"
                     },
                     {
                         "requestedAt": "2023-09-19T05:38:52.590518Z",
                         "status": "PROCESSING",
                         "amount": {
                             "total": 1000,
                             "currency": "USD",
                             "dollarsEquivalent": 1000
                         },
                         "numberOfRewardedContributions": 25,
                         "rewardedUserLogin": "PierreOucif",
                         "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/16590657?v=4",
                         "id": "85f8358c-5339-42ac-a577-16d7760d1e28"
                     },
                     {
                         "requestedAt": "2023-09-19T05:38:22.018458Z",
                         "status": "PROCESSING",
                         "amount": {
                             "total": 1000,
                             "currency": "USD",
                             "dollarsEquivalent": 1000
                         },
                         "numberOfRewardedContributions": 25,
                         "rewardedUserLogin": "PierreOucif",
                         "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/16590657?v=4",
                         "id": "2ac80cc6-7e83-4eef-bc0c-932b58f683c0"
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
    @Order(0)
    void should_return_forbidden_status_when_getting_project_rewards_given_user_not_project_lead() {
        // Given
        final String jwt = userHelper.authenticatePierre().jwt();
        final UUID projectId = UUID.fromString("298a547f-ecb6-4ab2-8975-68f4e9bf7b39");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_BUDGETS, projectId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(1)
    void should_get_projects_rewards() {
        // Given
        final String jwt = userHelper.authenticatePierre().jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of("page_index", "0", "page_size",
                        "10000", "sort", "AMOUNT", "direction", "DESC")))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECT_REWARDS_JSON_RESPONSE);

        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of("page_index", "0", "page_size",
                        "4", "sort", "REQUESTED_AT", "direction", "ASC")))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .json(GET_PROJECT_REWARDS_JSON_RESPONSE_PAGE_0);

        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of("page_index", "1", "page_size",
                        "4", "sort", "REQUESTED_AT", "direction", "ASC")))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody()
                .json(GET_PROJECT_REWARDS_JSON_RESPONSE_PAGE_1);
    }

    @Test
    @Order(2)
    void should_get_project_rewards_with_multiple_currencies() {
        // Given
        final String jwt = userHelper.authenticatePierre().jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

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

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of("page_index", "0", "page_size",
                        "20", "sort", "AMOUNT", "direction", "DESC")))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody()
                .jsonPath("$.rewards[0].id").isEqualTo("2ac80cc6-7e83-4eef-bc0c-932b58f683c0")
                .jsonPath("$.rewards[0].status").isEqualTo("PROCESSING")
                .jsonPath("$.rewards[0].amount.currency").isEqualTo("APT")
                .jsonPath("$.rewards[0].amount.dollarsEquivalent").isEqualTo("100000")
                .jsonPath("$.rewards[0].amount.total").isEqualTo("500")
                .jsonPath("$.rewards[0].requestedAt").isEqualTo("2023-09-19T05:38:22.018458Z")

                .jsonPath("$.rewards[1].id").isEqualTo("e1498a17-5090-4071-a88a-6f0b0c337c3a")
                .jsonPath("$.rewards[1].status").isEqualTo("PROCESSING")
                .jsonPath("$.rewards[1].amount.currency").isEqualTo("ETH")
                .jsonPath("$.rewards[1].amount.dollarsEquivalent").isEqualTo("75000")
                .jsonPath("$.rewards[1].amount.total").isEqualTo("50")
                .jsonPath("$.rewards[1].requestedAt").isEqualTo("2023-09-20T06:46:52.77875Z")

                .jsonPath("$.rewards[2].id").isEqualTo("40fda3c6-2a3f-4cdd-ba12-0499dd232d53")
                .jsonPath("$.rewards[2].status").isEqualTo("COMPLETE")
                .jsonPath("$.rewards[2].amount.currency").isEqualTo("ETH")
                .jsonPath("$.rewards[2].amount.dollarsEquivalent").isEqualTo("15000")
                .jsonPath("$.rewards[2].amount.total").isEqualTo("10")
                .jsonPath("$.rewards[2].requestedAt").isEqualTo("2023-09-19T05:40:26.971981Z")

                .jsonPath("$.rewards[3].id").isEqualTo("8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0")
                .jsonPath("$.rewards[3].status").isEqualTo("PROCESSING")
                .jsonPath("$.rewards[3].amount.currency").isEqualTo("APT")
                .jsonPath("$.rewards[3].amount.dollarsEquivalent").isEqualTo("6000")
                .jsonPath("$.rewards[3].amount.total").isEqualTo("30")
                .jsonPath("$.rewards[3].requestedAt").isEqualTo("2023-09-19T05:39:54.45638Z")

                .jsonPath("$.rewards[4].id").isEqualTo("5b96ca1e-4ad2-41c1-8819-520b885d9223")
                .jsonPath("$.rewards[4].status").isEqualTo("PROCESSING")
                .jsonPath("$.rewards[4].amount.currency").isEqualTo("USD")
                .jsonPath("$.rewards[4].amount.dollarsEquivalent").isEqualTo("1000")
                .jsonPath("$.rewards[4].requestedAt").isEqualTo("2023-09-19T05:39:23.730967Z")

                .jsonPath("$.rewards[5].id").isEqualTo("85f8358c-5339-42ac-a577-16d7760d1e28")
                .jsonPath("$.rewards[5].status").isEqualTo("PROCESSING")
                .jsonPath("$.rewards[5].amount.currency").isEqualTo("USD")
                .jsonPath("$.rewards[5].amount.dollarsEquivalent").isEqualTo("1000")
                .jsonPath("$.rewards[5].requestedAt").isEqualTo("2023-09-19T05:38:52.590518Z");
    }
}
