package onlydust.com.marketplace.api.bootstrap.it.bo;

import org.junit.jupiter.api.Test;

import java.util.Map;

public class BackofficeUserApiIT extends AbstractMarketplaceBackOfficeApiIT {

    @Test
    void should_return_users_page() {
        // When
        client.get()
                .uri(getApiURI(GET_USERS, Map.of("pageIndex", "0", "pageSize", "5")))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 6,
                          "totalItemNumber": 27,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "users": [
                            {
                              "id": "eaa1ddf3-fea5-4cef-825b-336f8e775e05",
                              "githubUserId": 5160414,
                              "login": "haydencleary",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                              "email": "haydenclearymusic@gmail.com",
                              "lastSeenAt": "2023-10-05T08:25:44.601Z",
                              "signedUpAt": "2023-10-03T09:06:07.741395Z"
                            },
                            {
                              "id": "f4af340d-6923-453c-bffe-2f1ce1880ff4",
                              "githubUserId": 144809540,
                              "login": "CamilleOD",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/144809540?v=4",
                              "email": "admin@onlydust.xyz",
                              "lastSeenAt": "2023-09-27T12:19:00.474Z",
                              "signedUpAt": "2023-09-27T12:04:06.149173Z"
                            },
                            {
                              "id": "0d29b7bd-9514-4a03-ad14-99bbcbef4733",
                              "githubUserId": 142427301,
                              "login": "letkev",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/142427301?v=4",
                              "email": "kevin@lettria.com",
                              "lastSeenAt": "2023-10-01T16:16:52.185Z",
                              "signedUpAt": "2023-09-18T15:45:50.175156Z"
                            },
                            {
                              "id": "a0da3c1e-6493-4ea1-8bd0-8c46d653f274",
                              "githubUserId": 31901905,
                              "login": "kaelsky",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                              "email": "chimansky.mickael@gmail.com",
                              "lastSeenAt": "2023-10-05T15:50:14.139Z",
                              "signedUpAt": "2023-09-08T14:35:57.032522Z"
                            },
                            {
                              "id": "46fec596-7a91-422e-8532-5f479e790217",
                              "githubUserId": 141839618,
                              "login": "Blumebee",
                              "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/16582211468658783329.webp",
                              "email": "emilie.blum88@gmail.com",
                              "lastSeenAt": "2023-10-05T07:23:47.728Z",
                              "signedUpAt": "2023-09-04T13:14:19.302602Z"
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_return_users_page_filtered_by_login() {
        // When
        client.get()
                .uri(getApiURI(GET_USERS, Map.of("pageIndex", "0", "pageSize", "5", "login", "buisset")))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 1,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "users": [
                            {
                              "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                              "githubUserId": 43467246,
                              "login": "AnthonyBuisset",
                              "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                              "email": "abuisset@gmail.com",
                              "lastSeenAt": "2023-10-05T19:06:50.034Z",
                              "signedUpAt": "2022-12-12T09:51:58.48559Z"
                            }
                          ]
                        }
                        """);
    }
}
