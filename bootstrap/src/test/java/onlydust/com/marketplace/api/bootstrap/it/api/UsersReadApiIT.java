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
                              "projects": [],
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
                              "projects": [],
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
