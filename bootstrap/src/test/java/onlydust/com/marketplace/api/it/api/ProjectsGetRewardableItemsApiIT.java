package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.IgnoredContributionEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.IgnoredContributionsRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.suites.tags.TagReward;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@TagReward
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectsGetRewardableItemsApiIT extends AbstractMarketplaceApiIT {


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
        userAuthHelper.signUpUser(1L, faker.rickAndMorty().character(), faker.internet().url(),
                false);
        final String jwt = userAuthHelper.authenticateUser(1L).jwt();
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
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
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
                              "number": 1516,
                              "id": "587070f037e591f8653f9c872793704aee2ff6440a225f63d0ca61c056a16cb3",
                              "contributionId": "1c65dd5daa99303857d6ead12147416d9ee430ba962586d5d5b871f2e7a5611c",
                              "title": "Fix api contract id to GitHub user",
                              "createdAt": "2023-12-04T10:59:20Z",
                              "completedAt": "2023-12-04T11:00:23Z",
                              "githubBody": null,
                              "author": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                              },
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1516"
                            },
                            {
                              "number": 1507,
                              "id": "1625619352",
                              "contributionId": "6b8a468dcd745391335826d2f442e6c43130f32fee4a11b7002c877bc7ce825f",
                              "title": "Renaming sponsor into sponsorId",
                              "createdAt": "2023-12-01T12:34:02Z",
                              "completedAt": "2023-12-01T12:36:35Z",
                              "githubBody": null,
                              "author": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                              },
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "PULL_REQUEST",
                              "commitsCount": 1,
                              "userCommitsCount": 1,
                              "commentsCount": 1,
                              "status": "MERGED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1507"
                            },
                            {
                              "number": 1506,
                              "id": "b8da5c8611c943b9733a9b2d458731c855c8182efe7810fc4880639de7152a31",
                              "contributionId": "374a4bdb40fee860faa96ddeb820baed8b923c26b267fb6e6cecfd4fe0c7eee2",
                              "title": "E 829 alert leads when we they need to fix their projects qa 06",
                              "createdAt": "2023-11-30T17:59:38Z",
                              "completedAt": "2023-11-30T18:00:36Z",
                              "githubBody": null,
                              "author": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                              },
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1506"
                            },
                            {
                              "number": 1505,
                              "id": "366590d4a7831e0acf04bfae49cf25864177408e3887742d687e44b0ab8737f1",
                              "contributionId": "280735ac974b29b07b0dc45b0d8445fc2143ec55f801ac4959f659eb03fbf72d",
                              "title": "E 829 alert leads when we they need to fix their projects qa 05",
                              "createdAt": "2023-11-30T17:50:33Z",
                              "completedAt": "2023-11-30T17:51:59Z",
                              "githubBody": null,
                              "author": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                              },
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1505"
                            },
                            {
                              "number": 1504,
                              "id": "707ed965d899deb5a03a452624739c9554ddf4bcf59829ca063cf4fbfcba41f3",
                              "contributionId": "7470c37ad63fe695c23636928823f48cfd881859faf9dda1c9e811c5c5f14ef0",
                              "title": "E 893 typo is url when copy profile address to clipboard",
                              "createdAt": "2023-11-30T17:17:12Z",
                              "completedAt": "2023-11-30T17:27:01Z",
                              "githubBody": null,
                              "author": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                              },
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1504"
                            },
                            {
                              "number": 1501,
                              "id": "de1df6ce11f24f62aed2fae55c8ebf51d90ee70696881d0ad2bc7ee3c8b68093",
                              "contributionId": "64b9dcfcf5c282ebd6056d03a38f124120726cb185e45ee2301b38b83dfbdee8",
                              "title": "Edit create project fix qa 05",
                              "createdAt": "2023-11-30T15:07:51Z",
                              "completedAt": "2023-11-30T15:10:08Z",
                              "githubBody": null,
                              "author": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                              },
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1501"
                            },
                            {
                              "number": 1499,
                              "id": "d60c4052d0f8870e95ebedb9000ea45d2fff1cb21db45f0627fac750e9ceeadf",
                              "contributionId": "d00a4813bd960fba8a0df7b2bf17f52a08f72afe847bcc2f315e84c6afc21a5b",
                              "title": "fix: wording",
                              "createdAt": "2023-11-30T14:47:57Z",
                              "completedAt": "2023-11-30T14:48:44Z",
                              "githubBody": null,
                              "author": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                              },
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1499"
                            },
                            {
                              "number": 1498,
                              "id": "cf3e0b5e580cedeff024c4fc83cde1c1ca0d2f1f66aadb8efb4082ae87dd77f8",
                              "contributionId": "c08b5ec45a5ff783562f738e78e15c4203a744cfabfcdf3590e8e1590f79cf34",
                              "title": "refactor: translation",
                              "createdAt": "2023-11-30T13:29:15Z",
                              "completedAt": "2023-11-30T13:29:57Z",
                              "githubBody": null,
                              "author": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                              },
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1498"
                            },
                            {
                              "number": 1495,
                              "id": "518ecde3e73f5b342fd1a780d4509e9e6d7713b1083902e7a6f6869623d8fb46",
                              "contributionId": "eb5ce564e6e3530f8bc20e6abd6d301bfc8151eab940c04fe553967178130470",
                              "title": "Fix actionmenu position",
                              "createdAt": "2023-11-29T17:34:31Z",
                              "completedAt": "2023-11-29T17:35:54Z",
                              "githubBody": null,
                              "author": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                              },
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1495"
                            },
                            {
                              "number": 1493,
                              "id": "b53d96cc39e16eeac06eb73bdc5263de1a5fe8fa2f928e6f66c0b5a3c722b8a0",
                              "contributionId": "286996afd496088f0be44abeec739ebd16b43984e6e87ab372bc92082bff6334",
                              "title": "Fix autoadd counts",
                              "createdAt": "2023-11-29T17:07:39Z",
                              "completedAt": "2023-11-29T17:08:48Z",
                              "githubBody": null,
                              "author": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                              },
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1493"
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 15,
                          "totalItemNumber": 146,
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
                              "number": 1153,
                              "id": "e1376e0f7e5ebd666772199dc030a9db779f330d309760a7a773a2ee61f1403a",
                              "contributionId": "978eb90a010e91661d4c8df1cffbb6498c4aeadbd6f4f6c841ee3f4903bbd6ed",
                              "title": "Prevent auto-zoom when focus input on mobile",
                              "createdAt": "2023-08-01T19:27:28Z",
                              "completedAt": "2023-08-02T09:47:22Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1153"
                            },
                            {
                              "number": 1152,
                              "id": "c716bf6091234a7a3a377bed4adca0041df290dd18e6904af9166e7ac03401b9",
                              "contributionId": "1106e2603a0cf7db2030358e3475b87ed8527db2ac2b6415fb3f69b44ee2857c",
                              "title": "[E-642] Index extra fields in github pull requests",
                              "createdAt": "2023-08-01T14:26:33Z",
                              "completedAt": "2023-08-07T07:47:51Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 3,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1152"
                            },
                            {
                              "number": 1151,
                              "id": "9926e35c5e760c1274aa767bbfc45acb7a2f734c3f9e384b8d86f2c15b0990ab",
                              "contributionId": "60e82c5c7278724beaa8ba44a339e774d62c3e5d20fdb461960c640458c0c4ed",
                              "title": "[E-641] Index extra fields for github issues",
                              "createdAt": "2023-08-01T11:24:24Z",
                              "completedAt": "2023-08-01T11:35:20Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1151"
                            },
                            {
                              "number": 1150,
                              "id": "3bfcdc6c8f353bc4a4ce283a73b1a39160ecf06f8848a7a059ca779cdbc0e72a",
                              "contributionId": "eca160b2f28ac4bf342d7166d8916e159beba3f9498d6bd73baba8ed7a0b3743",
                              "title": "Use proper DTO for github graphql API",
                              "createdAt": "2023-08-01T09:38:44Z",
                              "completedAt": "2023-08-01T09:47:46Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1150"
                            },
                            {
                              "number": 1149,
                              "id": "3190d0dff1d68d17b35c236a9a0cd36fa43eaf906e9c1e692ba30cd0e504e8dd",
                              "contributionId": "f2b15245858299ad20cda8e9579773c034ff599a3271099e041014ee1b08b50a",
                              "title": "Isolate the story book files from the production code",
                              "createdAt": "2023-07-31T20:20:52Z",
                              "completedAt": "2023-08-01T10:52:03Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1149"
                            },
                            {
                              "number": 1147,
                              "id": "1bed345d3c6ef22a6eb0e9df879294e4711ea5df39e50b65ad7fcfe3ef679247",
                              "contributionId": "2eac27fd9c2c8f32d0ceea8f62571af439f524c030b8d5a894bd1af4da0333ee",
                              "title": "Scrollbars are broken on public profile page",
                              "createdAt": "2023-07-31T18:01:54Z",
                              "completedAt": "2023-07-31T18:16:02Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1147"
                            },
                            {
                              "number": 1148,
                              "id": "813a336e99ecb10778d5b2dedac0c3c1dd484e076a7043fa35e672bb8d2cbe0b",
                              "contributionId": "ffd826c697abd544df6dac59c24c8d5903daf72013970e2af70616ebdf518253",
                              "title": "[E-640] Split github issues and pull requests",
                              "createdAt": "2023-07-31T16:47:24Z",
                              "completedAt": "2023-08-01T06:41:27Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1148"
                            },
                            {
                              "number": 1146,
                              "id": "abf154057a6ff3c8b21ed750337b8be31080577c7dc00685504a0f853b2978c0",
                              "contributionId": "c7c486f40055a7bbcdfb4234a0a013a2f1e6c12e78f3dea7ecedb893e33342cc",
                              "title": "Hide tooltips on mobile",
                              "createdAt": "2023-07-31T11:23:37Z",
                              "completedAt": "2023-07-31T16:16:14Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1146"
                            },
                            {
                              "number": 1143,
                              "id": "b33abb84069b1dbabdc474344165cc4370205cd3ef5ef2d71f512ca7e6bb61a2",
                              "contributionId": "28d1af9960485f93b491f62551c2c1a69128f445c59ae0983a9028c72e740624",
                              "title": "[E-614] Invite users to share their contact upon showing their interest to project",
                              "createdAt": "2023-07-27T10:23:34Z",
                              "completedAt": "2023-07-28T10:03:48Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 8,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1143"
                            },
                            {
                              "number": 1140,
                              "id": "f373239267368ed631303e5d6122676ac10b9a493b6df5c02e726de4defbc614",
                              "contributionId": "7baff771ad7b4fdbb82f501318e9450a0a6ea9c0c0b96285e2d22864811aa4dd",
                              "title": "Broken PR - DO NOT MERGE",
                              "createdAt": "2023-07-26T12:30:16Z",
                              "completedAt": "2023-07-27T15:43:21Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "PENDING",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1140"
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 15,
                          "totalItemNumber": 146,
                          "nextPageIndex": 14
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
                              "number": 1153,
                              "id": "e1376e0f7e5ebd666772199dc030a9db779f330d309760a7a773a2ee61f1403a",
                              "contributionId": "978eb90a010e91661d4c8df1cffbb6498c4aeadbd6f4f6c841ee3f4903bbd6ed",
                              "title": "Prevent auto-zoom when focus input on mobile",
                              "createdAt": "2023-08-01T19:27:28Z",
                              "completedAt": "2023-08-02T09:47:22Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1153"
                            },
                            {
                              "number": 1152,
                              "id": "c716bf6091234a7a3a377bed4adca0041df290dd18e6904af9166e7ac03401b9",
                              "contributionId": "1106e2603a0cf7db2030358e3475b87ed8527db2ac2b6415fb3f69b44ee2857c",
                              "title": "[E-642] Index extra fields in github pull requests",
                              "createdAt": "2023-08-01T14:26:33Z",
                              "completedAt": "2023-08-07T07:47:51Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 3,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1152"
                            },
                            {
                              "number": 1151,
                              "id": "9926e35c5e760c1274aa767bbfc45acb7a2f734c3f9e384b8d86f2c15b0990ab",
                              "contributionId": "60e82c5c7278724beaa8ba44a339e774d62c3e5d20fdb461960c640458c0c4ed",
                              "title": "[E-641] Index extra fields for github issues",
                              "createdAt": "2023-08-01T11:24:24Z",
                              "completedAt": "2023-08-01T11:35:20Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1151"
                            },
                            {
                              "number": 1150,
                              "id": "3bfcdc6c8f353bc4a4ce283a73b1a39160ecf06f8848a7a059ca779cdbc0e72a",
                              "contributionId": "eca160b2f28ac4bf342d7166d8916e159beba3f9498d6bd73baba8ed7a0b3743",
                              "title": "Use proper DTO for github graphql API",
                              "createdAt": "2023-08-01T09:38:44Z",
                              "completedAt": "2023-08-01T09:47:46Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1150"
                            },
                            {
                              "number": 1149,
                              "id": "3190d0dff1d68d17b35c236a9a0cd36fa43eaf906e9c1e692ba30cd0e504e8dd",
                              "contributionId": "f2b15245858299ad20cda8e9579773c034ff599a3271099e041014ee1b08b50a",
                              "title": "Isolate the story book files from the production code",
                              "createdAt": "2023-07-31T20:20:52Z",
                              "completedAt": "2023-08-01T10:52:03Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1149"
                            },
                            {
                              "number": 1147,
                              "id": "1bed345d3c6ef22a6eb0e9df879294e4711ea5df39e50b65ad7fcfe3ef679247",
                              "contributionId": "2eac27fd9c2c8f32d0ceea8f62571af439f524c030b8d5a894bd1af4da0333ee",
                              "title": "Scrollbars are broken on public profile page",
                              "createdAt": "2023-07-31T18:01:54Z",
                              "completedAt": "2023-07-31T18:16:02Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1147"
                            },
                            {
                              "number": 1148,
                              "id": "813a336e99ecb10778d5b2dedac0c3c1dd484e076a7043fa35e672bb8d2cbe0b",
                              "contributionId": "ffd826c697abd544df6dac59c24c8d5903daf72013970e2af70616ebdf518253",
                              "title": "[E-640] Split github issues and pull requests",
                              "createdAt": "2023-07-31T16:47:24Z",
                              "completedAt": "2023-08-01T06:41:27Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1148"
                            },
                            {
                              "number": 1146,
                              "id": "abf154057a6ff3c8b21ed750337b8be31080577c7dc00685504a0f853b2978c0",
                              "contributionId": "c7c486f40055a7bbcdfb4234a0a013a2f1e6c12e78f3dea7ecedb893e33342cc",
                              "title": "Hide tooltips on mobile",
                              "createdAt": "2023-07-31T11:23:37Z",
                              "completedAt": "2023-07-31T16:16:14Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1146"
                            },
                            {
                              "number": 1143,
                              "id": "b33abb84069b1dbabdc474344165cc4370205cd3ef5ef2d71f512ca7e6bb61a2",
                              "contributionId": "28d1af9960485f93b491f62551c2c1a69128f445c59ae0983a9028c72e740624",
                              "title": "[E-614] Invite users to share their contact upon showing their interest to project",
                              "createdAt": "2023-07-27T10:23:34Z",
                              "completedAt": "2023-07-28T10:03:48Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 8,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1143"
                            },
                            {
                              "number": 1140,
                              "id": "f373239267368ed631303e5d6122676ac10b9a493b6df5c02e726de4defbc614",
                              "contributionId": "7baff771ad7b4fdbb82f501318e9450a0a6ea9c0c0b96285e2d22864811aa4dd",
                              "title": "Broken PR - DO NOT MERGE",
                              "createdAt": "2023-07-26T12:30:16Z",
                              "completedAt": "2023-07-27T15:43:21Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "PENDING",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1140"
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 15,
                          "totalItemNumber": 146,
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
                              "number": 1507,
                              "id": "1625619352",
                              "contributionId": "6b8a468dcd745391335826d2f442e6c43130f32fee4a11b7002c877bc7ce825f",
                              "title": "Renaming sponsor into sponsorId",
                              "createdAt": "2023-12-01T12:34:02Z",
                              "completedAt": "2023-12-01T12:36:35Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "PULL_REQUEST",
                              "commitsCount": 1,
                              "userCommitsCount": 1,
                              "commentsCount": 1,
                              "status": "MERGED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1507"
                            },
                            {
                              "number": 1477,
                              "id": "1620434711",
                              "contributionId": "9f90962e5b70d1c4064320d18d31428dc21db9d686c85b4cd60c7b6ec810fde7",
                              "title": "Contribution type upper case",
                              "createdAt": "2023-11-28T18:08:41Z",
                              "completedAt": "2023-11-28T18:09:02Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "PULL_REQUEST",
                              "commitsCount": 79,
                              "userCommitsCount": 15,
                              "commentsCount": 1,
                              "status": "CLOSED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1477"
                            },
                            {
                              "number": 1407,
                              "id": "1604560589",
                              "contributionId": "12d69e0032e2da43a96ab3b8613bec3ca244f012e7d9644ac46ba04bf3a33323",
                              "title": "Merge/staging",
                              "createdAt": "2023-11-16T14:47:21Z",
                              "completedAt": "2023-11-16T14:49:35Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "PULL_REQUEST",
                              "commitsCount": 4,
                              "userCommitsCount": 3,
                              "commentsCount": 1,
                              "status": "CLOSED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1407"
                            },
                            {
                              "number": 1378,
                              "id": "1588841186",
                              "contributionId": "4286747370468a1395d78642b48a97c1d81db6bf3b51d6540a2a610af250f33d",
                              "title": "fix lint and types",
                              "createdAt": "2023-11-06T15:15:28Z",
                              "completedAt": "2023-11-07T07:53:38Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "PULL_REQUEST",
                              "commitsCount": 3,
                              "userCommitsCount": 1,
                              "commentsCount": 1,
                              "status": "MERGED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1378"
                            },
                            {
                              "number": 1377,
                              "id": "1588806258",
                              "contributionId": "7c5211884e70166194c71ed5895a876c36914f15796fba51987ddec76ff84320",
                              "title": "Fixing contributions filter",
                              "createdAt": "2023-11-06T14:56:30Z",
                              "completedAt": "2023-11-06T14:57:34Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "PULL_REQUEST",
                              "commitsCount": 1,
                              "userCommitsCount": 1,
                              "commentsCount": 1,
                              "status": "MERGED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1377"
                            },
                            {
                              "number": 1319,
                              "id": "1572539484",
                              "contributionId": "fbfe8d8b8c2a63d1226ab1dc1f0fd773839830ecf08a6410c4abe20760308ee4",
                              "title": "ci: API types generation on build",
                              "createdAt": "2023-10-25T09:39:26Z",
                              "completedAt": "2023-10-30T09:56:58Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "PULL_REQUEST",
                              "commitsCount": 3,
                              "userCommitsCount": 1,
                              "commentsCount": 1,
                              "status": "CLOSED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1319"
                            },
                            {
                              "number": 1318,
                              "id": "1571638326",
                              "contributionId": "b8c5d6619154e8bb017c7464b0468379ff09f5f9ff6d80527b38c8424dd658ec",
                              "title": "This is a test PR for development purposes",
                              "createdAt": "2023-10-24T19:32:31Z",
                              "completedAt": "2023-10-24T19:36:49Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "PULL_REQUEST",
                              "commitsCount": 27,
                              "userCommitsCount": 12,
                              "commentsCount": 1,
                              "status": "CLOSED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1318"
                            },
                            {
                              "number": 1301,
                              "id": "1558436259",
                              "contributionId": "21d6e401ff24399fa97914de130211af686a5adf5f3be76c52d4eafbaa514ceb",
                              "title": "Docker build for e2 e",
                              "createdAt": "2023-10-16T11:51:28Z",
                              "completedAt": "2023-10-16T11:51:55Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "PULL_REQUEST",
                              "commitsCount": 27,
                              "userCommitsCount": 12,
                              "commentsCount": 1,
                              "status": "CLOSED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1301"
                            },
                            {
                              "number": 1300,
                              "id": "1557981540",
                              "contributionId": "946a969d9e5bc9bdf97746ded417effabeb4b912c995c21b9c8edbe1722b4e15",
                              "title": "Add new technologies",
                              "createdAt": "2023-10-16T07:07:41Z",
                              "completedAt": "2023-10-19T07:03:27Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "PULL_REQUEST",
                              "commitsCount": 1,
                              "userCommitsCount": 1,
                              "commentsCount": 1,
                              "status": "MERGED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1300"
                            },
                            {
                              "number": 1259,
                              "id": "1526646573",
                              "contributionId": "2884dc233c8512d062d7dd0b60d78d58e416349bf0a3e1feddff1183a01895e8",
                              "title": "[E 626] Search fix",
                              "createdAt": "2023-09-22T14:04:48Z",
                              "completedAt": "2023-09-22T15:25:55Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "PULL_REQUEST",
                              "commitsCount": 27,
                              "userCommitsCount": 12,
                              "commentsCount": 1,
                              "status": "MERGED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1259"
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 2,
                          "totalItemNumber": 17,
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
                              "number": 1516,
                              "id": "587070f037e591f8653f9c872793704aee2ff6440a225f63d0ca61c056a16cb3",
                              "contributionId": "1c65dd5daa99303857d6ead12147416d9ee430ba962586d5d5b871f2e7a5611c",
                              "title": "Fix api contract id to GitHub user",
                              "createdAt": "2023-12-04T10:59:20Z",
                              "completedAt": "2023-12-04T11:00:23Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1516"
                            },
                            {
                              "number": 1506,
                              "id": "b8da5c8611c943b9733a9b2d458731c855c8182efe7810fc4880639de7152a31",
                              "contributionId": "374a4bdb40fee860faa96ddeb820baed8b923c26b267fb6e6cecfd4fe0c7eee2",
                              "title": "E 829 alert leads when we they need to fix their projects qa 06",
                              "createdAt": "2023-11-30T17:59:38Z",
                              "completedAt": "2023-11-30T18:00:36Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1506"
                            },
                            {
                              "number": 1505,
                              "id": "366590d4a7831e0acf04bfae49cf25864177408e3887742d687e44b0ab8737f1",
                              "contributionId": "280735ac974b29b07b0dc45b0d8445fc2143ec55f801ac4959f659eb03fbf72d",
                              "title": "E 829 alert leads when we they need to fix their projects qa 05",
                              "createdAt": "2023-11-30T17:50:33Z",
                              "completedAt": "2023-11-30T17:51:59Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1505"
                            },
                            {
                              "number": 1504,
                              "id": "707ed965d899deb5a03a452624739c9554ddf4bcf59829ca063cf4fbfcba41f3",
                              "contributionId": "7470c37ad63fe695c23636928823f48cfd881859faf9dda1c9e811c5c5f14ef0",
                              "title": "E 893 typo is url when copy profile address to clipboard",
                              "createdAt": "2023-11-30T17:17:12Z",
                              "completedAt": "2023-11-30T17:27:01Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1504"
                            },
                            {
                              "number": 1501,
                              "id": "de1df6ce11f24f62aed2fae55c8ebf51d90ee70696881d0ad2bc7ee3c8b68093",
                              "contributionId": "64b9dcfcf5c282ebd6056d03a38f124120726cb185e45ee2301b38b83dfbdee8",
                              "title": "Edit create project fix qa 05",
                              "createdAt": "2023-11-30T15:07:51Z",
                              "completedAt": "2023-11-30T15:10:08Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1501"
                            },
                            {
                              "number": 1499,
                              "id": "d60c4052d0f8870e95ebedb9000ea45d2fff1cb21db45f0627fac750e9ceeadf",
                              "contributionId": "d00a4813bd960fba8a0df7b2bf17f52a08f72afe847bcc2f315e84c6afc21a5b",
                              "title": "fix: wording",
                              "createdAt": "2023-11-30T14:47:57Z",
                              "completedAt": "2023-11-30T14:48:44Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1499"
                            },
                            {
                              "number": 1498,
                              "id": "cf3e0b5e580cedeff024c4fc83cde1c1ca0d2f1f66aadb8efb4082ae87dd77f8",
                              "contributionId": "c08b5ec45a5ff783562f738e78e15c4203a744cfabfcdf3590e8e1590f79cf34",
                              "title": "refactor: translation",
                              "createdAt": "2023-11-30T13:29:15Z",
                              "completedAt": "2023-11-30T13:29:57Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1498"
                            },
                            {
                              "number": 1495,
                              "id": "518ecde3e73f5b342fd1a780d4509e9e6d7713b1083902e7a6f6869623d8fb46",
                              "contributionId": "eb5ce564e6e3530f8bc20e6abd6d301bfc8151eab940c04fe553967178130470",
                              "title": "Fix actionmenu position",
                              "createdAt": "2023-11-29T17:34:31Z",
                              "completedAt": "2023-11-29T17:35:54Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1495"
                            },
                            {
                              "number": 1493,
                              "id": "b53d96cc39e16eeac06eb73bdc5263de1a5fe8fa2f928e6f66c0b5a3c722b8a0",
                              "contributionId": "286996afd496088f0be44abeec739ebd16b43984e6e87ab372bc92082bff6334",
                              "title": "Fix autoadd counts",
                              "createdAt": "2023-11-29T17:07:39Z",
                              "completedAt": "2023-11-29T17:08:48Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1493"
                            },
                            {
                              "number": 1492,
                              "id": "a6e01e21965a9b41d5ec755f96c180173ff4623d6086438ecf1add062cec8c78",
                              "contributionId": "4eadc1396781268cfbbba7d317f5d6a8408691426ecefb22954edf887096345a",
                              "title": "e-829-qa",
                              "createdAt": "2023-11-29T15:50:12Z",
                              "completedAt": "2023-11-29T16:05:37Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1492"
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 13,
                          "totalItemNumber": 129,
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
                              "number": 1506,
                              "id": "b8da5c8611c943b9733a9b2d458731c855c8182efe7810fc4880639de7152a31",
                              "contributionId": "374a4bdb40fee860faa96ddeb820baed8b923c26b267fb6e6cecfd4fe0c7eee2",
                              "title": "E 829 alert leads when we they need to fix their projects qa 06",
                              "createdAt": "2023-11-30T17:59:38Z",
                              "completedAt": "2023-11-30T18:00:36Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1506"
                            },
                            {
                              "number": 1505,
                              "id": "366590d4a7831e0acf04bfae49cf25864177408e3887742d687e44b0ab8737f1",
                              "contributionId": "280735ac974b29b07b0dc45b0d8445fc2143ec55f801ac4959f659eb03fbf72d",
                              "title": "E 829 alert leads when we they need to fix their projects qa 05",
                              "createdAt": "2023-11-30T17:50:33Z",
                              "completedAt": "2023-11-30T17:51:59Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1505"
                            },
                            {
                              "number": 1501,
                              "id": "de1df6ce11f24f62aed2fae55c8ebf51d90ee70696881d0ad2bc7ee3c8b68093",
                              "contributionId": "64b9dcfcf5c282ebd6056d03a38f124120726cb185e45ee2301b38b83dfbdee8",
                              "title": "Edit create project fix qa 05",
                              "createdAt": "2023-11-30T15:07:51Z",
                              "completedAt": "2023-11-30T15:10:08Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1501"
                            },
                            {
                              "number": 1492,
                              "id": "a6e01e21965a9b41d5ec755f96c180173ff4623d6086438ecf1add062cec8c78",
                              "contributionId": "4eadc1396781268cfbbba7d317f5d6a8408691426ecefb22954edf887096345a",
                              "title": "e-829-qa",
                              "createdAt": "2023-11-29T15:50:12Z",
                              "completedAt": "2023-11-29T16:05:37Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1492"
                            },
                            {
                              "number": 1354,
                              "id": "6aa96da1df00b7abaa8174ad1a2621f071c3e9f8223066eff261a1d8f93a25b5",
                              "contributionId": "870831e2a427e91e26d1f7946b8d538549abb5892a02530156d63df0a3f3f43a",
                              "title": "E 692 qa status",
                              "createdAt": "2023-11-02T10:25:42Z",
                              "completedAt": "2023-11-02T12:11:10Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "PENDING",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1354"
                            },
                            {
                              "number": 1352,
                              "id": "74a21ee1b10b6bc89602d0cc86d7d7e3acf9bc2cdd08436c9e868661023996cb",
                              "contributionId": "a93a3eec8f658a0fb0fb81dd86cc76e9d918be84e6fffab926a374ceaf1d3fdb",
                              "title": "feat: payout side panel QA",
                              "createdAt": "2023-10-31T17:21:25Z",
                              "completedAt": "2023-11-02T08:11:27Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1352"
                            },
                            {
                              "number": 1351,
                              "id": "cdbe805e392331a053120e46b0d0b09b89b92ba552bc11c9152bc7c6ad6ca13b",
                              "contributionId": "331574dd3aa78737d775eb4948491ea306ec153f5a999b3dbf85b76d21ae4021",
                              "title": "Multi currencies QA 01",
                              "createdAt": "2023-10-31T15:45:53Z",
                              "completedAt": "2023-10-31T17:55:18Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1351"
                            }
                          ],
                          "hasMore": false,
                          "totalPageNumber": 1,
                          "totalItemNumber": 7,
                          "nextPageIndex": 0
                        }
                        """);

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_REWARDABLE_ITEMS, projectId), Map.of("githubUserId",
                        pierre.user().getGithubUserId().toString(),
                        "pageIndex", "0", "pageSize", "10", "search", "qa", "status", "COMPLETED")))
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
                              "number": 1506,
                              "id": "b8da5c8611c943b9733a9b2d458731c855c8182efe7810fc4880639de7152a31",
                              "contributionId": "374a4bdb40fee860faa96ddeb820baed8b923c26b267fb6e6cecfd4fe0c7eee2",
                              "title": "E 829 alert leads when we they need to fix their projects qa 06",
                              "createdAt": "2023-11-30T17:59:38Z",
                              "completedAt": "2023-11-30T18:00:36Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1506"
                            },
                            {
                              "number": 1505,
                              "id": "366590d4a7831e0acf04bfae49cf25864177408e3887742d687e44b0ab8737f1",
                              "contributionId": "280735ac974b29b07b0dc45b0d8445fc2143ec55f801ac4959f659eb03fbf72d",
                              "title": "E 829 alert leads when we they need to fix their projects qa 05",
                              "createdAt": "2023-11-30T17:50:33Z",
                              "completedAt": "2023-11-30T17:51:59Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1505"
                            },
                            {
                              "number": 1501,
                              "id": "de1df6ce11f24f62aed2fae55c8ebf51d90ee70696881d0ad2bc7ee3c8b68093",
                              "contributionId": "64b9dcfcf5c282ebd6056d03a38f124120726cb185e45ee2301b38b83dfbdee8",
                              "title": "Edit create project fix qa 05",
                              "createdAt": "2023-11-30T15:07:51Z",
                              "completedAt": "2023-11-30T15:10:08Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1501"
                            },
                            {
                              "number": 1492,
                              "id": "a6e01e21965a9b41d5ec755f96c180173ff4623d6086438ecf1add062cec8c78",
                              "contributionId": "4eadc1396781268cfbbba7d317f5d6a8408691426ecefb22954edf887096345a",
                              "title": "e-829-qa",
                              "createdAt": "2023-11-29T15:50:12Z",
                              "completedAt": "2023-11-29T16:05:37Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1492"
                            },
                            {
                              "number": 1352,
                              "id": "74a21ee1b10b6bc89602d0cc86d7d7e3acf9bc2cdd08436c9e868661023996cb",
                              "contributionId": "a93a3eec8f658a0fb0fb81dd86cc76e9d918be84e6fffab926a374ceaf1d3fdb",
                              "title": "feat: payout side panel QA",
                              "createdAt": "2023-10-31T17:21:25Z",
                              "completedAt": "2023-11-02T08:11:27Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1352"
                            },
                            {
                              "number": 1351,
                              "id": "cdbe805e392331a053120e46b0d0b09b89b92ba552bc11c9152bc7c6ad6ca13b",
                              "contributionId": "331574dd3aa78737d775eb4948491ea306ec153f5a999b3dbf85b76d21ae4021",
                              "title": "Multi currencies QA 01",
                              "createdAt": "2023-10-31T15:45:53Z",
                              "completedAt": "2023-10-31T17:55:18Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1351"
                            }
                          ],
                          "hasMore": false,
                          "totalPageNumber": 1,
                          "totalItemNumber": 6,
                          "nextPageIndex": 0
                        }
                        """);

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_REWARDABLE_ITEMS, projectId), Map.of("githubUserId",
                        pierre.user().getGithubUserId().toString(),
                        "pageIndex", "0", "pageSize", "10", "search", "qa", "type", "PULL_REQUEST")))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(200)
                .expectBody()
                .json("""
                        {
                          "rewardableItems": [],
                          "hasMore": false,
                          "totalPageNumber": 0,
                          "totalItemNumber": 0,
                          "nextPageIndex": 0
                        }
                                                
                        """);

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_REWARDABLE_ITEMS, projectId), Map.of("githubUserId",
                        pierre.user().getGithubUserId().toString(),
                        "pageIndex", "0", "pageSize", "10", "search", "1133", "type", "PULL_REQUEST")))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "rewardableItems": [],
                          "hasMore": false,
                          "totalPageNumber": 0,
                          "totalItemNumber": 0,
                          "nextPageIndex": 0
                        }
                                                
                        """);
    }

    @Test
    @Order(20)
    void should_get_rewardable_items_given_a_project_lead_and_ignored_contributions() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
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
                        .build()),
                new IgnoredContributionEntity(IgnoredContributionEntity.Id.builder()
                        .contributionId("abc741ada4822926f7f92fb99441868664ae850006629864d6562726f7a53f59")
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
                              "number": 1516,
                              "id": "587070f037e591f8653f9c872793704aee2ff6440a225f63d0ca61c056a16cb3",
                              "contributionId": "1c65dd5daa99303857d6ead12147416d9ee430ba962586d5d5b871f2e7a5611c",
                              "title": "Fix api contract id to GitHub user",
                              "createdAt": "2023-12-04T10:59:20Z",
                              "completedAt": "2023-12-04T11:00:23Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1516"
                            },
                            {
                              "number": 1507,
                              "id": "1625619352",
                              "contributionId": "6b8a468dcd745391335826d2f442e6c43130f32fee4a11b7002c877bc7ce825f",
                              "title": "Renaming sponsor into sponsorId",
                              "createdAt": "2023-12-01T12:34:02Z",
                              "completedAt": "2023-12-01T12:36:35Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "PULL_REQUEST",
                              "commitsCount": 1,
                              "userCommitsCount": 1,
                              "commentsCount": 1,
                              "status": "MERGED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1507"
                            },
                            {
                              "number": 1506,
                              "id": "b8da5c8611c943b9733a9b2d458731c855c8182efe7810fc4880639de7152a31",
                              "contributionId": "374a4bdb40fee860faa96ddeb820baed8b923c26b267fb6e6cecfd4fe0c7eee2",
                              "title": "E 829 alert leads when we they need to fix their projects qa 06",
                              "createdAt": "2023-11-30T17:59:38Z",
                              "completedAt": "2023-11-30T18:00:36Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1506"
                            },
                            {
                              "number": 1505,
                              "id": "366590d4a7831e0acf04bfae49cf25864177408e3887742d687e44b0ab8737f1",
                              "contributionId": "280735ac974b29b07b0dc45b0d8445fc2143ec55f801ac4959f659eb03fbf72d",
                              "title": "E 829 alert leads when we they need to fix their projects qa 05",
                              "createdAt": "2023-11-30T17:50:33Z",
                              "completedAt": "2023-11-30T17:51:59Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1505"
                            },
                            {
                              "number": 1504,
                              "id": "707ed965d899deb5a03a452624739c9554ddf4bcf59829ca063cf4fbfcba41f3",
                              "contributionId": "7470c37ad63fe695c23636928823f48cfd881859faf9dda1c9e811c5c5f14ef0",
                              "title": "E 893 typo is url when copy profile address to clipboard",
                              "createdAt": "2023-11-30T17:17:12Z",
                              "completedAt": "2023-11-30T17:27:01Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1504"
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 28,
                          "totalItemNumber": 140,
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
                               "number": 1516,
                               "id": "587070f037e591f8653f9c872793704aee2ff6440a225f63d0ca61c056a16cb3",
                               "contributionId": "1c65dd5daa99303857d6ead12147416d9ee430ba962586d5d5b871f2e7a5611c",
                               "title": "Fix api contract id to GitHub user",
                               "createdAt": "2023-12-04T10:59:20Z",
                               "completedAt": "2023-12-04T11:00:23Z",
                               "repoName": "marketplace-frontend",
                               "repoId": 498695724,
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": null,
                               "commentsCount": 1,
                               "status": "APPROVED",
                               "ignored": false,
                               "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1516"
                             },
                             {
                               "number": 1507,
                               "id": "1625619352",
                               "contributionId": "6b8a468dcd745391335826d2f442e6c43130f32fee4a11b7002c877bc7ce825f",
                               "title": "Renaming sponsor into sponsorId",
                               "createdAt": "2023-12-01T12:34:02Z",
                               "completedAt": "2023-12-01T12:36:35Z",
                               "repoName": "marketplace-frontend",
                               "repoId": 498695724,
                               "type": "PULL_REQUEST",
                               "commitsCount": 1,
                               "userCommitsCount": 1,
                               "commentsCount": 1,
                               "status": "MERGED",
                               "ignored": false,
                               "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1507"
                             },
                             {
                               "number": 1506,
                               "id": "b8da5c8611c943b9733a9b2d458731c855c8182efe7810fc4880639de7152a31",
                               "contributionId": "374a4bdb40fee860faa96ddeb820baed8b923c26b267fb6e6cecfd4fe0c7eee2",
                               "title": "E 829 alert leads when we they need to fix their projects qa 06",
                               "createdAt": "2023-11-30T17:59:38Z",
                               "completedAt": "2023-11-30T18:00:36Z",
                               "repoName": "marketplace-frontend",
                               "repoId": 498695724,
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": null,
                               "commentsCount": 1,
                               "status": "APPROVED",
                               "ignored": false,
                               "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1506"
                             },
                             {
                               "number": 1505,
                               "id": "366590d4a7831e0acf04bfae49cf25864177408e3887742d687e44b0ab8737f1",
                               "contributionId": "280735ac974b29b07b0dc45b0d8445fc2143ec55f801ac4959f659eb03fbf72d",
                               "title": "E 829 alert leads when we they need to fix their projects qa 05",
                               "createdAt": "2023-11-30T17:50:33Z",
                               "completedAt": "2023-11-30T17:51:59Z",
                               "repoName": "marketplace-frontend",
                               "repoId": 498695724,
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": null,
                               "commentsCount": 1,
                               "status": "APPROVED",
                               "ignored": false,
                               "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1505"
                             },
                             {
                               "number": 1504,
                               "id": "707ed965d899deb5a03a452624739c9554ddf4bcf59829ca063cf4fbfcba41f3",
                               "contributionId": "7470c37ad63fe695c23636928823f48cfd881859faf9dda1c9e811c5c5f14ef0",
                               "title": "E 893 typo is url when copy profile address to clipboard",
                               "createdAt": "2023-11-30T17:17:12Z",
                               "completedAt": "2023-11-30T17:27:01Z",
                               "repoName": "marketplace-frontend",
                               "repoId": 498695724,
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": null,
                               "commentsCount": 2,
                               "status": "APPROVED",
                               "ignored": false,
                               "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1504"
                             },
                             {
                               "number": 1501,
                               "id": "de1df6ce11f24f62aed2fae55c8ebf51d90ee70696881d0ad2bc7ee3c8b68093",
                               "contributionId": "64b9dcfcf5c282ebd6056d03a38f124120726cb185e45ee2301b38b83dfbdee8",
                               "title": "Edit create project fix qa 05",
                               "createdAt": "2023-11-30T15:07:51Z",
                               "completedAt": "2023-11-30T15:10:08Z",
                               "repoName": "marketplace-frontend",
                               "repoId": 498695724,
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": null,
                               "commentsCount": 1,
                               "status": "APPROVED",
                               "ignored": false,
                               "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1501"
                             },
                             {
                               "number": 1499,
                               "id": "d60c4052d0f8870e95ebedb9000ea45d2fff1cb21db45f0627fac750e9ceeadf",
                               "contributionId": "d00a4813bd960fba8a0df7b2bf17f52a08f72afe847bcc2f315e84c6afc21a5b",
                               "title": "fix: wording",
                               "createdAt": "2023-11-30T14:47:57Z",
                               "completedAt": "2023-11-30T14:48:44Z",
                               "repoName": "marketplace-frontend",
                               "repoId": 498695724,
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": null,
                               "commentsCount": 1,
                               "status": "APPROVED",
                               "ignored": false,
                               "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1499"
                             },
                             {
                               "number": 1498,
                               "id": "cf3e0b5e580cedeff024c4fc83cde1c1ca0d2f1f66aadb8efb4082ae87dd77f8",
                               "contributionId": "c08b5ec45a5ff783562f738e78e15c4203a744cfabfcdf3590e8e1590f79cf34",
                               "title": "refactor: translation",
                               "createdAt": "2023-11-30T13:29:15Z",
                               "completedAt": "2023-11-30T13:29:57Z",
                               "repoName": "marketplace-frontend",
                               "repoId": 498695724,
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": null,
                               "commentsCount": 1,
                               "status": "APPROVED",
                               "ignored": false,
                               "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1498"
                             },
                             {
                               "number": 1495,
                               "id": "518ecde3e73f5b342fd1a780d4509e9e6d7713b1083902e7a6f6869623d8fb46",
                               "contributionId": "eb5ce564e6e3530f8bc20e6abd6d301bfc8151eab940c04fe553967178130470",
                               "title": "Fix actionmenu position",
                               "createdAt": "2023-11-29T17:34:31Z",
                               "completedAt": "2023-11-29T17:35:54Z",
                               "repoName": "marketplace-frontend",
                               "repoId": 498695724,
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": null,
                               "commentsCount": 1,
                               "status": "APPROVED",
                               "ignored": false,
                               "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1495"
                             },
                             {
                               "number": 1493,
                               "id": "b53d96cc39e16eeac06eb73bdc5263de1a5fe8fa2f928e6f66c0b5a3c722b8a0",
                               "contributionId": "286996afd496088f0be44abeec739ebd16b43984e6e87ab372bc92082bff6334",
                               "title": "Fix autoadd counts",
                               "createdAt": "2023-11-29T17:07:39Z",
                               "completedAt": "2023-11-29T17:08:48Z",
                               "repoName": "marketplace-frontend",
                               "repoId": 498695724,
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": null,
                               "commentsCount": 1,
                               "status": "APPROVED",
                               "ignored": false,
                               "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1493"
                             }
                           ],
                           "hasMore": true,
                           "totalPageNumber": 15,
                           "totalItemNumber": 146,
                           "nextPageIndex": 1
                        }
                         """);
    }

    @Test
    @Order(30)
    void should_get_all_completed_rewardable_items_given_a_project_lead_and_ignored_contributions() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
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
                        .build()),
                new IgnoredContributionEntity(IgnoredContributionEntity.Id.builder()
                        .contributionId("abc741ada4822926f7f92fb99441868664ae850006629864d6562726f7a53f59")
                        .projectId(projectId)
                        .build())
        ));


        // When
        // @formatter:off
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_ALL_COMPLETED_REWARDABLE_ITEMS, projectId), Map.of(
                        "githubUserId",
                        pierre.user().getGithubUserId().toString())))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewardableIssues.length()").isEqualTo(0)
                .jsonPath("$.rewardablePullRequests.length()").isEqualTo(5)
                .jsonPath("$.rewardablePullRequests[?(@.status != 'MERGED')]").doesNotExist()
                .jsonPath("$.rewardableCodeReviews.length()").isEqualTo(112)
                .jsonPath("$.rewardableCodeReviews[?(@.status nin ['APPROVED', 'CHANGES_REQUESTED'])]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='279c2e7794a6f798c0de46c6fe23cbffcc2feb485072a25fdefc726eaf90e34d')]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='803254a420051b6b04c8cb2030922f3a93cfe2bfbac34c61b56dde93184dbd70')]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='938c879cca27fb6e59ef30658ea5587d2c830dd8bf52a3ec0192b5a780fea267')]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='c6ffc682726fc0ffd3a41a9bee04d62d288761501b5906968577bcd132e36cbf')]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='4b44061840a2f8185f80f2fa381c2aa1bd228e56bde84ab8e9a243ca6da7b073')]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='abc741ada4822926f7f92fb99441868664ae850006629864d6562726f7a53f59')]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='72e253a8ea9fef3e9fee718f0b5c901efdbebb0fff8304cbf09f18066edd3e2f')].githubBody").isEqualTo( "IT test structure\nDocker container support\nFirst API integration test: create project")
                .jsonPath("$..[?(@.contributionId=='72e253a8ea9fef3e9fee718f0b5c901efdbebb0fff8304cbf09f18066edd3e2f')].author.githubUserId").isEqualTo(16590657)
                .jsonPath("$..[?(@.contributionId=='72e253a8ea9fef3e9fee718f0b5c901efdbebb0fff8304cbf09f18066edd3e2f')].author.login").isEqualTo("PierreOucif")
                .jsonPath("$..[?(@.contributionId=='72e253a8ea9fef3e9fee718f0b5c901efdbebb0fff8304cbf09f18066edd3e2f')].author.avatarUrl").isEqualTo("https://avatars.githubusercontent.com/u/16590657?v=4")
        ;
        // @formatter:on
    }
}
