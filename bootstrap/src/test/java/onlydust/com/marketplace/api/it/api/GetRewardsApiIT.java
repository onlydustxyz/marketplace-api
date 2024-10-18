package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagMe;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;


@TagMe
public class GetRewardsApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    BillingProfileStoragePort billingProfileStoragePort;

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
    void should_list_pierre_rewards() {
        // When
        client.get()
                .uri(getApiURI(String.format(GET_REWARDS), Map.of(
                        "pageIndex", "0",
                        "pageSize", "5")
                ))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 2,
                          "totalItemNumber": 6,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "rewards": [
                            {
                              "id": "e1498a17-5090-4071-a88a-6f0b0c337c3a",
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
                              "status": "COMPLETE",
                              "from": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true,
                                "id": "fc92397c-3431-4a84-8054-845376b630a0"
                              },
                              "to": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true,
                                "id": "fc92397c-3431-4a84-8054-845376b630a0"
                              },
                              "requestedAt": "2023-09-20T08:46:52.77875Z",
                              "processedAt": "2023-08-12T00:00:00Z",
                              "unlockDate": null
                            },
                            {
                              "id": "40fda3c6-2a3f-4cdd-ba12-0499dd232d53",
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
                              "status": "COMPLETE",
                              "from": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true,
                                "id": "fc92397c-3431-4a84-8054-845376b630a0"
                              },
                              "to": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true,
                                "id": "fc92397c-3431-4a84-8054-845376b630a0"
                              },
                              "requestedAt": "2023-09-19T07:40:26.971981Z",
                              "processedAt": "2023-07-12T00:00:00Z",
                              "unlockDate": null
                            },
                            {
                              "id": "8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0",
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
                              "status": "PROCESSING",
                              "from": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true,
                                "id": "fc92397c-3431-4a84-8054-845376b630a0"
                              },
                              "to": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true,
                                "id": "fc92397c-3431-4a84-8054-845376b630a0"
                              },
                              "requestedAt": "2023-09-19T07:39:54.45638Z",
                              "processedAt": null,
                              "unlockDate": null
                            },
                            {
                              "id": "5b96ca1e-4ad2-41c1-8819-520b885d9223",
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
                              "status": "PAYOUT_INFO_MISSING",
                              "from": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true,
                                "id": "fc92397c-3431-4a84-8054-845376b630a0"
                              },
                              "to": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true,
                                "id": "fc92397c-3431-4a84-8054-845376b630a0"
                              },
                              "requestedAt": "2023-09-19T07:39:23.730967Z",
                              "processedAt": null,
                              "unlockDate": null
                            },
                            {
                              "id": "85f8358c-5339-42ac-a577-16d7760d1e28",
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
                              "status": "PENDING_REQUEST",
                              "from": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true,
                                "id": "fc92397c-3431-4a84-8054-845376b630a0"
                              },
                              "to": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true,
                                "id": "fc92397c-3431-4a84-8054-845376b630a0"
                              },
                              "requestedAt": "2023-09-19T07:38:52.590518Z",
                              "processedAt": null,
                              "unlockDate": null
                            }
                          ]
                        }
                        """);

    }

}
