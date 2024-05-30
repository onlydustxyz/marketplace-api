package onlydust.com.marketplace.api.bootstrap.it.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

public class EcosystemReadApiIT extends AbstractMarketplaceApiIT {
    @Test
    void should_list_ecosystems() {
        // When
        client.get()
                .uri(getApiURI(V2_ECOSYSTEMS))
                .exchange()
                // Then
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 2,
                          "totalItemNumber": 8,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "ecosystems": [
                            {
                              "id": "b599313c-a074-440f-af04-a466529ab2e7",
                              "slug": null,
                              "name": "Zama",
                              "description": null,
                              "banners": null,
                              "topProjects": null,
                              "projectCount": 0
                            },
                            {
                              "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                              "slug": null,
                              "name": "Starknet",
                              "description": null,
                              "banners": null,
                              "topProjects": null,
                              "projectCount": 0
                            },
                            {
                              "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                              "slug": null,
                              "name": "Aptos",
                              "description": null,
                              "banners": null,
                              "topProjects": null,
                              "projectCount": 0
                            },
                            {
                              "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                              "slug": null,
                              "name": "Ethereum",
                              "description": null,
                              "banners": null,
                              "topProjects": null,
                              "projectCount": 0
                            },
                            {
                              "id": "f7821bfb-df73-464c-9d87-a94dfb4f5aef",
                              "slug": null,
                              "name": "Lava",
                              "description": null,
                              "banners": null,
                              "topProjects": null,
                              "projectCount": 0
                            }
                          ]
                        }
                        """, true);
    }
}
