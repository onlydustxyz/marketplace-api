package onlydust.com.marketplace.api.bootstrap.it.api;

import org.junit.jupiter.api.Test;


public class ActivityApiIT extends AbstractMarketplaceApiIT {


    @Test
    void should_return_server_starting_date() {
        // Given

        // When
        client.get()
                .uri("/api/v1/public-activity")
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 18,
                          "totalItemNumber": 89,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "activities": [
                            {
                              "type": "PULL_REQUEST",
                              "timestamp": "2023-12-04T14:12:51Z",
                              "pullRequest": {
                                "project": {
                                  "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                  "slug": "kaaper",
                                  "name": "kaaper",
                                  "logoUrl": null
                                },
                                "author": {
                                  "githubUserId": 5160414,
                                  "login": "haydencleary",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4"
                                }
                              },
                              "rewardCreated": null,
                              "rewardClaimed": null,
                              "projectCreated": null
                            },
                            {
                              "type": "PULL_REQUEST",
                              "timestamp": "2023-11-29T16:20:44Z",
                              "pullRequest": {
                                "project": {
                                  "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                  "slug": "bretzel",
                                  "name": "Bretzel",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                                },
                                "author": {
                                  "githubUserId": 8642470,
                                  "login": "gregcha",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp"
                                }
                              },
                              "rewardCreated": null,
                              "rewardClaimed": null,
                              "projectCreated": null
                            },
                            {
                              "type": "REWARD_CREATED",
                              "timestamp": "2023-10-08T10:09:31.842Z",
                              "pullRequest": null,
                              "rewardCreated": {
                                "project": {
                                  "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                  "slug": "bretzel",
                                  "name": "Bretzel",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                                },
                                "recipient": {
                                  "githubUserId": 8642470,
                                  "login": "gregcha",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp"
                                },
                                "amount": {
                                  "amount": 1000.00,
                                  "currency": {
                                    "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                    "code": "USD",
                                    "name": "US Dollar",
                                    "logoUrl": null,
                                    "decimals": 2
                                  }
                                }
                              },
                              "rewardClaimed": null,
                              "projectCreated": null
                            },
                            {
                              "type": "PROJECT_CREATED",
                              "timestamp": "2023-10-04T10:40:58.413Z",
                              "pullRequest": null,
                              "rewardCreated": null,
                              "rewardClaimed": null,
                              "projectCreated": {
                                "project": {
                                  "id": "2a95f786-beb2-461d-b573-7150e4a1b65b",
                                  "slug": "poor-project",
                                  "name": "Poor Project",
                                  "logoUrl": null
                                },
                                "createdBy": {
                                  "githubUserId": 31901905,
                                  "login": "kaelsky",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4"
                                }
                              }
                            },
                            {
                              "type": "PROJECT_CREATED",
                              "timestamp": "2023-09-28T13:57:48.257Z",
                              "pullRequest": null,
                              "rewardCreated": null,
                              "rewardClaimed": null,
                              "projectCreated": {
                                "project": {
                                  "id": "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e",
                                  "slug": "calcom",
                                  "name": "Cal.com",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5271998260751715005.png"
                                },
                                "createdBy": {
                                  "githubUserId": 117665867,
                                  "login": "gilbertVDB17",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/117665867?v=4"
                                }
                              }
                            }
                          ]
                        }
                        """);
    }
}
