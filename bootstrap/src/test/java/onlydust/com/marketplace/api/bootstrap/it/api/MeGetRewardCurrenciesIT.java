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
