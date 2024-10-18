package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.postgres.adapter.repository.old.UserProfileInfoRepository;
import onlydust.com.marketplace.api.suites.tags.TagUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@TagUser
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
        final var user = userAuthHelper.authenticateAntho().user();

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
                           "totalItemNumber": 4,
                           "hasMore": false,
                           "nextPageIndex": 0,
                           "languages": [
                             {
                               "rank": 2,
                               "contributingStatus": "GREEN",
                               "contributedProjectCount": 6,
                               "contributionCount": 217,
                               "rewardCount": 10,
                               "totalEarnedUsd": 900080.00,
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
                                   "id": "5aabf0f1-7495-4bff-8de2-4396837ce6b4",
                                   "slug": "marketplace-2",
                                   "name": "Marketplace 2",
                                   "logoUrl": null
                                 },
                                 {
                                   "id": "90fb751a-1137-4815-b3c4-54927a5db059",
                                   "slug": "no-sponsors",
                                   "name": "No sponsors",
                                   "logoUrl": null
                                 },
                                 {
                                   "id": "d4e8ab3b-a4a8-493d-83bd-a4c8283b94f9",
                                   "slug": "oscars-awesome-project",
                                   "name": "oscar's awesome project",
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
                                 "slug": "typescript",
                                 "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                                 "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                               }
                             },
                             {
                               "rank": 2,
                               "contributingStatus": "GREEN",
                               "contributedProjectCount": 5,
                               "contributionCount": 192,
                               "rewardCount": 12,
                               "totalEarnedUsd": 10605.00,
                               "projects": [
                                 {
                                   "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                   "slug": "kaaper",
                                   "name": "kaaper",
                                   "logoUrl": null
                                 },
                                 {
                                   "id": "29cdf359-f60c-41a0-8b11-18d6841311f6",
                                   "slug": "kaaper-3",
                                   "name": "kaaper 3",
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
                                 "slug": "rust",
                                 "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                                 "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                               }
                             },
                             {
                               "rank": 3,
                               "contributingStatus": "GREEN",
                               "contributedProjectCount": 4,
                               "contributionCount": 24,
                               "rewardCount": 0,
                               "totalEarnedUsd": 0,
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
                                 "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                                 "name": "Javascript",
                                 "slug": "javascript",
                                 "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                                 "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                               }
                             },
                             {
                               "rank": 16,
                               "contributingStatus": "GREEN",
                               "contributedProjectCount": 1,
                               "contributionCount": 9,
                               "rewardCount": 0,
                               "totalEarnedUsd": 0,
                               "projects": [
                                 {
                                   "id": "00490be6-2c03-4720-993b-aea3e07edd81",
                                   "slug": "zama",
                                   "name": "Zama",
                                   "logoUrl": "https://dl.airtable.com/.attachments/f776b6ea66adbe46d86adaea58626118/610d50f6/15TqNyRwTMGoVeAX2u1M"
                                 }
                               ],
                               "language": {
                                 "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                                 "name": "Cairo",
                                 "slug": "cairo",
                                 "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                                 "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                               }
                             },
                             {
                               "rank": 20,
                               "contributingStatus": "GREEN",
                               "contributedProjectCount": 1,
                               "contributionCount": 7,
                               "rewardCount": 0,
                               "totalEarnedUsd": 0,
                               "projects": [
                                 {
                                   "id": "00490be6-2c03-4720-993b-aea3e07edd81",
                                   "slug": "zama",
                                   "name": "Zama",
                                   "logoUrl": "https://dl.airtable.com/.attachments/f776b6ea66adbe46d86adaea58626118/610d50f6/15TqNyRwTMGoVeAX2u1M"
                                 }
                               ],
                               "language": {
                                 "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                                 "name": "Python",
                                 "slug": "python",
                                 "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                                 "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                               }
                             }
                           ]
                         }
                        """, true);
    }

    @Test
    void should_return_users_ecosystems_stats() {
        // When
        client.get()
                .uri(getApiURI(USER_ECOSYSTEMS.formatted(userAuthHelper.authenticateAntho().user().getGithubUserId())))
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
                              "rank": 1,
                              "contributingStatus": "GREEN",
                              "contributedProjectCount": 1,
                              "contributionCount": 112,
                              "rewardCount": 0,
                              "totalEarnedUsd": 0,
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
                                "bannerUrl": null,
                                "slug": "avail",
                                "hidden": true
                              }
                            },
                            {
                              "rank": 1,
                              "contributingStatus": "GREEN",
                              "contributedProjectCount": 1,
                              "contributionCount": 112,
                              "rewardCount": 0,
                              "totalEarnedUsd": 0,
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
                                "bannerUrl": null,
                                "slug": "starknet",
                                "hidden": false
                              }
                            },
                            {
                              "rank": 1,
                              "contributingStatus": "GREEN",
                              "contributedProjectCount": 1,
                              "contributionCount": 112,
                              "rewardCount": 0,
                              "totalEarnedUsd": 0,
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
                                "bannerUrl": null,
                                "slug": "aztec",
                                "hidden": false
                              }
                            }
                          ]
                        }
                        """, true);

        // When
        client.get()
                .uri(getApiURI(USER_ECOSYSTEMS.formatted(userAuthHelper.authenticateUser(21149076L).user().getGithubUserId())))
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
                              "rank": 4,
                              "contributingStatus": "GREEN",
                              "contributedProjectCount": 1,
                              "contributionCount": 213,
                              "rewardCount": 1,
                              "totalEarnedUsd": 10100.00,
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
                                "bannerUrl": null,
                                "slug": "avail",
                                "hidden": true
                              }
                            },
                            {
                              "rank": 28,
                              "contributingStatus": "GREEN",
                              "contributedProjectCount": 1,
                              "contributionCount": 213,
                              "rewardCount": 1,
                              "totalEarnedUsd": 1010.00,
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
                                "bannerUrl": null,
                                "slug": "starknet",
                                "hidden": false
                              }
                            },
                            {
                              "rank": 4,
                              "contributingStatus": "GREEN",
                              "contributedProjectCount": 1,
                              "contributionCount": 213,
                              "rewardCount": 1,
                              "totalEarnedUsd": 1010.00,
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
                                "bannerUrl": null,
                                "slug": "aztec",
                                "hidden": false
                              }
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_return_new_user_stats() {
        // Given
        final var newUser = userAuthHelper.signUpUser(666, "DeViL", "https://devil.com/avatar.jpg", false);

        // When
        client.get()
                .uri(getApiURI(USER_STATS.formatted(newUser.user().getGithubUserId())))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "activity": [],
                          "earnings": {
                            "totalEarnedUsd": 0,
                            "perProject": []
                          },
                          "workDistribution": {
                            "codeReviewCount": 0,
                            "issueCount": 0,
                            "pullRequestCount": 0
                          }
                        }
                        """);
    }


    @Test
    void should_return_new_user_profile() {
        // Given
        final var newUser = userAuthHelper.signUpUser(777, "AnGeL", "https://angel.com/avatar.jpg", false);

        // When
        client.get()
                .uri(getApiURI(USER_BY_ID.formatted(newUser.user().getGithubUserId())))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "githubUserId": 777,
                          "login": "AnGeL",
                          "avatarUrl": "https://angel.com/avatar.jpg",
                          "htmlUrl": "https://github.com/AnGeL",
                          "location": null,
                          "bio": null,
                          "website": null,
                          "signedUpOnGithubAt": null,
                          "contacts": [],
                          "statsSummary": {
                            "rank": 0,
                            "rankPercentile": 100,
                            "rankCategory": "F",
                            "contributedProjectCount": 0,
                            "leadedProjectCount": 0,
                            "contributionCount": 0,
                            "rewardCount": 0
                          },
                          "ecosystems": []
                        }
                        """);

        // When
        client.get()
                .uri(getApiURI(USER_BY_LOGIN.formatted("AnGeL")))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "githubUserId": 777,
                          "login": "AnGeL",
                          "avatarUrl": "https://angel.com/avatar.jpg",
                          "htmlUrl": null,
                          "location": null,
                          "bio": null,
                          "website": null,
                          "signedUpOnGithubAt": null,
                          "contacts": [],
                          "statsSummary": {
                            "rank": 0,
                            "rankPercentile": 100,
                            "rankCategory": "F",
                            "contributedProjectCount": 0,
                            "leadedProjectCount": 0,
                            "contributionCount": 0,
                            "rewardCount": 0
                          },
                          "ecosystems": []
                        }
                        """);
    }


    @Test
    void should_return_new_user_languages() {
        // Given
        final var newUser = userAuthHelper.signUpUser(888, "A", "https://a.com/avatar.jpg", false);

        // When
        client.get()
                .uri(getApiURI(USER_LANGUAGES.formatted(newUser.user().getGithubUserId())))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {"totalPageNumber":0,"totalItemNumber":0,"hasMore":false,"nextPageIndex":0,"languages":[]}
                        """);
    }


    @Test
    void should_return_new_user_ecosystems() {
        // Given
        final var newUser = userAuthHelper.signUpUser(999, "B", "https://a.com/avatar.jpg", false);

        // When
        client.get()
                .uri(getApiURI(USER_ECOSYSTEMS.formatted(newUser.user().getGithubUserId())))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {"totalPageNumber":0,"totalItemNumber":0,"hasMore":false,"nextPageIndex":0,"ecosystems":[]}
                        """);
    }

    @Test
    void should_return_user_profile() {
        // Given
        final var user = userAuthHelper.authenticateAntho().user();

        // When
        client.get()
                .uri(getApiURI(USER_BY_ID.formatted(user.getGithubUserId())))
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
                          "signedUpOnGithubAt": "2018-09-21T08:45:50Z",
                          "signedUpAt": "2022-12-12T09:51:58.48559Z",
                          "lastSeenAt": "2023-10-05T19:06:50.034Z",
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
                            "contributionCount": 137,
                            "rewardCount": 21
                          },
                          "ecosystems": [
                            {
                              "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                              "name": "Avail",
                              "url": "https://www.availproject.org/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                              "bannerUrl": null,
                              "slug": "avail"
                            },
                            {
                              "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                              "name": "Starknet",
                              "url": "https://www.starknet.io/en",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                              "bannerUrl": null,
                              "slug": "starknet"
                            },
                            {
                              "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                              "name": "Aztec",
                              "url": "https://aztec.network/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                              "bannerUrl": null,
                              "slug": "aztec"
                            }
                          ]
                        }
                        """);

        // When
        client.get()
                .uri(getApiURI(USER_BY_LOGIN.formatted(user.getGithubLogin())))
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
                          "signedUpOnGithubAt": "2018-09-21T08:45:50Z",
                          "signedUpAt": "2022-12-12T09:51:58.48559Z",
                          "lastSeenAt": "2023-10-05T19:06:50.034Z",
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
                              "bannerUrl": null,
                              "slug": "avail"
                            },
                            {
                              "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                              "name": "Starknet",
                              "url": "https://www.starknet.io/en",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                              "bannerUrl": null,
                              "slug": "starknet"
                            },
                            {
                              "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                              "name": "Aztec",
                              "url": "https://aztec.network/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                              "bannerUrl": null,
                              "slug": "aztec"
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_return_users_stats() {
        // Given
        final var user = userAuthHelper.authenticateAntho().user();

        // When
        client.get()
                .uri(getApiURI(USER_STATS.formatted(user.getGithubUserId())))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "activity": [
                            {
                              "year": 2022,
                              "week": 15,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 1,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 22,
                              "codeReviewCount": 1,
                              "issueCount": 0,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 25,
                              "codeReviewCount": 0,
                              "issueCount": 3,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 26,
                              "codeReviewCount": 4,
                              "issueCount": 0,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 27,
                              "codeReviewCount": 0,
                              "issueCount": 3,
                              "pullRequestCount": 1,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 28,
                              "codeReviewCount": 5,
                              "issueCount": 5,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 29,
                              "codeReviewCount": 1,
                              "issueCount": 0,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 30,
                              "codeReviewCount": 1,
                              "issueCount": 0,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 31,
                              "codeReviewCount": 1,
                              "issueCount": 0,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 43,
                              "codeReviewCount": 1,
                              "issueCount": 0,
                              "pullRequestCount": 1,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 46,
                              "codeReviewCount": 3,
                              "issueCount": 0,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 4,
                              "codeReviewCount": 1,
                              "issueCount": 0,
                              "pullRequestCount": 0,
                              "rewardCount": 2
                            },
                            {
                              "year": 2023,
                              "week": 5,
                              "codeReviewCount": 1,
                              "issueCount": 0,
                              "pullRequestCount": 4,
                              "rewardCount": 1
                            },
                            {
                              "year": 2023,
                              "week": 6,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 3,
                              "rewardCount": 1
                            },
                            {
                              "year": 2023,
                              "week": 8,
                              "codeReviewCount": 1,
                              "issueCount": 0,
                              "pullRequestCount": 1,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 10,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 1,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 11,
                              "codeReviewCount": 1,
                              "issueCount": 0,
                              "pullRequestCount": 1,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 12,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 13,
                              "codeReviewCount": 0,
                              "issueCount": 2,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 17,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 1,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 18,
                              "codeReviewCount": 1,
                              "issueCount": 0,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 22,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 2
                            },
                            {
                              "year": 2023,
                              "week": 23,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 9,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 24,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 5,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 25,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 8,
                              "rewardCount": 2
                            },
                            {
                              "year": 2023,
                              "week": 26,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 13,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 27,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 28,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 15,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 29,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 4,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 30,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 31,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 4,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 32,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 12,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 35,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 36,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 37,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 38,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 4,
                              "rewardCount": 10
                            }
                          ],
                          "earnings": {
                            "totalEarnedUsd": 2692632.50,
                            "perProject": [
                              {
                                "projectName": "kaaper 3",
                                "totalEarnedUsd": 2525.00
                              },
                              {
                                "projectName": "oscar's awesome project",
                                "totalEarnedUsd": 1010.00
                              },
                              {
                                "projectName": "kaaper",
                                "totalEarnedUsd": 1792080.00
                              },
                              {
                                "projectName": "Aldébaran du Taureau",
                                "totalEarnedUsd": 1767.50
                              },
                              {
                                "projectName": "Pizzeria Yoshi !",
                                "totalEarnedUsd": 4260.00
                              },
                              {
                                "projectName": "Marketplace 2",
                                "totalEarnedUsd": 890990.00
                              }
                            ]
                          },
                          "workDistribution": {
                            "codeReviewCount": 22,
                            "issueCount": 13,
                            "pullRequestCount": 102
                          }
                        }
                        """);
    }

    @Test
    void should_filter_users_stats_per_ecosystem() {
        // Given
        final var user = userAuthHelper.authenticateAntho().user();

        // When
        client.get()
                .uri(getApiURI(USER_STATS.formatted(user.getGithubUserId()), Map.of(
                        "ecosystem", "99b6c284-f9bb-4f89-8ce7-03771465ef8e"
                )))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "activity": [
                            {
                              "year": 2022,
                              "week": 25,
                              "codeReviewCount": 0,
                              "issueCount": 3,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 27,
                              "codeReviewCount": 0,
                              "issueCount": 3,
                              "pullRequestCount": 1,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 28,
                              "codeReviewCount": 0,
                              "issueCount": 5,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 43,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 1,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 5,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 4,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 6,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 3,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 10,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 1,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 11,
                              "codeReviewCount": 1,
                              "issueCount": 0,
                              "pullRequestCount": 1,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 12,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 18,
                              "codeReviewCount": 1,
                              "issueCount": 0,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 22,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 23,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 9,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 24,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 5,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 25,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 8,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 26,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 13,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 27,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 28,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 15,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 29,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 4,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 30,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 31,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 4,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 32,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 12,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 35,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 36,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 37,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 38,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 4,
                              "rewardCount": 0
                            }
                          ],
                          "earnings": {
                            "totalEarnedUsd": 0,
                            "perProject": []
                          },
                          "workDistribution": {
                            "codeReviewCount": 2,
                            "issueCount": 11,
                            "pullRequestCount": 99
                          }
                        }
                        """);
    }

    @Test
    void should_filter_users_stats_per_date() {
        // Given
        final var user = userAuthHelper.authenticateAntho().user();

        // When
        client.get()
                .uri(getApiURI(USER_STATS.formatted(user.getGithubUserId()), Map.of(
                        "fromDate", "2023-01-01",
                        "toDate", "2023-06-01"
                )))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "earnings": {
                            "totalEarnedUsd": 898785.00,
                            "perProject": [
                              {
                                "projectName": "kaaper 3",
                                "totalEarnedUsd": 2525.00
                              },
                              {
                                "projectName": "Pizzeria Yoshi !",
                                "totalEarnedUsd": 4260.00
                              },
                              {
                                "projectName": "Marketplace 2",
                                "totalEarnedUsd": 890990.00
                              },
                              {
                                "projectName": "oscar's awesome project",
                                "totalEarnedUsd": 1010.00
                              }
                            ]
                          }
                        }
                        """);
    }
}
