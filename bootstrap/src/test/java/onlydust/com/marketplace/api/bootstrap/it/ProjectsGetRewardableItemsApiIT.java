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
                               "number": 1393,
                               "id": "1fd8ac0fba88c9245d294630a7e5d3604eb05319f9dc6b7b7cb10bc032b2f6c1",
                               "contributionId": "b88b3aac4b3e975c292074e203b60ca9d5bd46f3fd863c07c9e7774d436c497c",
                               "title": "fix: gen types",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1393",
                               "createdAt": "2023-11-10T17:04:16Z",
                               "lastUpdateAt": "2023-11-10T17:05:18Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1388,
                               "id": "e3d03d5b8b23a051e734f5740ed267db84794f0bc6615a28a65019797bb42ef2",
                               "contributionId": "bac9985221e5a0c3811b3bc232221dd9173cb1bfb37c410793f0668fbd5c9a9e",
                               "title": "revert this fix : I see not completed items within my send reward panel",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1388",
                               "createdAt": "2023-11-09T12:59:11Z",
                               "lastUpdateAt": "2023-11-09T14:25:59Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1384,
                               "id": "f992b5df804e0fe21879d12be23fe78dc6e58c0ac13604c2bc4b4534a4033519",
                               "contributionId": "2fc89b8762d0536a804d3d0fa819170cc574629d2bd015a93612738c5efad961",
                               "title": "E-841 Front fixies",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1384",
                               "createdAt": "2023-11-07T15:05:35Z",
                               "lastUpdateAt": "2023-11-07T15:07:31Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1383,
                               "id": "a1decdc64d45a3241e6e4ea72ce93bbfe806ab8c9a43fd1c77207834884b46af",
                               "contributionId": "0d0720a102776b9992b5c94fbeb8829a8eb67f4c4a6ce3da8a78974ea6a815e0",
                               "title": "[E-843]  Dynamic statusses payout alerting",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1383",
                               "createdAt": "2023-11-07T14:13:25Z",
                               "lastUpdateAt": "2023-11-08T16:38:31Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1380,
                               "id": "04d85058abaacad15b44bc99bd55131c9e17e168a99026cc065ad24eef352011",
                               "contributionId": "8fb9cdb66d15767bfba6fb6da4d0d3c6bce5be8c051bfb603ab5cf17e73d8ba0",
                               "title": "fix test and delete duplicate const",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1380",
                               "createdAt": "2023-11-07T09:06:10Z",
                               "lastUpdateAt": "2023-11-07T09:06:37Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1379,
                               "id": "4b8579d770b660299dd6f6e60813b22232f7fdb94cc818156e900f935161f610",
                               "contributionId": "c90613bf0e7d05ca1fc4451e3b25c2a040da1ecd6094ce412c2b013a900d45e8",
                               "title": "feat: hasMissingUsdcWallet logic to payout info form",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1379",
                               "createdAt": "2023-11-06T16:54:38Z",
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
                               "number": 1378,
                               "id": "1588841186",
                               "contributionId": "4286747370468a1395d78642b48a97c1d81db6bf3b51d6540a2a610af250f33d",
                               "title": "fix lint and types",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1378",
                               "createdAt": "2023-11-06T16:15:28Z",
                               "lastUpdateAt": "2023-11-07T08:53:38Z",
                               "repoName": "marketplace-frontend",
                               "type": "PULL_REQUEST",
                               "commitsCount": 0,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": null,
                               "status": "OPEN"
                             },
                             {
                               "number": 1378,
                               "id": "7087293e7224df3efbca56768f001efa1ab243e77753ea97ad8af4654faa0c44",
                               "contributionId": "6ec7b4345a5a5ef3b71e98d67847621ca0a264334781ac3b66d636897eeea0b8",
                               "title": "fix lint and types",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1378",
                               "createdAt": "2023-11-06T16:15:28Z",
                               "lastUpdateAt": "2023-11-07T08:53:11Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1377,
                               "id": "1588806258",
                               "contributionId": "7c5211884e70166194c71ed5895a876c36914f15796fba51987ddec76ff84320",
                               "title": "Fixing contributions filter",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1377",
                               "createdAt": "2023-11-06T15:56:30Z",
                               "lastUpdateAt": "2023-11-06T15:57:34Z",
                               "repoName": "marketplace-frontend",
                               "type": "PULL_REQUEST",
                               "commitsCount": 0,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": null,
                               "status": "OPEN"
                             },
                             {
                               "number": 1375,
                               "id": "9732cc6275825278467dd08a41ad87b6f381f3deada8661630d0538df6566f20",
                               "contributionId": "279c2e7794a6f798c0de46c6fe23cbffcc2feb485072a25fdefc726eaf90e34d",
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
                             }
                           ],
                           "hasMore": true,
                           "totalPageNumber": 15,
                           "totalItemNumber": 147,
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
                              "number": 1151,
                              "id": "9926e35c5e760c1274aa767bbfc45acb7a2f734c3f9e384b8d86f2c15b0990ab",
                              "contributionId": "60e82c5c7278724beaa8ba44a339e774d62c3e5d20fdb461960c640458c0c4ed",
                              "title": "[E-641] Index extra fields for github issues",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1151",
                              "createdAt": "2023-08-01T13:24:24Z",
                              "lastUpdateAt": "2023-08-01T13:35:20Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": "APPROVED",
                              "status": "OPEN"
                            },
                            {
                              "number": 1150,
                              "id": "3bfcdc6c8f353bc4a4ce283a73b1a39160ecf06f8848a7a059ca779cdbc0e72a",
                              "contributionId": "eca160b2f28ac4bf342d7166d8916e159beba3f9498d6bd73baba8ed7a0b3743",
                              "title": "Use proper DTO for github graphql API",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1150",
                              "createdAt": "2023-08-01T11:38:44Z",
                              "lastUpdateAt": "2023-08-01T11:47:46Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": "APPROVED",
                              "status": "OPEN"
                            },
                            {
                              "number": 1149,
                              "id": "3190d0dff1d68d17b35c236a9a0cd36fa43eaf906e9c1e692ba30cd0e504e8dd",
                              "contributionId": "f2b15245858299ad20cda8e9579773c034ff599a3271099e041014ee1b08b50a",
                              "title": "Isolate the story book files from the production code",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1149",
                              "createdAt": "2023-07-31T20:20:52Z",
                              "lastUpdateAt": "2023-07-31T21:52:30Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": "APPROVED",
                              "status": "OPEN"
                            },
                            {
                              "number": 1148,
                              "id": "813a336e99ecb10778d5b2dedac0c3c1dd484e076a7043fa35e672bb8d2cbe0b",
                              "contributionId": "ffd826c697abd544df6dac59c24c8d5903daf72013970e2af70616ebdf518253",
                              "title": "[E-640] Split github issues and pull requests",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1148",
                              "createdAt": "2023-07-31T18:47:24Z",
                              "lastUpdateAt": "2023-08-01T08:41:27Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": "APPROVED",
                              "status": "OPEN"
                            },
                            {
                              "number": 1147,
                              "id": "1bed345d3c6ef22a6eb0e9df879294e4711ea5df39e50b65ad7fcfe3ef679247",
                              "contributionId": "2eac27fd9c2c8f32d0ceea8f62571af439f524c030b8d5a894bd1af4da0333ee",
                              "title": "Scrollbars are broken on public profile page",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1147",
                              "createdAt": "2023-07-31T18:01:54Z",
                              "lastUpdateAt": "2023-07-31T18:02:53Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": "APPROVED",
                              "status": "OPEN"
                            },
                            {
                              "number": 1146,
                              "id": "abf154057a6ff3c8b21ed750337b8be31080577c7dc00685504a0f853b2978c0",
                              "contributionId": "c7c486f40055a7bbcdfb4234a0a013a2f1e6c12e78f3dea7ecedb893e33342cc",
                              "title": "Hide tooltips on mobile",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1146",
                              "createdAt": "2023-07-31T11:23:37Z",
                              "lastUpdateAt": "2023-07-31T11:32:08Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": "APPROVED",
                              "status": "OPEN"
                            },
                            {
                              "number": 1143,
                              "id": "b33abb84069b1dbabdc474344165cc4370205cd3ef5ef2d71f512ca7e6bb61a2",
                              "contributionId": "28d1af9960485f93b491f62551c2c1a69128f445c59ae0983a9028c72e740624",
                              "title": "[E-614] Invite users to share their contact upon showing their interest to project",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1143",
                              "createdAt": "2023-07-27T12:23:34Z",
                              "lastUpdateAt": "2023-07-28T12:03:48Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": "APPROVED",
                              "status": "OPEN"
                            },
                            {
                              "number": 1140,
                              "id": "f373239267368ed631303e5d6122676ac10b9a493b6df5c02e726de4defbc614",
                              "contributionId": "7baff771ad7b4fdbb82f501318e9450a0a6ea9c0c0b96285e2d22864811aa4dd",
                              "title": "Broken PR - DO NOT MERGE",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1140",
                              "createdAt": "2023-07-26T14:30:16Z",
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
                              "number": 1139,
                              "id": "cee7bc15a3fc40ead97bd56a29facd0ed373ef619094e68112a04441715d79ef",
                              "contributionId": "2f1c832f96109e7ea29ff49c01fad772c3c0221a33c285b6a4dc8c636883cef0",
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
                              "number": 1139,
                              "id": "1450049569",
                              "contributionId": "ba6442e3b192e0b5b79dbc358755d3f64a47f2c529599067ec20e97198c60a39",
                              "title": "Configure datadog to foward front error logs",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1139",
                              "createdAt": "2023-07-26T13:10:36Z",
                              "lastUpdateAt": "2023-07-26T17:48:36Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 1,
                              "userCommitsCount": 1,
                              "commentsCount": null,
                              "codeReviewOutcome": null,
                              "status": "OPEN"
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 15,
                          "totalItemNumber": 147,
                          "nextPageIndex": 14
                        }
                         """);

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_REWARDABLE_ITEMS, projectId), Map.of("githubUserId",
                        pierre.user().getGithubUserId().toString(),
                        "pageIndex", "15", "pageSize", "10")))
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
                           "totalPageNumber": 15,
                           "totalItemNumber": 147,
                           "nextPageIndex": 15
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
                              "number": 1378,
                              "id": "1588841186",
                              "contributionId": "4286747370468a1395d78642b48a97c1d81db6bf3b51d6540a2a610af250f33d",
                              "title": "fix lint and types",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1378",
                              "createdAt": "2023-11-06T16:15:28Z",
                              "lastUpdateAt": "2023-11-07T08:53:38Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 0,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": null,
                              "status": "OPEN"
                            },
                            {
                              "number": 1377,
                              "id": "1588806258",
                              "contributionId": "7c5211884e70166194c71ed5895a876c36914f15796fba51987ddec76ff84320",
                              "title": "Fixing contributions filter",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1377",
                              "createdAt": "2023-11-06T15:56:30Z",
                              "lastUpdateAt": "2023-11-06T15:57:34Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 0,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": null,
                              "status": "OPEN"
                            },
                            {
                              "number": 1319,
                              "id": "1572539484",
                              "contributionId": "fbfe8d8b8c2a63d1226ab1dc1f0fd773839830ecf08a6410c4abe20760308ee4",
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
                              "id": "1571638326",
                              "contributionId": "b8c5d6619154e8bb017c7464b0468379ff09f5f9ff6d80527b38c8424dd658ec",
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
                              "id": "1558436259",
                              "contributionId": "21d6e401ff24399fa97914de130211af686a5adf5f3be76c52d4eafbaa514ceb",
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
                              "id": "1557981540",
                              "contributionId": "946a969d9e5bc9bdf97746ded417effabeb4b912c995c21b9c8edbe1722b4e15",
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
                              "id": "1526646573",
                              "contributionId": "2884dc233c8512d062d7dd0b60d78d58e416349bf0a3e1feddff1183a01895e8",
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
                              "id": "1519041128",
                              "contributionId": "e473d496d162d4ed79eac072e74bc2a48501d73cc4fb6fa3e67a484a55cf0896",
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
                              "id": "1511546916",
                              "contributionId": "a290ea203b1264105bf581aebbdf3e79edfdd89811da50dc6bd076272d810b2e",
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
                              "id": "1507701129",
                              "contributionId": "3ecff6bfaa190bdbb8b42f0a842fda14bf34f93ee245e4b0ea0069af5cf74d1f",
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
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 4,
                          "totalItemNumber": 40,
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
                               "number": 1393,
                               "id": "1fd8ac0fba88c9245d294630a7e5d3604eb05319f9dc6b7b7cb10bc032b2f6c1",
                               "contributionId": "b88b3aac4b3e975c292074e203b60ca9d5bd46f3fd863c07c9e7774d436c497c",
                               "title": "fix: gen types",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1393",
                               "createdAt": "2023-11-10T17:04:16Z",
                               "lastUpdateAt": "2023-11-10T17:05:18Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1388,
                               "id": "e3d03d5b8b23a051e734f5740ed267db84794f0bc6615a28a65019797bb42ef2",
                               "contributionId": "bac9985221e5a0c3811b3bc232221dd9173cb1bfb37c410793f0668fbd5c9a9e",
                               "title": "revert this fix : I see not completed items within my send reward panel",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1388",
                               "createdAt": "2023-11-09T12:59:11Z",
                               "lastUpdateAt": "2023-11-09T14:25:59Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1384,
                               "id": "f992b5df804e0fe21879d12be23fe78dc6e58c0ac13604c2bc4b4534a4033519",
                               "contributionId": "2fc89b8762d0536a804d3d0fa819170cc574629d2bd015a93612738c5efad961",
                               "title": "E-841 Front fixies",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1384",
                               "createdAt": "2023-11-07T15:05:35Z",
                               "lastUpdateAt": "2023-11-07T15:07:31Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1383,
                               "id": "a1decdc64d45a3241e6e4ea72ce93bbfe806ab8c9a43fd1c77207834884b46af",
                               "contributionId": "0d0720a102776b9992b5c94fbeb8829a8eb67f4c4a6ce3da8a78974ea6a815e0",
                               "title": "[E-843]  Dynamic statusses payout alerting",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1383",
                               "createdAt": "2023-11-07T14:13:25Z",
                               "lastUpdateAt": "2023-11-08T16:38:31Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1380,
                               "id": "04d85058abaacad15b44bc99bd55131c9e17e168a99026cc065ad24eef352011",
                               "contributionId": "8fb9cdb66d15767bfba6fb6da4d0d3c6bce5be8c051bfb603ab5cf17e73d8ba0",
                               "title": "fix test and delete duplicate const",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1380",
                               "createdAt": "2023-11-07T09:06:10Z",
                               "lastUpdateAt": "2023-11-07T09:06:37Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1379,
                               "id": "4b8579d770b660299dd6f6e60813b22232f7fdb94cc818156e900f935161f610",
                               "contributionId": "c90613bf0e7d05ca1fc4451e3b25c2a040da1ecd6094ce412c2b013a900d45e8",
                               "title": "feat: hasMissingUsdcWallet logic to payout info form",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1379",
                               "createdAt": "2023-11-06T16:54:38Z",
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
                               "number": 1378,
                               "id": "7087293e7224df3efbca56768f001efa1ab243e77753ea97ad8af4654faa0c44",
                               "contributionId": "6ec7b4345a5a5ef3b71e98d67847621ca0a264334781ac3b66d636897eeea0b8",
                               "title": "fix lint and types",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1378",
                               "createdAt": "2023-11-06T16:15:28Z",
                               "lastUpdateAt": "2023-11-07T08:53:11Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1375,
                               "id": "9732cc6275825278467dd08a41ad87b6f381f3deada8661630d0538df6566f20",
                               "contributionId": "279c2e7794a6f798c0de46c6fe23cbffcc2feb485072a25fdefc726eaf90e34d",
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
                               "id": "6824d497fa4fb30169aaeaf5b788b2648ea8364a1b5ce859291b5e663a65cc38",
                               "contributionId": "803254a420051b6b04c8cb2030922f3a93cfe2bfbac34c61b56dde93184dbd70",
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
                               "id": "b8bc6442fb2045a804d4263cd30b7de3981844790644007ddb3c55b028097612",
                               "contributionId": "938c879cca27fb6e59ef30658ea5587d2c830dd8bf52a3ec0192b5a780fea267",
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
                             }
                           ],
                           "hasMore": true,
                           "totalPageNumber": 11,
                           "totalItemNumber": 107,
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
                               "id": "6aa96da1df00b7abaa8174ad1a2621f071c3e9f8223066eff261a1d8f93a25b5",
                               "contributionId": "870831e2a427e91e26d1f7946b8d538549abb5892a02530156d63df0a3f3f43a",
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
                               "id": "74a21ee1b10b6bc89602d0cc86d7d7e3acf9bc2cdd08436c9e868661023996cb",
                               "contributionId": "a93a3eec8f658a0fb0fb81dd86cc76e9d918be84e6fffab926a374ceaf1d3fdb",
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
                               "id": "cdbe805e392331a053120e46b0d0b09b89b92ba552bc11c9152bc7c6ad6ca13b",
                               "contributionId": "331574dd3aa78737d775eb4948491ea306ec153f5a999b3dbf85b76d21ae4021",
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
                               "number": 1393,
                               "id": "1fd8ac0fba88c9245d294630a7e5d3604eb05319f9dc6b7b7cb10bc032b2f6c1",
                               "contributionId": "b88b3aac4b3e975c292074e203b60ca9d5bd46f3fd863c07c9e7774d436c497c",
                               "title": "fix: gen types",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1393",
                               "createdAt": "2023-11-10T17:04:16Z",
                               "lastUpdateAt": "2023-11-10T17:05:18Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1388,
                               "id": "e3d03d5b8b23a051e734f5740ed267db84794f0bc6615a28a65019797bb42ef2",
                               "contributionId": "bac9985221e5a0c3811b3bc232221dd9173cb1bfb37c410793f0668fbd5c9a9e",
                               "title": "revert this fix : I see not completed items within my send reward panel",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1388",
                               "createdAt": "2023-11-09T12:59:11Z",
                               "lastUpdateAt": "2023-11-09T14:25:59Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1384,
                               "id": "f992b5df804e0fe21879d12be23fe78dc6e58c0ac13604c2bc4b4534a4033519",
                               "contributionId": "2fc89b8762d0536a804d3d0fa819170cc574629d2bd015a93612738c5efad961",
                               "title": "E-841 Front fixies",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1384",
                               "createdAt": "2023-11-07T15:05:35Z",
                               "lastUpdateAt": "2023-11-07T15:07:31Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1383,
                               "id": "a1decdc64d45a3241e6e4ea72ce93bbfe806ab8c9a43fd1c77207834884b46af",
                               "contributionId": "0d0720a102776b9992b5c94fbeb8829a8eb67f4c4a6ce3da8a78974ea6a815e0",
                               "title": "[E-843]  Dynamic statusses payout alerting",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1383",
                               "createdAt": "2023-11-07T14:13:25Z",
                               "lastUpdateAt": "2023-11-08T16:38:31Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN"
                             },
                             {
                               "number": 1380,
                               "id": "04d85058abaacad15b44bc99bd55131c9e17e168a99026cc065ad24eef352011",
                               "contributionId": "8fb9cdb66d15767bfba6fb6da4d0d3c6bce5be8c051bfb603ab5cf17e73d8ba0",
                               "title": "fix test and delete duplicate const",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1380",
                               "createdAt": "2023-11-07T09:06:10Z",
                               "lastUpdateAt": "2023-11-07T09:06:37Z",
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
                           "totalPageNumber": 29,
                           "totalItemNumber": 142,
                           "nextPageIndex": 1
                         }
                         """);

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_REWARDABLE_ITEMS, projectId), Map.of("githubUserId",
                        pierre.user().getGithubUserId().toString(),
                        "pageIndex", "0", "pageSize", "10", "include_ignored_items", "true")))
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
                              "number": 1393,
                              "id": "b88b3aac4b3e975c292074e203b60ca9d5bd46f3fd863c07c9e7774d436c497c",
                              "title": "fix: gen types",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1393",
                              "createdAt": "2023-11-10T17:04:16Z",
                              "lastUpdateAt": "2023-11-10T17:05:18Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": "APPROVED",
                              "status": "OPEN"
                            },
                            {
                              "number": 1388,
                              "id": "bac9985221e5a0c3811b3bc232221dd9173cb1bfb37c410793f0668fbd5c9a9e",
                              "title": "revert this fix : I see not completed items within my send reward panel",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1388",
                              "createdAt": "2023-11-09T12:59:11Z",
                              "lastUpdateAt": "2023-11-09T14:25:59Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": "APPROVED",
                              "status": "OPEN"
                            },
                            {
                              "number": 1384,
                              "id": "2fc89b8762d0536a804d3d0fa819170cc574629d2bd015a93612738c5efad961",
                              "title": "E-841 Front fixies",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1384",
                              "createdAt": "2023-11-07T15:05:35Z",
                              "lastUpdateAt": "2023-11-07T15:07:31Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": "APPROVED",
                              "status": "OPEN"
                            },
                            {
                              "number": 1383,
                              "id": "0d0720a102776b9992b5c94fbeb8829a8eb67f4c4a6ce3da8a78974ea6a815e0",
                              "title": "[E-843]  Dynamic statusses payout alerting",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1383",
                              "createdAt": "2023-11-07T14:13:25Z",
                              "lastUpdateAt": "2023-11-08T16:38:31Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": "APPROVED",
                              "status": "OPEN"
                            },
                            {
                              "number": 1380,
                              "id": "8fb9cdb66d15767bfba6fb6da4d0d3c6bce5be8c051bfb603ab5cf17e73d8ba0",
                              "title": "fix test and delete duplicate const",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1380",
                              "createdAt": "2023-11-07T09:06:10Z",
                              "lastUpdateAt": "2023-11-07T09:06:37Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": "APPROVED",
                              "status": "OPEN"
                            },
                            {
                              "number": 1379,
                              "id": "c90613bf0e7d05ca1fc4451e3b25c2a040da1ecd6094ce412c2b013a900d45e8",
                              "title": "feat: hasMissingUsdcWallet logic to payout info form",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1379",
                              "createdAt": "2023-11-06T16:54:38Z",
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
                              "number": 1378,
                              "id": "4286747370468a1395d78642b48a97c1d81db6bf3b51d6540a2a610af250f33d",
                              "title": "fix lint and types",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1378",
                              "createdAt": "2023-11-06T16:15:28Z",
                              "lastUpdateAt": "2023-11-07T08:53:38Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 0,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": null,
                              "status": "OPEN"
                            },
                            {
                              "number": 1378,
                              "id": "6ec7b4345a5a5ef3b71e98d67847621ca0a264334781ac3b66d636897eeea0b8",
                              "title": "fix lint and types",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1378",
                              "createdAt": "2023-11-06T16:15:28Z",
                              "lastUpdateAt": "2023-11-07T08:53:11Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": "APPROVED",
                              "status": "OPEN"
                            },
                            {
                              "number": 1377,
                              "id": "7c5211884e70166194c71ed5895a876c36914f15796fba51987ddec76ff84320",
                              "title": "Fixing contributions filter",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1377",
                              "createdAt": "2023-11-06T15:56:30Z",
                              "lastUpdateAt": "2023-11-06T15:57:34Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 0,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": null,
                              "status": "OPEN"
                            },
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
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 15,
                          "totalItemNumber": 147,
                          "nextPageIndex": 1
                        }
                         """);
    }
}
