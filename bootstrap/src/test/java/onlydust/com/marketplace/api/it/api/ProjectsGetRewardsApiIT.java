package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.api.contract.model.RewardsPageResponse;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagReward;
import onlydust.com.marketplace.kernel.model.blockchain.Optimism;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;


@TagReward
public class ProjectsGetRewardsApiIT extends AbstractMarketplaceApiIT {
    UserAuthHelper.AuthenticatedUser pierre;

    @Autowired
    private BillingProfileStoragePort billingProfileStoragePort;

    @BeforeEach
    void setup() {
        pierre = userAuthHelper.authenticatePierre();

        accountingHelper.patchBillingProfile(UUID.fromString("20282367-56b0-42d3-81d3-5e4b38f67e3e"), BillingProfile.Type.COMPANY,
                VerificationStatus.VERIFIED);

        accountingHelper.patchReward("40fda3c6-2a3f-4cdd-ba12-0499dd232d53", 10.12345678987654321, "ETH", 15000.112233445566778899, null, "2023-07-12");
        accountingHelper.patchReward("e1498a17-5090-4071-a88a-6f0b0c337c3a", 50, "ETH", 75000, null, "2023-08-12");
        accountingHelper.patchReward("2ac80cc6-7e83-4eef-bc0c-932b58f683c0", 500, "APT", 100000, null, null);
        accountingHelper.patchReward("8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0", 30, "OP", 6000, "2023-08-14", null);
        accountingHelper.patchReward("5b96ca1e-4ad2-41c1-8819-520b885d9223", 5, "STRK", null, null, null);

        billingProfileStoragePort.savePayoutInfoForBillingProfile(PayoutInfo.builder()
                .optimismAddress(Optimism.accountAddress("0x" + faker.random().hex(40)))
                .build(), BillingProfile.Id.of("20282367-56b0-42d3-81d3-5e4b38f67e3e"));
    }

