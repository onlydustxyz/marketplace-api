package onlydust.com.marketplace.api.bootstrap.it;

import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.IgnoredContributionEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.IgnoredContributionsRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@ActiveProfiles({"hasura_auth"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectsGetRewardableItemsApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    HasuraUserHelper hasuraUserHelper;
    @Autowired
    IgnoredContributionsRepository ignoredContributionsRepository;
    @Autowired
    ProjectRepository projectRepository;


    @Test
    @Order(1)
    public void should_be_unauthorized() {
        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_REWARDABLE_ITEMS, UUID.randomUUID()), Map.of("githubUserId"
                        , "1",
                        "pageIndex", "0", "pageSize", "100")))
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(401);
    }

    @Test
    @Order(2)
    void should_be_forbidden_given_authenticated_user_not_project_lead() {
        // Given
        hasuraUserHelper.newFakeUser(UUID.randomUUID(), 1L, faker.rickAndMorty().character(), faker.internet().url(),
                false);
        final String jwt = hasuraUserHelper.authenticateUser(1L).jwt();
        final UUID projectId = projectRepository.findAll().get(0).getId();

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_REWARDABLE_ITEMS, projectId), Map.of("githubUserId", "1",
                        "pageIndex", "0", "pageSize", "100")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(403)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Only project leads can read rewardable items on their projects");
    }

    @Test
    @Order(10)
    void should_get_rewardable_items_given_a_project_lead() {
        // Given
        final HasuraUserHelper.AuthenticatedUser pierre = hasuraUserHelper.authenticatePierre();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_REWARDABLE_ITEMS, projectId), Map.of("githubUserId",
                        pierre.user().getGithubUserId().toString(),
                        "pageIndex", "0", "pageSize", "10")))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(206)
                .expectBody()
                .json("""
                        {
                           "rewardableItems": [
                             {
                               "number": 1375,
                               "id": "279c2e7794a6f798c0de46c6fe23cbffcc2feb485072a25fdefc726eaf90e34d",
                               "title": "fix tooltip text",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1375",
                               "createdAt": "2023-11-03T16:42:17Z",
                               "lastUpdateAt": "2023-11-03T16:43:49Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1374,
                               "id": "803254a420051b6b04c8cb2030922f3a93cfe2bfbac34c61b56dde93184dbd70",
                               "title": "remove cache on contributor",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1374",
                               "createdAt": "2023-11-03T16:37:19Z",
                               "lastUpdateAt": "2023-11-03T16:37:30Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1367,
                               "id": "938c879cca27fb6e59ef30658ea5587d2c830dd8bf52a3ec0192b5a780fea267",
                               "title": "fix/contribution-status",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1367",
                               "createdAt": "2023-11-03T11:43:26Z",
                               "lastUpdateAt": "2023-11-03T11:47:30Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1365,
                               "id": "c6ffc682726fc0ffd3a41a9bee04d62d288761501b5906968577bcd132e36cbf",
                               "title": "feat: prevent submitting separate iban bic",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1365",
                               "createdAt": "2023-11-02T19:48:44Z",
                               "lastUpdateAt": null,
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": null,
                               "status": "OPEN"
                             },
                             {
                               "number": 1364,
                               "id": "4b44061840a2f8185f80f2fa381c2aa1bd228e56bde84ab8e9a243ca6da7b073",
                               "title": "feat: send empty sepa infos",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1364",
                               "createdAt": "2023-11-02T19:22:47Z",
                               "lastUpdateAt": "2023-11-02T19:23:41Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1354,
                               "id": "870831e2a427e91e26d1f7946b8d538549abb5892a02530156d63df0a3f3f43a",
                               "title": "E 692 qa status",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1354",
                               "createdAt": "2023-11-02T11:25:42Z",
                               "lastUpdateAt": null,
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": null,
                               "status": "OPEN"
                             },
                             {
                               "number": 1352,
                               "id": "a93a3eec8f658a0fb0fb81dd86cc76e9d918be84e6fffab926a374ceaf1d3fdb",
                               "title": "feat: payout side panel QA",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1352",
                               "createdAt": "2023-10-31T18:21:25Z",
                               "lastUpdateAt": "2023-11-02T09:11:27Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1351,
                               "id": "331574dd3aa78737d775eb4948491ea306ec153f5a999b3dbf85b76d21ae4021",
                               "title": "Multi currencies QA 01",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1351",
                               "createdAt": "2023-10-31T16:45:53Z",
                               "lastUpdateAt": "2023-10-31T18:55:18Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1350,
                               "id": "2a9e1bd1918d8f1718a4c8d14ee4cf0ea05ee3ded79083f961fa5f54def48f7b",
                               "title": "Fix/impersonate",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1350",
                               "createdAt": "2023-10-31T16:25:03Z",
                               "lastUpdateAt": "2023-10-31T16:51:37Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1343,
                               "id": "c7bdbbd8fc81d3f23820301a2b50bc3b9d9050d0923dd063090074a4faab805e",
                               "title": "fix: available conversion chip",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1343",
                               "createdAt": "2023-10-30T16:45:00Z",
                               "lastUpdateAt": "2023-10-30T16:50:20Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             }
                           ],
                           "hasMore": true,
                           "totalPageNumber": 14,
                           "totalItemNumber": 138,
                           "nextPageIndex": 1
                         }
                         """);

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_REWARDABLE_ITEMS, projectId), Map.of("githubUserId",
                        pierre.user().getGithubUserId().toString(),
                        "pageIndex", "13", "pageSize", "10")))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(206)
                .expectBody()
                .json("""
                        {
                            "rewardableItems": [
                              {
                                "number": 1139,
                                "id": "2f1c832f96109e7ea29ff49c01fad772c3c0221a33c285b6a4dc8c636883cef0",
                                "title": "Configure datadog to foward front error logs",
                                "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1139",
                                "createdAt": "2023-07-26T13:10:36Z",
                                "lastUpdateAt": "2023-07-26T13:11:21Z",
                                "repoName": "marketplace-frontend",
                                "type": "CODE_REVIEW",
                                "commitsCount": null,
                                "userCommitsCount": 0,
                                "commentsCount": null,
                                "codeReviewOutcome": null,
                                "status": "OPEN"
                              },
                              {
                                "number": 1138,
                                "id": "b7ce941d8509b13d6cbf525dbf34c7062207a8c94607a4b06277faa5196159b2",
                                "title": "Adding gitguardian pre-commit",
                                "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1138",
                                "createdAt": "2023-07-26T11:04:28Z",
                                "lastUpdateAt": "2023-07-26T13:38:35Z",
                                "repoName": "marketplace-frontend",
                                "type": "PULL_REQUEST",
                                "commitsCount": 1,
                                "userCommitsCount": 1,
                                "commentsCount": null,
                                "codeReviewOutcome": null,
                                "status": "OPEN"
                              },
                              {
                                "number": 1137,
                                "id": "a866d0b8349fd8626464b7e54f73ffa2bc3d7cd133fb8bd2c641b7e7cddf8f41",
                                "title": "[E-582] Have sexy URLs for projects, profiles and others pages",
                                "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1137",
                                "createdAt": "2023-07-26T10:38:53Z",
                                "lastUpdateAt": "2023-07-26T14:28:13Z",
                                "repoName": "marketplace-frontend",
                                "type": "CODE_REVIEW",
                                "commitsCount": null,
                                "userCommitsCount": 0,
                                "commentsCount": null,
                                "codeReviewOutcome": "APPROVED",
                                "status": "OPEN"
                              },
                              {
                                "number": 1135,
                                "id": "ed389486455bfb37e70963dbc2c3f6370d220a669bb9eb62adcfedf9baf7af96",
                                "title": "Fix impersonation",
                                "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1135",
                                "createdAt": "2023-07-24T17:26:03Z",
                                "lastUpdateAt": "2023-07-24T17:40:33Z",
                                "repoName": "marketplace-frontend",
                                "type": "CODE_REVIEW",
                                "commitsCount": null,
                                "userCommitsCount": 0,
                                "commentsCount": null,
                                "codeReviewOutcome": "APPROVED",
                                "status": "OPEN"
                              },
                              {
                                "number": 1133,
                                "id": "f434009b52172d9812155a7c73d9d3a1fc2873858ef4e3cda003e9a086f8cb00",
                                "title": "e 609 restore credentials check at s3 connect time",
                                "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1133",
                                "createdAt": "2023-07-21T17:39:00Z",
                                "lastUpdateAt": "2023-07-26T06:46:50Z",
                                "repoName": "marketplace-frontend",
                                "type": "CODE_REVIEW",
                                "commitsCount": null,
                                "userCommitsCount": 0,
                                "commentsCount": null,
                                "codeReviewOutcome": "APPROVED",
                                "status": "OPEN"
                              },
                              {
                                "number": 1129,
                                "id": "72e253a8ea9fef3e9fee718f0b5c901efdbebb0fff8304cbf09f18066edd3e2f",
                                "title": "First API integration test",
                                "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1129",
                                "createdAt": "2023-07-20T10:45:18Z",
                                "lastUpdateAt": "2023-07-21T14:59:15Z",
                                "repoName": "marketplace-frontend",
                                "type": "CODE_REVIEW",
                                "commitsCount": null,
                                "userCommitsCount": 0,
                                "commentsCount": null,
                                "codeReviewOutcome": "APPROVED",
                                "status": "OPEN"
                              },
                              {
                                "number": 1129,
                                "id": "6d709dd5f85a8b8eaff9cc8837ab837ef9a1a1109ead76580490c0a730a87d9d",
                                "title": "First API integration test",
                                "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1129",
                                "createdAt": "2023-07-20T10:45:18Z",
                                "lastUpdateAt": "2023-07-21T15:00:05Z",
                                "repoName": "marketplace-frontend",
                                "type": "PULL_REQUEST",
                                "commitsCount": 39,
                                "userCommitsCount": 16,
                                "commentsCount": null,
                                "codeReviewOutcome": null,
                                "status": "OPEN"
                              },
                              {
                                "number": 1105,
                                "id": "ea209c984e15e5abadb98867658595c359578f391d272f1246bd744b1bb2de6f",
                                "title": "allow running our stack in a container",
                                "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1105",
                                "createdAt": "2023-07-12T13:54:35Z",
                                "lastUpdateAt": "2023-07-12T18:17:16Z",
                                "repoName": "marketplace-frontend",
                                "type": "CODE_REVIEW",
                                "commitsCount": null,
                                "userCommitsCount": 0,
                                "commentsCount": null,
                                "codeReviewOutcome": "APPROVED",
                                "status": "OPEN"
                              }
                            ],
                            "hasMore": false,
                            "totalPageNumber": 14,
                            "totalItemNumber": 138,
                            "nextPageIndex": 13
                          }
                         """);

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_REWARDABLE_ITEMS, projectId), Map.of("githubUserId",
                        pierre.user().getGithubUserId().toString(),
                        "pageIndex", "14", "pageSize", "10")))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(206)
                .expectBody()
                .json("""
                        {
                           "rewardableItems": [],
                           "hasMore": false,
                           "totalPageNumber": 14,
                           "totalItemNumber": 138,
                           "nextPageIndex": 14
                         }
                         """);


        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_REWARDABLE_ITEMS, projectId), Map.of("githubUserId",
                        pierre.user().getGithubUserId().toString(),
                        "pageIndex", "0", "pageSize", "10", "type", "PULL_REQUEST")))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(206)
                .expectBody()
                .json("""
                        {
                          "rewardableItems": [
                            {
                              "number": 1319,
                              "id": "fbfe8d8b8c2a63d1226ab1dc1f0fd773839830ecf08a6410c4abe20760308ee4",
                              "title": "ci: API types generation on build",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1319",
                              "createdAt": "2023-10-25T11:39:26Z",
                              "lastUpdateAt": "2023-10-30T10:56:58Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 0,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": null,
                              "status": "OPEN"
                            },
                            {
                              "number": 1318,
                              "id": "b8c5d6619154e8bb017c7464b0468379ff09f5f9ff6d80527b38c8424dd658ec",
                              "title": "This is a test PR for development purposes",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1318",
                              "createdAt": "2023-10-24T21:32:31Z",
                              "lastUpdateAt": "2023-10-24T21:36:49Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 0,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": null,
                              "status": "OPEN"
                            },
                            {
                              "number": 1301,
                              "id": "21d6e401ff24399fa97914de130211af686a5adf5f3be76c52d4eafbaa514ceb",
                              "title": "Docker build for e2 e",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1301",
                              "createdAt": "2023-10-16T13:51:28Z",
                              "lastUpdateAt": "2023-10-16T13:51:55Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 0,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": null,
                              "status": "OPEN"
                            },
                            {
                              "number": 1300,
                              "id": "946a969d9e5bc9bdf97746ded417effabeb4b912c995c21b9c8edbe1722b4e15",
                              "title": "Add new technologies",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1300",
                              "createdAt": "2023-10-16T09:07:41Z",
                              "lastUpdateAt": "2023-10-19T09:03:27Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 0,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": null,
                              "status": "OPEN"
                            },
                            {
                              "number": 1259,
                              "id": "2884dc233c8512d062d7dd0b60d78d58e416349bf0a3e1feddff1183a01895e8",
                              "title": "[E 626] Search fix",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1259",
                              "createdAt": "2023-09-22T16:04:48Z",
                              "lastUpdateAt": "2023-09-22T17:25:55Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 38,
                              "userCommitsCount": 13,
                              "commentsCount": null,
                              "codeReviewOutcome": null,
                              "status": "OPEN"
                            },
                            {
                              "number": 1238,
                              "id": "e473d496d162d4ed79eac072e74bc2a48501d73cc4fb6fa3e67a484a55cf0896",
                              "title": "feat: update ReadMe with libpq info",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1238",
                              "createdAt": "2023-09-18T10:24:21Z",
                              "lastUpdateAt": "2023-09-18T11:53:37Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 26,
                              "userCommitsCount": 13,
                              "commentsCount": null,
                              "codeReviewOutcome": null,
                              "status": "OPEN"
                            },
                            {
                              "number": 1232,
                              "id": "a290ea203b1264105bf581aebbdf3e79edfdd89811da50dc6bd076272d810b2e",
                              "title": "Addin sitemap.xml in robots.txt",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1232",
                              "createdAt": "2023-09-12T09:38:04Z",
                              "lastUpdateAt": "2023-09-12T09:45:12Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 1,
                              "userCommitsCount": 1,
                              "commentsCount": null,
                              "codeReviewOutcome": null,
                              "status": "OPEN"
                            },
                            {
                              "number": 1226,
                              "id": "3ecff6bfaa190bdbb8b42f0a842fda14bf34f93ee245e4b0ea0069af5cf74d1f",
                              "title": "Add noir language",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1226",
                              "createdAt": "2023-09-08T13:09:48Z",
                              "lastUpdateAt": "2023-09-08T13:20:32Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 1,
                              "userCommitsCount": 1,
                              "commentsCount": null,
                              "codeReviewOutcome": null,
                              "status": "OPEN"
                            },
                            {
                              "number": 1225,
                              "id": "955b084215e36980ded785f5afef2725e82f24188a48afaa59d1909c97d60ad6",
                              "title": "E 730 migrate oscar frontend documentation",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1225",
                              "createdAt": "2023-09-08T10:14:32Z",
                              "lastUpdateAt": "2023-09-08T10:19:55Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 3,
                              "userCommitsCount": 3,
                              "commentsCount": null,
                              "codeReviewOutcome": null,
                              "status": "OPEN"
                            },
                            {
                              "number": 1223,
                              "id": "e9389e1188692f483b1c82d4ecfdaeb3393a2f5a6b103b8babcc087a3596684a",
                              "title": "E 730 migrate oscar frontend documentation",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1223",
                              "createdAt": "2023-09-07T15:35:28Z",
                              "lastUpdateAt": "2023-09-08T10:10:39Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 2,
                              "userCommitsCount": 2,
                              "commentsCount": null,
                              "codeReviewOutcome": null,
                              "status": "OPEN"
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 4,
                          "totalItemNumber": 38,
                          "nextPageIndex": 1
                        }
                         """);

        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_REWARDABLE_ITEMS, projectId), Map.of("githubUserId",
                        pierre.user().getGithubUserId().toString(),
                        "pageIndex", "0", "pageSize", "10", "type", "ISSUE")))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(200)
                .expectBody()
                .json("""
                        {"rewardableItems":[],"hasMore":false,"totalPageNumber":0,"totalItemNumber":0,"nextPageIndex":0}
                         """);

        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_REWARDABLE_ITEMS, projectId), Map.of("githubUserId",
                        pierre.user().getGithubUserId().toString(),
                        "pageIndex", "0", "pageSize", "10", "type", "CODE_REVIEW")))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(206)
                .expectBody()
                .json("""
                        {
                             "rewardableItems": [
                               {
                                 "number": 1375,
                                 "id": "279c2e7794a6f798c0de46c6fe23cbffcc2feb485072a25fdefc726eaf90e34d",
                                 "title": "fix tooltip text",
                                 "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1375",
                                 "createdAt": "2023-11-03T16:42:17Z",
                                 "lastUpdateAt": "2023-11-03T16:43:49Z",
                                 "repoName": "marketplace-frontend",
                                 "type": "CODE_REVIEW",
                                 "commitsCount": null,
                                 "userCommitsCount": 0,
                                 "commentsCount": null,
                                 "codeReviewOutcome": "APPROVED",
                                 "status": "OPEN"
                               },
                               {
                                 "number": 1374,
                                 "id": "803254a420051b6b04c8cb2030922f3a93cfe2bfbac34c61b56dde93184dbd70",
                                 "title": "remove cache on contributor",
                                 "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1374",
                                 "createdAt": "2023-11-03T16:37:19Z",
                                 "lastUpdateAt": "2023-11-03T16:37:30Z",
                                 "repoName": "marketplace-frontend",
                                 "type": "CODE_REVIEW",
                                 "commitsCount": null,
                                 "userCommitsCount": 0,
                                 "commentsCount": null,
                                 "codeReviewOutcome": "APPROVED",
                                 "status": "OPEN"
                               },
                               {
                                 "number": 1367,
                                 "id": "938c879cca27fb6e59ef30658ea5587d2c830dd8bf52a3ec0192b5a780fea267",
                                 "title": "fix/contribution-status",
                                 "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1367",
                                 "createdAt": "2023-11-03T11:43:26Z",
                                 "lastUpdateAt": "2023-11-03T11:47:30Z",
                                 "repoName": "marketplace-frontend",
                                 "type": "CODE_REVIEW",
                                 "commitsCount": null,
                                 "userCommitsCount": 0,
                                 "commentsCount": null,
                                 "codeReviewOutcome": "APPROVED",
                                 "status": "OPEN"
                               },
                               {
                                 "number": 1365,
                                 "id": "c6ffc682726fc0ffd3a41a9bee04d62d288761501b5906968577bcd132e36cbf",
                                 "title": "feat: prevent submitting separate iban bic",
                                 "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1365",
                                 "createdAt": "2023-11-02T19:48:44Z",
                                 "lastUpdateAt": null,
                                 "repoName": "marketplace-frontend",
                                 "type": "CODE_REVIEW",
                                 "commitsCount": null,
                                 "userCommitsCount": 0,
                                 "commentsCount": null,
                                 "codeReviewOutcome": null,
                                 "status": "OPEN"
                               },
                               {
                                 "number": 1364,
                                 "id": "4b44061840a2f8185f80f2fa381c2aa1bd228e56bde84ab8e9a243ca6da7b073",
                                 "title": "feat: send empty sepa infos",
                                 "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1364",
                                 "createdAt": "2023-11-02T19:22:47Z",
                                 "lastUpdateAt": "2023-11-02T19:23:41Z",
                                 "repoName": "marketplace-frontend",
                                 "type": "CODE_REVIEW",
                                 "commitsCount": null,
                                 "userCommitsCount": 0,
                                 "commentsCount": null,
                                 "codeReviewOutcome": "APPROVED",
                                 "status": "OPEN"
                               },
                               {
                                 "number": 1354,
                                 "id": "870831e2a427e91e26d1f7946b8d538549abb5892a02530156d63df0a3f3f43a",
                                 "title": "E 692 qa status",
                                 "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1354",
                                 "createdAt": "2023-11-02T11:25:42Z",
                                 "lastUpdateAt": null,
                                 "repoName": "marketplace-frontend",
                                 "type": "CODE_REVIEW",
                                 "commitsCount": null,
                                 "userCommitsCount": 0,
                                 "commentsCount": null,
                                 "codeReviewOutcome": null,
                                 "status": "OPEN"
                               },
                               {
                                 "number": 1352,
                                 "id": "a93a3eec8f658a0fb0fb81dd86cc76e9d918be84e6fffab926a374ceaf1d3fdb",
                                 "title": "feat: payout side panel QA",
                                 "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1352",
                                 "createdAt": "2023-10-31T18:21:25Z",
                                 "lastUpdateAt": "2023-11-02T09:11:27Z",
                                 "repoName": "marketplace-frontend",
                                 "type": "CODE_REVIEW",
                                 "commitsCount": null,
                                 "userCommitsCount": 0,
                                 "commentsCount": null,
                                 "codeReviewOutcome": "APPROVED",
                                 "status": "OPEN"
                               },
                               {
                                 "number": 1351,
                                 "id": "331574dd3aa78737d775eb4948491ea306ec153f5a999b3dbf85b76d21ae4021",
                                 "title": "Multi currencies QA 01",
                                 "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1351",
                                 "createdAt": "2023-10-31T16:45:53Z",
                                 "lastUpdateAt": "2023-10-31T18:55:18Z",
                                 "repoName": "marketplace-frontend",
                                 "type": "CODE_REVIEW",
                                 "commitsCount": null,
                                 "userCommitsCount": 0,
                                 "commentsCount": null,
                                 "codeReviewOutcome": "APPROVED",
                                 "status": "OPEN"
                               },
                               {
                                 "number": 1350,
                                 "id": "2a9e1bd1918d8f1718a4c8d14ee4cf0ea05ee3ded79083f961fa5f54def48f7b",
                                 "title": "Fix/impersonate",
                                 "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1350",
                                 "createdAt": "2023-10-31T16:25:03Z",
                                 "lastUpdateAt": "2023-10-31T16:51:37Z",
                                 "repoName": "marketplace-frontend",
                                 "type": "CODE_REVIEW",
                                 "commitsCount": null,
                                 "userCommitsCount": 0,
                                 "commentsCount": null,
                                 "codeReviewOutcome": "APPROVED",
                                 "status": "OPEN"
                               },
                               {
                                 "number": 1343,
                                 "id": "c7bdbbd8fc81d3f23820301a2b50bc3b9d9050d0923dd063090074a4faab805e",
                                 "title": "fix: available conversion chip",
                                 "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1343",
                                 "createdAt": "2023-10-30T16:45:00Z",
                                 "lastUpdateAt": "2023-10-30T16:50:20Z",
                                 "repoName": "marketplace-frontend",
                                 "type": "CODE_REVIEW",
                                 "commitsCount": null,
                                 "userCommitsCount": 0,
                                 "commentsCount": null,
                                 "codeReviewOutcome": "APPROVED",
                                 "status": "OPEN"
                               }
                             ],
                             "hasMore": true,
                             "totalPageNumber": 10,
                             "totalItemNumber": 100,
                             "nextPageIndex": 1
                           }
                         """);


        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_REWARDABLE_ITEMS, projectId), Map.of("githubUserId",
                        pierre.user().getGithubUserId().toString(),
                        "pageIndex", "0", "pageSize", "10", "search", "qa")))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(200)
                .expectBody()
                .json("""
                        {
                          "rewardableItems": [
                            {
                              "number": 1354,
                              "id": "870831e2a427e91e26d1f7946b8d538549abb5892a02530156d63df0a3f3f43a",
                              "title": "E 692 qa status",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1354",
                              "createdAt": "2023-11-02T11:25:42Z",
                              "lastUpdateAt": null,
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": null,
                              "status": "OPEN"
                            },
                            {
                              "number": 1352,
                              "id": "a93a3eec8f658a0fb0fb81dd86cc76e9d918be84e6fffab926a374ceaf1d3fdb",
                              "title": "feat: payout side panel QA",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1352",
                              "createdAt": "2023-10-31T18:21:25Z",
                              "lastUpdateAt": "2023-11-02T09:11:27Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": "APPROVED",
                              "status": "OPEN"
                            },
                            {
                              "number": 1351,
                              "id": "331574dd3aa78737d775eb4948491ea306ec153f5a999b3dbf85b76d21ae4021",
                              "title": "Multi currencies QA 01",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1351",
                              "createdAt": "2023-10-31T16:45:53Z",
                              "lastUpdateAt": "2023-10-31T18:55:18Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": "APPROVED",
                              "status": "OPEN"
                            }
                          ],
                          "hasMore": false,
                          "totalPageNumber": 1,
                          "totalItemNumber": 3,
                          "nextPageIndex": 0
                        }
                        """);
    }

    @Test
    @Order(20)
    void should_get_rewardable_items_given_a_project_lead_and_ignored_contributions() {
        // Given
        final HasuraUserHelper.AuthenticatedUser pierre = hasuraUserHelper.authenticatePierre();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        ignoredContributionsRepository.saveAll(List.of(
                new IgnoredContributionEntity(IgnoredContributionEntity.Id.builder()
                        .contributionId("279c2e7794a6f798c0de46c6fe23cbffcc2feb485072a25fdefc726eaf90e34d")
                        .projectId(projectId)
                        .build()),
                new IgnoredContributionEntity(IgnoredContributionEntity.Id.builder()
                        .contributionId("803254a420051b6b04c8cb2030922f3a93cfe2bfbac34c61b56dde93184dbd70")
                        .projectId(projectId)
                        .build()),
                new IgnoredContributionEntity(IgnoredContributionEntity.Id.builder()
                        .contributionId("938c879cca27fb6e59ef30658ea5587d2c830dd8bf52a3ec0192b5a780fea267")
                        .projectId(projectId)
                        .build()),
                new IgnoredContributionEntity(IgnoredContributionEntity.Id.builder()
                        .contributionId("c6ffc682726fc0ffd3a41a9bee04d62d288761501b5906968577bcd132e36cbf")
                        .projectId(projectId)
                        .build()),
                new IgnoredContributionEntity(IgnoredContributionEntity.Id.builder()
                        .contributionId("4b44061840a2f8185f80f2fa381c2aa1bd228e56bde84ab8e9a243ca6da7b073")
                        .projectId(projectId)
                        .build())
        ));


        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_REWARDABLE_ITEMS, projectId), Map.of("githubUserId",
                        pierre.user().getGithubUserId().toString(),
                        "pageIndex", "0", "pageSize", "5")))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(206)
                .expectBody()
                .json("""
                        {
                           "rewardableItems": [
                             {
                               "number": 1354,
                               "id": "870831e2a427e91e26d1f7946b8d538549abb5892a02530156d63df0a3f3f43a",
                               "title": "E 692 qa status",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1354",
                               "createdAt": "2023-11-02T11:25:42Z",
                               "lastUpdateAt": null,
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": null,
                               "status": "OPEN"
                             },
                             {
                               "number": 1352,
                               "id": "a93a3eec8f658a0fb0fb81dd86cc76e9d918be84e6fffab926a374ceaf1d3fdb",
                               "title": "feat: payout side panel QA",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1352",
                               "createdAt": "2023-10-31T18:21:25Z",
                               "lastUpdateAt": "2023-11-02T09:11:27Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1351,
                               "id": "331574dd3aa78737d775eb4948491ea306ec153f5a999b3dbf85b76d21ae4021",
                               "title": "Multi currencies QA 01",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1351",
                               "createdAt": "2023-10-31T16:45:53Z",
                               "lastUpdateAt": "2023-10-31T18:55:18Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1350,
                               "id": "2a9e1bd1918d8f1718a4c8d14ee4cf0ea05ee3ded79083f961fa5f54def48f7b",
                               "title": "Fix/impersonate",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1350",
                               "createdAt": "2023-10-31T16:25:03Z",
                               "lastUpdateAt": "2023-10-31T16:51:37Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1343,
                               "id": "c7bdbbd8fc81d3f23820301a2b50bc3b9d9050d0923dd063090074a4faab805e",
                               "title": "fix: available conversion chip",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1343",
                               "createdAt": "2023-10-30T16:45:00Z",
                               "lastUpdateAt": "2023-10-30T16:50:20Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             }
                           ],
                           "hasMore": true,
                           "totalPageNumber": 27,
                           "totalItemNumber": 133,
                           "nextPageIndex": 1
                         }
                         """);
    }
}
