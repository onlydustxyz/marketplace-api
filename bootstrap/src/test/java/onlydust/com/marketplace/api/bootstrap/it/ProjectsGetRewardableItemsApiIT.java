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
                              "number": 1444,
                              "id": "d9094ca2959d36ec73e6bf629b9bdca0c58b7a09b0e96d7c19d49d5026c37769",
                              "contributionId": "f83f1324a1babced55620f8b014b594c03a97c57f8e01a4502c7d9f0863de541",
                              "title": "feat: rm repos validation",
                              "createdAt": "2023-11-22T17:23:45Z",
                              "completedAt": "2023-11-23T08:18:18Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "PENDING",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1444"
                            },
                            {
                              "number": 1434,
                              "id": "10036311426b07dc64e0fa016642910210ce1d317929ee3a64f9e53c043129c3",
                              "contributionId": "5d8f7a47b3ffff60edf744c11268823c391021769918de0af12d5bc6de06147c",
                              "title": "Fix condition",
                              "createdAt": "2023-11-22T14:04:05Z",
                              "completedAt": "2023-11-22T14:04:28Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1434"
                            },
                            {
                              "number": 1420,
                              "id": "2655d5d3d94287d8eb8f78a59969888c3fddca9d57ef4134e4e21d484728e9b7",
                              "contributionId": "d8e61500a5e07cd6a11b1f84ec468181897087a6532c90aacd876c675f77cc5b",
                              "title": "Fix callback on github",
                              "createdAt": "2023-11-21T12:47:23Z",
                              "completedAt": "2023-11-21T13:31:44Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1420"
                            },
                            {
                              "number": 1419,
                              "id": "7ce073474e2ed13e1542b2f9ccef4985140ee92687f1ca752e2bfb90b8398c7a",
                              "contributionId": "3a6f695d9334b31dd5858f257e13f026296d07ce4e13d0eb2abd02bb25eab644",
                              "title": "Config",
                              "createdAt": "2023-11-21T09:47:22Z",
                              "completedAt": "2023-11-21T09:47:42Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1419"
                            },
                            {
                              "number": 1418,
                              "id": "2c2e3ff933ad3a71eb757be83e11672edc2539f47c5978ca8b8e0f5b429381d0",
                              "contributionId": "f51b745dfb6b8026adb69211e0bce68cc14bc443f1e9295601687eacb1ef7522",
                              "title": "E 826 allow users to create a project auth 02",
                              "createdAt": "2023-11-21T09:23:56Z",
                              "completedAt": "2023-11-21T09:25:28Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1418"
                            },
                            {
                              "number": 1417,
                              "id": "5e93e1cc5a779c9f6aca834541d13e0dadd89d7fca7cbe620f69312d51e01cd2",
                              "contributionId": "e206485940d8bbc2bc5c96d0b20895674b59e95a010b0799692b9b0486fe75e2",
                              "title": "E 826 allow users to create a project auth",
                              "createdAt": "2023-11-21T09:11:49Z",
                              "completedAt": "2023-11-21T09:12:10Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1417"
                            },
                            {
                              "number": 1409,
                              "id": "3c03bb1770a7236752d98b3183b8a7aedd84e5db361fc430bb0f94b51f9b7457",
                              "contributionId": "abc741ada4822926f7f92fb99441868664ae850006629864d6562726f7a53f59",
                              "title": "use totalItemNumber to display total count",
                              "createdAt": "2023-11-16T16:43:47Z",
                              "completedAt": "2023-11-16T16:44:18Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1409"
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
                              "number": 1401,
                              "id": "af7c484cd7bd4b75c851bff3898644ad0422409449b93d1964f57eeda6856215",
                              "contributionId": "60fcb4d628d1cb827d0eba8e0b3a768d6f694f572bc6039357188e5bf69fb6a1",
                              "title": "delete getUserAvatarUrl query",
                              "createdAt": "2023-11-13T18:00:02Z",
                              "completedAt": "2023-11-13T18:02:06Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1401"
                            },
                            {
                              "number": 1400,
                              "id": "27225976beb161f308de99bf7f026a03694823c58c2431e994d3bbbdc8341cec",
                              "contributionId": "6cae6248d6baadf5dbddcc2be4546cfb22d9679afa3b864db71a781c894af1c6",
                              "title": "delete useReloadOnNewRelease hook and query",
                              "createdAt": "2023-11-13T16:57:21Z",
                              "completedAt": "2023-11-13T17:06:54Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1400"
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 14,
                          "totalItemNumber": 131,
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
                .expectBody().consumeWith(System.out::println)
                .json("""
                        {
                          "rewardableItems": [
                            {
                              "number": 1105,
                              "id": "b3bd439120c942adf4ad78fa805ca9f9445475ef686ed860d25d29f314a06f83",
                              "contributionId": "ea209c984e15e5abadb98867658595c359578f391d272f1246bd744b1bb2de6f",
                              "title": "allow running our stack in a container",
                              "createdAt": "2023-07-12T11:54:35Z",
                              "completedAt": "2023-07-12T16:17:16Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 6,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1105"
                            }
                          ],
                          "hasMore": false,
                          "totalPageNumber": 14,
                          "totalItemNumber": 131,
                          "nextPageIndex": 13
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
                              "number": 1105,
                              "id": "b3bd439120c942adf4ad78fa805ca9f9445475ef686ed860d25d29f314a06f83",
                              "contributionId": "ea209c984e15e5abadb98867658595c359578f391d272f1246bd744b1bb2de6f",
                              "title": "allow running our stack in a container",
                              "createdAt": "2023-07-12T11:54:35Z",
                              "completedAt": "2023-07-12T16:17:16Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 6,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1105"
                            }
                          ],
                          "hasMore": false,
                          "totalPageNumber": 14,
                          "totalItemNumber": 131,
                          "nextPageIndex": 13
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
                .expectBody().consumeWith(System.out::println)
                .json("""
                        {
                          "rewardableItems": [
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
                            },
                            {
                              "number": 1238,
                              "id": "1519041128",
                              "contributionId": "e473d496d162d4ed79eac072e74bc2a48501d73cc4fb6fa3e67a484a55cf0896",
                              "title": "feat: update ReadMe with libpq info",
                              "createdAt": "2023-09-18T08:24:21Z",
                              "completedAt": "2023-09-18T09:53:37Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "PULL_REQUEST",
                              "commitsCount": 23,
                              "userCommitsCount": 13,
                              "commentsCount": 2,
                              "status": "CLOSED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1238"
                            },
                            {
                              "number": 1226,
                              "id": "1507701129",
                              "contributionId": "3ecff6bfaa190bdbb8b42f0a842fda14bf34f93ee245e4b0ea0069af5cf74d1f",
                              "title": "Add noir language",
                              "createdAt": "2023-09-08T11:09:48Z",
                              "completedAt": "2023-09-08T11:20:32Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "PULL_REQUEST",
                              "commitsCount": 1,
                              "userCommitsCount": 1,
                              "commentsCount": 1,
                              "status": "CLOSED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1226"
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 2,
                          "totalItemNumber": 15,
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
                .expectBody().consumeWith(System.out::println)
                .json("""
                        {
                          "rewardableItems": [
                            {
                              "number": 1444,
                              "id": "d9094ca2959d36ec73e6bf629b9bdca0c58b7a09b0e96d7c19d49d5026c37769",
                              "contributionId": "f83f1324a1babced55620f8b014b594c03a97c57f8e01a4502c7d9f0863de541",
                              "title": "feat: rm repos validation",
                              "createdAt": "2023-11-22T17:23:45Z",
                              "completedAt": "2023-11-23T08:18:18Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "PENDING",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1444"
                            },
                            {
                              "number": 1434,
                              "id": "10036311426b07dc64e0fa016642910210ce1d317929ee3a64f9e53c043129c3",
                              "contributionId": "5d8f7a47b3ffff60edf744c11268823c391021769918de0af12d5bc6de06147c",
                              "title": "Fix condition",
                              "createdAt": "2023-11-22T14:04:05Z",
                              "completedAt": "2023-11-22T14:04:28Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1434"
                            },
                            {
                              "number": 1420,
                              "id": "2655d5d3d94287d8eb8f78a59969888c3fddca9d57ef4134e4e21d484728e9b7",
                              "contributionId": "d8e61500a5e07cd6a11b1f84ec468181897087a6532c90aacd876c675f77cc5b",
                              "title": "Fix callback on github",
                              "createdAt": "2023-11-21T12:47:23Z",
                              "completedAt": "2023-11-21T13:31:44Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1420"
                            },
                            {
                              "number": 1419,
                              "id": "7ce073474e2ed13e1542b2f9ccef4985140ee92687f1ca752e2bfb90b8398c7a",
                              "contributionId": "3a6f695d9334b31dd5858f257e13f026296d07ce4e13d0eb2abd02bb25eab644",
                              "title": "Config",
                              "createdAt": "2023-11-21T09:47:22Z",
                              "completedAt": "2023-11-21T09:47:42Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1419"
                            },
                            {
                              "number": 1418,
                              "id": "2c2e3ff933ad3a71eb757be83e11672edc2539f47c5978ca8b8e0f5b429381d0",
                              "contributionId": "f51b745dfb6b8026adb69211e0bce68cc14bc443f1e9295601687eacb1ef7522",
                              "title": "E 826 allow users to create a project auth 02",
                              "createdAt": "2023-11-21T09:23:56Z",
                              "completedAt": "2023-11-21T09:25:28Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1418"
                            },
                            {
                              "number": 1417,
                              "id": "5e93e1cc5a779c9f6aca834541d13e0dadd89d7fca7cbe620f69312d51e01cd2",
                              "contributionId": "e206485940d8bbc2bc5c96d0b20895674b59e95a010b0799692b9b0486fe75e2",
                              "title": "E 826 allow users to create a project auth",
                              "createdAt": "2023-11-21T09:11:49Z",
                              "completedAt": "2023-11-21T09:12:10Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1417"
                            },
                            {
                              "number": 1409,
                              "id": "3c03bb1770a7236752d98b3183b8a7aedd84e5db361fc430bb0f94b51f9b7457",
                              "contributionId": "abc741ada4822926f7f92fb99441868664ae850006629864d6562726f7a53f59",
                              "title": "use totalItemNumber to display total count",
                              "createdAt": "2023-11-16T16:43:47Z",
                              "completedAt": "2023-11-16T16:44:18Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1409"
                            },
                            {
                              "number": 1401,
                              "id": "af7c484cd7bd4b75c851bff3898644ad0422409449b93d1964f57eeda6856215",
                              "contributionId": "60fcb4d628d1cb827d0eba8e0b3a768d6f694f572bc6039357188e5bf69fb6a1",
                              "title": "delete getUserAvatarUrl query",
                              "createdAt": "2023-11-13T18:00:02Z",
                              "completedAt": "2023-11-13T18:02:06Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1401"
                            },
                            {
                              "number": 1400,
                              "id": "27225976beb161f308de99bf7f026a03694823c58c2431e994d3bbbdc8341cec",
                              "contributionId": "6cae6248d6baadf5dbddcc2be4546cfb22d9679afa3b864db71a781c894af1c6",
                              "title": "delete useReloadOnNewRelease hook and query",
                              "createdAt": "2023-11-13T16:57:21Z",
                              "completedAt": "2023-11-13T17:06:54Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1400"
                            },
                            {
                              "number": 1393,
                              "id": "1fd8ac0fba88c9245d294630a7e5d3604eb05319f9dc6b7b7cb10bc032b2f6c1",
                              "contributionId": "b88b3aac4b3e975c292074e203b60ca9d5bd46f3fd863c07c9e7774d436c497c",
                              "title": "fix: gen types",
                              "createdAt": "2023-11-10T16:04:16Z",
                              "completedAt": "2023-11-10T16:05:18Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1393"
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 12,
                          "totalItemNumber": 116,
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
                              "createdAt": "2023-11-02T10:25:42Z",
                              "completedAt": "2023-11-02T12:11:10Z",
                              "repoName": "marketplace-frontend",
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
                          "totalItemNumber": 3,
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
                              "number": 1352,
                              "id": "74a21ee1b10b6bc89602d0cc86d7d7e3acf9bc2cdd08436c9e868661023996cb",
                              "contributionId": "a93a3eec8f658a0fb0fb81dd86cc76e9d918be84e6fffab926a374ceaf1d3fdb",
                              "title": "feat: payout side panel QA",
                              "createdAt": "2023-10-31T17:21:25Z",
                              "completedAt": "2023-11-02T08:11:27Z",
                              "repoName": "marketplace-frontend",
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
                          "totalItemNumber": 2,
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
                              "number": 1444,
                              "id": "d9094ca2959d36ec73e6bf629b9bdca0c58b7a09b0e96d7c19d49d5026c37769",
                              "contributionId": "f83f1324a1babced55620f8b014b594c03a97c57f8e01a4502c7d9f0863de541",
                              "title": "feat: rm repos validation",
                              "createdAt": "2023-11-22T17:23:45Z",
                              "completedAt": "2023-11-23T08:18:18Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "PENDING",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1444"
                            },
                            {
                              "number": 1434,
                              "id": "10036311426b07dc64e0fa016642910210ce1d317929ee3a64f9e53c043129c3",
                              "contributionId": "5d8f7a47b3ffff60edf744c11268823c391021769918de0af12d5bc6de06147c",
                              "title": "Fix condition",
                              "createdAt": "2023-11-22T14:04:05Z",
                              "completedAt": "2023-11-22T14:04:28Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1434"
                            },
                            {
                              "number": 1420,
                              "id": "2655d5d3d94287d8eb8f78a59969888c3fddca9d57ef4134e4e21d484728e9b7",
                              "contributionId": "d8e61500a5e07cd6a11b1f84ec468181897087a6532c90aacd876c675f77cc5b",
                              "title": "Fix callback on github",
                              "createdAt": "2023-11-21T12:47:23Z",
                              "completedAt": "2023-11-21T13:31:44Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1420"
                            },
                            {
                              "number": 1419,
                              "id": "7ce073474e2ed13e1542b2f9ccef4985140ee92687f1ca752e2bfb90b8398c7a",
                              "contributionId": "3a6f695d9334b31dd5858f257e13f026296d07ce4e13d0eb2abd02bb25eab644",
                              "title": "Config",
                              "createdAt": "2023-11-21T09:47:22Z",
                              "completedAt": "2023-11-21T09:47:42Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1419"
                            },
                            {
                              "number": 1418,
                              "id": "2c2e3ff933ad3a71eb757be83e11672edc2539f47c5978ca8b8e0f5b429381d0",
                              "contributionId": "f51b745dfb6b8026adb69211e0bce68cc14bc443f1e9295601687eacb1ef7522",
                              "title": "E 826 allow users to create a project auth 02",
                              "createdAt": "2023-11-21T09:23:56Z",
                              "completedAt": "2023-11-21T09:25:28Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1418"
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 25,
                          "totalItemNumber": 125,
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
                .expectBody().consumeWith(System.out::println)
                .json("""
                        {
                          "rewardableItems": [
                            {
                              "number": 1444,
                              "id": "d9094ca2959d36ec73e6bf629b9bdca0c58b7a09b0e96d7c19d49d5026c37769",
                              "contributionId": "f83f1324a1babced55620f8b014b594c03a97c57f8e01a4502c7d9f0863de541",
                              "title": "feat: rm repos validation",
                              "createdAt": "2023-11-22T17:23:45Z",
                              "completedAt": "2023-11-23T08:18:18Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "PENDING",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1444"
                            },
                            {
                              "number": 1434,
                              "id": "10036311426b07dc64e0fa016642910210ce1d317929ee3a64f9e53c043129c3",
                              "contributionId": "5d8f7a47b3ffff60edf744c11268823c391021769918de0af12d5bc6de06147c",
                              "title": "Fix condition",
                              "createdAt": "2023-11-22T14:04:05Z",
                              "completedAt": "2023-11-22T14:04:28Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1434"
                            },
                            {
                              "number": 1420,
                              "id": "2655d5d3d94287d8eb8f78a59969888c3fddca9d57ef4134e4e21d484728e9b7",
                              "contributionId": "d8e61500a5e07cd6a11b1f84ec468181897087a6532c90aacd876c675f77cc5b",
                              "title": "Fix callback on github",
                              "createdAt": "2023-11-21T12:47:23Z",
                              "completedAt": "2023-11-21T13:31:44Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1420"
                            },
                            {
                              "number": 1419,
                              "id": "7ce073474e2ed13e1542b2f9ccef4985140ee92687f1ca752e2bfb90b8398c7a",
                              "contributionId": "3a6f695d9334b31dd5858f257e13f026296d07ce4e13d0eb2abd02bb25eab644",
                              "title": "Config",
                              "createdAt": "2023-11-21T09:47:22Z",
                              "completedAt": "2023-11-21T09:47:42Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1419"
                            },
                            {
                              "number": 1418,
                              "id": "2c2e3ff933ad3a71eb757be83e11672edc2539f47c5978ca8b8e0f5b429381d0",
                              "contributionId": "f51b745dfb6b8026adb69211e0bce68cc14bc443f1e9295601687eacb1ef7522",
                              "title": "E 826 allow users to create a project auth 02",
                              "createdAt": "2023-11-21T09:23:56Z",
                              "completedAt": "2023-11-21T09:25:28Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1418"
                            },
                            {
                              "number": 1417,
                              "id": "5e93e1cc5a779c9f6aca834541d13e0dadd89d7fca7cbe620f69312d51e01cd2",
                              "contributionId": "e206485940d8bbc2bc5c96d0b20895674b59e95a010b0799692b9b0486fe75e2",
                              "title": "E 826 allow users to create a project auth",
                              "createdAt": "2023-11-21T09:11:49Z",
                              "completedAt": "2023-11-21T09:12:10Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1417"
                            },
                            {
                              "number": 1409,
                              "id": "3c03bb1770a7236752d98b3183b8a7aedd84e5db361fc430bb0f94b51f9b7457",
                              "contributionId": "abc741ada4822926f7f92fb99441868664ae850006629864d6562726f7a53f59",
                              "title": "use totalItemNumber to display total count",
                              "createdAt": "2023-11-16T16:43:47Z",
                              "completedAt": "2023-11-16T16:44:18Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": true,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1409"
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
                              "number": 1401,
                              "id": "af7c484cd7bd4b75c851bff3898644ad0422409449b93d1964f57eeda6856215",
                              "contributionId": "60fcb4d628d1cb827d0eba8e0b3a768d6f694f572bc6039357188e5bf69fb6a1",
                              "title": "delete getUserAvatarUrl query",
                              "createdAt": "2023-11-13T18:00:02Z",
                              "completedAt": "2023-11-13T18:02:06Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1401"
                            },
                            {
                              "number": 1400,
                              "id": "27225976beb161f308de99bf7f026a03694823c58c2431e994d3bbbdc8341cec",
                              "contributionId": "6cae6248d6baadf5dbddcc2be4546cfb22d9679afa3b864db71a781c894af1c6",
                              "title": "delete useReloadOnNewRelease hook and query",
                              "createdAt": "2023-11-13T16:57:21Z",
                              "completedAt": "2023-11-13T17:06:54Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "APPROVED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1400"
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 14,
                          "totalItemNumber": 131,
                          "nextPageIndex": 1
                        }
                                                
                         """);
    }

    @Test
    @Order(30)
    void should_get_all_completed_rewardable_items_given_a_project_lead_and_ignored_contributions() {
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
                .jsonPath("$.rewardablePullRequests.length()").isEqualTo(4)
                .jsonPath("$.rewardablePullRequests[?(@.status != 'MERGED')]").doesNotExist()
                .jsonPath("$.rewardableCodeReviews.length()").isEqualTo(99)
                .jsonPath("$.rewardableCodeReviews[?(@.status nin ['APPROVED', 'CHANGES_REQUESTED'])]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='279c2e7794a6f798c0de46c6fe23cbffcc2feb485072a25fdefc726eaf90e34d')]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='803254a420051b6b04c8cb2030922f3a93cfe2bfbac34c61b56dde93184dbd70')]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='938c879cca27fb6e59ef30658ea5587d2c830dd8bf52a3ec0192b5a780fea267')]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='c6ffc682726fc0ffd3a41a9bee04d62d288761501b5906968577bcd132e36cbf')]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='4b44061840a2f8185f80f2fa381c2aa1bd228e56bde84ab8e9a243ca6da7b073')]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='abc741ada4822926f7f92fb99441868664ae850006629864d6562726f7a53f59')]").doesNotExist();
        // @formatter:on
    }
}