    @Test
    void should_return_forbidden_status_when_getting_project_rewards_given_user_not_project_lead() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
        final var projectId = UUID.fromString("298a547f-ecb6-4ab2-8975-68f4e9bf7b39");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of(
                        "pageIndex", "0",
                        "pageSize", "20",
                        "sort", "AMOUNT",
                        "direction", "DESC"
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void should_get_project_rewards() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
        final var projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of(
                        "pageIndex", "0",
                        "pageSize", "20",
                        "sort", "AMOUNT",
                        "direction", "DESC"
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.rewards[0].id").isEqualTo("2ac80cc6-7e83-4eef-bc0c-932b58f683c0")
                .jsonPath("$.rewards[0].status").isEqualTo("PAYOUT_INFO_MISSING")
                .jsonPath("$.rewards[0].amount.currency.code").isEqualTo("APT")
                .jsonPath("$.rewards[0].amount.usdEquivalent").isEqualTo("100000.0")
                .jsonPath("$.rewards[0].amount.amount").isEqualTo("500.0")
                .jsonPath("$.rewards[0].requestedAt").isEqualTo("2023-09-19T07:38:22.018458Z")

                .jsonPath("$.rewards[1].id").isEqualTo("e1498a17-5090-4071-a88a-6f0b0c337c3a")
                .jsonPath("$.rewards[1].status").isEqualTo("COMPLETE")
                .jsonPath("$.rewards[1].amount.currency.code").isEqualTo("ETH")
                .jsonPath("$.rewards[1].amount.usdEquivalent").isEqualTo("75000.0")
                .jsonPath("$.rewards[1].amount.amount").isEqualTo("50.0")
                .jsonPath("$.rewards[1].requestedAt").isEqualTo("2023-09-20T08:46:52.77875Z")

                .jsonPath("$.rewards[2].id").isEqualTo("40fda3c6-2a3f-4cdd-ba12-0499dd232d53")
                .jsonPath("$.rewards[2].status").isEqualTo("COMPLETE")
                .jsonPath("$.rewards[2].amount.currency.code").isEqualTo("ETH")
                .jsonPath("$.rewards[2].amount.usdEquivalent").isEqualTo("15000.11")
                .jsonPath("$.rewards[2].amount.amount").isEqualTo("10.123456789876544")
                .jsonPath("$.rewards[2].amount.prettyAmount").isEqualTo("10.12346")
                .jsonPath("$.rewards[2].requestedAt").isEqualTo("2023-09-19T07:40:26.971981Z")

                .jsonPath("$.rewards[3].id").isEqualTo("8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0")
                .jsonPath("$.rewards[3].status").isEqualTo("PROCESSING")
                .jsonPath("$.rewards[3].amount.currency.code").isEqualTo("OP")
                .jsonPath("$.rewards[3].amount.usdEquivalent").isEqualTo("6000.0")
                .jsonPath("$.rewards[3].amount.amount").isEqualTo("30.0")
                .jsonPath("$.rewards[3].requestedAt").isEqualTo("2023-09-19T07:39:54.45638Z")

                .jsonPath("$.rewards[4].id").isEqualTo("85f8358c-5339-42ac-a577-16d7760d1e28")
                .jsonPath("$.rewards[4].status").isEqualTo("PAYOUT_INFO_MISSING")
                .jsonPath("$.rewards[4].amount.currency.code").isEqualTo("USDC")
                .jsonPath("$.rewards[4].amount.usdEquivalent").isEqualTo("1010.0")
                .jsonPath("$.rewards[4].requestedAt").isEqualTo("2023-09-19T07:38:52.590518Z")

                .jsonPath("$.rewards[5].id").isEqualTo("5b96ca1e-4ad2-41c1-8819-520b885d9223")
                .jsonPath("$.rewards[5].status").isEqualTo("PAYOUT_INFO_MISSING")
                .jsonPath("$.rewards[5].amount.currency.code").isEqualTo("STRK")
                .jsonPath("$.rewards[5].amount.usdEquivalent").doesNotExist()
                .jsonPath("$.rewards[5].requestedAt").isEqualTo("2023-09-19T07:39:23.730967Z")
        ;
    }

    @Test
    void should_get_projects_rewards_filtered_by_currency() {
        // Given
        final String jwt = userAuthHelper.authenticateGregoire().jwt();
        final var projectId = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of(
                        "pageIndex", "0",
                        "pageSize", "10000",
                        "currencies", currencyRepository.findByCode("ETH").orElseThrow().id().toString()
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.rewards[?(@.amount.currency.code != 'ETH')]").doesNotExist()
                .json("""
                        {
                          "rewards": [],
                          "remainingBudget": {
                            "totalUsdEquivalent": 5345951.96,
                            "totalPerCurrency": [
                              {
                                "amount": 3000,
                                "prettyAmount": 3000,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 5345951.96,
                                "usdConversionRate": null
                              }
                            ]
                          },
                          "spentAmount": {
                            "totalUsdEquivalent": 0.00,
                            "totalPerCurrency": [
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
                                "usdEquivalent": 0.00,
                                "usdConversionRate": null
                              }
                            ]
                          },
                          "sentRewardsCount": 0,
                          "rewardedContributionsCount": 0,
                          "rewardedContributorsCount": 0,
                          "hasMore": false,
                          "totalPageNumber": 0,
                          "totalItemNumber": 0,
                          "nextPageIndex": 0
                        }
                        """);
    }

    @Test
    void should_get_projects_rewards_filtered_by_contributors() {
        // Given
        final String jwt = userAuthHelper.authenticateGregoire().jwt();
        final var projectId = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");

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
                .jsonPath("$.rewards[?(@.rewardedUser.login != 'gregcha')]").doesNotExist()
                .jsonPath("$.rewards[?(@.rewardedUser.login == 'gregcha')]").exists()
                .jsonPath("$.remainingBudget.amount").doesNotExist()
                .jsonPath("$.remainingBudget.currency").doesNotExist()
                .jsonPath("$.remainingBudget.totalUsdEquivalent").isEqualTo(5566730)
                .jsonPath("$.spentAmount.amount").doesNotExist()
                .jsonPath("$.spentAmount.currency").doesNotExist()
                .jsonPath("$.spentAmount.totalUsdEquivalent").isEqualTo(9060)
                .jsonPath("$.sentRewardsCount").isEqualTo(9)
                .jsonPath("$.rewardedContributionsCount").isEqualTo(3)
                .jsonPath("$.rewardedContributorsCount").isEqualTo(1)
        ;
    }

    @Test
    void should_get_projects_rewards_filtered_by_date() {
        // Given
        final String jwt = userAuthHelper.authenticateGregoire().jwt();
        final var projectId = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");

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
                .jsonPath("$.remainingBudget.totalUsdEquivalent").isEqualTo(5566730)
                .jsonPath("$.spentAmount.amount").doesNotExist()
                .jsonPath("$.spentAmount.currency").doesNotExist()
                .jsonPath("$.spentAmount.totalUsdEquivalent").isEqualTo(2020)
                .jsonPath("$.sentRewardsCount").isEqualTo(2)
                .jsonPath("$.rewardedContributionsCount").isEqualTo(1)
                .jsonPath("$.rewardedContributorsCount").isEqualTo(1)
        ;
    }

    @Test
    void should_return_empty_state_when_no_result_found() {

        // Given
        final String jwt = userAuthHelper.authenticateGregoire().jwt();
        final var projectId = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of(
                        "pageIndex", "0",
                        "pageSize", "10000",
                        "fromDate", "2023-09-25",
                        "toDate", "2023-09-25",
                        "currencies", currencyRepository.findByCode("ETH").orElseThrow().id().toString()
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "rewards": [],
                          "remainingBudget": {
                            "totalUsdEquivalent": 5345951.96,
                            "totalPerCurrency": [
                              {
                                "amount": 3000,
                                "prettyAmount": 3000,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 5345951.96,
                                "usdConversionRate": null
                              }
                            ]
                          },
                          "spentAmount": {
                            "totalUsdEquivalent": 0.00,
                            "totalPerCurrency": [
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
                                "usdEquivalent": 0.00,
                                "usdConversionRate": null
                              }
                            ]
                          },
                          "sentRewardsCount": 0,
                          "rewardedContributionsCount": 0,
                          "rewardedContributorsCount": 0,
                          "hasMore": false,
                          "totalPageNumber": 0,
                          "totalItemNumber": 0,
                          "nextPageIndex": 0
                        }
                        """)
        ;


        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of(
                        "pageIndex", "0",
                        "pageSize", "10000",
                        "fromDate", "2020-09-25",
                        "toDate", "2020-09-25",
                        "currencies", currencyRepository.findByCode("USD").orElseThrow().id().toString()
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "rewards": [],
                          "remainingBudget": {
                            "totalUsdEquivalent": 0,
                            "totalPerCurrency": [
                              {
                                "amount": 0,
                                "prettyAmount": 0,
                                "currency": {
                                  "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                  "code": "USD",
                                  "name": "US Dollar",
                                  "logoUrl": null,
                                  "decimals": 2
                                },
                                "usdEquivalent": 0,
                                "usdConversionRate": null
                              }
                            ]
                          },
                          "spentAmount": {
                            "totalUsdEquivalent": 0,
                            "totalPerCurrency": [
                              {
                                "amount": 0,
                                "prettyAmount": 0,
                                "currency": {
                                  "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                  "code": "USD",
                                  "name": "US Dollar",
                                  "logoUrl": null,
                                  "decimals": 2
                                },
                                "usdEquivalent": 0,
                                "usdConversionRate": null
                              }
                            ]
                          },
                          "sentRewardsCount": 0,
                          "rewardedContributionsCount": 0,
                          "rewardedContributorsCount": 0,
                          "hasMore": false,
                          "totalPageNumber": 0,
                          "totalItemNumber": 0,
                          "nextPageIndex": 0
                        }
                        """)
        ;


        //When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of(
                        "pageIndex", "0",
                        "pageSize", "10000",
                        "fromDate", "2020-09-25",
                        "toDate", "2020-09-25",
                        "currencies", currencyRepository.findByCode("USDC").orElseThrow().id().toString()
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "rewards": [],
                          "remainingBudget": {
                            "totalUsdEquivalent": 100242.60,
                            "totalPerCurrency": [
                              {
                                "amount": 99250.00,
                                "prettyAmount": 99250.00,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "usdEquivalent": 100242.60,
                                "usdConversionRate": null
                              }
                            ]
                          },
                          "spentAmount": {
                            "totalUsdEquivalent": 0.00,
                            "totalPerCurrency": [
                              {
                                "amount": 0,
                                "prettyAmount": 0,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "usdEquivalent": 0.00,
                                "usdConversionRate": null
                              }
                            ]
                          },
                          "sentRewardsCount": 0,
                          "rewardedContributionsCount": 0,
                          "rewardedContributorsCount": 0
                        }
                        """)
        ;
    }

    @Test
    void should_get_projects_rewards_when_no_usd_equivalent() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
        final var projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of(
                        "pageIndex", "0",
                        "pageSize", "20"
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.rewards[?(@.amount.currency.code == 'STRK' && @.amount.usdEquivalent == null)]").exists()
                .jsonPath("$.rewards[?(@.amount.currency.code == 'STRK' && @.amount.usdEquivalent != null)]").doesNotExist()
                .jsonPath("$.spentAmount.totalUsdEquivalent").isEqualTo(1010)
                .jsonPath("$.remainingBudget.totalUsdEquivalent").isEqualTo(4040)
        ;
    }

    @Test
    void should_get_projects_rewards_with_search() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
        final var projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of(
                        "pageIndex", "0",
                        "pageSize", "20",
                        "search", "STRK"
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.rewards[*].amount.currency.code").isEqualTo("STRK")
        ;

        // When
        final RewardsPageResponse responseBody = client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of(
                        "pageIndex", "0",
                        "pageSize", "20",
                        "search", "PierreO"
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody(RewardsPageResponse.class)
                .returnResult().getResponseBody();

        responseBody.getRewards()
                .forEach(reward -> assertEquals("PierreOucif", reward.getRewardedUser().getLogin()));


        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId), Map.of(
                        "pageIndex", "0",
                        "pageSize", "20",
                        "search", "2AC80"
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.rewards[0].id").isEqualTo("2ac80cc6-7e83-4eef-bc0c-932b58f683c0")
                .jsonPath("$.totalItemNumber").isEqualTo(1);

    }
}
