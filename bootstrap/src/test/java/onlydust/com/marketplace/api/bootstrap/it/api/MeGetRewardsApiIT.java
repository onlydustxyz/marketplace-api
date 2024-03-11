package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.VerificationStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CurrencyRepository;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;


public class MeGetRewardsApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    CurrencyRepository currencyRepository;
    @Autowired
    BillingProfileStoragePort billingProfileStoragePort;

    UserAuthHelper.AuthenticatedUser pierre;

    @BeforeEach
    void setup() {
        pierre = userAuthHelper.authenticatePierre();

        accountingHelper.patchBillingProfile(UUID.fromString("20282367-56b0-42d3-81d3-5e4b38f67e3e"), BillingProfileEntity.Type.COMPANY,
                VerificationStatusEntity.VERIFIED);

        accountingHelper.patchReward("40fda3c6-2a3f-4cdd-ba12-0499dd232d53", 10, "ETH", 15000, null, "2023-07-12");
        accountingHelper.patchReward("e1498a17-5090-4071-a88a-6f0b0c337c3a", 50, "ETH", 75000, null, "2023-08-12");
        accountingHelper.patchReward("2ac80cc6-7e83-4eef-bc0c-932b58f683c0", 500, "APT", 100000, null, null);
        accountingHelper.patchReward("8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0", 30, "OP", null, "2023-08-14", null);
        accountingHelper.patchReward("5b96ca1e-4ad2-41c1-8819-520b885d9223", 9511147, "STRK", null, null, null);

        billingProfileStoragePort.savePayoutInfoForBillingProfile(PayoutInfo.builder()
                .ethWallet(Ethereum.wallet("vitalik.eth"))
                .aptosAddress(Aptos.accountAddress("0x" + faker.random().hex(40)))
                .build(), BillingProfile.Id.of("20282367-56b0-42d3-81d3-5e4b38f67e3e"));
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
                        "currencies", currencyRepository.findByCode("ETH").orElseThrow().id().toString()
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
                .jsonPath("$.rewardedAmount.usdEquivalent").isEqualTo(2683070)
                .jsonPath("$.pendingAmount.amount").doesNotExist()
                .jsonPath("$.pendingAmount.currency").doesNotExist()
                .jsonPath("$.pendingAmount.usdEquivalent").isEqualTo(1792080.0)
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
}
