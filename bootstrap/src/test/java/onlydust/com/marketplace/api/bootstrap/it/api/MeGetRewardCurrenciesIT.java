package onlydust.com.marketplace.api.bootstrap.it.api;

import org.junit.jupiter.api.Test;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;


public class MeGetRewardCurrenciesIT extends AbstractMarketplaceApiIT {


    @Test
    void should_get_all_my_reward_currencies() {
        // Given
        final String jwt = userAuthHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(ME_GET_REWARD_CURRENCIES))
                .header("Authorization", BEARER_PREFIX + jwt)
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
                              "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                              "code": "STRK",
                              "name": "StarkNet Token",
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
                              "logoUrl": null,
                              "decimals": 6
                            }
                          ]
                        }
                        """);
    }
}
