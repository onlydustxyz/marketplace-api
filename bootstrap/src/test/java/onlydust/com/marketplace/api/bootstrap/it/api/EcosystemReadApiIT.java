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
                              "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                              "slug": "aptos",
                              "name": "Aptos",
                              "description": "Aptos ecosystem",
                              "banners": {
                                "xl": {
                                  "url": "https://s3.amazonaws.com/onlydust/ecosystem_banners/aptos-xl.png",
                                  "fontColor": "LIGHT"
                                },
                                "md": {
                                  "url": "https://s3.amazonaws.com/onlydust/ecosystem_banners/aptos-md.png",
                                  "fontColor": "LIGHT"
                                }
                              },
                              "topProjects": [
                                {
                                  "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                  "slug": "bretzel",
                                  "name": "Bretzel",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                                }
                              ],
                              "projectCount": 1
                            },
                            {
                              "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                              "slug": "avail",
                              "name": "Avail",
                              "description": "Avail ecosystem",
                              "banners": {
                                "xl": {
                                  "url": "https://s3.amazonaws.com/onlydust/ecosystem_banners/avail-xl.png",
                                  "fontColor": "DARK"
                                },
                                "md": {
                                  "url": "https://s3.amazonaws.com/onlydust/ecosystem_banners/avail-md.png",
                                  "fontColor": "DARK"
                                }
                              },
                              "topProjects": [
                                {
                                  "id": "90fb751a-1137-4815-b3c4-54927a5db059",
                                  "slug": "no-sponsors",
                                  "name": "No sponsors",
                                  "logoUrl": null
                                }
                              ],
                              "projectCount": 1
                            },
                            {
                              "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                              "slug": "aztec",
                              "name": "Aztec",
                              "description": "Aztec ecosystem",
                              "banners": {
                                "xl": {
                                  "url": "https://s3.amazonaws.com/onlydust/ecosystem_banners/aztec-xl.png",
                                  "fontColor": "DARK"
                                },
                                "md": {
                                  "url": "https://s3.amazonaws.com/onlydust/ecosystem_banners/aztec-md.png",
                                  "fontColor": "DARK"
                                }
                              },
                              "topProjects": [
                                {
                                  "id": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                                  "slug": "mooooooonlight",
                                  "name": "Mooooooonlight",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1913921207486176664.jpg"
                                }
                              ],
                              "projectCount": 1
                            },
                            {
                              "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                              "slug": "ethereum",
                              "name": "Ethereum",
                              "description": "Ethereum ecosystem",
                              "banners": {
                                "xl": {
                                  "url": "https://s3.amazonaws.com/onlydust/ecosystem_banners/ethereum-xl.png",
                                  "fontColor": "LIGHT"
                                },
                                "md": {
                                  "url": "https://s3.amazonaws.com/onlydust/ecosystem_banners/ethereum-md.png",
                                  "fontColor": "LIGHT"
                                }
                              },
                              "topProjects": [
                                {
                                  "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                  "slug": "bretzel",
                                  "name": "Bretzel",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                                }
                              ],
                              "projectCount": 1
                            },
                            {
                              "id": "f7821bfb-df73-464c-9d87-a94dfb4f5aef",
                              "slug": "lava",
                              "name": "Lava",
                              "description": "Lava ecosystem",
                              "banners": {
                                "xl": {
                                  "url": "https://s3.amazonaws.com/onlydust/ecosystem_banners/lava-xl.png",
                                  "fontColor": "LIGHT"
                                },
                                "md": {
                                  "url": "https://s3.amazonaws.com/onlydust/ecosystem_banners/lava-md.png",
                                  "fontColor": "LIGHT"
                                }
                              },
                              "topProjects": [
                                {
                                  "id": "27ca7e18-9e71-468f-8825-c64fe6b79d66",
                                  "slug": "b-conseil",
                                  "name": "B Conseil",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11012050846615405488.png"
                                }
                              ],
                              "projectCount": 1
                            }
                          ]
                        }
                        """, true);
    }
}
