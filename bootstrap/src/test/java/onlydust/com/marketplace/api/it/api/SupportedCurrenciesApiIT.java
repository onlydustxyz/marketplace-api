package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagAccounting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

@TagAccounting
public class SupportedCurrenciesApiIT extends AbstractMarketplaceApiIT {

    UserAuthHelper.AuthenticatedUser caller;

    @BeforeEach
    void setUp() {
        caller = userAuthHelper.create();
    }

    @Test
    void should_return_supported_currencies() {
        // When
        client.get()
                .uri("/api/v1/currencies")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "currencies": [
                            {
                              "id": "48388edb-fda2-4a32-b228-28152a147500",
                              "code": "APT",
                              "name": "Aptos Coin",
                              "logoUrl": null,
                              "decimals": 8,
                              "onlyDustWallets": []
                            },
                            {
                              "id": "3f6e1c98-8659-493a-b941-943a803bd91f",
                              "code": "BTC",
                              "name": "Bitcoin",
                              "logoUrl": null,
                              "decimals": 8,
                              "onlyDustWallets": []
                            },
                            {
                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                              "code": "ETH",
                              "name": "Ether",
                              "logoUrl": null,
                              "decimals": 18,
                              "onlyDustWallets": [
                                {
                                  "address": "0xb060429d14266d06a8be63281205668be823604f",
                                  "network": "ETHEREUM"
                                }
                              ]
                            },
                            {
                              "id": "b9593e4e-61d3-440b-88ff-3410fd72a1eb",
                              "code": "EUR",
                              "name": "Euro",
                              "logoUrl": null,
                              "decimals": 2,
                              "onlyDustWallets": []
                            },
                            {
                              "id": "00ca98a5-0197-4b76-a208-4bfc55ea8256",
                              "code": "OP",
                              "name": "Optimism",
                              "logoUrl": null,
                              "decimals": 18,
                              "onlyDustWallets": [
                                {
                                  "address": "0xaEF011B2374D723652796BF25a9FEE9e81C45a36",
                                  "network": "OPTIMISM"
                                }
                              ]
                            },
                            {
                              "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                              "code": "STRK",
                              "name": "StarkNet Token",
                              "logoUrl": null,
                              "decimals": 18,
                              "onlyDustWallets": [
                                {
                                  "address": "0xb060429d14266d06a8be63281205668be823604f",
                                  "network": "ETHEREUM"
                                }
                              ]
                            },
                            {
                              "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                              "code": "USD",
                              "name": "US Dollar",
                              "logoUrl": null,
                              "decimals": 2,
                              "onlyDustWallets": []
                            },
                            {
                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                              "code": "USDC",
                              "name": "USD Coin",
                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                              "decimals": 6,
                              "onlyDustWallets": [
                                {
                                  "address": "0xb060429d14266d06a8be63281205668be823604f",
                                  "network": "ETHEREUM"
                                },
                                {
                                  "address": "GCY2AHYGO4DBKMMNITVD7ZHYG5W2PEYFW7XOCJVUPI3GAOYNR5HVRN3O",
                                  "network": "STELLAR"
                                }
                              ]
                            }
                          ]
                        }
                        """);
    }
}
