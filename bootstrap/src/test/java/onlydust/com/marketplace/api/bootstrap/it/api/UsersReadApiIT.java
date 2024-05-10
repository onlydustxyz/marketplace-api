package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.postgres.adapter.repository.old.UserProfileInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class UsersReadApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    UserProfileInfoRepository userProfileInfoRepository;

    @BeforeEach
    void setup() {
        final var olivier = userAuthHelper.authenticateOlivier().user();
        userProfileInfoRepository.deleteById(olivier.getId());
    }

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
                        """, true);
    }

    @Test
    void should_return_users_ecosystems_stats() {
        // Given
        final var user = userAuthHelper.authenticateAnthony().user();

        // When
        client.get()
                .uri(getApiURI(USER_ECOSYSTEMS.formatted(user.getGithubUserId())))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 3,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "ecosystems": [
                            {
                              "rank": 10,
                              "contributingStatus": "GREEN",
                              "contributedProjectCount": 1,
                              "contributionCount": 88,
                              "rewardCount": 14,
                              "totalEarnedUsd": 1564155.00,
                              "projects": [
                                {
                                  "id": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                                  "slug": "mooooooonlight",
                                  "name": "Mooooooonlight",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1913921207486176664.jpg"
                                }
                              ],
                              "ecosystem": {
                                "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                                "name": "Starknet",
                                "url": "https://www.starknet.io/en",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                                "bannerUrl": null
                              }
                            },
                            {
                              "rank": 1,
                              "contributingStatus": "GREEN",
                              "contributedProjectCount": 1,
                              "contributionCount": 88,
                              "rewardCount": 14,
                              "totalEarnedUsd": 1564155.00,
                              "projects": [
                                {
                                  "id": "90fb751a-1137-4815-b3c4-54927a5db059",
                                  "slug": "no-sponsors",
                                  "name": "No sponsors",
                                  "logoUrl": null
                                }
                              ],
                              "ecosystem": {
                                "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                                "name": "Avail",
                                "url": "https://www.availproject.org/",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                                "bannerUrl": null
                              }
                            },
                            {
                              "rank": 1,
                              "contributingStatus": "GREEN",
                              "contributedProjectCount": 1,
                              "contributionCount": 88,
                              "rewardCount": 14,
                              "totalEarnedUsd": 1564155.00,
                              "projects": [
                                {
                                  "id": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                                  "slug": "mooooooonlight",
                                  "name": "Mooooooonlight",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1913921207486176664.jpg"
                                }
                              ],
                              "ecosystem": {
                                "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                                "name": "Aztec",
                                "url": "https://aztec.network/",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                                "bannerUrl": null
                              }
                            }
                          ]
                        }
                        """, true);
    }

    @Test
    void should_return_user_contributor_profile_by_id() {
        // Given
        final var user = userAuthHelper.authenticateAnthony().user();

        // When
        client.get()
                .uri(getApiURI(V2_USER.formatted(user.getGithubUserId())))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "githubUserId": 43467246,
                          "login": "AnthonyBuisset",
                          "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                          "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                          "htmlUrl": "https://github.com/AnthonyBuisset",
                          "location": "Vence, France",
                          "bio": "FullStack engineerr",
                          "website": "https://linktr.ee/abuisset",
                          "signedUpOnGithubAt": null,
                          "signedUpAt": "2022-12-12T09:51:58.48559Z",
                          "lastSeenAt": "2023-10-05T19:06:50.034Z",
                          "contacts": [
                            {
                              "channel": "EMAIL",
                              "contact": "abuisset@gmail.com",
                              "visibility": "private"
                            },
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
                          "statsSummary": null,
                          "ecosystems": [
                            "397df411-045d-4d9f-8d65-8284c88f9208",
                            "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                            "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                            "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                            "b599313c-a074-440f-af04-a466529ab2e7",
                            "dd6f737e-2a9d-40b9-be62-8f64ec157989",
                            "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                            "f7821bfb-df73-464c-9d87-a94dfb4f5aef"
                          ]
                        }
                        """);
    }

    @Test
    void should_return_default_to_github_info() {
        // Given
        final var user = userAuthHelper.authenticateOlivier().user();

        // When
        client.get()
                .uri(getApiURI(V2_USER.formatted(user.getGithubUserId())))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "githubUserId": 595505,
                          "login": "ofux",
                          "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4",
                          "id": "e461c019-ba23-4671-9b6c-3a5a18748af9",
                          "htmlUrl": "https://github.com/ofux",
                          "location": "Paris, France",
                          "bio": "Web3, Cloud, Unity3D",
                          "website": "",
                          "signedUpOnGithubAt": null,
                          "signedUpAt": "2022-12-12T15:52:23.593172Z",
                          "lastSeenAt": "2023-09-27T08:52:36.037Z",
                          "contacts": [
                            {
                              "channel": "TWITTER",
                              "contact": "https://twitter.com/fuxeto",
                              "visibility": "public"
                            },
                            {
                              "channel": "LINKEDIN",
                              "contact": "https://www.linkedin.com/in/olivier-fuxet/",
                              "visibility": "public"
                            }
                          ],
                          "statsSummary": null,
                          "ecosystems": [
                            "397df411-045d-4d9f-8d65-8284c88f9208",
                            "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                            "dd6f737e-2a9d-40b9-be62-8f64ec157989",
                            "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                            "f7821bfb-df73-464c-9d87-a94dfb4f5aef"
                          ]
                        }
                        """);
    }
}
