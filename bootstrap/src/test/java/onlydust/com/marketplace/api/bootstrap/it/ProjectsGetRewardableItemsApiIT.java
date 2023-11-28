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
                              "status": "CANCELLED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
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
                              "status": "CANCELLED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1400"
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 16,
                          "totalItemNumber": 157,
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
                              "number": 1159,
                              "id": "3eb2f30036713457510488c091bc982539caeda203f12456dc621ad3938be5d2",
                              "contributionId": "32c4bee9d2a4f4ae233b7dac8720d3dec154fb3077f50a99c64e9ba8dc0df1c0",
                              "title": "[E-616] Enhance panel stacking",
                              "createdAt": "2023-08-07T09:50:28Z",
                              "completedAt": "2023-08-07T10:09:54Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "COMPLETED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1159"
                            },
                            {
                              "number": 1158,
                              "id": "6b3ddd609f472f970e6994ef53f4b15faf8e859f925a98f9ead99fcbfa7dd41a",
                              "contributionId": "b938c7a6954bf4e00496cf4052bc0e63478fd6aa190cddc9c8c862e1c1334e51",
                              "title": "[E-660] Responsive on large screen",
                              "createdAt": "2023-08-07T09:48:36Z",
                              "completedAt": "2023-08-07T09:57:22Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "COMPLETED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1158"
                            },
                            {
                              "number": 1160,
                              "id": "a01b1db7c72cbf64ea460730bf45428b0e7047813ee0ef7ef30db33187016d3b",
                              "contributionId": "01dfb88a627380d946ac1826d02ad84bf76ca192655e1579024c1982f2cfccae",
                              "title": "encode jwt in test to have dynamic expiry date",
                              "createdAt": "2023-08-07T09:21:46Z",
                              "completedAt": "2023-08-07T09:22:38Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "COMPLETED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1160"
                            },
                            {
                              "number": 1157,
                              "id": "1462592330",
                              "contributionId": "8ee7c70189d929860f9e059bdc8ca1ad8541a8a2b1fc3fa1afce29af6295e0c0",
                              "title": "Add close issue on create issue action",
                              "createdAt": "2023-08-04T09:07:22Z",
                              "completedAt": "2023-08-09T08:04:19Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "PULL_REQUEST",
                              "commitsCount": 14,
                              "userCommitsCount": 14,
                              "commentsCount": 20,
                              "status": "COMPLETED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1157"
                            },
                            {
                              "number": 1157,
                              "id": "a6f0ef781a5c1eda4d191884aaf05260f06b8857d2172c78d44b080674f20da3",
                              "contributionId": "9b5d5c722322b821e1cfb14eb77b6c107c0a339b92aedd2bbbf505a489816ae1",
                              "title": "Add close issue on create issue action",
                              "createdAt": "2023-08-04T09:07:22Z",
                              "completedAt": "2023-08-09T08:04:19Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 20,
                              "status": "CANCELLED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1157"
                            },
                            {
                              "number": 1156,
                              "id": "2fb3f0cbd6af7f17b71555bf3f83598435faed6bbaaa52e6b436e15a9ae3f1d8",
                              "contributionId": "43d0fc77c7896c9073d33c2f2b1c47a3881c2737136b997aa2de291ee266e95e",
                              "title": "[E-661] Show NotFound page if project doesn't exist",
                              "createdAt": "2023-08-03T16:38:49Z",
                              "completedAt": "2023-08-09T09:11:07Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "COMPLETED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1156"
                            },
                            {
                              "number": 1155,
                              "id": "4d9e6152887128542dbe97c85d23af4ae69b0108cb658bf9a13ee2a0f1cbc873",
                              "contributionId": "f852c9511d21d4132c42b93f51def3273b6810307705bd8f746fa69d38856e2b",
                              "title": "(E-651) Fix inconsistent page title",
                              "createdAt": "2023-08-03T11:11:04Z",
                              "completedAt": "2023-08-04T09:57:20Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "COMPLETED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1155"
                            },
                            {
                              "number": 1154,
                              "id": "5f12f156f6576626832110790529eaec4ae659cfd6433e1d21950b3eb80950fb",
                              "contributionId": "f767584cdbea25c03578589ff6735620dbd338abf7675f893445d29e33190c85",
                              "title": "Responsive app improvements",
                              "createdAt": "2023-08-02T17:43:53Z",
                              "completedAt": "2023-08-03T09:04:26Z",
                              "repoName": "marketplace-frontend",
                              "repoId": 498695724,
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "COMPLETED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1154"
                            },
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1152"
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 16,
                          "totalItemNumber": 157,
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
                          "rewardableItems": [
                            {
                              "number": 1138,
                              "id": "1449825806",
                              "contributionId": "b7ce941d8509b13d6cbf525dbf34c7062207a8c94607a4b06277faa5196159b2",
                              "title": "Adding gitguardian pre-commit",
                              "createdAt": "2023-07-26T09:04:28Z",
                              "completedAt": "2023-07-26T11:38:35Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 1,
                              "userCommitsCount": 1,
                              "commentsCount": 2,
                              "status": "COMPLETED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1138"
                            },
                            {
                              "number": 1137,
                              "id": "a63fac01ffc310b448a084c8d1c4085f3a5dc7f38f6b0e58ab3a296f0069ad38",
                              "contributionId": "a866d0b8349fd8626464b7e54f73ffa2bc3d7cd133fb8bd2c641b7e7cddf8f41",
                              "title": "[E-582] Have sexy URLs for projects, profiles and others pages",
                              "createdAt": "2023-07-26T08:38:53Z",
                              "completedAt": "2023-07-26T12:28:13Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "COMPLETED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1137"
                            },
                            {
                              "number": 1135,
                              "id": "443bd5d30b4683967133b283b2f5bcc205e618d8a16366d69b3ac54fc1eff4e8",
                              "contributionId": "ed389486455bfb37e70963dbc2c3f6370d220a669bb9eb62adcfedf9baf7af96",
                              "title": "Fix impersonation",
                              "createdAt": "2023-07-24T15:26:03Z",
                              "completedAt": "2023-07-24T15:40:33Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "COMPLETED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1135"
                            },
                            {
                              "number": 1133,
                              "id": "5a82f37e298feb5f0b869dac45cd22aba5c7f71c9922ef5a3119307c63753773",
                              "contributionId": "f434009b52172d9812155a7c73d9d3a1fc2873858ef4e3cda003e9a086f8cb00",
                              "title": "e 609 restore credentials check at s3 connect time",
                              "createdAt": "2023-07-21T15:39:00Z",
                              "completedAt": "2023-07-26T04:46:50Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 2,
                              "status": "COMPLETED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1133"
                            },
                            {
                              "number": 1129,
                              "id": "1442413635",
                              "contributionId": "6d709dd5f85a8b8eaff9cc8837ab837ef9a1a1109ead76580490c0a730a87d9d",
                              "title": "First API integration test",
                              "createdAt": "2023-07-20T08:45:18Z",
                              "completedAt": "2023-07-21T13:00:05Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 30,
                              "userCommitsCount": 16,
                              "commentsCount": 8,
                              "status": "COMPLETED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1129"
                            },
                            {
                              "number": 1129,
                              "id": "04f41f00e4e36373d3e3f1b70ae3a88f4178338655200fa93a60897577fb10f8",
                              "contributionId": "72e253a8ea9fef3e9fee718f0b5c901efdbebb0fff8304cbf09f18066edd3e2f",
                              "title": "First API integration test",
                              "createdAt": "2023-07-20T08:45:18Z",
                              "completedAt": "2023-07-21T12:59:15Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 8,
                              "status": "COMPLETED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1129"
                            },
                            {
                              "number": 1105,
                              "id": "b3bd439120c942adf4ad78fa805ca9f9445475ef686ed860d25d29f314a06f83",
                              "contributionId": "ea209c984e15e5abadb98867658595c359578f391d272f1246bd744b1bb2de6f",
                              "title": "allow running our stack in a container",
                              "createdAt": "2023-07-12T11:54:35Z",
                              "completedAt": "2023-07-12T16:17:16Z",
                              "repoName": "marketplace-frontend",
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 6,
                              "status": "COMPLETED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1105"
                            }
                          ],
                          "hasMore": false,
                          "totalPageNumber": 16,
                          "totalItemNumber": 157,
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
                              "type": "PULL_REQUEST",
                              "commitsCount": 4,
                              "userCommitsCount": 3,
                              "commentsCount": 1,
                              "status": "CANCELLED",
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
                              "type": "PULL_REQUEST",
                              "commitsCount": 3,
                              "userCommitsCount": 1,
                              "commentsCount": 1,
                              "status": "COMPLETED",
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
                              "type": "PULL_REQUEST",
                              "commitsCount": 1,
                              "userCommitsCount": 1,
                              "commentsCount": 1,
                              "status": "COMPLETED",
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
                              "type": "PULL_REQUEST",
                              "commitsCount": 3,
                              "userCommitsCount": 1,
                              "commentsCount": 1,
                              "status": "CANCELLED",
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
                              "type": "PULL_REQUEST",
                              "commitsCount": 27,
                              "userCommitsCount": 12,
                              "commentsCount": 1,
                              "status": "CANCELLED",
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
                              "type": "PULL_REQUEST",
                              "commitsCount": 27,
                              "userCommitsCount": 12,
                              "commentsCount": 1,
                              "status": "CANCELLED",
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
                              "type": "PULL_REQUEST",
                              "commitsCount": 1,
                              "userCommitsCount": 1,
                              "commentsCount": 1,
                              "status": "COMPLETED",
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
                              "type": "PULL_REQUEST",
                              "commitsCount": 27,
                              "userCommitsCount": 12,
                              "commentsCount": 1,
                              "status": "COMPLETED",
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
                              "type": "PULL_REQUEST",
                              "commitsCount": 23,
                              "userCommitsCount": 13,
                              "commentsCount": 2,
                              "status": "CANCELLED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1238"
                            },
                            {
                              "number": 1232,
                              "id": "1511546916",
                              "contributionId": "a290ea203b1264105bf581aebbdf3e79edfdd89811da50dc6bd076272d810b2e",
                              "title": "Addin sitemap.xml in robots.txt",
                              "createdAt": "2023-09-12T07:38:04Z",
                              "completedAt": "2023-09-12T07:45:12Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 1,
                              "userCommitsCount": 1,
                              "commentsCount": 1,
                              "status": "COMPLETED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1232"
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 5,
                          "totalItemNumber": 41,
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
                              "status": "CANCELLED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
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
                              "status": "CANCELLED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
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
                              "status": "CANCELLED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1418"
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 31,
                          "totalItemNumber": 151,
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
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "CANCELLED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
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
                              "type": "PULL_REQUEST",
                              "commitsCount": 4,
                              "userCommitsCount": 3,
                              "commentsCount": 1,
                              "status": "CANCELLED",
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
                              "type": "CODE_REVIEW",
                              "commitsCount": null,
                              "userCommitsCount": null,
                              "commentsCount": 1,
                              "status": "COMPLETED",
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
                              "status": "COMPLETED",
                              "ignored": false,
                              "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1400"
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 16,
                          "totalItemNumber": 157,
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
                .jsonPath("$.rewardablePullRequests.length()").isEqualTo(29)
                .jsonPath("$.rewardablePullRequests[?(@.status!='COMPLETED')]").doesNotExist()
                .jsonPath("$.rewardableCodeReviews.length()").isEqualTo(99)
                .jsonPath("$.rewardableCodeReviews[?(@.status!='COMPLETED')]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='279c2e7794a6f798c0de46c6fe23cbffcc2feb485072a25fdefc726eaf90e34d')]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='803254a420051b6b04c8cb2030922f3a93cfe2bfbac34c61b56dde93184dbd70')]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='938c879cca27fb6e59ef30658ea5587d2c830dd8bf52a3ec0192b5a780fea267')]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='c6ffc682726fc0ffd3a41a9bee04d62d288761501b5906968577bcd132e36cbf')]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='4b44061840a2f8185f80f2fa381c2aa1bd228e56bde84ab8e9a243ca6da7b073')]").doesNotExist()
                .jsonPath("$..[?(@.contributionId=='abc741ada4822926f7f92fb99441868664ae850006629864d6562726f7a53f59')]").doesNotExist();
        // @formatter:on
    }
}
