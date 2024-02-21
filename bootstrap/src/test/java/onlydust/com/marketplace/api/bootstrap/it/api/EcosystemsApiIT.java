package onlydust.com.marketplace.api.bootstrap.it.api;

import org.junit.jupiter.api.Test;

import java.util.Map;

public class EcosystemsApiIT extends AbstractMarketplaceApiIT {

    @Test
    void should_return_all_ecosystem_given_an_authenticated_user() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();

        // When
        client.get()
                .uri(getApiURI(GET_ALL_ECOSYSTEMS, Map.of("pageIndex", "0", "pageSize", "50")))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 8,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "ecosystems": [
                            {
                              "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                              "name": "Aptos",
                              "url": "https://aptosfoundation.org/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png"
                            },
                            {
                              "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                              "name": "Avail",
                              "url": "https://www.availproject.org/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png"
                            },
                            {
                              "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                              "name": "Aztec",
                              "url": "https://aztec.network/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg"
                            },
                            {
                              "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                              "name": "Ethereum",
                              "url": "https://ethereum.foundation/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png"
                            },
                            {
                              "id": "f7821bfb-df73-464c-9d87-a94dfb4f5aef",
                              "name": "Lava",
                              "url": "https://www.lavanet.xyz/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15939879525439639427.jpg"
                            },
                            {
                              "id": "dd6f737e-2a9d-40b9-be62-8f64ec157989",
                              "name": "Optimism",
                              "url": "https://www.optimism.io/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12058007825795511084.png"
                            },
                            {
                              "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                              "name": "Starknet",
                              "url": "https://www.starknet.io/en",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png"
                            },
                            {
                              "id": "b599313c-a074-440f-af04-a466529ab2e7",
                              "name": "Zama",
                              "url": "https://www.zama.ai/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png"
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_return_unauthorized_given_not_authenticated_user() {
        // When
        client.get()
                .uri(getApiURI(GET_ALL_ECOSYSTEMS, Map.of("pageIndex", "0", "pageSize", "50")))
                .exchange()
                // Then
                .expectStatus()
                .is4xxClientError();
    }
}
