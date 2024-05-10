package onlydust.com.marketplace.api.bootstrap.it.api;

import org.junit.jupiter.api.Test;


public class UsersReadApiIT extends AbstractMarketplaceApiIT {

    @Test
    void should_return_users_languages_stats() {
        // Given
        final var user = userAuthHelper.authenticateAnthony().user();

        // When
        client.get()
                .uri(getApiURI(USER_LANGUAGES.formatted(user.getGithubUserId())))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "languages": [
                            {
                              "rank": 3,
                              "contributingStatus": "GREEN",
                              "contributedProjectCount": 4,
                              "contributionCount": 42,
                              "rewardCount": 10,
                              "totalEarnedUsd": 8461120.00,
                              "projects": [
                                {
                                  "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                  "slug": "kaaper",
                                  "name": "kaaper",
                                  "logoUrl": null
                                },
                                {
                                  "id": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                                  "slug": "mooooooonlight",
                                  "name": "Mooooooonlight",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1913921207486176664.jpg"
                                },
                                {
                                  "id": "90fb751a-1137-4815-b3c4-54927a5db059",
                                  "slug": "no-sponsors",
                                  "name": "No sponsors",
                                  "logoUrl": null
                                },
                                {
                                  "id": "f39b827f-df73-498c-8853-99bc3f562723",
                                  "slug": "qa-new-contributions",
                                  "name": "QA new contributions",
                                  "logoUrl": null
                                }
                              ],
                              "language": {
                                "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                                "name": "Typescript",
                                "url": null,
                                "logoUrl": null,
                                "bannerUrl": null
                              }
                            },
                            {
                              "rank": 2,
                              "contributingStatus": "GREEN",
                              "contributedProjectCount": 4,
                              "contributionCount": 28,
                              "rewardCount": 12,
                              "totalEarnedUsd": 882740.00,
                              "projects": [
                                {
                                  "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                  "slug": "kaaper",
                                  "name": "kaaper",
                                  "logoUrl": null
                                },
                                {
                                  "id": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                                  "slug": "mooooooonlight",
                                  "name": "Mooooooonlight",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1913921207486176664.jpg"
                                },
                                {
                                  "id": "90fb751a-1137-4815-b3c4-54927a5db059",
                                  "slug": "no-sponsors",
                                  "name": "No sponsors",
                                  "logoUrl": null
                                },
                                {
                                  "id": "f39b827f-df73-498c-8853-99bc3f562723",
                                  "slug": "qa-new-contributions",
                                  "name": "QA new contributions",
                                  "logoUrl": null
                                }
                              ],
                              "language": {
                                "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                                "name": "Rust",
                                "url": null,
                                "logoUrl": null,
                                "bannerUrl": null
                              }
                            }
                          ]
                        }
                        """);
    }
}
