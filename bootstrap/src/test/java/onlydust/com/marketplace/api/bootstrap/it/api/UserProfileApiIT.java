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
                "rankPercentile": 1,
                "rankCategory": "A",
                "contributedProjectCount": 9,
                "leadedProjectCount": 1,
                "contributionCount": 1700,
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
                  "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                  "name": "Ethereum",
                  "url": "https://ethereum.foundation/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
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
                  "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                  "name": "Aptos",
                  "url": "https://aptosfoundation.org/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                  "bannerUrl": null
                },
                {
                  "id": "b599313c-a074-440f-af04-a466529ab2e7",
                  "name": "Zama",
                  "url": "https://www.zama.ai/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                  "bannerUrl": null
                },
                {
                  "id": "dd6f737e-2a9d-40b9-be62-8f64ec157989",
                  "name": "Optimism",
                  "url": "https://www.optimism.io/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12058007825795511084.png",
                  "bannerUrl": null
                },
                {
                  "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                  "name": "Aztec",
                  "url": "https://aztec.network/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                  "bannerUrl": null
                },
                {
                  "id": "f7821bfb-df73-464c-9d87-a94dfb4f5aef",
                  "name": "Lava",
                  "url": "https://www.lavanet.xyz/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15939879525439639427.jpg",
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
