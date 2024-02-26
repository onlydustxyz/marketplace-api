package onlydust.com.marketplace.api.bootstrap.it.api;

import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.CryptoUsdQuotesEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentRequestEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.BudgetRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.CryptoUsdQuotesRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRequestRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectsGetRewardsApiIT extends AbstractMarketplaceApiIT {


    private static final String GET_PROJECT_REWARDS_JSON_RESPONSE_PAGE_1 = """
            {
              "rewards": [
                {
                  "requestedAt": "2023-09-19T07:40:26.971981Z",
                  "processedAt": null,
                  "status": "PENDING_CONTRIBUTOR",
                  "unlockDate": null,
                  "amount": {
                    "total": 1000,
                    "currency": "USDC",
                    "dollarsEquivalent": 1010.00
                  },
                  "numberOfRewardedContributions": 25,
                  "rewardedUserLogin": "PierreOucif",
                  "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "id": "40fda3c6-2a3f-4cdd-ba12-0499dd232d53"
                },
                {
                  "requestedAt": "2023-09-20T08:46:52.77875Z",
                  "processedAt": null,
                  "status": "PENDING_CONTRIBUTOR",
                  "unlockDate": null,
                  "amount": {
                    "total": 1000,
                    "currency": "USDC",
                    "dollarsEquivalent": 1010.00
                  },
                  "numberOfRewardedContributions": 1,
                  "rewardedUserLogin": "PierreOucif",
                  "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "id": "e1498a17-5090-4071-a88a-6f0b0c337c3a"
                }
              ],
              "remainingBudget": {
                "amount": null,
                "currency": null,
                "usdEquivalent": 360436.00
              },
              "spentAmount": {
                "amount": null,
                "currency": null,
                "usdEquivalent": 6060.00
              },
              "sentRewardsCount": 6,
              "rewardedContributionsCount": 26,
              "rewardedContributorsCount": 1,
              "hasMore": false,
              "totalPageNumber": 2,
              "totalItemNumber": 6,
              "nextPageIndex": 1
            }
            """;
    private static final String GET_PROJECT_REWARDS_JSON_RESPONSE_PAGE_0 = """
            {
              "rewards": [
                {
                  "requestedAt": "2023-09-19T07:38:22.018458Z",
                  "processedAt": null,
                  "status": "PENDING_CONTRIBUTOR",
                  "unlockDate": null,
                  "amount": {
                    "total": 1000,
                    "currency": "USDC",
                    "dollarsEquivalent": 1010.00
                  },
                  "numberOfRewardedContributions": 25,
                  "rewardedUserLogin": "PierreOucif",
                  "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "id": "2ac80cc6-7e83-4eef-bc0c-932b58f683c0"
                },
                {
                  "requestedAt": "2023-09-19T07:38:52.590518Z",
                  "processedAt": null,
                  "status": "PENDING_CONTRIBUTOR",
                  "unlockDate": null,
                  "amount": {
                    "total": 1000,
                    "currency": "USDC",
                    "dollarsEquivalent": 1010.00
                  },
                  "numberOfRewardedContributions": 25,
                  "rewardedUserLogin": "PierreOucif",
                  "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "id": "85f8358c-5339-42ac-a577-16d7760d1e28"
                },
                {
                  "requestedAt": "2023-09-19T07:39:23.730967Z",
                  "processedAt": null,
                  "status": "PENDING_CONTRIBUTOR",
                  "unlockDate": null,
                  "amount": {
                    "total": 1000,
                    "currency": "USDC",
                    "dollarsEquivalent": 1010.00
                  },
                  "numberOfRewardedContributions": 25,
                  "rewardedUserLogin": "PierreOucif",
                  "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "id": "5b96ca1e-4ad2-41c1-8819-520b885d9223"
                },
                {
                  "requestedAt": "2023-09-19T07:39:54.45638Z",
                  "processedAt": null,
                  "status": "PENDING_CONTRIBUTOR",
                  "unlockDate": null,
                  "amount": {
                    "total": 1000,
                    "currency": "USDC",
                    "dollarsEquivalent": 1010.00
                  },
                  "numberOfRewardedContributions": 25,
                  "rewardedUserLogin": "PierreOucif",
                  "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "id": "8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0"
                }
              ],
              "remainingBudget": {
                "amount": null,
                "currency": null,
                "usdEquivalent": 360436.00
              },
              "spentAmount": {
                "amount": null,
                "currency": null,
                "usdEquivalent": 6060.00
              },
              "sentRewardsCount": 6,
              "rewardedContributionsCount": 26,
              "rewardedContributorsCount": 1,
              "hasMore": true,
              "totalPageNumber": 2,
              "totalItemNumber": 6,
              "nextPageIndex": 1
            }
            """;
    private static final String GET_PROJECT_REWARDS_JSON_RESPONSE = """
            {
              "rewards": [
                {
                  "requestedAt": "2023-09-20T08:46:52.77875Z",
                  "processedAt": null,
                  "status": "PENDING_CONTRIBUTOR",
                  "unlockDate": null,
                  "amount": {
                    "total": 1000,
                    "currency": "USDC",
                    "dollarsEquivalent": 1010.00
                  },
                  "numberOfRewardedContributions": 1,
                  "rewardedUserLogin": "PierreOucif",
                  "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "id": "e1498a17-5090-4071-a88a-6f0b0c337c3a"
                },
                {
                  "requestedAt": "2023-09-19T07:40:26.971981Z",
                  "processedAt": null,
                  "status": "PENDING_CONTRIBUTOR",
                  "unlockDate": null,
                  "amount": {
                    "total": 1000,
                    "currency": "USDC",
                    "dollarsEquivalent": 1010.00
                  },
                  "numberOfRewardedContributions": 25,
                  "rewardedUserLogin": "PierreOucif",
                  "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "id": "40fda3c6-2a3f-4cdd-ba12-0499dd232d53"
                },
                {
                  "requestedAt": "2023-09-19T07:39:54.45638Z",
                  "processedAt": null,
                  "status": "PENDING_CONTRIBUTOR",
                  "unlockDate": null,
                  "amount": {
                    "total": 1000,
                    "currency": "USDC",
                    "dollarsEquivalent": 1010.00
                  },
                  "numberOfRewardedContributions": 25,
                  "rewardedUserLogin": "PierreOucif",
                  "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "id": "8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0"
                },
                {
                  "requestedAt": "2023-09-19T07:39:23.730967Z",
                  "processedAt": null,
                  "status": "PENDING_CONTRIBUTOR",
                  "unlockDate": null,
                  "amount": {
                    "total": 1000,
                    "currency": "USDC",
                    "dollarsEquivalent": 1010.00
                  },
                  "numberOfRewardedContributions": 25,
                  "rewardedUserLogin": "PierreOucif",
                  "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "id": "5b96ca1e-4ad2-41c1-8819-520b885d9223"
                },
                {
                  "requestedAt": "2023-09-19T07:38:52.590518Z",
                  "processedAt": null,
                  "status": "PENDING_CONTRIBUTOR",
                  "unlockDate": null,
                  "amount": {
                    "total": 1000,
                    "currency": "USDC",
                    "dollarsEquivalent": 1010.00
                  },
                  "numberOfRewardedContributions": 25,
                  "rewardedUserLogin": "PierreOucif",
                  "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "id": "85f8358c-5339-42ac-a577-16d7760d1e28"
                },
                {
                  "requestedAt": "2023-09-19T07:38:22.018458Z",
                  "processedAt": null,
                  "status": "PENDING_CONTRIBUTOR",
                  "unlockDate": null,
                  "amount": {
                    "total": 1000,
                    "currency": "USDC",
                    "dollarsEquivalent": 1010.00
                  },
                  "numberOfRewardedContributions": 25,
                  "rewardedUserLogin": "PierreOucif",
                  "rewardedUserAvatar": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "id": "2ac80cc6-7e83-4eef-bc0c-932b58f683c0"
                }
              ],
              "remainingBudget": {
                "amount": null,
                "currency": null,
                "usdEquivalent": 360436.00
              },
              "spentAmount": {
                "amount": null,
                "currency": null,
                "usdEquivalent": 6060.00
              },
              "sentRewardsCount": 6,
              "rewardedContributionsCount": 26,
              "rewardedContributorsCount": 1,
              "hasMore": false,
              "totalPageNumber": 1,
              "totalItemNumber": 6,
              "nextPageIndex": 0
            }
            """;


    @Autowired
    PaymentRequestRepository paymentRequestRepository;
    @Autowired
    CryptoUsdQuotesRepository cryptoUsdQuotesRepository;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    BudgetRepository budgetRepository;

    @Test
    @Order(0)
    void should_return_forbidden_status_when_getting_project_rewards_given_user_not_project_lead() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
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
        final String jwt = userAuthHelper.authenticatePierre().jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of("pageIndex", "0", "pageSize",
                        "10000", "sort", "AMOUNT", "direction", "DESC")))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECT_REWARDS_JSON_RESPONSE)
                .jsonPath("$.remainingBudget.amount").doesNotExist()
                .jsonPath("$.remainingBudget.currency").doesNotExist()
                .jsonPath("$.remainingBudget.usdEquivalent").isNumber()
                .jsonPath("$.spentAmount.amount").doesNotExist()
                .jsonPath("$.spentAmount.currency").doesNotExist()
                .jsonPath("$.spentAmount.usdEquivalent").isNumber()
                .jsonPath("$.sentRewardsCount").isNumber()
                .jsonPath("$.rewardedContributionsCount").isNumber()
                .jsonPath("$.rewardedContributorsCount").isNumber()
        ;

        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of("pageIndex", "0", "pageSize",
                        "4", "sort", "REQUESTED_AT", "direction", "ASC")))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .json(GET_PROJECT_REWARDS_JSON_RESPONSE_PAGE_0);

        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of("pageIndex", "1", "pageSize",
                        "4", "sort", "REQUESTED_AT", "direction", "ASC")))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .json(GET_PROJECT_REWARDS_JSON_RESPONSE_PAGE_1);
    }

    @Test
    @Order(2)
    void should_get_project_rewards_with_multiple_currencies() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        final PaymentRequestEntity paymentRequestEntity = paymentRequestRepository.findById(
                        UUID.fromString("40fda3c6-2a3f-4cdd-ba12-0499dd232d53"))
                .orElseThrow();
        paymentRequestEntity.setCurrency(CurrencyEnumEntity.eth);
        paymentRequestEntity.setAmount(BigDecimal.valueOf(10));
        paymentRequestEntity.setUsdAmount(BigDecimal.valueOf(15000));
        paymentRequestRepository.save(paymentRequestEntity);
        paymentRepository.save(new PaymentEntity(UUID.randomUUID(), paymentRequestEntity.getAmount(), "ETH",
                JacksonUtil.toJsonNode("{}"), paymentRequestEntity.getId(), new Date()));

        final PaymentRequestEntity paymentRequestEntity2 = paymentRequestRepository.findById(
                        UUID.fromString("e1498a17-5090-4071-a88a-6f0b0c337c3a"))
                .orElseThrow();
        paymentRequestEntity2.setCurrency(CurrencyEnumEntity.eth);
        paymentRequestEntity2.setAmount(BigDecimal.valueOf(50));
        paymentRequestEntity2.setUsdAmount(BigDecimal.valueOf(75000));
        paymentRequestRepository.save(paymentRequestEntity2);

        final PaymentRequestEntity paymentRequestEntity3 = paymentRequestRepository.findById(
                        UUID.fromString("2ac80cc6-7e83-4eef-bc0c-932b58f683c0"))
                .orElseThrow();
        paymentRequestEntity3.setCurrency(CurrencyEnumEntity.apt);
        paymentRequestEntity3.setAmount(BigDecimal.valueOf(500));
        paymentRequestEntity3.setUsdAmount(BigDecimal.valueOf(100000));
        paymentRequestRepository.save(paymentRequestEntity3);

        final PaymentRequestEntity paymentRequestEntity4 = paymentRequestRepository.findById(
                        UUID.fromString("8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0"))
                .orElseThrow();
        paymentRequestEntity4.setCurrency(CurrencyEnumEntity.apt);
        paymentRequestEntity4.setAmount(BigDecimal.valueOf(30));
        paymentRequestEntity4.setUsdAmount(BigDecimal.valueOf(6000));
        paymentRequestRepository.save(paymentRequestEntity4);

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of("pageIndex", "0", "pageSize",
                        "20", "sort", "AMOUNT", "direction", "DESC")))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.rewards[0].id").isEqualTo("2ac80cc6-7e83-4eef-bc0c-932b58f683c0")
                .jsonPath("$.rewards[0].status").isEqualTo("PENDING_CONTRIBUTOR")
                .jsonPath("$.rewards[0].amount.currency").isEqualTo("APT")
                .jsonPath("$.rewards[0].amount.dollarsEquivalent").isEqualTo("100000")
                .jsonPath("$.rewards[0].amount.total").isEqualTo("500")
                .jsonPath("$.rewards[0].requestedAt").isEqualTo("2023-09-19T07:38:22.018458Z")

                .jsonPath("$.rewards[1].id").isEqualTo("e1498a17-5090-4071-a88a-6f0b0c337c3a")
                .jsonPath("$.rewards[1].status").isEqualTo("PENDING_CONTRIBUTOR")
                .jsonPath("$.rewards[1].amount.currency").isEqualTo("ETH")
                .jsonPath("$.rewards[1].amount.dollarsEquivalent").isEqualTo("75000")
                .jsonPath("$.rewards[1].amount.total").isEqualTo("50")
                .jsonPath("$.rewards[1].requestedAt").isEqualTo("2023-09-20T08:46:52.77875Z")

                .jsonPath("$.rewards[2].id").isEqualTo("40fda3c6-2a3f-4cdd-ba12-0499dd232d53")
                .jsonPath("$.rewards[2].status").isEqualTo("PENDING_CONTRIBUTOR")
                .jsonPath("$.rewards[2].amount.currency").isEqualTo("ETH")
                .jsonPath("$.rewards[2].amount.dollarsEquivalent").isEqualTo("15000")
                .jsonPath("$.rewards[2].amount.total").isEqualTo("10")
                .jsonPath("$.rewards[2].requestedAt").isEqualTo("2023-09-19T07:40:26.971981Z")

                .jsonPath("$.rewards[3].id").isEqualTo("8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0")
                .jsonPath("$.rewards[3].status").isEqualTo("PENDING_CONTRIBUTOR")
                .jsonPath("$.rewards[3].amount.currency").isEqualTo("APT")
                .jsonPath("$.rewards[3].amount.dollarsEquivalent").isEqualTo("6000")
                .jsonPath("$.rewards[3].amount.total").isEqualTo("30")
                .jsonPath("$.rewards[3].requestedAt").isEqualTo("2023-09-19T07:39:54.45638Z")

                .jsonPath("$.rewards[4].id").isEqualTo("5b96ca1e-4ad2-41c1-8819-520b885d9223")
                .jsonPath("$.rewards[4].status").isEqualTo("PENDING_CONTRIBUTOR")
                .jsonPath("$.rewards[4].amount.currency").isEqualTo("USDC")
                .jsonPath("$.rewards[4].amount.dollarsEquivalent").isEqualTo("1010.0")
                .jsonPath("$.rewards[4].requestedAt").isEqualTo("2023-09-19T07:39:23.730967Z")

                .jsonPath("$.rewards[5].id").isEqualTo("85f8358c-5339-42ac-a577-16d7760d1e28")
                .jsonPath("$.rewards[5].status").isEqualTo("PENDING_CONTRIBUTOR")
                .jsonPath("$.rewards[5].amount.currency").isEqualTo("USDC")
                .jsonPath("$.rewards[5].amount.dollarsEquivalent").isEqualTo("1010.0")
                .jsonPath("$.rewards[5].requestedAt").isEqualTo("2023-09-19T07:38:52.590518Z");
    }

    @Test
    @Order(2)
    void should_get_projects_rewards_filtered_by_currency() {
        // Given
        final String jwt = userAuthHelper.authenticateGregoire().jwt();
        final UUID projectId = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");

        final var budget = budgetRepository.findById(UUID.fromString("5cd59ea2-f67a-4723-b90f-f5fd9036d6d1"))
                .orElseThrow();
        budget.setInitialAmount(BigDecimal.valueOf(30));
        budget.setRemainingAmount(BigDecimal.valueOf(28));
        budgetRepository.save(budget);

        cryptoUsdQuotesRepository.save(CryptoUsdQuotesEntity.builder()
                .currency(CurrencyEnumEntity.eth)
                .price(BigDecimal.valueOf(1500L))
                .updatedAt(new Date())
                .build());

        paymentRequestRepository.saveAll(paymentRequestRepository.findAll()
                .stream()
                .filter(p -> p.getProjectId().equals(projectId))
                .filter(p -> p.getCurrency().equals(CurrencyEnumEntity.usdc))
                .limit(7)
                .map(p -> p.toBuilder().amount(BigDecimal.valueOf(1)).currency(CurrencyEnumEntity.eth).build())
                .toList()
        );

        paymentRequestRepository.saveAll(paymentRequestRepository.findAll()
                .stream()
                .filter(p -> p.getProjectId().equals(projectId))
                .filter(p -> p.getCurrency().equals(CurrencyEnumEntity.usd))
                .limit(3)
                .map(p -> p.toBuilder().amount(BigDecimal.valueOf(1)).currency(CurrencyEnumEntity.eth).build())
                .toList()
        );

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of(
                        "pageIndex", "0",
                        "pageSize", "10000",
                        "currencies", "ETH"
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards[?(@.amount.currency != 'ETH')]").doesNotExist()
                .jsonPath("$.remainingBudget.currency").isEqualTo("ETH")
                .jsonPath("$.remainingBudget.amount").isEqualTo(28)
                .jsonPath("$.remainingBudget.usdEquivalent").isEqualTo(42000)
                .jsonPath("$.spentAmount.amount").isEqualTo(10)
                .jsonPath("$.spentAmount.currency").isEqualTo("ETH")
                .jsonPath("$.spentAmount.usdEquivalent").isEqualTo(15000)
                .jsonPath("$.sentRewardsCount").isEqualTo(10)
                .jsonPath("$.rewardedContributionsCount").isEqualTo(4)
                .jsonPath("$.rewardedContributorsCount").isEqualTo(2)
        ;
    }

    @Test
    @Order(3)
    void should_get_projects_rewards_filtered_by_contributors() {
        // Given
        final String jwt = userAuthHelper.authenticateGregoire().jwt();
        final UUID projectId = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of(
                        "pageIndex", "0",
                        "pageSize", "10000",
                        "contributors", "8642470"
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards[?(@.rewardedUserLogin != 'gregcha')]").doesNotExist()
                .jsonPath("$.remainingBudget.amount").doesNotExist()
                .jsonPath("$.remainingBudget.currency").doesNotExist()
                .jsonPath("$.remainingBudget.usdEquivalent").isEqualTo(142242)
                .jsonPath("$.spentAmount.amount").doesNotExist()
                .jsonPath("$.spentAmount.currency").doesNotExist()
                .jsonPath("$.spentAmount.usdEquivalent").isEqualTo(13500)
                .jsonPath("$.sentRewardsCount").isEqualTo(9)
                .jsonPath("$.rewardedContributionsCount").isEqualTo(3)
                .jsonPath("$.rewardedContributorsCount").isEqualTo(1)
        ;
    }

    @Test
    @Order(4)
    void should_get_projects_rewards_filtered_by_date() {
        // Given
        final String jwt = userAuthHelper.authenticateGregoire().jwt();
        final UUID projectId = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of(
                        "pageIndex", "0",
                        "pageSize", "10000",
                        "fromDate", "2023-09-25",
                        "toDate", "2023-09-25"
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                // we have at least one correct date
                .jsonPath("$.rewards[?(@.requestedAt >= '2023-09-25')]").exists()
                .jsonPath("$.rewards[?(@.requestedAt < '2023-09-26')]").exists()
                // we do not have any incorrect date
                .jsonPath("$.rewards[?(@.requestedAt < '2023-09-25')]").doesNotExist()
                .jsonPath("$.rewards[?(@.requestedAt > '2023-09-26')]").doesNotExist()
                .jsonPath("$.remainingBudget.amount").doesNotExist()
                .jsonPath("$.remainingBudget.currency").doesNotExist()
                .jsonPath("$.remainingBudget.usdEquivalent").isEqualTo(142242)
                .jsonPath("$.spentAmount.amount").doesNotExist()
                .jsonPath("$.spentAmount.currency").doesNotExist()
                .jsonPath("$.spentAmount.usdEquivalent").isEqualTo(3000)
                .jsonPath("$.sentRewardsCount").isEqualTo(2)
                .jsonPath("$.rewardedContributionsCount").isEqualTo(1)
                .jsonPath("$.rewardedContributorsCount").isEqualTo(1)
        ;
    }

    @Test
    @Order(5)
    void should_return_empty_state_when_no_result_found() {

        // Given
        final String jwt = userAuthHelper.authenticateGregoire().jwt();
        final UUID projectId = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of(
                        "pageIndex", "0",
                        "pageSize", "10000",
                        "fromDate", "2023-09-25",
                        "toDate", "2023-09-25",
                        "currencies", "STRK"
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.rewards").isEmpty()
                .jsonPath("$.remainingBudget.amount").doesNotExist()
                .jsonPath("$.remainingBudget.currency").doesNotExist()
                .jsonPath("$.remainingBudget.usdEquivalent").isEqualTo(0)
                .jsonPath("$.spentAmount.amount").doesNotExist()
                .jsonPath("$.spentAmount.currency").doesNotExist()
                .jsonPath("$.spentAmount.usdEquivalent").isEqualTo(0)
                .jsonPath("$.sentRewardsCount").isEqualTo(0)
                .jsonPath("$.rewardedContributionsCount").isEqualTo(0)
                .jsonPath("$.rewardedContributorsCount").isEqualTo(0);


        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of(
                        "pageIndex", "0",
                        "pageSize", "10000",
                        "fromDate", "2020-09-25",
                        "toDate", "2020-09-25",
                        "currencies", "USD"
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.rewards").isEmpty()
                .jsonPath("$.remainingBudget.amount").isEqualTo(0)
                .jsonPath("$.remainingBudget.currency").isEqualTo("USD")
                .jsonPath("$.remainingBudget.usdEquivalent").isEqualTo(0)
                .jsonPath("$.spentAmount.amount").isEqualTo(0)
                .jsonPath("$.spentAmount.currency").isEqualTo("USD")
                .jsonPath("$.spentAmount.usdEquivalent").isEqualTo(0)
                .jsonPath("$.sentRewardsCount").isEqualTo(0)
                .jsonPath("$.rewardedContributionsCount").isEqualTo(0)
                .jsonPath("$.rewardedContributorsCount").isEqualTo(0);


        //When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of(
                        "pageIndex", "0",
                        "pageSize", "10000",
                        "fromDate", "2020-09-25",
                        "toDate", "2020-09-25",
                        "currencies", "USDC"
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.rewards").isEmpty()
                .jsonPath("$.remainingBudget.amount").isEqualTo(99250)
                .jsonPath("$.remainingBudget.currency").isEqualTo("USDC")
                .jsonPath("$.remainingBudget.usdEquivalent").isEqualTo(100242)
                .jsonPath("$.spentAmount.amount").isEqualTo(0)
                .jsonPath("$.spentAmount.currency").isEqualTo("USDC")
                .jsonPath("$.spentAmount.usdEquivalent").isEqualTo(0)
                .jsonPath("$.sentRewardsCount").isEqualTo(0)
                .jsonPath("$.rewardedContributionsCount").isEqualTo(0)
                .jsonPath("$.rewardedContributorsCount").isEqualTo(0);
    }


    @Test
    @Order(6)
    void should_get_projects_rewards_when_no_usd_equivalent() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        cryptoUsdQuotesRepository.deleteById(CurrencyEnumEntity.eth);

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of("pageIndex", "0", "pageSize",
                        "20", "sort", "AMOUNT", "direction", "DESC")))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.remainingBudget.usdEquivalent").isEqualTo(4040)
                .jsonPath("$.spentAmount.usdEquivalent").isEqualTo(2020);
    }
}
