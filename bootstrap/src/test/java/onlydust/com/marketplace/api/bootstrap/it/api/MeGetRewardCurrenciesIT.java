package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.VerificationStatusEntity;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.springframework.http.MediaType.APPLICATION_JSON;


public class MeGetRewardCurrenciesIT extends AbstractMarketplaceApiIT {


    @Test
    void should_get_all_my_reward_currencies() {
        // When
        client.get()
                .uri(getApiURI(ME_GET_REWARD_CURRENCIES))
                .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticateOlivier().jwt())
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "currencies": [
                            {
                              "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                              "code": "USD",
                              "name": "US Dollar",
                              "logoUrl": null,
                              "decimals": 2
                            },
                            {
                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                              "code": "USDC",
                              "name": "USD Coin",
                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                              "decimals": 6
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_also_get_reward_currencies_of_managed_billing_profiles() {
        // Given
        final var pierre = userAuthHelper.authenticatePierre();
        final var olivier = userAuthHelper.authenticateOlivier();

        accountingHelper.patchBillingProfile(UUID.fromString("20282367-56b0-42d3-81d3-5e4b38f67e3e"), BillingProfile.Type.COMPANY,
                VerificationStatusEntity.VERIFIED);
        accountingHelper.patchReward("40fda3c6-2a3f-4cdd-ba12-0499dd232d53", 10, "ETH", 15000, null, "2023-07-12");

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

        // When
        client.get()
                .uri(getApiURI(ME_GET_REWARD_CURRENCIES))
                .header("Authorization", BEARER_PREFIX + olivier.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "currencies": [
                            {
                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                              "code": "ETH",
                              "name": "Ether",
                              "logoUrl": null,
                              "decimals": 18
                            },
                            {
                              "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                              "code": "USD",
                              "name": "US Dollar",
                              "logoUrl": null,
                              "decimals": 2
                            },
                            {
                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                              "code": "USDC",
                              "name": "USD Coin",
                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                              "decimals": 6
                            }
                          ]
                        }
                        """);
    }
}
