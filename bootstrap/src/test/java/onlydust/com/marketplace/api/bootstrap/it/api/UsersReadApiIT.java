package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.postgres.adapter.repository.old.UserProfileInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;


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
                          "totalItemNumber": 3,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "languages": [
                            {
                              "rank": 2,
                              "contributingStatus": "GREEN",
                              "contributedProjectCount": 5,
                              "contributionCount": 207,
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
                                "url": null,
                                "logoUrl": null,
                                "bannerUrl": null
                              }
                            },
                            {
                              "rank": 3,
                              "contributingStatus": "GREEN",
                              "contributedProjectCount": 6,
                              "contributionCount": 225,
                              "rewardCount": 10,
                              "totalEarnedUsd": 1799150.00,
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
                                "url": null,
                                "logoUrl": null,
                                "bannerUrl": null
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
                                "url": null,
                                "logoUrl": null,
                                "bannerUrl": null
                              }
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_return_users_ecosystems_stats() {
        // When
        client.get()
                .uri(getApiURI(USER_ECOSYSTEMS.formatted(userAuthHelper.authenticateAnthony().user().getGithubUserId())))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 2,
                          "totalItemNumber": 6,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "ecosystems": [
                            {
                              "rank": 1,
                              "contributingStatus": "GREEN",
                              "contributedProjectCount": 1,
                              "contributionCount": 1045,
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
                                "bannerUrl": null
                              }
                            },
                            {
                              "rank": 1,
                              "contributingStatus": "GREEN",
                              "contributedProjectCount": 1,
                              "contributionCount": 1045,
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
                                "bannerUrl": null
                              }
                            },
                            {
                              "rank": 3,
                              "contributingStatus": "ORANGE",
                              "contributedProjectCount": 1,
                              "contributionCount": 1,
                              "rewardCount": 0,
                              "totalEarnedUsd": 0,
                              "projects": [
                                {
                                  "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                  "slug": "bretzel",
                                  "name": "Bretzel",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                                }
                              ],
                              "ecosystem": {
                                "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                                "name": "Ethereum",
                                "url": "https://ethereum.foundation/",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
                                "bannerUrl": null
                              }
                            },
                            {
                              "rank": 3,
                              "contributingStatus": "ORANGE",
                              "contributedProjectCount": 1,
                              "contributionCount": 1,
                              "rewardCount": 0,
                              "totalEarnedUsd": 0,
                              "projects": [
                                {
                                  "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                  "slug": "bretzel",
                                  "name": "Bretzel",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                                }
                              ],
                              "ecosystem": {
                                "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                                "name": "Aptos",
                                "url": "https://aptosfoundation.org/",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                                "bannerUrl": null
                              }
                            },
                            {
                              "rank": 3,
                              "contributingStatus": "ORANGE",
                              "contributedProjectCount": 1,
                              "contributionCount": 1,
                              "rewardCount": 0,
                              "totalEarnedUsd": 0,
                              "projects": [
                                {
                                  "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                  "slug": "bretzel",
                                  "name": "Bretzel",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                                }
                              ],
                              "ecosystem": {
                                "id": "b599313c-a074-440f-af04-a466529ab2e7",
                                "name": "Zama",
                                "url": "https://www.zama.ai/",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                                "bannerUrl": null
                              }
                            }
                          ]
                        }
                        """);

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
                              "contributionCount": 269,
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
                                "bannerUrl": null
                              }
                            },
                            {
                              "rank": 4,
                              "contributingStatus": "GREEN",
                              "contributedProjectCount": 1,
                              "contributionCount": 269,
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
                                "bannerUrl": null
                              }
                            },
                            {
                              "rank": 29,
                              "contributingStatus": "GREEN",
                              "contributedProjectCount": 1,
                              "contributionCount": 269,
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
                                "bannerUrl": null
                              }
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_return_users_stats() {
        // Given
        final var user = userAuthHelper.authenticateAnthony().user();

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
                              "pullRequestCount": 4,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 16,
                              "codeReviewCount": 7,
                              "issueCount": 0,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 17,
                              "codeReviewCount": 2,
                              "issueCount": 0,
                              "pullRequestCount": 6,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 18,
                              "codeReviewCount": 1,
                              "issueCount": 0,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 19,
                              "codeReviewCount": 2,
                              "issueCount": 0,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 20,
                              "codeReviewCount": 2,
                              "issueCount": 0,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 21,
                              "codeReviewCount": 1,
                              "issueCount": 0,
                              "pullRequestCount": 0,
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
                              "week": 24,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 25,
                              "codeReviewCount": 1,
                              "issueCount": 3,
                              "pullRequestCount": 3,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 26,
                              "codeReviewCount": 4,
                              "issueCount": 0,
                              "pullRequestCount": 3,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 27,
                              "codeReviewCount": 10,
                              "issueCount": 3,
                              "pullRequestCount": 11,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 28,
                              "codeReviewCount": 9,
                              "issueCount": 5,
                              "pullRequestCount": 8,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 29,
                              "codeReviewCount": 9,
                              "issueCount": 0,
                              "pullRequestCount": 1,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 30,
                              "codeReviewCount": 7,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 31,
                              "codeReviewCount": 11,
                              "issueCount": 0,
                              "pullRequestCount": 15,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 32,
                              "codeReviewCount": 1,
                              "issueCount": 0,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 33,
                              "codeReviewCount": 3,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 34,
                              "codeReviewCount": 5,
                              "issueCount": 0,
                              "pullRequestCount": 5,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 35,
                              "codeReviewCount": 8,
                              "issueCount": 0,
                              "pullRequestCount": 12,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 36,
                              "codeReviewCount": 11,
                              "issueCount": 0,
                              "pullRequestCount": 13,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 37,
                              "codeReviewCount": 7,
                              "issueCount": 0,
                              "pullRequestCount": 6,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 38,
                              "codeReviewCount": 7,
                              "issueCount": 0,
                              "pullRequestCount": 6,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 39,
                              "codeReviewCount": 3,
                              "issueCount": 0,
                              "pullRequestCount": 5,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 40,
                              "codeReviewCount": 10,
                              "issueCount": 0,
                              "pullRequestCount": 8,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 41,
                              "codeReviewCount": 6,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 42,
                              "codeReviewCount": 3,
                              "issueCount": 0,
                              "pullRequestCount": 7,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 43,
                              "codeReviewCount": 6,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 44,
                              "codeReviewCount": 2,
                              "issueCount": 0,
                              "pullRequestCount": 3,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 45,
                              "codeReviewCount": 5,
                              "issueCount": 0,
                              "pullRequestCount": 4,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 46,
                              "codeReviewCount": 20,
                              "issueCount": 0,
                              "pullRequestCount": 8,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 47,
                              "codeReviewCount": 13,
                              "issueCount": 0,
                              "pullRequestCount": 10,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 48,
                              "codeReviewCount": 11,
                              "issueCount": 0,
                              "pullRequestCount": 6,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 49,
                              "codeReviewCount": 11,
                              "issueCount": 0,
                              "pullRequestCount": 11,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 50,
                              "codeReviewCount": 8,
                              "issueCount": 0,
                              "pullRequestCount": 5,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 51,
                              "codeReviewCount": 9,
                              "issueCount": 0,
                              "pullRequestCount": 12,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 52,
                              "codeReviewCount": 8,
                              "issueCount": 0,
                              "pullRequestCount": 9,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 1,
                              "codeReviewCount": 11,
                              "issueCount": 0,
                              "pullRequestCount": 9,
                              "rewardCount": 1
                            },
                            {
                              "year": 2023,
                              "week": 2,
                              "codeReviewCount": 12,
                              "issueCount": 0,
                              "pullRequestCount": 7,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 3,
                              "codeReviewCount": 16,
                              "issueCount": 0,
                              "pullRequestCount": 8,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 4,
                              "codeReviewCount": 23,
                              "issueCount": 0,
                              "pullRequestCount": 11,
                              "rewardCount": 2
                            },
                            {
                              "year": 2023,
                              "week": 5,
                              "codeReviewCount": 30,
                              "issueCount": 0,
                              "pullRequestCount": 19,
                              "rewardCount": 1
                            },
                            {
                              "year": 2023,
                              "week": 6,
                              "codeReviewCount": 17,
                              "issueCount": 0,
                              "pullRequestCount": 9,
                              "rewardCount": 1
                            },
                            {
                              "year": 2023,
                              "week": 8,
                              "codeReviewCount": 15,
                              "issueCount": 0,
                              "pullRequestCount": 10,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 9,
                              "codeReviewCount": 13,
                              "issueCount": 0,
                              "pullRequestCount": 14,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 10,
                              "codeReviewCount": 14,
                              "issueCount": 0,
                              "pullRequestCount": 24,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 11,
                              "codeReviewCount": 10,
                              "issueCount": 0,
                              "pullRequestCount": 10,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 12,
                              "codeReviewCount": 8,
                              "issueCount": 0,
                              "pullRequestCount": 17,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 13,
                              "codeReviewCount": 6,
                              "issueCount": 4,
                              "pullRequestCount": 7,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 14,
                              "codeReviewCount": 7,
                              "issueCount": 0,
                              "pullRequestCount": 7,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 15,
                              "codeReviewCount": 6,
                              "issueCount": 0,
                              "pullRequestCount": 5,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 16,
                              "codeReviewCount": 7,
                              "issueCount": 0,
                              "pullRequestCount": 10,
                              "rewardCount": 1
                            },
                            {
                              "year": 2023,
                              "week": 17,
                              "codeReviewCount": 8,
                              "issueCount": 0,
                              "pullRequestCount": 20,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 18,
                              "codeReviewCount": 7,
                              "issueCount": 0,
                              "pullRequestCount": 18,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 19,
                              "codeReviewCount": 10,
                              "issueCount": 0,
                              "pullRequestCount": 10,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 20,
                              "codeReviewCount": 6,
                              "issueCount": 0,
                              "pullRequestCount": 14,
                              "rewardCount": 1
                            },
                            {
                              "year": 2023,
                              "week": 21,
                              "codeReviewCount": 4,
                              "issueCount": 0,
                              "pullRequestCount": 17,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 22,
                              "codeReviewCount": 2,
                              "issueCount": 0,
                              "pullRequestCount": 8,
                              "rewardCount": 2
                            },
                            {
                              "year": 2023,
                              "week": 23,
                              "codeReviewCount": 2,
                              "issueCount": 0,
                              "pullRequestCount": 9,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 24,
                              "codeReviewCount": 5,
                              "issueCount": 0,
                              "pullRequestCount": 6,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 25,
                              "codeReviewCount": 4,
                              "issueCount": 0,
                              "pullRequestCount": 8,
                              "rewardCount": 2
                            },
                            {
                              "year": 2023,
                              "week": 26,
                              "codeReviewCount": 6,
                              "issueCount": 0,
                              "pullRequestCount": 13,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 27,
                              "codeReviewCount": 2,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 28,
                              "codeReviewCount": 7,
                              "issueCount": 0,
                              "pullRequestCount": 15,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 29,
                              "codeReviewCount": 4,
                              "issueCount": 0,
                              "pullRequestCount": 4,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 30,
                              "codeReviewCount": 5,
                              "issueCount": 0,
                              "pullRequestCount": 3,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 31,
                              "codeReviewCount": 7,
                              "issueCount": 0,
                              "pullRequestCount": 4,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 32,
                              "codeReviewCount": 6,
                              "issueCount": 0,
                              "pullRequestCount": 13,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 33,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 1,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 35,
                              "codeReviewCount": 6,
                              "issueCount": 0,
                              "pullRequestCount": 3,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 36,
                              "codeReviewCount": 7,
                              "issueCount": 0,
                              "pullRequestCount": 7,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 37,
                              "codeReviewCount": 3,
                              "issueCount": 0,
                              "pullRequestCount": 4,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 38,
                              "codeReviewCount": 5,
                              "issueCount": 0,
                              "pullRequestCount": 11,
                              "rewardCount": 10
                            },
                            {
                              "year": 2023,
                              "week": 39,
                              "codeReviewCount": 2,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 40,
                              "codeReviewCount": 1,
                              "issueCount": 0,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 41,
                              "codeReviewCount": 1,
                              "issueCount": 0,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 42,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 1,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 43,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 1,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 48,
                              "codeReviewCount": 1,
                              "issueCount": 0,
                              "pullRequestCount": 1,
                              "rewardCount": 0
                            }
                          ],
                          "earnings": {
                            "totalEarnedUsd": 2692632.50,
                            "perProject": [
                              {
                                "projectName": "kaaper",
                                "totalEarnedUsd": 1792080.00
                              },
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
                                "projectName": "Aldébaran du Taureau",
                                "totalEarnedUsd": 1767.50
                              },
                              {
                                "projectName": "oscar's awesome project",
                                "totalEarnedUsd": 1010.00
                              }
                            ]
                          },
                          "workDistribution": {
                            "codeReviewCount": 530,
                            "issueCount": 15,
                            "pullRequestCount": 553
                          }
                        }
                        """);
    }

    @Test
    void should_filter_users_stats_per_ecosystem() {
        // Given
        final var user = userAuthHelper.authenticateAnthony().user();

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
                              "week": 24,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 25,
                              "codeReviewCount": 1,
                              "issueCount": 3,
                              "pullRequestCount": 3,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 26,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 3,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 27,
                              "codeReviewCount": 10,
                              "issueCount": 3,
                              "pullRequestCount": 11,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 28,
                              "codeReviewCount": 4,
                              "issueCount": 5,
                              "pullRequestCount": 8,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 29,
                              "codeReviewCount": 8,
                              "issueCount": 0,
                              "pullRequestCount": 1,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 30,
                              "codeReviewCount": 6,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 31,
                              "codeReviewCount": 10,
                              "issueCount": 0,
                              "pullRequestCount": 15,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 32,
                              "codeReviewCount": 1,
                              "issueCount": 0,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 33,
                              "codeReviewCount": 3,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 34,
                              "codeReviewCount": 5,
                              "issueCount": 0,
                              "pullRequestCount": 5,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 35,
                              "codeReviewCount": 8,
                              "issueCount": 0,
                              "pullRequestCount": 12,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 36,
                              "codeReviewCount": 11,
                              "issueCount": 0,
                              "pullRequestCount": 13,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 37,
                              "codeReviewCount": 7,
                              "issueCount": 0,
                              "pullRequestCount": 6,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 38,
                              "codeReviewCount": 7,
                              "issueCount": 0,
                              "pullRequestCount": 6,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 39,
                              "codeReviewCount": 3,
                              "issueCount": 0,
                              "pullRequestCount": 5,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 40,
                              "codeReviewCount": 10,
                              "issueCount": 0,
                              "pullRequestCount": 8,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 41,
                              "codeReviewCount": 6,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 42,
                              "codeReviewCount": 3,
                              "issueCount": 0,
                              "pullRequestCount": 7,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 43,
                              "codeReviewCount": 5,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 44,
                              "codeReviewCount": 2,
                              "issueCount": 0,
                              "pullRequestCount": 3,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 45,
                              "codeReviewCount": 5,
                              "issueCount": 0,
                              "pullRequestCount": 4,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 46,
                              "codeReviewCount": 17,
                              "issueCount": 0,
                              "pullRequestCount": 8,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 47,
                              "codeReviewCount": 13,
                              "issueCount": 0,
                              "pullRequestCount": 10,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 48,
                              "codeReviewCount": 11,
                              "issueCount": 0,
                              "pullRequestCount": 6,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 49,
                              "codeReviewCount": 11,
                              "issueCount": 0,
                              "pullRequestCount": 11,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 50,
                              "codeReviewCount": 8,
                              "issueCount": 0,
                              "pullRequestCount": 5,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 51,
                              "codeReviewCount": 9,
                              "issueCount": 0,
                              "pullRequestCount": 12,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 52,
                              "codeReviewCount": 8,
                              "issueCount": 0,
                              "pullRequestCount": 9,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 1,
                              "codeReviewCount": 11,
                              "issueCount": 0,
                              "pullRequestCount": 9,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 2,
                              "codeReviewCount": 12,
                              "issueCount": 0,
                              "pullRequestCount": 7,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 3,
                              "codeReviewCount": 16,
                              "issueCount": 0,
                              "pullRequestCount": 8,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 4,
                              "codeReviewCount": 22,
                              "issueCount": 0,
                              "pullRequestCount": 11,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 5,
                              "codeReviewCount": 29,
                              "issueCount": 0,
                              "pullRequestCount": 19,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 6,
                              "codeReviewCount": 17,
                              "issueCount": 0,
                              "pullRequestCount": 9,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 8,
                              "codeReviewCount": 14,
                              "issueCount": 0,
                              "pullRequestCount": 8,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 9,
                              "codeReviewCount": 13,
                              "issueCount": 0,
                              "pullRequestCount": 14,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 10,
                              "codeReviewCount": 14,
                              "issueCount": 0,
                              "pullRequestCount": 24,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 11,
                              "codeReviewCount": 10,
                              "issueCount": 0,
                              "pullRequestCount": 10,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 12,
                              "codeReviewCount": 8,
                              "issueCount": 0,
                              "pullRequestCount": 17,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 13,
                              "codeReviewCount": 6,
                              "issueCount": 0,
                              "pullRequestCount": 7,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 14,
                              "codeReviewCount": 7,
                              "issueCount": 0,
                              "pullRequestCount": 7,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 15,
                              "codeReviewCount": 6,
                              "issueCount": 0,
                              "pullRequestCount": 5,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 16,
                              "codeReviewCount": 7,
                              "issueCount": 0,
                              "pullRequestCount": 10,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 17,
                              "codeReviewCount": 8,
                              "issueCount": 0,
                              "pullRequestCount": 19,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 18,
                              "codeReviewCount": 7,
                              "issueCount": 0,
                              "pullRequestCount": 18,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 19,
                              "codeReviewCount": 10,
                              "issueCount": 0,
                              "pullRequestCount": 10,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 20,
                              "codeReviewCount": 6,
                              "issueCount": 0,
                              "pullRequestCount": 14,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 21,
                              "codeReviewCount": 4,
                              "issueCount": 0,
                              "pullRequestCount": 17,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 22,
                              "codeReviewCount": 2,
                              "issueCount": 0,
                              "pullRequestCount": 8,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 23,
                              "codeReviewCount": 2,
                              "issueCount": 0,
                              "pullRequestCount": 9,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 24,
                              "codeReviewCount": 5,
                              "issueCount": 0,
                              "pullRequestCount": 6,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 25,
                              "codeReviewCount": 4,
                              "issueCount": 0,
                              "pullRequestCount": 8,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 26,
                              "codeReviewCount": 6,
                              "issueCount": 0,
                              "pullRequestCount": 13,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 27,
                              "codeReviewCount": 2,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 28,
                              "codeReviewCount": 7,
                              "issueCount": 0,
                              "pullRequestCount": 15,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 29,
                              "codeReviewCount": 4,
                              "issueCount": 0,
                              "pullRequestCount": 4,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 30,
                              "codeReviewCount": 5,
                              "issueCount": 0,
                              "pullRequestCount": 3,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 31,
                              "codeReviewCount": 7,
                              "issueCount": 0,
                              "pullRequestCount": 4,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 32,
                              "codeReviewCount": 6,
                              "issueCount": 0,
                              "pullRequestCount": 13,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 33,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 1,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 35,
                              "codeReviewCount": 6,
                              "issueCount": 0,
                              "pullRequestCount": 3,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 36,
                              "codeReviewCount": 7,
                              "issueCount": 0,
                              "pullRequestCount": 7,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 37,
                              "codeReviewCount": 3,
                              "issueCount": 0,
                              "pullRequestCount": 4,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 38,
                              "codeReviewCount": 5,
                              "issueCount": 0,
                              "pullRequestCount": 11,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 39,
                              "codeReviewCount": 1,
                              "issueCount": 0,
                              "pullRequestCount": 2,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 40,
                              "codeReviewCount": 1,
                              "issueCount": 0,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 41,
                              "codeReviewCount": 1,
                              "issueCount": 0,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 42,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 1,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 43,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 1,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 48,
                              "codeReviewCount": 1,
                              "issueCount": 0,
                              "pullRequestCount": 1,
                              "rewardCount": 0
                            }
                          ],
                          "earnings": {
                            "totalEarnedUsd": 0,
                            "perProject": []
                          },
                          "workDistribution": {
                            "codeReviewCount": 494,
                            "issueCount": 11,
                            "pullRequestCount": 540
                          }
                        }
                        """);
    }
}
