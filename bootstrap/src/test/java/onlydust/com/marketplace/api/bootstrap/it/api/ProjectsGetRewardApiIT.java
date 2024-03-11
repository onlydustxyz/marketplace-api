package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.VerificationStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.BillingProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.util.Map;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;


public class ProjectsGetRewardApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    BillingProfileRepository billingProfileRepository;

    @Test
    void should_return_a_403_given_not_project_lead_to_get_reward() {
        // Given
        final String jwt = userAuthHelper.newFakeUser(UUID.randomUUID(), 1L, faker.rickAndMorty().location(),
                faker.internet().url(), false).jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final UUID rewardId = UUID.fromString("85f8358c-5339-42ac-a577-16d7760d1e28");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARD, projectId, rewardId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(403);
    }

    @Test
    void should_get_reward_given_a_project_lead() throws ParseException {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final UUID rewardId = UUID.fromString("85f8358c-5339-42ac-a577-16d7760d1e28");

        final var billingProfile = billingProfileRepository.findById(UUID.fromString("20282367-56b0-42d3-81d3-5e4b38f67e3e")).orElseThrow();
        accountingHelper.patchBillingProfile(billingProfile.getId(), null, VerificationStatusEntity.VERIFIED);
        billingProfileRepository.save(billingProfile);

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_REWARD, projectId, rewardId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "id": "85f8358c-5339-42ac-a577-16d7760d1e28",
                          "currency": "USDC",
                          "amount": 1000,
                          "dollarsEquivalent": 1010.00,
                          "status": "PENDING_CONTRIBUTOR",
                          "unlockDate": null,
                          "from": {
                            "githubUserId": 16590657,
                            "login": "PierreOucif",
                            "htmlUrl": null,
                            "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                            "isRegistered": null
                          },
                          "to": {
                            "githubUserId": 16590657,
                            "login": "PierreOucif",
                            "htmlUrl": null,
                            "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                            "isRegistered": null
                          },
                          "createdAt": "2023-09-19T07:38:52.590518Z",
                          "processedAt": null,
                          "project": {
                            "id": "f39b827f-df73-498c-8853-99bc3f562723",
                            "slug": "qa-new-contributions",
                            "name": "QA new contributions",
                            "shortDescription": "QA new contributions",
                            "logoUrl": null,
                            "visibility": "PUBLIC"
                          },
                          "receipt": null
                        }
                        """);
    }


    @Test
    void should_return_a_403_given_not_project_lead_to_get_reward_items() {
        // Given
        final String jwt = userAuthHelper.newFakeUser(UUID.randomUUID(), 2L, faker.rickAndMorty().location(),
                faker.internet().url(), false).jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final UUID rewardId = UUID.fromString("85f8358c-5339-42ac-a577-16d7760d1e28");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_REWARD_ITEMS, projectId, rewardId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(403);
    }

    @Test
    void should_return_pagination_reward_items_given_a_project_lead() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final UUID rewardId = UUID.fromString("85f8358c-5339-42ac-a577-16d7760d1e28");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_REWARD_ITEMS, projectId, rewardId), Map.of("pageSize", "2",
                        "pageIndex", "0")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "rewardItems": [
                            {
                              "number": 1232,
                              "id": "1511546916",
                              "contributionId": "a290ea203b1264105bf581aebbdf3e79edfdd89811da50dc6bd076272d810b2e",
                              "title": "Addin sitemap.xml in robots.txt",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1232",
                              "createdAt": "2023-09-12T07:38:04Z",
                              "completedAt": "2023-09-12T07:45:12Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 1,
                              "userCommitsCount": 1,
                              "commentsCount": null,
                              "status": "MERGED",
                              "githubAuthorId": 16590657,
                              "authorLogin": "PierreOucif",
                              "authorAvatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                              "authorGithubUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                              "githubBody": null
                            },
                            {
                              "number": 1225,
                              "id": "1507455279",
                              "contributionId": "955b084215e36980ded785f5afef2725e82f24188a48afaa59d1909c97d60ad6",
                              "title": "E 730 migrate oscar frontend documentation",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1225",
                              "createdAt": "2023-09-08T08:14:32Z",
                              "completedAt": "2023-09-08T08:19:55Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 3,
                              "userCommitsCount": 3,
                              "commentsCount": null,
                              "status": "MERGED",
                              "githubAuthorId": 16590657,
                              "authorLogin": "PierreOucif",
                              "authorAvatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                              "authorGithubUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                              "githubBody": null
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 13,
                          "totalItemNumber": 25,
                          "nextPageIndex": 1
                        }
                        """);

        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_REWARD_ITEMS, projectId, rewardId), Map.of("pageSize", "2",
                        "pageIndex", "12")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""     
                        {
                          "rewardItems": [
                            {
                              "number": 1129,
                              "id": "1442413635",
                              "contributionId": "6d709dd5f85a8b8eaff9cc8837ab837ef9a1a1109ead76580490c0a730a87d9d",
                              "title": "First API integration test",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1129",
                              "createdAt": "2023-07-20T08:45:18Z",
                              "completedAt": "2023-07-21T13:00:05Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 30,
                              "userCommitsCount": 16,
                              "commentsCount": null,
                              "status": "MERGED",
                              "githubAuthorId": 43467246,
                              "authorLogin": "AnthonyBuisset",
                              "authorAvatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                              "authorGithubUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                              "githubBody": "IT test structure\\nDocker container support\\nFirst API integration test: create project"
                            }
                          ],
                          "hasMore": false,
                          "totalPageNumber": 13,
                          "totalItemNumber": 25,
                          "nextPageIndex": 12
                        }                                            
                        """);
    }

    @Test
    void should_return_reward_items_when_they_are_not_related_to_any_contribution() {
        // Given
        final String jwt = userAuthHelper.authenticateGregoire().jwt();
        final UUID projectId = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");
        final UUID rewardId = UUID.fromString("4ccf3463-c77d-42cd-85f3-b393901a89c1");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_REWARD_ITEMS, projectId, rewardId), Map.of("pageSize", "2",
                        "pageIndex", "0")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "rewardItems": [
                            {
                              "number": 1,
                              "id": "546577833",
                              "contributionId": null,
                              "title": "Update README.md",
                              "githubUrl": "https://github.com/MaximeBeasse/KeyDecoder/pull/1",
                              "createdAt": "2020-12-29T17:46:26Z",
                              "completedAt": "2020-12-29T18:11:50Z",
                              "repoName": "KeyDecoder",
                              "type": "PULL_REQUEST",
                              "commitsCount": 1,
                              "userCommitsCount": null,
                              "commentsCount": null,
                              "status": "MERGED",
                              "githubAuthorId": 8495664,
                              "authorLogin": "doomed-theory",
                              "authorAvatarUrl": "https://avatars.githubusercontent.com/u/8495664?v=4",
                              "authorGithubUrl": "https://avatars.githubusercontent.com/u/8495664?v=4",
                              "githubBody": "Grammar"
                            }
                          ],
                          "hasMore": false,
                          "totalPageNumber": 1,
                          "totalItemNumber": 1,
                          "nextPageIndex": 0
                        }
                        """);
    }

    @Test
    void should_return_reward_items_with_null_contributionId_when_contribution_belongs_to_another_project() {
        // Given
        final String jwt = userAuthHelper.authenticateUser(21149076L).jwt();
        final UUID projectId = UUID.fromString("6d955622-c1ce-4227-85ea-51cb1b3207b1");
        final UUID rewardId = UUID.fromString("f4b7c3e4-4a45-47c7-bcba-87d6d767c3de");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_REWARD_ITEMS, projectId, rewardId), Map.of("pageSize", "200",
                        "pageIndex", "0")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewardItems[?(@.id==1092315108)]").exists()
                .jsonPath("$.rewardItems[?(@.id==1092315108)].title").isEqualTo(":sparkles: Implement stack pop_n")
                .jsonPath("$.rewardItems[?(@.id==1092315108)].contributionId").isEqualTo(null);
    }

}
