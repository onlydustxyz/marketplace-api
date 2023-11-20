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
                               "number": 1419,
                               "id": "7ce073474e2ed13e1542b2f9ccef4985140ee92687f1ca752e2bfb90b8398c7a",
                               "contributionId": "3a6f695d9334b31dd5858f257e13f026296d07ce4e13d0eb2abd02bb25eab644",
                               "title": "Config",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1419",
                               "createdAt": "2023-11-21T10:47:22Z",
                               "lastUpdateAt": "2023-11-21T10:47:42Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN",
                               "ignored": false
                             },
                             {
                               "number": 1418,
                               "id": "2c2e3ff933ad3a71eb757be83e11672edc2539f47c5978ca8b8e0f5b429381d0",
                               "contributionId": "f51b745dfb6b8026adb69211e0bce68cc14bc443f1e9295601687eacb1ef7522",
                               "title": "E 826 allow users to create a project auth 02",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1418",
                               "createdAt": "2023-11-21T10:23:56Z",
                               "lastUpdateAt": "2023-11-21T10:25:28Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN",
                               "ignored": false
                             },
                             {
                               "number": 1417,
                               "id": "5e93e1cc5a779c9f6aca834541d13e0dadd89d7fca7cbe620f69312d51e01cd2",
                               "contributionId": "e206485940d8bbc2bc5c96d0b20895674b59e95a010b0799692b9b0486fe75e2",
                               "title": "E 826 allow users to create a project auth",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1417",
                               "createdAt": "2023-11-21T10:11:49Z",
                               "lastUpdateAt": "2023-11-21T10:12:10Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN",
                               "ignored": false
                             },
                             {
                               "number": 1409,
                               "id": "3c03bb1770a7236752d98b3183b8a7aedd84e5db361fc430bb0f94b51f9b7457",
                               "contributionId": "abc741ada4822926f7f92fb99441868664ae850006629864d6562726f7a53f59",
                               "title": "use totalItemNumber to display total count",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1409",
                               "createdAt": "2023-11-16T17:43:47Z",
                               "lastUpdateAt": "2023-11-16T17:44:18Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN",
                               "ignored": false
                             },
                             {
                               "number": 1407,
                               "id": "1604560589",
                               "contributionId": "12d69e0032e2da43a96ab3b8613bec3ca244f012e7d9644ac46ba04bf3a33323",
                               "title": "Merge/staging",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1407",
                               "createdAt": "2023-11-16T15:47:21Z",
                               "lastUpdateAt": "2023-11-16T15:49:35Z",
                               "repoName": "marketplace-frontend",
                               "type": "PULL_REQUEST",
                               "commitsCount": 0,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": null,
                               "status": "OPEN",
                               "ignored": false
                             },
                             {
                               "number": 1401,
                               "id": "af7c484cd7bd4b75c851bff3898644ad0422409449b93d1964f57eeda6856215",
                               "contributionId": "60fcb4d628d1cb827d0eba8e0b3a768d6f694f572bc6039357188e5bf69fb6a1",
                               "title": "delete getUserAvatarUrl query",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1401",
                               "createdAt": "2023-11-13T19:00:02Z",
                               "lastUpdateAt": "2023-11-13T19:02:06Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN",
                               "ignored": false
                             },
                             {
                               "number": 1400,
                               "id": "27225976beb161f308de99bf7f026a03694823c58c2431e994d3bbbdc8341cec",
                               "contributionId": "6cae6248d6baadf5dbddcc2be4546cfb22d9679afa3b864db71a781c894af1c6",
                               "title": "delete useReloadOnNewRelease hook and query",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1400",
                               "createdAt": "2023-11-13T17:57:21Z",
                               "lastUpdateAt": "2023-11-13T18:06:54Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN",
                               "ignored": false
                             },
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
                               "status": "OPEN",
                               "ignored": false
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
                               "status": "OPEN",
                               "ignored": false
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
                               "status": "OPEN",
                               "ignored": false
                             }
                           ],
                           "hasMore": true,
                           "totalPageNumber": 16,
                           "totalItemNumber": 154,
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
                               "number": 1157,
                               "id": "a6f0ef781a5c1eda4d191884aaf05260f06b8857d2172c78d44b080674f20da3",
                               "contributionId": "9b5d5c722322b821e1cfb14eb77b6c107c0a339b92aedd2bbbf505a489816ae1",
                               "title": "Add close issue on create issue action",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1157",
                               "createdAt": "2023-08-04T11:07:22Z",
                               "lastUpdateAt": "2023-08-08T22:02:15Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": null,
                               "status": "OPEN",
                               "ignored": false
                             },
                             {
                               "number": 1157,
                               "id": "1462592330",
                               "contributionId": "8ee7c70189d929860f9e059bdc8ca1ad8541a8a2b1fc3fa1afce29af6295e0c0",
                               "title": "Add close issue on create issue action",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1157",
                               "createdAt": "2023-08-04T11:07:22Z",
                               "lastUpdateAt": "2023-08-09T10:04:19Z",
                               "repoName": "marketplace-frontend",
                               "type": "PULL_REQUEST",
                               "commitsCount": 14,
                               "userCommitsCount": 14,
                               "commentsCount": null,
                               "codeReviewOutcome": null,
                               "status": "OPEN",
                               "ignored": false
                             },
                             {
                               "number": 1156,
                               "id": "2fb3f0cbd6af7f17b71555bf3f83598435faed6bbaaa52e6b436e15a9ae3f1d8",
                               "contributionId": "43d0fc77c7896c9073d33c2f2b1c47a3881c2737136b997aa2de291ee266e95e",
                               "title": "[E-661] Show NotFound page if project doesn't exist",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1156",
                               "createdAt": "2023-08-03T16:38:49Z",
                               "lastUpdateAt": "2023-08-08T17:31:48Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN",
                               "ignored": false
                             },
                             {
                               "number": 1155,
                               "id": "4d9e6152887128542dbe97c85d23af4ae69b0108cb658bf9a13ee2a0f1cbc873",
                               "contributionId": "f852c9511d21d4132c42b93f51def3273b6810307705bd8f746fa69d38856e2b",
                               "title": "(E-651) Fix inconsistent page title",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1155",
                               "createdAt": "2023-08-03T11:11:04Z",
                               "lastUpdateAt": "2023-08-04T09:51:03Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN",
                               "ignored": false
                             },
                             {
                               "number": 1154,
                               "id": "5f12f156f6576626832110790529eaec4ae659cfd6433e1d21950b3eb80950fb",
                               "contributionId": "f767584cdbea25c03578589ff6735620dbd338abf7675f893445d29e33190c85",
                               "title": "Responsive app improvements",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1154",
                               "createdAt": "2023-08-02T17:43:53Z",
                               "lastUpdateAt": "2023-08-02T21:04:08Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN",
                               "ignored": false
                             },
                             {
                               "number": 1153,
                               "id": "e1376e0f7e5ebd666772199dc030a9db779f330d309760a7a773a2ee61f1403a",
                               "contributionId": "978eb90a010e91661d4c8df1cffbb6498c4aeadbd6f4f6c841ee3f4903bbd6ed",
                               "title": "Prevent auto-zoom when focus input on mobile",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1153",
                               "createdAt": "2023-08-01T19:27:28Z",
                               "lastUpdateAt": "2023-08-02T07:18:50Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN",
                               "ignored": false
                             },
                             {
                               "number": 1152,
                               "id": "c716bf6091234a7a3a377bed4adca0041df290dd18e6904af9166e7ac03401b9",
                               "contributionId": "1106e2603a0cf7db2030358e3475b87ed8527db2ac2b6415fb3f69b44ee2857c",
                               "title": "[E-642] Index extra fields in github pull requests",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1152",
                               "createdAt": "2023-08-01T16:26:33Z",
                               "lastUpdateAt": "2023-08-07T09:47:51Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN",
                               "ignored": false
                             },
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
                               "status": "OPEN",
                               "ignored": false
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
                               "status": "OPEN",
                               "ignored": false
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
                               "status": "OPEN",
                               "ignored": false
                             }
                           ],
                           "hasMore": true,
                           "totalPageNumber": 16,
                           "totalItemNumber": 154,
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
                               "number": 1133,
                               "id": "5a82f37e298feb5f0b869dac45cd22aba5c7f71c9922ef5a3119307c63753773",
                               "contributionId": "f434009b52172d9812155a7c73d9d3a1fc2873858ef4e3cda003e9a086f8cb00",
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
                               "status": "OPEN",
                               "ignored": false
                             },
                             {
                               "number": 1129,
                               "id": "1442413635",
                               "contributionId": "6d709dd5f85a8b8eaff9cc8837ab837ef9a1a1109ead76580490c0a730a87d9d",
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
                               "status": "OPEN",
                               "ignored": false
                             },
                             {
                               "number": 1129,
                               "id": "04f41f00e4e36373d3e3f1b70ae3a88f4178338655200fa93a60897577fb10f8",
                               "contributionId": "72e253a8ea9fef3e9fee718f0b5c901efdbebb0fff8304cbf09f18066edd3e2f",
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
                               "status": "OPEN",
                               "ignored": false
                             },
                             {
                               "number": 1105,
                               "id": "b3bd439120c942adf4ad78fa805ca9f9445475ef686ed860d25d29f314a06f83",
                               "contributionId": "ea209c984e15e5abadb98867658595c359578f391d272f1246bd744b1bb2de6f",
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
                               "status": "OPEN",
                               "ignored": false
                             }
                           ],
                           "hasMore": false,
                           "totalPageNumber": 16,
                           "totalItemNumber": 154,
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
                              "number": 1407,
                              "id": "1604560589",
                              "contributionId": "12d69e0032e2da43a96ab3b8613bec3ca244f012e7d9644ac46ba04bf3a33323",
                              "title": "Merge/staging",
                              "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1407",
                              "createdAt": "2023-11-16T15:47:21Z",
                              "lastUpdateAt": "2023-11-16T15:49:35Z",
                              "repoName": "marketplace-frontend",
                              "type": "PULL_REQUEST",
                              "commitsCount": 0,
                              "userCommitsCount": 0,
                              "commentsCount": null,
                              "codeReviewOutcome": null,
                              "status": "OPEN",
                              "ignored": false
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
                              "status": "OPEN",
                              "ignored": false
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
                              "status": "OPEN",
                              "ignored": false
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
                              "status": "OPEN",
                              "ignored": false
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
                              "status": "OPEN",
                              "ignored": false
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
                              "status": "OPEN",
                              "ignored": false
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
                              "status": "OPEN",
                              "ignored": false
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
                              "status": "OPEN",
                              "ignored": false
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
                              "status": "OPEN",
                              "ignored": false
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
                              "status": "OPEN",
                              "ignored": false
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
                               "number": 1419,
                               "id": "7ce073474e2ed13e1542b2f9ccef4985140ee92687f1ca752e2bfb90b8398c7a",
                               "contributionId": "3a6f695d9334b31dd5858f257e13f026296d07ce4e13d0eb2abd02bb25eab644",
                               "title": "Config",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1419",
                               "createdAt": "2023-11-21T10:47:22Z",
                               "lastUpdateAt": "2023-11-21T10:47:42Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN",
                               "ignored": false
                             },
                             {
                               "number": 1418,
                               "id": "2c2e3ff933ad3a71eb757be83e11672edc2539f47c5978ca8b8e0f5b429381d0",
                               "contributionId": "f51b745dfb6b8026adb69211e0bce68cc14bc443f1e9295601687eacb1ef7522",
                               "title": "E 826 allow users to create a project auth 02",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1418",
                               "createdAt": "2023-11-21T10:23:56Z",
                               "lastUpdateAt": "2023-11-21T10:25:28Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN",
                               "ignored": false
                             },
                             {
                               "number": 1417,
                               "id": "5e93e1cc5a779c9f6aca834541d13e0dadd89d7fca7cbe620f69312d51e01cd2",
                               "contributionId": "e206485940d8bbc2bc5c96d0b20895674b59e95a010b0799692b9b0486fe75e2",
                               "title": "E 826 allow users to create a project auth",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1417",
                               "createdAt": "2023-11-21T10:11:49Z",
                               "lastUpdateAt": "2023-11-21T10:12:10Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN",
                               "ignored": false
                             },
                             {
                               "number": 1409,
                               "id": "3c03bb1770a7236752d98b3183b8a7aedd84e5db361fc430bb0f94b51f9b7457",
                               "contributionId": "abc741ada4822926f7f92fb99441868664ae850006629864d6562726f7a53f59",
                               "title": "use totalItemNumber to display total count",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1409",
                               "createdAt": "2023-11-16T17:43:47Z",
                               "lastUpdateAt": "2023-11-16T17:44:18Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN",
                               "ignored": false
                             },
                             {
                               "number": 1401,
                               "id": "af7c484cd7bd4b75c851bff3898644ad0422409449b93d1964f57eeda6856215",
                               "contributionId": "60fcb4d628d1cb827d0eba8e0b3a768d6f694f572bc6039357188e5bf69fb6a1",
                               "title": "delete getUserAvatarUrl query",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1401",
                               "createdAt": "2023-11-13T19:00:02Z",
                               "lastUpdateAt": "2023-11-13T19:02:06Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN",
                               "ignored": false
                             },
                             {
                               "number": 1400,
                               "id": "27225976beb161f308de99bf7f026a03694823c58c2431e994d3bbbdc8341cec",
                               "contributionId": "6cae6248d6baadf5dbddcc2be4546cfb22d9679afa3b864db71a781c894af1c6",
                               "title": "delete useReloadOnNewRelease hook and query",
                               "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1400",
                               "createdAt": "2023-11-13T17:57:21Z",
                               "lastUpdateAt": "2023-11-13T18:06:54Z",
                               "repoName": "marketplace-frontend",
                               "type": "CODE_REVIEW",
                               "commitsCount": null,
                               "userCommitsCount": 0,
                               "commentsCount": null,
                               "codeReviewOutcome": "APPROVED",
                               "status": "OPEN",
                               "ignored": false
                             },
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
                               "status": "OPEN",
                               "ignored": false
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
                               "status": "OPEN",
                               "ignored": false
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
                               "status": "OPEN",
                               "ignored": false
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
                               "status": "OPEN",
                               "ignored": false
                             }
                           ],
                           "hasMore": true,
                           "totalPageNumber": 12,
                           "totalItemNumber": 113,
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
                               "status": "OPEN",
                               "ignored": false
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
                               "status": "OPEN",
                               "ignored": false
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
                               "status": "OPEN",
                               "ignored": false
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
                                 "number": 1419,
                                 "id": "7ce073474e2ed13e1542b2f9ccef4985140ee92687f1ca752e2bfb90b8398c7a",
                                 "contributionId": "3a6f695d9334b31dd5858f257e13f026296d07ce4e13d0eb2abd02bb25eab644",
                                 "title": "Config",
                                 "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1419",
                                 "createdAt": "2023-11-21T10:47:22Z",
                                 "lastUpdateAt": "2023-11-21T10:47:42Z",
                                 "repoName": "marketplace-frontend",
                                 "type": "CODE_REVIEW",
                                 "commitsCount": null,
                                 "userCommitsCount": 0,
                                 "commentsCount": null,
                                 "codeReviewOutcome": "APPROVED",
                                 "status": "OPEN",
                                 "ignored": false
                               },
                               {
                                 "number": 1418,
                                 "id": "2c2e3ff933ad3a71eb757be83e11672edc2539f47c5978ca8b8e0f5b429381d0",
                                 "contributionId": "f51b745dfb6b8026adb69211e0bce68cc14bc443f1e9295601687eacb1ef7522",
                                 "title": "E 826 allow users to create a project auth 02",
                                 "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1418",
                                 "createdAt": "2023-11-21T10:23:56Z",
                                 "lastUpdateAt": "2023-11-21T10:25:28Z",
                                 "repoName": "marketplace-frontend",
                                 "type": "CODE_REVIEW",
                                 "commitsCount": null,
                                 "userCommitsCount": 0,
                                 "commentsCount": null,
                                 "codeReviewOutcome": "APPROVED",
                                 "status": "OPEN",
                                 "ignored": false
                               },
                               {
                                 "number": 1417,
                                 "id": "5e93e1cc5a779c9f6aca834541d13e0dadd89d7fca7cbe620f69312d51e01cd2",
                                 "contributionId": "e206485940d8bbc2bc5c96d0b20895674b59e95a010b0799692b9b0486fe75e2",
                                 "title": "E 826 allow users to create a project auth",
                                 "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1417",
                                 "createdAt": "2023-11-21T10:11:49Z",
                                 "lastUpdateAt": "2023-11-21T10:12:10Z",
                                 "repoName": "marketplace-frontend",
                                 "type": "CODE_REVIEW",
                                 "commitsCount": null,
                                 "userCommitsCount": 0,
                                 "commentsCount": null,
                                 "codeReviewOutcome": "APPROVED",
                                 "status": "OPEN",
                                 "ignored": false
                               },
                               {
                                 "number": 1407,
                                 "id": "1604560589",
                                 "contributionId": "12d69e0032e2da43a96ab3b8613bec3ca244f012e7d9644ac46ba04bf3a33323",
                                 "title": "Merge/staging",
                                 "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1407",
                                 "createdAt": "2023-11-16T15:47:21Z",
                                 "lastUpdateAt": "2023-11-16T15:49:35Z",
                                 "repoName": "marketplace-frontend",
                                 "type": "PULL_REQUEST",
                                 "commitsCount": 0,
                                 "userCommitsCount": 0,
                                 "commentsCount": null,
                                 "codeReviewOutcome": null,
                                 "status": "OPEN",
                                 "ignored": false
                               },
                               {
                                 "number": 1401,
                                 "id": "af7c484cd7bd4b75c851bff3898644ad0422409449b93d1964f57eeda6856215",
                                 "contributionId": "60fcb4d628d1cb827d0eba8e0b3a768d6f694f572bc6039357188e5bf69fb6a1",
                                 "title": "delete getUserAvatarUrl query",
                                 "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1401",
                                 "createdAt": "2023-11-13T19:00:02Z",
                                 "lastUpdateAt": "2023-11-13T19:02:06Z",
                                 "repoName": "marketplace-frontend",
                                 "type": "CODE_REVIEW",
                                 "commitsCount": null,
                                 "userCommitsCount": 0,
                                 "commentsCount": null,
                                 "codeReviewOutcome": "APPROVED",
                                 "status": "OPEN",
                                 "ignored": false
                               }
                             ],
                             "hasMore": true,
                             "totalPageNumber": 30,
                             "totalItemNumber": 148,
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
                                "number": 1419,
                                "id": "7ce073474e2ed13e1542b2f9ccef4985140ee92687f1ca752e2bfb90b8398c7a",
                                "contributionId": "3a6f695d9334b31dd5858f257e13f026296d07ce4e13d0eb2abd02bb25eab644",
                                "title": "Config",
                                "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1419",
                                "createdAt": "2023-11-21T10:47:22Z",
                                "lastUpdateAt": "2023-11-21T10:47:42Z",
                                "repoName": "marketplace-frontend",
                                "type": "CODE_REVIEW",
                                "commitsCount": null,
                                "userCommitsCount": 0,
                                "commentsCount": null,
                                "codeReviewOutcome": "APPROVED",
                                "status": "OPEN",
                                "ignored": false
                              },
                              {
                                "number": 1418,
                                "id": "2c2e3ff933ad3a71eb757be83e11672edc2539f47c5978ca8b8e0f5b429381d0",
                                "contributionId": "f51b745dfb6b8026adb69211e0bce68cc14bc443f1e9295601687eacb1ef7522",
                                "title": "E 826 allow users to create a project auth 02",
                                "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1418",
                                "createdAt": "2023-11-21T10:23:56Z",
                                "lastUpdateAt": "2023-11-21T10:25:28Z",
                                "repoName": "marketplace-frontend",
                                "type": "CODE_REVIEW",
                                "commitsCount": null,
                                "userCommitsCount": 0,
                                "commentsCount": null,
                                "codeReviewOutcome": "APPROVED",
                                "status": "OPEN",
                                "ignored": false
                              },
                              {
                                "number": 1417,
                                "id": "5e93e1cc5a779c9f6aca834541d13e0dadd89d7fca7cbe620f69312d51e01cd2",
                                "contributionId": "e206485940d8bbc2bc5c96d0b20895674b59e95a010b0799692b9b0486fe75e2",
                                "title": "E 826 allow users to create a project auth",
                                "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1417",
                                "createdAt": "2023-11-21T10:11:49Z",
                                "lastUpdateAt": "2023-11-21T10:12:10Z",
                                "repoName": "marketplace-frontend",
                                "type": "CODE_REVIEW",
                                "commitsCount": null,
                                "userCommitsCount": 0,
                                "commentsCount": null,
                                "codeReviewOutcome": "APPROVED",
                                "status": "OPEN",
                                "ignored": false
                              },
                              {
                                "number": 1409,
                                "id": "3c03bb1770a7236752d98b3183b8a7aedd84e5db361fc430bb0f94b51f9b7457",
                                "contributionId": "abc741ada4822926f7f92fb99441868664ae850006629864d6562726f7a53f59",
                                "title": "use totalItemNumber to display total count",
                                "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1409",
                                "createdAt": "2023-11-16T17:43:47Z",
                                "lastUpdateAt": "2023-11-16T17:44:18Z",
                                "repoName": "marketplace-frontend",
                                "type": "CODE_REVIEW",
                                "commitsCount": null,
                                "userCommitsCount": 0,
                                "commentsCount": null,
                                "codeReviewOutcome": "APPROVED",
                                "status": "OPEN",
                                "ignored": true
                              },
                              {
                                "number": 1407,
                                "id": "1604560589",
                                "contributionId": "12d69e0032e2da43a96ab3b8613bec3ca244f012e7d9644ac46ba04bf3a33323",
                                "title": "Merge/staging",
                                "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1407",
                                "createdAt": "2023-11-16T15:47:21Z",
                                "lastUpdateAt": "2023-11-16T15:49:35Z",
                                "repoName": "marketplace-frontend",
                                "type": "PULL_REQUEST",
                                "commitsCount": 0,
                                "userCommitsCount": 0,
                                "commentsCount": null,
                                "codeReviewOutcome": null,
                                "status": "OPEN",
                                "ignored": false
                              },
                              {
                                "number": 1401,
                                "id": "af7c484cd7bd4b75c851bff3898644ad0422409449b93d1964f57eeda6856215",
                                "contributionId": "60fcb4d628d1cb827d0eba8e0b3a768d6f694f572bc6039357188e5bf69fb6a1",
                                "title": "delete getUserAvatarUrl query",
                                "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1401",
                                "createdAt": "2023-11-13T19:00:02Z",
                                "lastUpdateAt": "2023-11-13T19:02:06Z",
                                "repoName": "marketplace-frontend",
                                "type": "CODE_REVIEW",
                                "commitsCount": null,
                                "userCommitsCount": 0,
                                "commentsCount": null,
                                "codeReviewOutcome": "APPROVED",
                                "status": "OPEN",
                                "ignored": false
                              },
                              {
                                "number": 1400,
                                "id": "27225976beb161f308de99bf7f026a03694823c58c2431e994d3bbbdc8341cec",
                                "contributionId": "6cae6248d6baadf5dbddcc2be4546cfb22d9679afa3b864db71a781c894af1c6",
                                "title": "delete useReloadOnNewRelease hook and query",
                                "githubUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1400",
                                "createdAt": "2023-11-13T17:57:21Z",
                                "lastUpdateAt": "2023-11-13T18:06:54Z",
                                "repoName": "marketplace-frontend",
                                "type": "CODE_REVIEW",
                                "commitsCount": null,
                                "userCommitsCount": 0,
                                "commentsCount": null,
                                "codeReviewOutcome": "APPROVED",
                                "status": "OPEN",
                                "ignored": false
                              },
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
                                "status": "OPEN",
                                "ignored": false
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
                                "status": "OPEN",
                                "ignored": false
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
                                "status": "OPEN",
                                "ignored": false
                              }
                            ],
                            "hasMore": true,
                            "totalPageNumber": 16,
                            "totalItemNumber": 154,
                            "nextPageIndex": 1
                          }
                         """);
    }
}
