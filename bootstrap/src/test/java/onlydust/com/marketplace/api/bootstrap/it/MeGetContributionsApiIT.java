package onlydust.com.marketplace.api.bootstrap.it;

import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@ActiveProfiles({"hasura_auth"})
public class MeGetContributionsApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    HasuraUserHelper userHelper;

    @Test
    void should_get_my_contributions() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(ME_GET_CONTRIBUTIONS, Map.of("pageSize", "3")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .json("""
                        {
                          "contributions": [
                            {
                              "id": "7b076143d6844660494a112d2182017a367914577b14ed562250ef1751de6547",
                              "createdAt": "2022-04-12T13:59:17Z",
                              "completedAt": "2022-04-13T11:00:49Z",
                              "type": "PULL_REQUEST",
                              "status": "COMPLETED",
                              "githubNumber": 1,
                              "githubTitle": "feat: add common model",
                              "githubHtmlUrl": "https://github.com/onlydustxyz/starklings/pull/1",
                              "githubBody": "Note: if we use ERC20 instead of ERC721 to keep track of dust balance, then `token_id` must be removed from `Dust` struct.",
                              "projectName": "Zama",
                              "repoName": "starklings",
                              "links": [],
                              "rewardIds": []
                            },
                            {
                              "id": "6d3bac610f1f9e983b179478916eefcd39583dd7ca869ec15529c66539ff9045",
                              "createdAt": "2022-04-12T18:56:44Z",
                              "completedAt": "2022-04-13T11:00:48Z",
                              "type": "PULL_REQUEST",
                              "status": "COMPLETED",
                              "githubNumber": 2,
                              "githubTitle": "DUST token creation",
                              "githubHtmlUrl": "https://github.com/onlydustxyz/starklings/pull/2",
                              "githubBody": "ERC721 based\\r\\ncan be minted and burned\\r\\non-chain metadata:\\r\\n* Space size\\r\\n* position (x, y)\\r\\n* direction (x, y)\\r\\n\\r\\nCan move and will bounce in case it hurts one of the border/corner of the space\\r\\n\\r\\nSome nice features added:\\r\\n* batch minting (with user defined position/direction)\\r\\n* random minting (on the edge of the board game)\\r\\n* random batch minting\\r\\n* optimization of storage",
                              "projectName": "Zama",
                              "repoName": "starklings",
                              "links": [],
                              "rewardIds": []
                            },
                            {
                              "id": "5befa137a9ef4264834de24e223c7ee0b6fada1bb24ca3dc713496a96fba805b",
                              "createdAt": "2022-04-13T17:42:14Z",
                              "completedAt": "2022-04-13T18:01:10Z",
                              "type": "PULL_REQUEST",
                              "status": "COMPLETED",
                              "githubNumber": 3,
                              "githubTitle": "Feature/space",
                              "githubHtmlUrl": "https://github.com/onlydustxyz/starklings/pull/3",
                              "githubBody": "Add a space contract that is in charge of processing game turns.\\r\\n\\r\\nAt each turn:\\r\\n- one dust is spawned\\r\\n- dust is moved on the grid\\r\\n- dust collisions are handled. When a collision occurs, one of the dust is burnt\\r\\n\\r\\nThe random generator is now in its own contract so we can mock it.\\r\\n\\r\\nThe TODOs can be found here: https://www.notion.so/onlydust/Workshop-TODO-9a67a936a5ca4180a3a23d7d94fecd61\\r\\n\\r\\n> Note: I just realised that we have different formatting between @AnthonyBuisset and me. I don't know why. This is quite annoying for tracking changes, so I apologise in advance.",
                              "projectName": "Zama",
                              "repoName": "starklings",
                              "links": [],
                              "rewardIds": []
                            }
                          ],
                          "projects": [
                            {
                              "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                              "slug": "kaaper",
                              "name": "kaaper",
                              "shortDescription": "Documentation generator for Cairo projects.",
                              "logoUrl": null,
                              "visibility": "PUBLIC"
                            },
                            {
                              "id": "57f76bd5-c6fb-4ef0-8a0a-74450f4ceca8",
                              "slug": "pizzeria-yoshi-",
                              "name": "Pizzeria Yoshi !",
                              "shortDescription": "Miaaaam une pizza !",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/14305950553200301786.png",
                              "visibility": "PUBLIC"
                            },
                            {
                              "id": "90fb751a-1137-4815-b3c4-54927a5db059",
                              "slug": "no-sponsors",
                              "name": "No sponsors",
                              "shortDescription": "afsasdas",
                              "logoUrl": null,
                              "visibility": "PUBLIC"
                            },
                            {
                              "id": "00490be6-2c03-4720-993b-aea3e07edd81",
                              "slug": "zama",
                              "name": "Zama",
                              "shortDescription": "A super description for Zama",
                              "logoUrl": "https://dl.airtable.com/.attachments/f776b6ea66adbe46d86adaea58626118/610d50f6/15TqNyRwTMGoVeAX2u1M",
                              "visibility": "PUBLIC"
                            },
                            {
                              "id": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                              "slug": "mooooooonlight",
                              "name": "Mooooooonlight",
                              "shortDescription": "hello la team",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1913921207486176664.jpg",
                              "visibility": "PUBLIC"
                            },
                            {
                              "id": "dc60d963-4b5f-4a96-928c-8440b4657138",
                              "slug": "zero-title-4",
                              "name": "Zero title 4",
                              "shortDescription": "Missing short description",
                              "logoUrl": null,
                              "visibility": "PUBLIC"
                            },
                            {
                              "id": "7d04163c-4187-4313-8066-61504d34fc56",
                              "slug": "bretzel",
                              "name": "Bretzel",
                              "shortDescription": "A project for people who love fruits",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png",
                              "visibility": "PUBLIC"
                            },
                            {
                              "id": "f39b827f-df73-498c-8853-99bc3f562723",
                              "slug": "qa-new-contributions",
                              "name": "QA new contributions",
                              "shortDescription": "QA new contributions",
                              "logoUrl": null,
                              "visibility": "PUBLIC"
                            },
                            {
                              "id": "27ca7e18-9e71-468f-8825-c64fe6b79d66",
                              "slug": "b-conseil",
                              "name": "B Conseil",
                              "shortDescription": "Nous sommes B.Conseil, la bonne gestion du Crédit d’Impôt Recherche.",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11012050846615405488.png",
                              "visibility": "PRIVATE"
                            }
                          ],
                          "repos": [
                            {
                              "id": 466482535,
                              "owner": "gregcha",
                              "name": "bretzel-ressources",
                              "htmlUrl": null
                            },
                            {
                              "id": 480776993,
                              "owner": "onlydustxyz",
                              "name": "starklings",
                              "htmlUrl": null
                            },
                            {
                              "id": 493591124,
                              "owner": "onlydustxyz",
                              "name": "kaaper",
                              "htmlUrl": null
                            },
                            {
                              "id": 498695724,
                              "owner": "onlydustxyz",
                              "name": "marketplace-frontend",
                              "htmlUrl": null
                            },
                            {
                              "id": 593701982,
                              "owner": "onlydustxyz",
                              "name": "gateway",
                              "htmlUrl": null
                            },
                            {
                              "id": 602953043,
                              "owner": "od-mocks",
                              "name": "cool-repo-A",
                              "htmlUrl": null
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 1410,
                          "totalItemNumber": 4230,
                          "nextPageIndex": 1
                        }
                        """);
    }

    @Test
    void should_get_my_rewards_with_project_filter() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(ME_GET_CONTRIBUTIONS, Map.of(
                        "projects", "f39b827f-df73-498c-8853-99bc3f562723,594ca5ca-48f7-49a8-9c26-84b949d4fdd9")
                ))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .jsonPath("$.contributions.length()").isEqualTo(50)
                .jsonPath("$.projects.length()").isEqualTo(2)
                .jsonPath("$.projects[?(@.id=='%s')]".formatted("594ca5ca-48f7-49a8-9c26-84b949d4fdd9")).exists()
                .jsonPath("$.projects[?(@.id=='%s')]".formatted("f39b827f-df73-498c-8853-99bc3f562723")).exists()
                .jsonPath("$.repos.length()").isEqualTo(2)
                .jsonPath("$.repos[?(@.id==%d)]".formatted(498695724)).exists()
                .jsonPath("$.repos[?(@.id==%d)]".formatted(593701982)).exists()
                .jsonPath("$.hasMore").isEqualTo(true)
                .jsonPath("$.totalPageNumber").isEqualTo(42)
                .jsonPath("$.totalItemNumber").isEqualTo(2083)
                .jsonPath("$.nextPageIndex").isEqualTo(1)
        ;
    }

    @Test
    void should_get_my_rewards_with_repos_filter() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(ME_GET_CONTRIBUTIONS, Map.of(
                        "repositories", "493591124")
                ))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.contributions.length()").isEqualTo(18)
                .jsonPath("$.projects.length()").isEqualTo(1)
                .jsonPath("$.projects[0].id").isEqualTo("298a547f-ecb6-4ab2-8975-68f4e9bf7b39")
                .jsonPath("$.repos.length()").isEqualTo(1)
                .jsonPath("$.repos[0].id").isEqualTo(493591124)
                .jsonPath("$.hasMore").isEqualTo(false)
                .jsonPath("$.totalPageNumber").isEqualTo(1)
                .jsonPath("$.totalItemNumber").isEqualTo(18)
                .jsonPath("$.nextPageIndex").isEqualTo(0)
        ;
    }

    @Test
    void should_get_my_rewards_with_type_filter() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(ME_GET_CONTRIBUTIONS, Map.of(
                        "types", "ISSUE")
                ))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.contributions.length()").isEqualTo(50)
                .jsonPath("$.contributions[0].type").isEqualTo("ISSUE")
                .jsonPath("$.hasMore").isEqualTo(true)
                .jsonPath("$.totalPageNumber").isEqualTo(2)
                .jsonPath("$.totalItemNumber").isEqualTo(52)
                .jsonPath("$.nextPageIndex").isEqualTo(1)
        ;
    }

    @Test
    void should_get_my_rewards_with_status_filter() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(ME_GET_CONTRIBUTIONS, Map.of(
                        "statuses", "IN_PROGRESS")
                ))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .jsonPath("$.contributions.length()").isEqualTo(50)
                .jsonPath("$.contributions[0].status").isEqualTo("IN_PROGRESS")
                .jsonPath("$.hasMore").isEqualTo(true)
                .jsonPath("$.totalPageNumber").isEqualTo(11)
                .jsonPath("$.totalItemNumber").isEqualTo(541)
                .jsonPath("$.nextPageIndex").isEqualTo(1)
        ;
    }

    @Test
    void should_get_list_rewards_associated_to_a_contribution() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(ME_GET_CONTRIBUTIONS,
                        Map.of("pageSize", "1",
                                "page", "17",
                                "projects", "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                "repositories", "498695724",
                                "statuses", "COMPLETED",
                                "types", "PULL_REQUEST")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .consumeWith(System.out::println)
                .json("""
                        {
                          "contributions": [
                            {
                              "rewardIds": [
                                "6587511b-3791-47c6-8430-8f793606c63a",
                                "0b275f04-bdb1-4d4f-8cd1-76fe135ccbdf",
                                "335e45a5-7f59-4519-8a12-1addc530214c",
                                "e9ebbe59-fb74-4a6c-9a51-6d9050412977",
                                "e33ea956-d2f5-496b-acf9-e2350faddb16",
                                "dd7d445f-6915-4955-9bae-078173627b05",
                                "d22f75ab-d9f5-4dc6-9a85-60dcd7452028",
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
        final String jwt = userHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(ME_GET_CONTRIBUTIONS,
                        Map.of("pageSize", "1",
                                "projects", "298a547f-ecb6-4ab2-8975-68f4e9bf7b39,594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                                "sort", "PROJECT_REPO_NAME"
                        )))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .jsonPath("$.contributions[0].projectName").isEqualTo("Mooooooonlight")
                .jsonPath("$.contributions[0].repoName").isEqualTo("gateway")
        ;

        // When
        client.get()
                .uri(getApiURI(ME_GET_CONTRIBUTIONS,
                        Map.of("pageSize", "1",
                                "projects", "298a547f-ecb6-4ab2-8975-68f4e9bf7b39,594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                                "sort", "PROJECT_REPO_NAME",
                                "direction", "DESC")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .jsonPath("$.contributions[0].projectName").isEqualTo("kaaper")
                .jsonPath("$.contributions[0].repoName").isEqualTo("marketplace-frontend")
        ;
    }

    @Test
    void should_order_by_github_number_title() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(ME_GET_CONTRIBUTIONS,
                        Map.of("pageSize", "1",
                                "projects", "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                "sort", "GITHUB_NUMBER_TITLE"
                        )))
                .header("Authorization", BEARER_PREFIX + jwt)
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
                .uri(getApiURI(ME_GET_CONTRIBUTIONS,
                        Map.of("pageSize", "1",
                                "projects", "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                "sort", "GITHUB_NUMBER_TITLE",
                                "direction", "DESC")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .jsonPath("$.contributions[0].githubNumber").isEqualTo("1318")
                .jsonPath("$.contributions[0].githubTitle").isEqualTo("This is a test PR for development purposes")
        ;
    }
}
