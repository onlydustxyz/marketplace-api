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
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "languages": [
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
                            },
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
                              "pullRequestCount": 8,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 25,
                              "codeReviewCount": 4,
                              "issueCount": 12,
                              "pullRequestCount": 12,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 26,
                              "codeReviewCount": 4,
                              "issueCount": 0,
                              "pullRequestCount": 12,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 27,
                              "codeReviewCount": 40,
                              "issueCount": 12,
                              "pullRequestCount": 44,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 28,
                              "codeReviewCount": 21,
                              "issueCount": 20,
                              "pullRequestCount": 32,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 29,
                              "codeReviewCount": 33,
                              "issueCount": 0,
                              "pullRequestCount": 4,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 30,
                              "codeReviewCount": 25,
                              "issueCount": 0,
                              "pullRequestCount": 8,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 31,
                              "codeReviewCount": 41,
                              "issueCount": 0,
                              "pullRequestCount": 60,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 32,
                              "codeReviewCount": 4,
                              "issueCount": 0,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 33,
                              "codeReviewCount": 12,
                              "issueCount": 0,
                              "pullRequestCount": 8,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 34,
                              "codeReviewCount": 20,
                              "issueCount": 0,
                              "pullRequestCount": 20,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 35,
                              "codeReviewCount": 32,
                              "issueCount": 0,
                              "pullRequestCount": 48,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 36,
                              "codeReviewCount": 44,
                              "issueCount": 0,
                              "pullRequestCount": 52,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 37,
                              "codeReviewCount": 28,
                              "issueCount": 0,
                              "pullRequestCount": 24,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 38,
                              "codeReviewCount": 28,
                              "issueCount": 0,
                              "pullRequestCount": 24,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 39,
                              "codeReviewCount": 12,
                              "issueCount": 0,
                              "pullRequestCount": 20,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 40,
                              "codeReviewCount": 40,
                              "issueCount": 0,
                              "pullRequestCount": 32,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 41,
                              "codeReviewCount": 24,
                              "issueCount": 0,
                              "pullRequestCount": 8,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 42,
                              "codeReviewCount": 12,
                              "issueCount": 0,
                              "pullRequestCount": 28,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 43,
                              "codeReviewCount": 21,
                              "issueCount": 0,
                              "pullRequestCount": 8,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 44,
                              "codeReviewCount": 8,
                              "issueCount": 0,
                              "pullRequestCount": 12,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 45,
                              "codeReviewCount": 20,
                              "issueCount": 0,
                              "pullRequestCount": 16,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 46,
                              "codeReviewCount": 71,
                              "issueCount": 0,
                              "pullRequestCount": 32,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 47,
                              "codeReviewCount": 52,
                              "issueCount": 0,
                              "pullRequestCount": 40,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 48,
                              "codeReviewCount": 44,
                              "issueCount": 0,
                              "pullRequestCount": 24,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 49,
                              "codeReviewCount": 44,
                              "issueCount": 0,
                              "pullRequestCount": 44,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 50,
                              "codeReviewCount": 32,
                              "issueCount": 0,
                              "pullRequestCount": 20,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 51,
                              "codeReviewCount": 36,
                              "issueCount": 0,
                              "pullRequestCount": 48,
                              "rewardCount": 0
                            },
                            {
                              "year": 2022,
                              "week": 52,
                              "codeReviewCount": 32,
                              "issueCount": 0,
                              "pullRequestCount": 36,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 1,
                              "codeReviewCount": 44,
                              "issueCount": 0,
                              "pullRequestCount": 36,
                              "rewardCount": 1
                            },
                            {
                              "year": 2023,
                              "week": 2,
                              "codeReviewCount": 48,
                              "issueCount": 0,
                              "pullRequestCount": 28,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 3,
                              "codeReviewCount": 64,
                              "issueCount": 0,
                              "pullRequestCount": 32,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 4,
                              "codeReviewCount": 89,
                              "issueCount": 0,
                              "pullRequestCount": 44,
                              "rewardCount": 2
                            },
                            {
                              "year": 2023,
                              "week": 5,
                              "codeReviewCount": 117,
                              "issueCount": 0,
                              "pullRequestCount": 76,
                              "rewardCount": 1
                            },
                            {
                              "year": 2023,
                              "week": 6,
                              "codeReviewCount": 68,
                              "issueCount": 0,
                              "pullRequestCount": 36,
                              "rewardCount": 1
                            },
                            {
                              "year": 2023,
                              "week": 8,
                              "codeReviewCount": 57,
                              "issueCount": 0,
                              "pullRequestCount": 34,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 9,
                              "codeReviewCount": 52,
                              "issueCount": 0,
                              "pullRequestCount": 56,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 10,
                              "codeReviewCount": 56,
                              "issueCount": 0,
                              "pullRequestCount": 96,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 11,
                              "codeReviewCount": 39,
                              "issueCount": 0,
                              "pullRequestCount": 40,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 12,
                              "codeReviewCount": 32,
                              "issueCount": 0,
                              "pullRequestCount": 68,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 13,
                              "codeReviewCount": 24,
                              "issueCount": 4,
                              "pullRequestCount": 28,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 14,
                              "codeReviewCount": 28,
                              "issueCount": 0,
                              "pullRequestCount": 28,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 15,
                              "codeReviewCount": 24,
                              "issueCount": 0,
                              "pullRequestCount": 20,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 16,
                              "codeReviewCount": 28,
                              "issueCount": 0,
                              "pullRequestCount": 40,
                              "rewardCount": 1
                            },
                            {
                              "year": 2023,
                              "week": 17,
                              "codeReviewCount": 32,
                              "issueCount": 0,
                              "pullRequestCount": 77,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 18,
                              "codeReviewCount": 27,
                              "issueCount": 0,
                              "pullRequestCount": 72,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 19,
                              "codeReviewCount": 40,
                              "issueCount": 0,
                              "pullRequestCount": 40,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 20,
                              "codeReviewCount": 24,
                              "issueCount": 0,
                              "pullRequestCount": 56,
                              "rewardCount": 1
                            },
                            {
                              "year": 2023,
                              "week": 21,
                              "codeReviewCount": 16,
                              "issueCount": 0,
                              "pullRequestCount": 68,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 22,
                              "codeReviewCount": 8,
                              "issueCount": 0,
                              "pullRequestCount": 32,
                              "rewardCount": 2
                            },
                            {
                              "year": 2023,
                              "week": 23,
                              "codeReviewCount": 8,
                              "issueCount": 0,
                              "pullRequestCount": 36,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 24,
                              "codeReviewCount": 20,
                              "issueCount": 0,
                              "pullRequestCount": 24,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 25,
                              "codeReviewCount": 16,
                              "issueCount": 0,
                              "pullRequestCount": 32,
                              "rewardCount": 2
                            },
                            {
                              "year": 2023,
                              "week": 26,
                              "codeReviewCount": 24,
                              "issueCount": 0,
                              "pullRequestCount": 52,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 27,
                              "codeReviewCount": 8,
                              "issueCount": 0,
                              "pullRequestCount": 8,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 28,
                              "codeReviewCount": 28,
                              "issueCount": 0,
                              "pullRequestCount": 59,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 29,
                              "codeReviewCount": 16,
                              "issueCount": 0,
                              "pullRequestCount": 16,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 30,
                              "codeReviewCount": 20,
                              "issueCount": 0,
                              "pullRequestCount": 12,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 31,
                              "codeReviewCount": 28,
                              "issueCount": 0,
                              "pullRequestCount": 16,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 32,
                              "codeReviewCount": 24,
                              "issueCount": 0,
                              "pullRequestCount": 52,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 33,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 4,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 35,
                              "codeReviewCount": 24,
                              "issueCount": 0,
                              "pullRequestCount": 12,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 36,
                              "codeReviewCount": 28,
                              "issueCount": 0,
                              "pullRequestCount": 28,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 37,
                              "codeReviewCount": 12,
                              "issueCount": 0,
                              "pullRequestCount": 16,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 38,
                              "codeReviewCount": 20,
                              "issueCount": 0,
                              "pullRequestCount": 44,
                              "rewardCount": 10
                            },
                            {
                              "year": 2023,
                              "week": 39,
                              "codeReviewCount": 5,
                              "issueCount": 0,
                              "pullRequestCount": 8,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 40,
                              "codeReviewCount": 4,
                              "issueCount": 0,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 41,
                              "codeReviewCount": 4,
                              "issueCount": 0,
                              "pullRequestCount": 0,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 42,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 4,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 43,
                              "codeReviewCount": 0,
                              "issueCount": 0,
                              "pullRequestCount": 4,
                              "rewardCount": 0
                            },
                            {
                              "year": 2023,
                              "week": 48,
                              "codeReviewCount": 4,
                              "issueCount": 0,
                              "pullRequestCount": 4,
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
                            "codeReviewCount": 2010,
                            "issueCount": 48,
                            "pullRequestCount": 2172
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