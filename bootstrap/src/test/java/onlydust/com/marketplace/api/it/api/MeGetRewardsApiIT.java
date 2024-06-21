package onlydust.com.marketplace.api.it.api;

import jakarta.persistence.EntityManagerFactory;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagMe;
import onlydust.com.marketplace.api.contract.model.MyRewardsPageResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardStatusDataEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CurrencyRepository;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;


@TagMe
public class MeGetRewardsApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    CurrencyRepository currencyRepository;
    @Autowired
    BillingProfileStoragePort billingProfileStoragePort;
    @Autowired
    EntityManagerFactory entityManagerFactory;

    UserAuthHelper.AuthenticatedUser pierre;

    @BeforeEach
    void setup() {
        pierre = userAuthHelper.authenticatePierre();

        accountingHelper.patchBillingProfile(UUID.fromString("20282367-56b0-42d3-81d3-5e4b38f67e3e"), BillingProfile.Type.COMPANY,
                VerificationStatus.VERIFIED);

        accountingHelper.patchReward("40fda3c6-2a3f-4cdd-ba12-0499dd232d53", 10.12345678987654321, "ETH", 15000.112233445566778899, null, "2023-07-12");
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
        // Given
        setUnlockDateToSomeReward();

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
                              "billingProfileId": "20282367-56b0-42d3-81d3-5e4b38f67e3e",
                              "status": "PENDING_REQUEST",
                              "unlockDate": null,
                              "amount": {
                                "amount": 500.0,
                                "prettyAmount": 500.0,
                                "currency": {
                                  "id": "48388edb-fda2-4a32-b228-28152a147500",
                                  "code": "APT",
                                  "name": "Aptos Coin",
                                  "logoUrl": null,
                                  "decimals": 8
                                },
                                "usdEquivalent": 100000.0,
                                "usdConversionRate": 200.0
                              },
                              "numberOfRewardedContributions": 25,
                              "rewardedOnProjectName": "QA new contributions",
                              "rewardedOnProjectLogoUrl": null,
                              "rewardedUser": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true
                              },
                              "id": "2ac80cc6-7e83-4eef-bc0c-932b58f683c0"
                            },
                            {
                              "requestedAt": "2023-09-20T08:46:52.77875Z",
                              "processedAt": "2023-08-12T00:00:00Z",
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "billingProfileId": "20282367-56b0-42d3-81d3-5e4b38f67e3e",
                              "status": "COMPLETE",
                              "unlockDate": null,
                              "amount": {
                                "amount": 50.0,
                                "prettyAmount": 50.0,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 75000.0,
                                "usdConversionRate": 1500.0
                              },
                              "numberOfRewardedContributions": 1,
                              "rewardedOnProjectName": "QA new contributions",
                              "rewardedOnProjectLogoUrl": null,
                              "rewardedUser": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true
                              },
                              "id": "e1498a17-5090-4071-a88a-6f0b0c337c3a"
                            },
                            {
                              "requestedAt": "2023-09-19T07:40:26.971981Z",
                              "processedAt": "2023-07-12T00:00:00Z",
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "billingProfileId": "20282367-56b0-42d3-81d3-5e4b38f67e3e",
                              "status": "COMPLETE",
                              "unlockDate": null,
                              "amount": {
                                "amount": 10.123456789876544,
                                "prettyAmount": 10.12346,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 15000.11,
                                "usdConversionRate": 1481.718403583811
                              },
                              "numberOfRewardedContributions": 25,
                              "rewardedOnProjectName": "QA new contributions",
                              "rewardedOnProjectLogoUrl": null,
                              "rewardedUser": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true
                              },
                              "id": "40fda3c6-2a3f-4cdd-ba12-0499dd232d53"
                            },
                            {
                              "requestedAt": "2023-09-19T07:38:52.590518Z",
                              "processedAt": null,
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "billingProfileId": "20282367-56b0-42d3-81d3-5e4b38f67e3e",
                              "status": "PENDING_REQUEST",
                              "unlockDate": null,
                              "amount": {
                                "amount": 1000,
                                "prettyAmount": 1000,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "usdEquivalent": 1010.00,
                                "usdConversionRate": 1.0100000000000000
                              },
                              "numberOfRewardedContributions": 25,
                              "rewardedOnProjectName": "QA new contributions",
                              "rewardedOnProjectLogoUrl": null,
                              "rewardedUser": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true
                              },
                              "id": "85f8358c-5339-42ac-a577-16d7760d1e28"
                            },
                            {
                              "requestedAt": "2023-09-19T07:39:54.45638Z",
                              "processedAt": null,
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "billingProfileId": "20282367-56b0-42d3-81d3-5e4b38f67e3e",
                              "status": "PROCESSING",
                              "unlockDate": "2024-08-23T00:00:00Z",
                              "amount": {
                                "amount": 30.0,
                                "prettyAmount": 30.0,
                                "currency": {
                                  "id": "00ca98a5-0197-4b76-a208-4bfc55ea8256",
                                  "code": "OP",
                                  "name": "Optimism",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": null,
                                "usdConversionRate": null
                              },
                              "numberOfRewardedContributions": 25,
                              "rewardedOnProjectName": "QA new contributions",
                              "rewardedOnProjectLogoUrl": null,
                              "rewardedUser": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true
                              },
                              "id": "8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0"
                            },
                            {
                              "requestedAt": "2023-09-19T07:39:23.730967Z",
                              "processedAt": null,
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "billingProfileId": "20282367-56b0-42d3-81d3-5e4b38f67e3e",
                              "status": "PAYOUT_INFO_MISSING",
                              "unlockDate": null,
                              "amount": {
                                "amount": 9511147.0,
                                "prettyAmount": 9511147.0,
                                "currency": {
                                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                  "code": "STRK",
                                  "name": "StarkNet Token",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": null,
                                "usdConversionRate": null
                              },
                              "numberOfRewardedContributions": 25,
                              "rewardedOnProjectName": "QA new contributions",
                              "rewardedOnProjectLogoUrl": null,
                              "rewardedUser": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true
                              },
                              "id": "5b96ca1e-4ad2-41c1-8819-520b885d9223"
                            }
                          ],
                          "hasMore": false,
                          "totalPageNumber": 1,
                          "totalItemNumber": 6,
                          "nextPageIndex": 0,
                          "rewardedAmount": {
                            "totalUsdEquivalent": 191010.11,
                            "totalPerCurrency": [
                              {
                                "amount": 30.0,
                                "prettyAmount": 30.0,
                                "currency": {
                                  "id": "00ca98a5-0197-4b76-a208-4bfc55ea8256",
                                  "code": "OP",
                                  "name": "Optimism",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 0,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 500.0,
                                "prettyAmount": 500.0,
                                "currency": {
                                  "id": "48388edb-fda2-4a32-b228-28152a147500",
                                  "code": "APT",
                                  "name": "Aptos Coin",
                                  "logoUrl": null,
                                  "decimals": 8
                                },
                                "usdEquivalent": 100000.0,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 1000,
                                "prettyAmount": 1000,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "usdEquivalent": 1010.00,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 60.123456789876544,
                                "prettyAmount": 60.12346,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 90000.11,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 9511147.0,
                                "prettyAmount": 9511147.0,
                                "currency": {
                                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                  "code": "STRK",
                                  "name": "StarkNet Token",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 0,
                                "usdConversionRate": null
                              }
                            ]
                          },
                          "pendingAmount": {
                            "totalUsdEquivalent": 101010.00,
                            "totalPerCurrency": [
                              {
                                "amount": 30.0,
                                "prettyAmount": 30.0,
                                "currency": {
                                  "id": "00ca98a5-0197-4b76-a208-4bfc55ea8256",
                                  "code": "OP",
                                  "name": "Optimism",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 0,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 500.0,
                                "prettyAmount": 500.0,
                                "currency": {
                                  "id": "48388edb-fda2-4a32-b228-28152a147500",
                                  "code": "APT",
                                  "name": "Aptos Coin",
                                  "logoUrl": null,
                                  "decimals": 8
                                },
                                "usdEquivalent": 100000.0,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 1000,
                                "prettyAmount": 1000,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "usdEquivalent": 1010.00,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 0,
                                "prettyAmount": 0,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 0,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 9511147.0,
                                "prettyAmount": 9511147.0,
                                "currency": {
                                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                  "code": "STRK",
                                  "name": "StarkNet Token",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 0,
                                "usdConversionRate": null
                              }
                            ]
                          },
                          "receivedRewardsCount": 6,
                          "rewardedContributionsCount": 26,
                          "rewardingProjectsCount": 1,
                          "pendingRequestCount": 2
                        }
                        """);

        // And given
        final var olivier = userAuthHelper.authenticateOlivier();

        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST_COWORKER_INVITATIONS.formatted("20282367-56b0-42d3-81d3-5e4b38f67e3e")))
                .header("Authorization", "Bearer " + pierre.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "githubUserId": %d,
                          "role": "ADMIN"
                        }
                        """.formatted(olivier.user().getGithubUserId()))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        client.post()
                .uri(getApiURI(ME_BILLING_PROFILES_POST_COWORKER_INVITATIONS.formatted("20282367-56b0-42d3-81d3-5e4b38f67e3e")))
                .header("Authorization", "Bearer " + olivier.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "accepted": true
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        client.put()
                .uri(getApiURI(BILLING_PROFILES_COWORKER_ROLE.formatted("20282367-56b0-42d3-81d3-5e4b38f67e3e", pierre.user().getGithubUserId())))
                .header("Authorization", "Bearer " + pierre.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "role": "MEMBER"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

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
                              "billingProfileId": "20282367-56b0-42d3-81d3-5e4b38f67e3e",
                              "status": "PENDING_COMPANY",
                              "unlockDate": null,
                              "amount": {
                                "amount": 500.0,
                                "prettyAmount": 500.0,
                                "currency": {
                                  "id": "48388edb-fda2-4a32-b228-28152a147500",
                                  "code": "APT",
                                  "name": "Aptos Coin",
                                  "logoUrl": null,
                                  "decimals": 8
                                },
                                "usdEquivalent": 100000.0,
                                "usdConversionRate": 200.0
                              },
                              "numberOfRewardedContributions": 25,
                              "rewardedOnProjectName": "QA new contributions",
                              "rewardedOnProjectLogoUrl": null,
                              "rewardedUser": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true
                              },
                              "id": "2ac80cc6-7e83-4eef-bc0c-932b58f683c0"
                            },
                            {
                              "requestedAt": "2023-09-20T08:46:52.77875Z",
                              "processedAt": "2023-08-12T00:00:00Z",
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "billingProfileId": "20282367-56b0-42d3-81d3-5e4b38f67e3e",
                              "status": "COMPLETE",
                              "unlockDate": null,
                              "amount": {
                                "amount": 50.0,
                                "prettyAmount": 50.0,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 75000.0,
                                "usdConversionRate": 1500.0
                              },
                              "numberOfRewardedContributions": 1,
                              "rewardedOnProjectName": "QA new contributions",
                              "rewardedOnProjectLogoUrl": null,
                              "rewardedUser": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true
                              },
                              "id": "e1498a17-5090-4071-a88a-6f0b0c337c3a"
                            },
                            {
                              "requestedAt": "2023-09-19T07:40:26.971981Z",
                              "processedAt": "2023-07-12T00:00:00Z",
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "billingProfileId": "20282367-56b0-42d3-81d3-5e4b38f67e3e",
                              "status": "COMPLETE",
                              "unlockDate": null,
                              "amount": {
                                "amount": 10.123456789876544,
                                "prettyAmount": 10.12346,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 15000.11,
                                "usdConversionRate": 1481.718403583811
                              },
                              "numberOfRewardedContributions": 25,
                              "rewardedOnProjectName": "QA new contributions",
                              "rewardedOnProjectLogoUrl": null,
                              "rewardedUser": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true
                              },
                              "id": "40fda3c6-2a3f-4cdd-ba12-0499dd232d53"
                            },
                            {
                              "requestedAt": "2023-09-19T07:38:52.590518Z",
                              "processedAt": null,
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "billingProfileId": "20282367-56b0-42d3-81d3-5e4b38f67e3e",
                              "status": "PENDING_COMPANY",
                              "unlockDate": null,
                              "amount": {
                                "amount": 1000,
                                "prettyAmount": 1000,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "usdEquivalent": 1010.00,
                                "usdConversionRate": 1.0100000000000000
                              },
                              "numberOfRewardedContributions": 25,
                              "rewardedOnProjectName": "QA new contributions",
                              "rewardedOnProjectLogoUrl": null,
                              "rewardedUser": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true
                              },
                              "id": "85f8358c-5339-42ac-a577-16d7760d1e28"
                            },
                            {
                              "requestedAt": "2023-09-19T07:39:54.45638Z",
                              "processedAt": null,
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "billingProfileId": "20282367-56b0-42d3-81d3-5e4b38f67e3e",
                              "status": "PROCESSING",
                              "unlockDate": "2024-08-23T00:00:00Z",
                              "amount": {
                                "amount": 30.0,
                                "prettyAmount": 30.0,
                                "currency": {
                                  "id": "00ca98a5-0197-4b76-a208-4bfc55ea8256",
                                  "code": "OP",
                                  "name": "Optimism",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": null,
                                "usdConversionRate": null
                              },
                              "numberOfRewardedContributions": 25,
                              "rewardedOnProjectName": "QA new contributions",
                              "rewardedOnProjectLogoUrl": null,
                              "rewardedUser": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true
                              },
                              "id": "8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0"
                            },
                            {
                              "requestedAt": "2023-09-19T07:39:23.730967Z",
                              "processedAt": null,
                              "projectId": "f39b827f-df73-498c-8853-99bc3f562723",
                              "billingProfileId": "20282367-56b0-42d3-81d3-5e4b38f67e3e",
                              "status": "PENDING_COMPANY",
                              "unlockDate": null,
                              "amount": {
                                "amount": 9511147.0,
                                "prettyAmount": 9511147.0,
                                "currency": {
                                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                  "code": "STRK",
                                  "name": "StarkNet Token",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": null,
                                "usdConversionRate": null
                              },
                              "numberOfRewardedContributions": 25,
                              "rewardedOnProjectName": "QA new contributions",
                              "rewardedOnProjectLogoUrl": null,
                              "rewardedUser": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true
                              },
                              "id": "5b96ca1e-4ad2-41c1-8819-520b885d9223"
                            }
                          ],
                          "hasMore": false,
                          "totalPageNumber": 1,
                          "totalItemNumber": 6,
                          "nextPageIndex": 0,
                          "rewardedAmount": {
                            "totalUsdEquivalent": 191010.11,
                            "totalPerCurrency": [
                              {
                                "amount": 30.0,
                                "prettyAmount": 30.0,
                                "currency": {
                                  "id": "00ca98a5-0197-4b76-a208-4bfc55ea8256",
                                  "code": "OP",
                                  "name": "Optimism",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 0,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 500.0,
                                "prettyAmount": 500.0,
                                "currency": {
                                  "id": "48388edb-fda2-4a32-b228-28152a147500",
                                  "code": "APT",
                                  "name": "Aptos Coin",
                                  "logoUrl": null,
                                  "decimals": 8
                                },
                                "usdEquivalent": 100000.0,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 1000,
                                "prettyAmount": 1000,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "usdEquivalent": 1010.00,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 60.123456789876544,
                                "prettyAmount": 60.12346,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 90000.11,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 9511147.0,
                                "prettyAmount": 9511147.0,
                                "currency": {
                                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                  "code": "STRK",
                                  "name": "StarkNet Token",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 0,
                                "usdConversionRate": null
                              }
                            ]
                          },
                          "pendingAmount": {
                            "totalUsdEquivalent": 101010.00,
                            "totalPerCurrency": [
                              {
                                "amount": 30.0,
                                "prettyAmount": 30.0,
                                "currency": {
                                  "id": "00ca98a5-0197-4b76-a208-4bfc55ea8256",
                                  "code": "OP",
                                  "name": "Optimism",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 0,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 500.0,
                                "prettyAmount": 500.0,
                                "currency": {
                                  "id": "48388edb-fda2-4a32-b228-28152a147500",
                                  "code": "APT",
                                  "name": "Aptos Coin",
                                  "logoUrl": null,
                                  "decimals": 8
                                },
                                "usdEquivalent": 100000.0,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 1000,
                                "prettyAmount": 1000,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "usdEquivalent": 1010.00,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 0,
                                "prettyAmount": 0,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 0,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 9511147.0,
                                "prettyAmount": 9511147.0,
                                "currency": {
                                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                  "code": "STRK",
                                  "name": "StarkNet Token",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 0,
                                "usdConversionRate": null
                              }
                            ]
                          },
                          "receivedRewardsCount": 6,
                          "rewardedContributionsCount": 26,
                          "rewardingProjectsCount": 1,
                          "pendingRequestCount": 0
                        }
                        """);
    }

    @ParameterizedTest
    @CsvSource({
            "AMOUNT, DESC",
            "AMOUNT, ASC",
            "CONTRIBUTION, DESC",
            "CONTRIBUTION, ASC",
            "STATUS, DESC",
            "STATUS, ASC",
            "REQUESTED_AT, DESC",
            "REQUESTED_AT, ASC"
    })
    void should_sort_by(String sort, String direction) {
        // When
        final var response = client.get()
                .uri(getApiURI(String.format(ME_GET_REWARDS), Map.of(
                        "pageIndex", "0",
                        "pageSize", "20",
                        "sort", sort,
                        "direction", direction)
                ))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody(MyRewardsPageResponse.class)
                .returnResult().getResponseBody();

        final var rewards = response.getRewards();
        final var first = direction.equals("ASC") ? rewards.get(0) : rewards.get(rewards.size() - 1);
        final var last = direction.equals("ASC") ? rewards.get(rewards.size() - 1) : rewards.get(0);

        switch (sort) {
            case "AMOUNT":
                assertThat(Optional.ofNullable(first.getAmount().getUsdEquivalent()).orElse(BigDecimal.ZERO))
                        .isLessThanOrEqualTo(Optional.ofNullable(last.getAmount().getUsdEquivalent()).orElse(BigDecimal.ZERO));
                break;
            case "CONTRIBUTION":
                assertThat(first.getNumberOfRewardedContributions()).isLessThanOrEqualTo(last.getNumberOfRewardedContributions());
                break;
            case "STATUS":
                assertThat(first.getStatus()).isLessThanOrEqualTo(last.getStatus());
                break;
            case "REQUESTED_AT":
                assertThat(first.getRequestedAt()).isBeforeOrEqualTo(last.getRequestedAt());
                break;
        }
    }

    private void setUnlockDateToSomeReward() {
        final var em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        final var rewardStatusData = em.find(RewardStatusDataEntity.class, UUID.fromString("8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0"))
                .unlockDate(Date.from(ZonedDateTime.parse("2024-08-23T00:00:00Z").toInstant()));
        em.merge(rewardStatusData);
        em.flush();
        em.getTransaction().commit();
        em.close();
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
                .jsonPath("$.rewardedAmount.totalPerCurrency.length()").isEqualTo(1)
                .jsonPath("$.rewardedAmount.totalPerCurrency[0].amount").isEqualTo(50)
                .jsonPath("$.rewardedAmount.totalPerCurrency[0].currency.code").isEqualTo("ETH")
                .jsonPath("$.rewardedAmount.totalUsdEquivalent").isEqualTo(75000)
                .jsonPath("$.pendingAmount.totalPerCurrency.length()").isEqualTo(1)
                .jsonPath("$.pendingAmount.totalPerCurrency[0].amount").isEqualTo(0)
                .jsonPath("$.pendingAmount.totalPerCurrency[0].currency.code").isEqualTo("ETH")
                .jsonPath("$.pendingAmount.totalUsdEquivalent").isEqualTo(0)
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
                .jsonPath("$.rewards[?(@.amount.currency.code == 'ETH')]").exists()
                .jsonPath("$.rewards[?(@.amount.currency.code != 'ETH')]").doesNotExist()
                .jsonPath("$.rewardedAmount.totalPerCurrency.length()").isEqualTo(1)
                .jsonPath("$.rewardedAmount.totalPerCurrency[0].amount").isEqualTo(60)
                .jsonPath("$.rewardedAmount.totalPerCurrency[0].currency.code").isEqualTo("ETH")
                .jsonPath("$.rewardedAmount.totalUsdEquivalent").isEqualTo(90000.11)
                .jsonPath("$.pendingAmount.totalPerCurrency.length()").isEqualTo(1)
                .jsonPath("$.pendingAmount.totalPerCurrency[0].amount").isEqualTo(0)
                .jsonPath("$.pendingAmount.totalPerCurrency[0].currency.code").isEqualTo("ETH")
                .jsonPath("$.pendingAmount.totalUsdEquivalent").isEqualTo(0.00)
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
                .jsonPath("$.rewardedAmount.totalPerCurrency.length()").isEqualTo(1)
                .jsonPath("$.rewardedAmount.totalPerCurrency[0].amount").isEqualTo(12500)
                .jsonPath("$.rewardedAmount.totalPerCurrency[0].currency.code").isEqualTo("USDC")
                .jsonPath("$.rewardedAmount.totalUsdEquivalent").isEqualTo(2683070)
                .jsonPath("$.pendingAmount.totalPerCurrency.length()").isEqualTo(1)
                .jsonPath("$.pendingAmount.totalPerCurrency[0].amount").isEqualTo(12000)
                .jsonPath("$.pendingAmount.totalPerCurrency[0].currency.code").isEqualTo("USDC")
                .jsonPath("$.pendingAmount.totalUsdEquivalent").isEqualTo(1792080.0)
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
                .jsonPath("$.rewards[?(@.amount.currency.code == 'STRK' && @.amount.usdEquivalent == null)]").exists()
                .jsonPath("$.rewards[?(@.amount.currency.code == 'STRK' && @.amount.usdEquivalent != null)]").doesNotExist()
                .json("""
                        {
                          "rewardedAmount": {
                            "totalUsdEquivalent": 191010.11,
                            "totalPerCurrency": [
                              {
                                "amount": 30.0,
                                "prettyAmount": 30.0,
                                "currency": {
                                  "id": "00ca98a5-0197-4b76-a208-4bfc55ea8256",
                                  "code": "OP",
                                  "name": "Optimism",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 0,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 500.0,
                                "prettyAmount": 500.0,
                                "currency": {
                                  "id": "48388edb-fda2-4a32-b228-28152a147500",
                                  "code": "APT",
                                  "name": "Aptos Coin",
                                  "logoUrl": null,
                                  "decimals": 8
                                },
                                "usdEquivalent": 100000.0,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 1000,
                                "prettyAmount": 1000,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "usdEquivalent": 1010.00,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 60.12345678987654,
                                "prettyAmount": 60.12346,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 90000.11,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 9511147.0,
                                "prettyAmount": 9511147.0,
                                "currency": {
                                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                  "code": "STRK",
                                  "name": "StarkNet Token",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 0,
                                "usdConversionRate": null
                              }
                            ]
                          },
                          "pendingAmount": {
                            "totalUsdEquivalent": 101010.00,
                            "totalPerCurrency": [
                              {
                                "amount": 30.0,
                                "prettyAmount": 30.0,
                                "currency": {
                                  "id": "00ca98a5-0197-4b76-a208-4bfc55ea8256",
                                  "code": "OP",
                                  "name": "Optimism",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 0,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 500.0,
                                "prettyAmount": 500.0,
                                "currency": {
                                  "id": "48388edb-fda2-4a32-b228-28152a147500",
                                  "code": "APT",
                                  "name": "Aptos Coin",
                                  "logoUrl": null,
                                  "decimals": 8
                                },
                                "usdEquivalent": 100000.0,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 1000,
                                "prettyAmount": 1000,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "usdEquivalent": 1010.00,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 0,
                                "prettyAmount": 0,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 0,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 9511147.0,
                                "prettyAmount": 9511147.0,
                                "currency": {
                                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                  "code": "STRK",
                                  "name": "StarkNet Token",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 0,
                                "usdConversionRate": null
                              }
                            ]
                          },
                          "receivedRewardsCount": 6,
                          "rewardedContributionsCount": 26,
                          "rewardingProjectsCount": 1,
                          "pendingRequestCount": 2
                        }
                        """)
        ;
    }


    @Test
    void should_get_rewards_filtered_by_status() {
        // When
        client.get()
                .uri(getApiURI(ME_GET_REWARDS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "10",
                        "status", "COMPLETE"
                )))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards[*].status").value(o -> ((List<String>) o).forEach(status -> Assertions.assertEquals("COMPLETE", status)));

        // When
        client.get()
                .uri(getApiURI(ME_GET_REWARDS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "10",
                        "status", "PROCESSING"
                )))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards[*].status").value(o -> ((List<String>) o).forEach(status -> Assertions.assertEquals("PROCESSING", status)));

        // When
        client.get()
                .uri(getApiURI(ME_GET_REWARDS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "10",
                        "status", "PENDING_REQUEST"
                )))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards[*].status").value(o -> ((List<String>) o).forEach(status -> Assertions.assertEquals("PENDING_REQUEST", status)));
    }
}
