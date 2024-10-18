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
        final var projectId = projectRepository.findAll().get(0).getId();

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
        final var projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

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
                          "totalPageNumber": 2,
                          "totalItemNumber": 11,
                          "nextPageIndex": 1
                        }
                        """);

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_REWARDABLE_ITEMS, projectId), Map.of("githubUserId",
                        pierre.user().getGithubUserId().toString(),
                        "pageIndex", "1", "pageSize", "10")))
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
                              "number": 1492,
                              "id": "a6e01e21965a9b41d5ec755f96c180173ff4623d6086438ecf1add062cec8c78",
                              "contributionId": "4eadc1396781268cfbbba7d317f5d6a8408691426ecefb22954edf887096345a",
                              "title": "e-829-qa",
                              "createdAt": "2023-11-29T15:50:12Z",
                              "completedAt": "2023-11-29T16:05:37Z",
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
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1492"
                            }
                          ]
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
                .isOk()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 1,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "rewardableItems": [
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
                            }
                          ]
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
                .isOk()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 10,
                          "hasMore": false,
                          "nextPageIndex": 0,
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
                            },
                            {
                              "number": 1492,
                              "id": "a6e01e21965a9b41d5ec755f96c180173ff4623d6086438ecf1add062cec8c78",
                              "contributionId": "4eadc1396781268cfbbba7d317f5d6a8408691426ecefb22954edf887096345a",
                              "title": "e-829-qa",
                              "createdAt": "2023-11-29T15:50:12Z",
                              "completedAt": "2023-11-29T16:05:37Z",
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
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1492"
                            }
                          ]
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
                          "totalPageNumber": 1,
                          "totalItemNumber": 4,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "rewardableItems": [
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
                              "number": 1492,
                              "id": "a6e01e21965a9b41d5ec755f96c180173ff4623d6086438ecf1add062cec8c78",
                              "contributionId": "4eadc1396781268cfbbba7d317f5d6a8408691426ecefb22954edf887096345a",
                              "title": "e-829-qa",
                              "createdAt": "2023-11-29T15:50:12Z",
                              "completedAt": "2023-11-29T16:05:37Z",
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
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1492"
                            }
                          ]
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
                          "totalPageNumber": 1,
                          "totalItemNumber": 4,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "rewardableItems": [
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
                              "number": 1492,
                              "id": "a6e01e21965a9b41d5ec755f96c180173ff4623d6086438ecf1add062cec8c78",
                              "contributionId": "4eadc1396781268cfbbba7d317f5d6a8408691426ecefb22954edf887096345a",
                              "title": "e-829-qa",
                              "createdAt": "2023-11-29T15:50:12Z",
                              "completedAt": "2023-11-29T16:05:37Z",
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
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1492"
                            }
                          ]
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
        final var projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
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
                          "totalPageNumber": 3,
                          "totalItemNumber": 11,
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
                           "totalPageNumber": 2,
                           "totalItemNumber": 11,
                           "nextPageIndex": 1
                        }
                        """);
    }

    @Test
    @Order(30)
    void should_get_all_completed_rewardable_items_given_a_project_lead_and_ignored_contributions() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        final var projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

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
                .consumeWith(System.out::println)
                .jsonPath("$.rewardableIssues.length()").isEqualTo(0)
                .jsonPath("$.rewardablePullRequests.length()").isEqualTo(1)
                .jsonPath("$.rewardablePullRequests[?(@.status != 'MERGED')]").doesNotExist()
                .jsonPath("$.rewardableCodeReviews.length()").isEqualTo(10)
                .jsonPath("$.rewardableCodeReviews[?(@.status nin ['APPROVED', 'CHANGES_REQUESTED'])]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='279c2e7794a6f798c0de46c6fe23cbffcc2feb485072a25fdefc726eaf90e34d')]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='803254a420051b6b04c8cb2030922f3a93cfe2bfbac34c61b56dde93184dbd70')]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='938c879cca27fb6e59ef30658ea5587d2c830dd8bf52a3ec0192b5a780fea267')]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='c6ffc682726fc0ffd3a41a9bee04d62d288761501b5906968577bcd132e36cbf')]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='4b44061840a2f8185f80f2fa381c2aa1bd228e56bde84ab8e9a243ca6da7b073')]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='abc741ada4822926f7f92fb99441868664ae850006629864d6562726f7a53f59')]").doesNotExist()
        ;
        // @formatter:on
    }
}
