package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.suites.tags.TagUser;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
import onlydust.com.marketplace.project.domain.port.output.LanguageStorage;
import onlydust.com.marketplace.project.domain.service.LanguageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;


@TagUser
public class UserContributionsApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    private LanguageStorage languageStorage;
    @Autowired
    private ImageStoragePort imageStoragePort;

    @Test
    void should_get_my_contributions_including_private_projects() {
        // Given
        final var user = userAuthHelper.authenticateAntho();

        // When
        client.get()
                .uri(getApiURI(USERS_GET_CONTRIBUTIONS.formatted(user.user().getGithubUserId()), Map.of("pageSize", "3", "includePrivateProjects", "true")))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .json("""
                        {
                          "contributions": [
                            {
                              "type": "PULL_REQUEST",
                              "repo": {
                                "id": 480776993,
                                "owner": "onlydustxyz",
                                "name": "starklings",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/starklings"
                              },
                              "githubAuthor": {
                                "githubUserId": 595505,
                                "login": "ofux",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp"
                              },
                              "githubNumber": 1,
                              "githubStatus": "DRAFT",
                              "githubTitle": "feat: add common model",
                              "githubHtmlUrl": "https://github.com/onlydustxyz/starklings/pull/1",
                              "githubBody": "Note: if we use ERC20 instead of ERC721 to keep track of dust balance, then `token_id` must be removed from `Dust` struct.",
                              "githubLabels": null,
                              "lastUpdatedAt": "2022-04-13T09:00:49Z",
                              "id": "7b076143d6844660494a112d2182017a367914577b14ed562250ef1751de6547",
                              "createdAt": "2022-04-12T11:59:17Z",
                              "completedAt": "2022-04-13T09:00:49Z",
                              "status": "COMPLETED",
                              "githubPullRequestReviewState": "PENDING_REVIEWER",
                              "rewardIds": [],
                              "project": {
                                "id": "00490be6-2c03-4720-993b-aea3e07edd81",
                                "slug": "zama",
                                "name": "Zama",
                                "logoUrl": "https://dl.airtable.com/.attachments/f776b6ea66adbe46d86adaea58626118/610d50f6/15TqNyRwTMGoVeAX2u1M",
                                "shortDescription": "A super description for Zama",
                                "visibility": "PUBLIC",
                                "languages": []
                              },
                              "contributor": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": true,
                                "id": null
                              },
                              "links": []
                            },
                            {
                              "type": "CODE_REVIEW",
                              "repo": {
                                "id": 480776993,
                                "owner": "onlydustxyz",
                                "name": "starklings",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/starklings"
                              },
                              "githubAuthor": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                              },
                              "githubNumber": 169,
                              "githubStatus": "APPROVED",
                              "githubTitle": "Educational content: follow-up exercise on hints.",
                              "githubHtmlUrl": "https://github.com/onlydustxyz/starklings/pull/169",
                              "githubBody": "A second exercise on hints",
                              "githubLabels": null,
                              "lastUpdatedAt": "2022-06-01T10:34:49Z",
                              "id": "1e80b860c62c6d43bb1b0e383e57fa2a645a20ed33d900a2f8a9810ccbdfbbc4",
                              "createdAt": "2022-06-01T10:24:19Z",
                              "completedAt": "2022-06-01T10:34:49Z",
                              "status": "COMPLETED",
                              "githubPullRequestReviewState": null,
                              "rewardIds": [],
                              "project": {
                                "id": "00490be6-2c03-4720-993b-aea3e07edd81",
                                "slug": "zama",
                                "name": "Zama",
                                "logoUrl": "https://dl.airtable.com/.attachments/f776b6ea66adbe46d86adaea58626118/610d50f6/15TqNyRwTMGoVeAX2u1M",
                                "shortDescription": "A super description for Zama",
                                "visibility": "PUBLIC",
                                "languages": []
                              },
                              "contributor": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": true,
                                "id": null
                              },
                              "links": [
                                {
                                  "type": "PULL_REQUEST",
                                  "repo": {
                                    "id": 480776993,
                                    "owner": "onlydustxyz",
                                    "name": "starklings",
                                    "description": null,
                                    "htmlUrl": "https://github.com/onlydustxyz/starklings"
                                  },
                                  "githubAuthor": {
                                    "githubUserId": 98529704,
                                    "login": "tekkac",
                                    "avatarUrl": "https://avatars.githubusercontent.com/u/98529704?v=4"
                                  },
                                  "githubNumber": 169,
                                  "githubStatus": "MERGED",
                                  "githubTitle": "Educational content: follow-up exercise on hints.",
                                  "githubHtmlUrl": "https://github.com/onlydustxyz/starklings/pull/169",
                                  "githubBody": "A second exercise on hints",
                                  "githubLabels": null,
                                  "lastUpdatedAt": null,
                                  "is_mine": false
                                }
                              ]
                            },
                            {
                              "type": "ISSUE",
                              "repo": {
                                "id": 498695724,
                                "owner": "onlydustxyz",
                                "name": "marketplace-frontend",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                              },
                              "githubAuthor": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                              },
                              "githubNumber": 15,
                              "githubStatus": "COMPLETED",
                              "githubTitle": "push contributions pr to github smart contract",
                              "githubHtmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/issues/15",
                              "githubBody": "When a new PR is merged for a registered user, we should be able to push the data on-chain.",
                              "githubLabels": null,
                              "lastUpdatedAt": "2022-06-21T15:49:26Z",
                              "id": "7c06e9b36cd92c6214a1b4af4a11b0c16d0be722105bfc0a75afd1e34f9c500e",
                              "createdAt": "2022-06-21T08:16:18Z",
                              "completedAt": "2022-06-21T15:49:26Z",
                              "status": "COMPLETED",
                              "githubPullRequestReviewState": null,
                              "rewardIds": [],
                              "project": {
                                "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                "slug": "kaaper",
                                "name": "kaaper",
                                "logoUrl": null,
                                "shortDescription": "Documentation generator for Cairo projects.",
                                "visibility": "PUBLIC",
                                "languages": []
                              },
                              "contributor": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": true,
                                "id": null
                              },
                              "links": []
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_get_my_contributions_without_private_projects() {
        // Given
        final var user = userAuthHelper.authenticateAntho();

        // When
        client.get()
                .uri(getApiURI(USERS_GET_CONTRIBUTIONS.formatted(user.user().getGithubUserId()), Map.of("pageSize", "3")))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .json("""
                        {
                          "contributions": [
                            {
                              "type": "PULL_REQUEST",
                              "repo": {
                                "id": 480776993,
                                "owner": "onlydustxyz",
                                "name": "starklings",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/starklings"
                              },
                              "githubAuthor": {
                                "githubUserId": 595505,
                                "login": "ofux",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp"
                              },
                              "githubNumber": 1,
                              "githubStatus": "DRAFT",
                              "githubTitle": "feat: add common model",
                              "githubHtmlUrl": "https://github.com/onlydustxyz/starklings/pull/1",
                              "githubBody": "Note: if we use ERC20 instead of ERC721 to keep track of dust balance, then `token_id` must be removed from `Dust` struct.",
                              "githubLabels": null,
                              "lastUpdatedAt": "2022-04-13T09:00:49Z",
                              "id": "7b076143d6844660494a112d2182017a367914577b14ed562250ef1751de6547",
                              "createdAt": "2022-04-12T11:59:17Z",
                              "completedAt": "2022-04-13T09:00:49Z",
                              "status": "COMPLETED",
                              "githubPullRequestReviewState": "PENDING_REVIEWER",
                              "rewardIds": [],
                              "project": {
                                "id": "00490be6-2c03-4720-993b-aea3e07edd81",
                                "slug": "zama",
                                "name": "Zama",
                                "logoUrl": "https://dl.airtable.com/.attachments/f776b6ea66adbe46d86adaea58626118/610d50f6/15TqNyRwTMGoVeAX2u1M",
                                "shortDescription": "A super description for Zama",
                                "visibility": "PUBLIC",
                                "languages": []
                              },
                              "contributor": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": true,
                                "id": null
                              },
                              "links": []
                            },
                            {
                              "type": "CODE_REVIEW",
                              "repo": {
                                "id": 480776993,
                                "owner": "onlydustxyz",
                                "name": "starklings",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/starklings"
                              },
                              "githubAuthor": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                              },
                              "githubNumber": 169,
                              "githubStatus": "APPROVED",
                              "githubTitle": "Educational content: follow-up exercise on hints.",
                              "githubHtmlUrl": "https://github.com/onlydustxyz/starklings/pull/169",
                              "githubBody": "A second exercise on hints",
                              "githubLabels": null,
                              "lastUpdatedAt": "2022-06-01T10:34:49Z",
                              "id": "1e80b860c62c6d43bb1b0e383e57fa2a645a20ed33d900a2f8a9810ccbdfbbc4",
                              "createdAt": "2022-06-01T10:24:19Z",
                              "completedAt": "2022-06-01T10:34:49Z",
                              "status": "COMPLETED",
                              "githubPullRequestReviewState": null,
                              "rewardIds": [],
                              "project": {
                                "id": "00490be6-2c03-4720-993b-aea3e07edd81",
                                "slug": "zama",
                                "name": "Zama",
                                "logoUrl": "https://dl.airtable.com/.attachments/f776b6ea66adbe46d86adaea58626118/610d50f6/15TqNyRwTMGoVeAX2u1M",
                                "shortDescription": "A super description for Zama",
                                "visibility": "PUBLIC",
                                "languages": []
                              },
                              "contributor": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": true,
                                "id": null
                              },
                              "links": [
                                {
                                  "type": "PULL_REQUEST",
                                  "repo": {
                                    "id": 480776993,
                                    "owner": "onlydustxyz",
                                    "name": "starklings",
                                    "description": null,
                                    "htmlUrl": "https://github.com/onlydustxyz/starklings"
                                  },
                                  "githubAuthor": {
                                    "githubUserId": 98529704,
                                    "login": "tekkac",
                                    "avatarUrl": "https://avatars.githubusercontent.com/u/98529704?v=4"
                                  },
                                  "githubNumber": 169,
                                  "githubStatus": "MERGED",
                                  "githubTitle": "Educational content: follow-up exercise on hints.",
                                  "githubHtmlUrl": "https://github.com/onlydustxyz/starklings/pull/169",
                                  "githubBody": "A second exercise on hints",
                                  "githubLabels": null,
                                  "lastUpdatedAt": null,
                                  "is_mine": false
                                }
                              ]
                            },
                            {
                              "type": "ISSUE",
                              "repo": {
                                "id": 498695724,
                                "owner": "onlydustxyz",
                                "name": "marketplace-frontend",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                              },
                              "githubAuthor": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                              },
                              "githubNumber": 15,
                              "githubStatus": "COMPLETED",
                              "githubTitle": "push contributions pr to github smart contract",
                              "githubHtmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/issues/15",
                              "githubBody": "When a new PR is merged for a registered user, we should be able to push the data on-chain.",
                              "githubLabels": null,
                              "lastUpdatedAt": "2022-06-21T15:49:26Z",
                              "id": "7c06e9b36cd92c6214a1b4af4a11b0c16d0be722105bfc0a75afd1e34f9c500e",
                              "createdAt": "2022-06-21T08:16:18Z",
                              "completedAt": "2022-06-21T15:49:26Z",
                              "status": "COMPLETED",
                              "githubPullRequestReviewState": null,
                              "rewardIds": [],
                              "project": {
                                "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                "slug": "kaaper",
                                "name": "kaaper",
                                "logoUrl": null,
                                "shortDescription": "Documentation generator for Cairo projects.",
                                "visibility": "PUBLIC",
                                "languages": []
                              },
                              "contributor": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": true,
                                "id": null
                              },
                              "links": []
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_get_my_rewards_with_project_filter() {
        // Given
        final var user = userAuthHelper.authenticateAntho();

        // When
        client.get()
                .uri(getApiURI(USERS_GET_CONTRIBUTIONS.formatted(user.user().getGithubUserId()), Map.of(
                        "projects", "f39b827f-df73-498c-8853-99bc3f562723,594ca5ca-48f7-49a8-9c26-84b949d4fdd9")
                ))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .jsonPath("$.contributions.length()").isEqualTo(50)
                .jsonPath("$.hasMore").isEqualTo(true)
                .jsonPath("$.totalPageNumber").isEqualTo(5)
                .jsonPath("$.totalItemNumber").isEqualTo(225)
                .jsonPath("$.nextPageIndex").isEqualTo(1)
        ;
    }

    @Test
    void should_get_my_rewards_with_ecosystem_filter() {
        // Given
        final var user = userAuthHelper.authenticateAntho();

        // When
        client.get()
                .uri(getApiURI(USERS_GET_CONTRIBUTIONS.formatted(user.user().getGithubUserId()), Map.of(
                        "ecosystems", "99b6c284-f9bb-4f89-8ce7-03771465ef8e,6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                        "includePrivateProjects", "true")
                ))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .jsonPath("$.contributions.length()").isEqualTo(50)
                .jsonPath("$.hasMore").isEqualTo(true)
                .jsonPath("$.totalPageNumber").isEqualTo(3)
                .jsonPath("$.totalItemNumber").isEqualTo(121)
                .jsonPath("$.nextPageIndex").isEqualTo(1)
        ;
    }

    @Test
    void should_get_my_rewards_with_language_filter() {
        // Given
        final var user = userAuthHelper.authenticateAntho();

        // When
        client.get()
                .uri(getApiURI(USERS_GET_CONTRIBUTIONS.formatted(user.user().getGithubUserId()), Map.of(
                        "languages", UUID.randomUUID().toString())
                ))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.contributions.length()").isEqualTo(0)
                .jsonPath("$.hasMore").isEqualTo(false)
                .jsonPath("$.totalPageNumber").isEqualTo(0)
                .jsonPath("$.totalItemNumber").isEqualTo(0)
        ;

        // And given
        final var languageService = new LanguageService(languageStorage, imageStoragePort);
        final var rust = languageService.listLanguages().stream().filter(language -> language.slug().equals("rust")).findFirst().orElseThrow();

        // When
        client.get()
                .uri(getApiURI(USERS_GET_CONTRIBUTIONS.formatted(user.user().getGithubUserId()), Map.of(
                        "languages", UUID.randomUUID() + "," + rust.id())
                ))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .jsonPath("$.contributions.length()").isEqualTo(50)
                .jsonPath("$.hasMore").isEqualTo(true)
                .jsonPath("$.totalPageNumber").isEqualTo(3)
                .jsonPath("$.totalItemNumber").isEqualTo(120)
        ;
    }

    @Test
    void should_get_my_rewards_with_repos_filter() {
        // Given
        final var user = userAuthHelper.authenticateAntho();

        // When
        client.get()
                .uri(getApiURI(USERS_GET_CONTRIBUTIONS.formatted(user.user().getGithubUserId()), Map.of(
                        "repositories", "493591124")
                ))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.contributions.length()").isEqualTo(18)
                .jsonPath("$.hasMore").isEqualTo(false)
                .jsonPath("$.totalPageNumber").isEqualTo(1)
                .jsonPath("$.totalItemNumber").isEqualTo(18)
                .jsonPath("$.nextPageIndex").isEqualTo(0)
        ;
    }

    @Test
    void should_get_my_rewards_with_type_filter() {
        // Given
        final var user = userAuthHelper.authenticateAntho();

        // When
        client.get()
                .uri(getApiURI(USERS_GET_CONTRIBUTIONS.formatted(user.user().getGithubUserId()), Map.of(
                        "types", "ISSUE", "includePrivateProjects", "true")
                ))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.contributions.length()").isEqualTo(48)
                .jsonPath("$.contributions[0].type").isEqualTo("ISSUE")
                .jsonPath("$.hasMore").isEqualTo(false)
                .jsonPath("$.totalPageNumber").isEqualTo(1)
                .jsonPath("$.totalItemNumber").isEqualTo(48)
                .jsonPath("$.nextPageIndex").isEqualTo(0)
        ;
    }

    @Test
    void should_get_my_rewards_with_status_filter() {
        // Given
        final var user = userAuthHelper.authenticateAntho();

        // When
        client.get()
                .uri(getApiURI(USERS_GET_CONTRIBUTIONS.formatted(user.user().getGithubUserId()), Map.of(
                        "statuses", "IN_PROGRESS")
                ))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.contributions.length()").isEqualTo(1)
                .jsonPath("$.contributions[0].status").isEqualTo("IN_PROGRESS")
                .jsonPath("$.hasMore").isEqualTo(false)
                .jsonPath("$.totalPageNumber").isEqualTo(1)
                .jsonPath("$.totalItemNumber").isEqualTo(1)
                .jsonPath("$.nextPageIndex").isEqualTo(0)
        ;
    }

    @Test
    void should_get_my_rewards_with_status_filter_and_includeing_private_projects() {
        // Given
        final var user = userAuthHelper.authenticateAntho();

        // When
        client.get()
                .uri(getApiURI(USERS_GET_CONTRIBUTIONS.formatted(user.user().getGithubUserId()), Map.of(
                        "statuses", "IN_PROGRESS", "includePrivateProjects", "true")
                ))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.contributions.length()").isEqualTo(2)
                .jsonPath("$.contributions[0].status").isEqualTo("IN_PROGRESS")
                .jsonPath("$.hasMore").isEqualTo(false)
                .jsonPath("$.totalPageNumber").isEqualTo(1)
                .jsonPath("$.totalItemNumber").isEqualTo(2)
                .jsonPath("$.nextPageIndex").isEqualTo(0)
        ;
    }

    @Test
    void should_get_list_rewards_associated_to_a_contribution() {
        // Given
        final var user = userAuthHelper.authenticateAntho();

        // When
        client.get()
                .uri(getApiURI(USERS_GET_CONTRIBUTIONS.formatted(user.user().getGithubUserId()),
                        Map.of("pageSize", "1",
                                "pageIndex", "17",
                                "projects", "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                "repositories", "498695724",
                                "statuses", "COMPLETED",
                                "types", "PULL_REQUEST")))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .json("""
                        {
                          "contributions": [
                            {
                              "rewardIds": [
                                "6587511b-3791-47c6-8430-8f793606c63a",
                                "0b275f04-bdb1-4d4f-8cd1-76fe135ccbdf",
                                "e9ebbe59-fb74-4a6c-9a51-6d9050412977",
                                "e33ea956-d2f5-496b-acf9-e2350faddb16",
                                "95e079c9-609c-4531-8c5c-13217306b299"
                              ]
                            }
                          ]
                        }
                        """)
        ;
    }

    @Test
    void should_order_by_project_repo_name() {
        // Given
        final var user = userAuthHelper.authenticateAntho();

        // When
        client.get()
                .uri(getApiURI(USERS_GET_CONTRIBUTIONS.formatted(user.user().getGithubUserId()),
                        Map.of("pageSize", "1",
                                "projects", "298a547f-ecb6-4ab2-8975-68f4e9bf7b39,594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                                "sort", "PROJECT_REPO_NAME"
                        )))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .jsonPath("$.contributions[0].project.name").isEqualTo("Mooooooonlight")
                .jsonPath("$.contributions[0].repo.name").isEqualTo("gateway")
        ;

        // When
        client.get()
                .uri(getApiURI(USERS_GET_CONTRIBUTIONS.formatted(user.user().getGithubUserId()),
                        Map.of("pageSize", "1",
                                "projects", "298a547f-ecb6-4ab2-8975-68f4e9bf7b39,594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                                "sort", "PROJECT_REPO_NAME",
                                "direction", "DESC")))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .jsonPath("$.contributions[0].project.name").isEqualTo("kaaper")
                .jsonPath("$.contributions[0].repo.name").isEqualTo("marketplace-frontend")
        ;
    }

    @Test
    void should_order_by_last_update_date() {
        // Given
        final var user = userAuthHelper.authenticateAntho();

        // When
        client.get()
                .uri(getApiURI(USERS_GET_CONTRIBUTIONS.formatted(user.user().getGithubUserId()),
                        Map.of("pageSize", "1",
                                "sort", "LAST_UPDATED_AT"
                        )))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .jsonPath("$.contributions[0].lastUpdatedAt").isEqualTo("2022-04-13T09:00:49Z")
        ;

        // When
        client.get()
                .uri(getApiURI(USERS_GET_CONTRIBUTIONS.formatted(user.user().getGithubUserId()),
                        Map.of("pageSize", "1",
                                "sort", "LAST_UPDATED_AT",
                                "direction", "DESC")))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .jsonPath("$.contributions[0].lastUpdatedAt").isEqualTo("2023-11-29T16:13:31Z")
        ;
    }

    @Test
    void should_order_by_github_number_title() {
        // Given
        final var user = userAuthHelper.authenticateAntho();

        // When
        client.get()
                .uri(getApiURI(USERS_GET_CONTRIBUTIONS.formatted(user.user().getGithubUserId()),
                        Map.of("pageSize", "1",
                                "projects", "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                "sort", "GITHUB_NUMBER_TITLE"
                        )))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .jsonPath("$.contributions[0].githubNumber").isEqualTo("8")
                .jsonPath("$.contributions[0].githubTitle").isEqualTo("feat: add main logic and output structure")
        ;

        // When
        client.get()
                .uri(getApiURI(USERS_GET_CONTRIBUTIONS.formatted(user.user().getGithubUserId()),
                        Map.of("pageSize", "1",
                                "projects", "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                "sort", "GITHUB_NUMBER_TITLE",
                                "direction", "DESC")))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .jsonPath("$.contributions[0].githubNumber").isEqualTo("1247")
                .jsonPath("$.contributions[0].githubTitle").isEqualTo("[E-723] Fetch token prices")
        ;
    }

    @Test
    void should_order_by_links_count() {
        // Given
        final var user = userAuthHelper.authenticateAntho();

        // When
        client.get()
                .uri(getApiURI(USERS_GET_CONTRIBUTIONS.formatted(user.user().getGithubUserId()),
                        Map.of("pageSize", "1",
                                "sort", "LINKS_COUNT")))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .jsonPath("$.contributions[0].links.length()").isEqualTo(0)
        ;

        // When
        client.get()
                .uri(getApiURI(USERS_GET_CONTRIBUTIONS.formatted(user.user().getGithubUserId()),
                        Map.of("pageSize", "1",
                                "sort", "LINKS_COUNT",
                                "direction", "DESC")))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .jsonPath("$.contributions[0].links.length()").isEqualTo(1)
        ;
    }


    @Test
    void should_not_duplicate_contributions() {
        // Given
        final var user = userAuthHelper.authenticateHayden();

        // When
        client.get()
                .uri(getApiURI(USERS_GET_CONTRIBUTIONS.formatted(user.user().getGithubUserId()),
                        Map.of("pageSize", "10",
                                "projects", "594ca5ca-48f7-49a8-9c26-84b949d4fdd9,90fb751a-1137-4815-b3c4-54927a5db059",
                                "types", "ISSUE",
                                "statuses", "IN_PROGRESS",
                                "sort", "PROJECT_REPO_NAME"
                        )))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.contributions.length()").isEqualTo(2)
                .jsonPath("$.contributions[0].project.name").isEqualTo("Mooooooonlight")
                .jsonPath("$.contributions[1].project.name").isEqualTo("No sponsors")
        ;
    }
}
