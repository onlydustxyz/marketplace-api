package onlydust.com.marketplace.api.bootstrap.it.api;

import org.junit.jupiter.api.Test;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;


public class UserProfileApiIT extends AbstractMarketplaceApiIT {

    private static final String GET_ANTHONY_PRIVATE_PROFILE_JSON_RESPONSE = """
            {
              "githubUserId": 43467246,
              "login": "AnthonyBuisset",
              "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
              "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
              "location": "Vence, France",
              "bio": "FullStack engineerr",
              "website": "https://linktr.ee/abuisset",
              "technologies": {
                "CSS": 4473,
                "Rust": 404344,
                "Procfile": 507,
                "PLpgSQL": 10486,
                "Makefile": 8658,
                "HTML": 435,
                "HCL": 23317,
                "TypeScript": 2030401,
                "Dockerfile": 951,
                "Shell": 1455,
                "Cairo": 42100,
                "JavaScript": 2205,
                "Nix": 12902,
                "Python": 45301
              },
              "contacts": [
                {
                  "channel": "TWITTER",
                  "contact": "https://twitter.com/abuisset",
                  "visibility": "public"
                },
                {
                  "channel": "TELEGRAM",
                  "contact": "https://t.me/abuisset",
                  "visibility": "public"
                },
                {
                  "channel": "DISCORD",
                  "contact": "antho",
                  "visibility": "public"
                },
                {
                  "channel": "EMAIL",
                  "contact": "abuisset@gmail.com",
                  "visibility": "private"
                }
              ],
              "firstName": "Anthony",
              "lastName": "BUISSET",
              "allocatedTimeToContribute": "NONE",
              "isLookingForAJob": false
            }
            """;

    private static final String GET_ANTHONY_PUBLIC_PROFILE_JSON_RESPONSE = """
            {
              "githubUserId": 43467246,
              "login": "AnthonyBuisset",
              "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
              "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
              "htmlUrl": "https://github.com/AnthonyBuisset",
              "location": "Vence, France",
              "bio": "FullStack engineerr",
              "website": "https://linktr.ee/abuisset",
              "signedUpOnGithubAt": "2018-09-21T00:00:00Z",
              "signedUpAt": "2022-12-12T09:51:58.48559Z",
              "contacts": [
                {
                  "channel": "TELEGRAM",
                  "contact": "https://t.me/abuisset",
                  "visibility": "public"
                },
                {
                  "channel": "TWITTER",
                  "contact": "https://twitter.com/abuisset",
                  "visibility": "public"
                },
                {
                  "channel": "DISCORD",
                  "contact": "antho",
                  "visibility": "public"
                }
              ],
              "statsSummary": {
                "rank": 1,
                "rankPercentile": 0.1,
                "rankCategory": "A",
                "contributedProjectCount": 7,
                "leadedProjectCount": 2,
                "contributionCount": 938,
                "rewardCount": 21
              },
              "ecosystems": [
                {
                  "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                  "name": "Avail",
                  "url": "https://www.availproject.org/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                  "bannerUrl": null
                },
                {
                  "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                  "name": "Starknet",
                  "url": "https://www.starknet.io/en",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                  "bannerUrl": null
                },
                {
                  "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                  "name": "Aztec",
                  "url": "https://aztec.network/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                  "bannerUrl": null
                }
              ]
            }
            """;


    @Test
    void should_return_a_not_found_error() {
        // Given
        final long notExistingUserId = 1;

        // When
        client.get()
                .uri(getApiURI(USERS_GET + "/" + notExistingUserId))
                .exchange()
                // Then
                .expectStatus()
                .isEqualTo(404);
    }

    @Test
    void should_get_public_user_profile() {
        // Given
        final long anthonyId = 43467246L;

        // When
        client.get()
                .uri(getApiURI(USERS_GET + "/" + anthonyId))
                .exchange()
                // Then
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.allocatedTimeToContribute").doesNotExist()
                .jsonPath("$.isLookingForAJob").doesNotExist()
                .jsonPath("$.contacts[?(@.contact=='abuisset@gmail.com')]").doesNotExist()
                .jsonPath("$.contacts[?(@.contact=='antho')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='antho')].channel").isEqualTo("DISCORD")
                .jsonPath("$.contacts[?(@.contact=='https://twitter.com/abuisset')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='https://twitter.com/abuisset')].channel").isEqualTo("TWITTER")
                .jsonPath("$.contacts[?(@.contact=='https://t.me/abuisset')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='https://t.me/abuisset')].channel").isEqualTo("TELEGRAM")
                .jsonPath("$.projects[?(@.visibility=='PRIVATE')]").doesNotExist()
                .jsonPath("$.lastSeenAt").isNotEmpty()
                .json(GET_ANTHONY_PUBLIC_PROFILE_JSON_RESPONSE);
    }

    @Test
    void should_get_public_user_profile_by_login() {
        // Given
        final String anthonyLogin = "AnthonyBuisset";

        // When
        client.get()
                .uri(getApiURI(USERS_GET_BY_LOGIN + "/" + anthonyLogin))
                .exchange()
                // Then
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.allocatedTimeToContribute").doesNotExist()
                .jsonPath("$.isLookingForAJob").doesNotExist()
                .jsonPath("$.contacts[?(@.contact=='abuisset@gmail.com')]").doesNotExist()
                .jsonPath("$.contacts[?(@.contact=='antho')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='antho')].channel").isEqualTo("DISCORD")
                .jsonPath("$.contacts[?(@.contact=='https://twitter.com/abuisset')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='https://twitter.com/abuisset')].channel").isEqualTo("TWITTER")
                .jsonPath("$.contacts[?(@.contact=='https://t.me/abuisset')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='https://t.me/abuisset')].channel").isEqualTo("TELEGRAM")
                .jsonPath("$.projects[?(@.visibility=='PRIVATE')]").doesNotExist()
                .jsonPath("$.lastSeenAt").isNotEmpty()
                .json(GET_ANTHONY_PUBLIC_PROFILE_JSON_RESPONSE);
    }

    @Test
    void should_get_private_user_profile() {
        // Given
        final String jwt = userAuthHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(ME_GET_PROFILE))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.allocatedTimeToContribute").isEqualTo("NONE")
                .jsonPath("$.isLookingForAJob").isEqualTo(false)
                .jsonPath("$.contacts[?(@.contact=='abuisset@gmail.com')].visibility").isEqualTo("private")
                .jsonPath("$.contacts[?(@.contact=='abuisset@gmail.com')].channel").isEqualTo("EMAIL")
                .jsonPath("$.contacts[?(@.contact=='antho')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='antho')].channel").isEqualTo("DISCORD")
                .jsonPath("$.contacts[?(@.contact=='https://twitter.com/abuisset')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='https://twitter.com/abuisset')].channel").isEqualTo("TWITTER")
                .jsonPath("$.contacts[?(@.contact=='https://t.me/abuisset')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='https://t.me/abuisset')].channel").isEqualTo("TELEGRAM")
                .json(GET_ANTHONY_PRIVATE_PROFILE_JSON_RESPONSE);
    }


    @Test
    void should_get_email_if_user_profile_has_no_contact() {
        // Given
        final String jwt = userAuthHelper.authenticateHayden().jwt();

        // When
        client.get()
                .uri(getApiURI(ME_GET_PROFILE))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.contacts[?(@.channel=='EMAIL')].contact").isEqualTo("haydenclearymusic@gmail.com");
    }
}
