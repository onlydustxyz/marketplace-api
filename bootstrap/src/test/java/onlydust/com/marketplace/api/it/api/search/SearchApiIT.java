package onlydust.com.marketplace.api.it.api.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.onlydust.marketplace.indexer.SearchIndexationService;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.it.api.AbstractMarketplaceApiIT;
import onlydust.com.marketplace.api.suites.tags.TagSearch;
import onlydust.com.marketplace.kernel.model.EcosystemId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.Language;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TagSearch
@Slf4j
public class SearchApiIT extends AbstractMarketplaceApiIT {

    private static ElasticsearchContainer elasticsearchContainer;
    private static String elasticsearchHost;

    @BeforeAll
    public static void startElasticsearchContainer() {
        elasticsearchContainer = new ElasticsearchContainer(
                "docker.elastic.co/elasticsearch/elasticsearch:8.1.2")
                .withEnv("xpack.security.enabled", "false")
                .withEnv("discovery.type", "single-node");
        elasticsearchContainer.start();
        elasticsearchHost = "http://" + elasticsearchContainer.getHttpHostAddress();
    }

    @AfterAll
    public static void stopElasticsearchContainer() {
        elasticsearchContainer.stop();
    }

    @DynamicPropertySource
    static void updateProperties(DynamicPropertyRegistry registry) {
        registry.add("infrastructure.elasticsearch.base-uri", () -> elasticsearchHost);
    }

    @Autowired
    SearchIndexationService searchIndexationService;

    final WebTestClient elasticSearchWebTestClient = WebTestClient.bindToServer()
            .baseUrl(elasticsearchHost)
            .build();

    @Test
    @Order(1)
    void should_index_all_projects() throws InterruptedException {
        // Given
        setupProjectsWithData();

        // When
        searchIndexationService.indexAllProjects();

        // Then
        Thread.sleep(3000);
        elasticSearchWebTestClient.get()
                .uri("/od-projects/_search")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.hits.total.value").isEqualTo(74);
    }

    @Test
    @Order(2)
    void should_index_all_contributors() throws InterruptedException {
        // When
        searchIndexationService.indexAllContributors();

        // Then
        boolean isIndexationCompleted = false;
        for (int i = 0; i < 20; i++) {
            Thread.sleep(3000);
            final JsonNode responseBody = elasticSearchWebTestClient.post()
                    .uri("/od-contributors/_search")
                    .contentType(MediaType.APPLICATION_JSON)
                    // "track_total_hits": true to bypass 10 000 total limitation
                    .bodyValue("""
                            {
                             "track_total_hits": true
                            }
                            """)
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody(JsonNode.class)
                    .returnResult().getResponseBody();
            final int totalIndexed = responseBody.get("hits").get("total").get("value").asInt();
            LOGGER.info("{} contributors are indexed", totalIndexed);
            if (totalIndexed == 23964) {
                isIndexationCompleted = true;
                break;
            }
        }
        assertTrue(isIndexationCompleted);
    }


    @Test
    @Order(10)
    void should_search_projects() {
        // Given
        final String keyword = "Bretzel";

        // When
        client.post()
                .uri(getApiURI(POST_SEARCH))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "keyword": "%s",
                          "pageSize": 10,
                          "pageIndex": 0,
                          "type": "PROJECT"
                        }
                        """.formatted(keyword))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "results": [
                            {
                              "type": "PROJECT",
                              "project": {
                                "name": "Bretzel 196",
                                "slug": "bretzel-196",
                                "id": "247ac542-762d-44cb-b8d4-4d6199c916be",
                                "shortDescription": "bretzel gives you wings",
                                "contributorCount": 0,
                                "starCount": 0,
                                "forkCount": 0,
                                "languages": null,
                                "categories": null,
                                "ecosystems": null
                              },
                              "contributor": null
                            },
                            {
                              "type": "PROJECT",
                              "project": {
                                "name": "Bretzel",
                                "slug": "bretzel",
                                "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                "shortDescription": "A project for people who love fruits",
                                "contributorCount": 6,
                                "starCount": 0,
                                "forkCount": 1,
                                "languages": [
                                  "Typescript"
                                ],
                                "categories": null,
                                "ecosystems": [
                                  "Ethereum",
                                  "Aptos",
                                  "Zama"
                                ]
                              },
                              "contributor": null
                            }
                          ],
                          "projectFacets": {
                            "ecosystems": [
                              {
                                "name": "Aptos",
                                "count": 1
                              },
                              {
                                "name": "Ethereum",
                                "count": 1
                              },
                              {
                                "name": "Zama",
                                "count": 1
                              }
                            ],
                            "categories": [],
                            "languages": [
                              {
                                "name": "Typescript",
                                "count": 1
                              }
                            ]
                          },
                          "typeFacets": {
                            "types": null
                          }
                        }
                        """);

        // When
        client.post()
                .uri(getApiURI(POST_SEARCH))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "keyword": "a",
                          "pageSize": 10,
                          "pageIndex": 0,
                          "type": "PROJECT",
                          "categories": ["AI"]
                        }
                        """.formatted(keyword))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                            "totalPageNumber": 1,
                            "totalItemNumber": 3,
                            "hasMore": false,
                            "nextPageIndex": 0,
                            "results": [
                              {
                                "type": "PROJECT",
                                "project": {
                                  "name": "Marketplace",
                                  "slug": "marketplace",
                                  "id": "45ca43d6-130e-4bf7-9776-2b1eb1dcb782",
                                  "shortDescription": "Our marketplace",
                                  "contributorCount": 0,
                                  "languages": null,
                                  "categories": [
                                    "AI"
                                  ],
                                  "ecosystems": null
                                },
                                "contributor": null
                              },
                              {
                                "type": "PROJECT",
                                "project": {
                                  "name": "Red bull",
                                  "slug": "red-bull",
                                  "id": "c6940f66-d64e-4b29-9a7f-07abf5c3e0ed",
                                  "shortDescription": "Red bull gives you wings!",
                                  "contributorCount": 0,
                                  "languages": null,
                                  "categories": [
                                    "AI"
                                  ],
                                  "ecosystems": null
                                },
                                "contributor": null
                              },
                              {
                                "type": "PROJECT",
                                "project": {
                                  "name": "Watermelon",
                                  "slug": "watermelon",
                                  "id": "fd10776c-3e09-45f0-998b-8537992a3726",
                                  "shortDescription": "A projects for those who love water and melon",
                                  "contributorCount": 0,
                                  "languages": null,
                                  "categories": [
                                    "AI"
                                  ],
                                  "ecosystems": null
                                },
                                "contributor": null
                              }
                            ],
                            "projectFacets": {
                              "ecosystems": [],
                              "categories": [
                                {
                                  "name": "AI",
                                  "count": 3
                                }
                              ],
                              "languages": []
                            },
                            "typeFacets": {
                              "types": null
                            }
                          }""");


    }

    @Test
    @Order(20)
    void should_search_contributors() {
        // Given
        final String keyword = "pierre";

        // When
        client.post()
                .uri(getApiURI(POST_SEARCH))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "keyword": "%s",
                          "pageSize": 10,
                          "pageIndex": 0,
                          "type": "CONTRIBUTOR"
                        }
                        """.formatted(keyword))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                            "totalPageNumber": 1,
                            "totalItemNumber": 6,
                            "hasMore": false,
                            "nextPageIndex": 0,
                            "results": [
                              {
                                "type": "CONTRIBUTOR",
                                "project": null,
                                "contributor": {
                                  "githubLogin": "pinonpierre",
                                  "githubId": 4507910,
                                  "htmlUrl": "https://github.com/pinonpierre",
                                  "bio": null,
                                  "contributionCount": 0,
                                  "projectCount": 0,
                                  "pullRequestCount": 0,
                                  "issueCount": 0
                                }
                              },
                              {
                                "type": "CONTRIBUTOR",
                                "project": null,
                                "contributor": {
                                  "githubLogin": "carllapierre",
                                  "githubId": 10599421,
                                  "htmlUrl": "https://github.com/carllapierre",
                                  "bio": "Software Development Lead @Osedea ",
                                  "contributionCount": 0,
                                  "projectCount": 1,
                                  "pullRequestCount": 0,
                                  "issueCount": 0
                                }
                              },
                              {
                                "type": "CONTRIBUTOR",
                                "project": null,
                                "contributor": {
                                  "githubLogin": "PierreOucif",
                                  "githubId": 16590657,
                                  "htmlUrl": "https://github.com/PierreOucif",
                                  "bio": null,
                                  "contributionCount": 314,
                                  "projectCount": 6,
                                  "pullRequestCount": 121,
                                  "issueCount": 2
                                }
                              },
                              {
                                "type": "CONTRIBUTOR",
                                "project": null,
                                "contributor": {
                                  "githubLogin": "lemoinepierre",
                                  "githubId": 57217210,
                                  "htmlUrl": "https://github.com/lemoinepierre",
                                  "bio": null,
                                  "contributionCount": 0,
                                  "projectCount": 0,
                                  "pullRequestCount": 0,
                                  "issueCount": 0
                                }
                              },
                              {
                                "type": "CONTRIBUTOR",
                                "project": null,
                                "contributor": {
                                  "githubLogin": "pierrejn-git",
                                  "githubId": 57374061,
                                  "htmlUrl": "https://github.com/pierrejn-git",
                                  "bio": null,
                                  "contributionCount": 0,
                                  "projectCount": 0,
                                  "pullRequestCount": 0,
                                  "issueCount": 0
                                }
                              },
                              {
                                "type": "CONTRIBUTOR",
                                "project": null,
                                "contributor": {
                                  "githubLogin": "PierreBastiani",
                                  "githubId": 59787523,
                                  "htmlUrl": "https://github.com/PierreBastiani",
                                  "bio": null,
                                  "contributionCount": 0,
                                  "projectCount": 1,
                                  "pullRequestCount": 0,
                                  "issueCount": 0
                                }
                              }
                            ],
                            "projectFacets": null,
                            "typeFacets": null
                          }
                        """);
    }

    @Test
    @Order(30)
    void should_suggest() {
        // When
        client.post()
                .uri(getApiURI(POST_SUGGEST))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "keyword": "bre",
                          "type": "PROJECT"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.value").value(o -> assertTrue(o.toString().toLowerCase().startsWith("bre")));

        // When
        client.post()
                .uri(getApiURI(POST_SUGGEST))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "keyword": "p",
                          "type": "CONTRIBUTOR"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.value").value(o -> assertTrue(o.toString().toLowerCase().startsWith("p")));

        // When
        client.post()
                .uri(getApiURI(POST_SUGGEST))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "keyword": "o"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.value").value(o -> assertTrue(o.toString().toLowerCase().startsWith("o")));
    }


    private void setupProjectsWithData() {
        // Add ecosystems to projects
        projectHelper.addEcosystem(ProjectId.of(UUID.fromString("b195f94e-74a2-4073-9ad8-03aba6372b56")),
                EcosystemId.of(UUID.fromString("9f82bdb4-22c2-455a-91a8-e3c7d96c47d7")));
        projectHelper.addEcosystem(ProjectId.of(UUID.fromString("8156fc5f-cec5-4f70-a0de-c368772edcd4")),
                EcosystemId.of(UUID.fromString("397df411-045d-4d9f-8d65-8284c88f9208")));
        projectHelper.addEcosystem(ProjectId.of(UUID.fromString("7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54")),
                EcosystemId.of(UUID.fromString("ed314d31-f5f2-40e5-9cfc-a962b35c572e")));
        projectHelper.addEcosystem(ProjectId.of(UUID.fromString("61ef7d3a-81a2-4baf-bdb0-e7ae5e165d17")),
                EcosystemId.of(UUID.fromString("6ab7fa6c-c418-4997-9c5f-55fb021a8e5c")));
        projectHelper.addEcosystem(ProjectId.of(UUID.fromString("6d955622-c1ce-4227-85ea-51cb1b3207b1")),
                EcosystemId.of(UUID.fromString("f7821bfb-df73-464c-9d87-a94dfb4f5aef")));
        projectHelper.addEcosystem(ProjectId.of(UUID.fromString("c66b929a-664d-40b9-96c4-90d3efd32a3c")),
                EcosystemId.of(UUID.fromString("dd6f737e-2a9d-40b9-be62-8f64ec157989")));
        projectHelper.addEcosystem(ProjectId.of(UUID.fromString("29cdf359-f60c-41a0-8b11-18d6841311f6")),
                EcosystemId.of(UUID.fromString("99b6c284-f9bb-4f89-8ce7-03771465ef8e")));
        projectHelper.addEcosystem(ProjectId.of(UUID.fromString("dcb3548a-977a-480e-8fb4-423d3f890c04")),
                EcosystemId.of(UUID.fromString("b599313c-a074-440f-af04-a466529ab2e7")));
        projectHelper.addEcosystem(ProjectId.of(UUID.fromString("4f33a304-b4e9-42a2-a041-d0e359ede3bc")),
                EcosystemId.of(UUID.fromString("9f82bdb4-22c2-455a-91a8-e3c7d96c47d7")));
        projectHelper.addEcosystem(ProjectId.of(UUID.fromString("d4e8ab3b-a4a8-493d-83bd-a4c8283b94f9")),
                EcosystemId.of(UUID.fromString("397df411-045d-4d9f-8d65-8284c88f9208")));
        projectHelper.addEcosystem(ProjectId.of(UUID.fromString("02a533f5-6cbb-4cb6-90fe-f6bee220443c")),
                EcosystemId.of(UUID.fromString("ed314d31-f5f2-40e5-9cfc-a962b35c572e")));

        githubHelper.createRepo("repo1", ProjectId.of(UUID.fromString("45ca43d6-130e-4bf7-9776-2b1eb1dcb782")));
        projectHelper.addLanguages(ProjectId.of(UUID.fromString("45ca43d6-130e-4bf7-9776-2b1eb1dcb782")), List.of(
                Language.Id.of(UUID.fromString("ca600cac-0f45-44e9-a6e8-25e21b0c6887")),
                Language.Id.of(UUID.fromString("f57d0866-89f3-4613-aaa2-32f4f4ecc972")),
                Language.Id.of(UUID.fromString("69eba92e-104c-4d3e-8721-ad6a5fa5ea5a")),
                Language.Id.of(UUID.fromString("1109d0a2-1143-4915-a9c1-69e8be6c1bea")),
                Language.Id.of(UUID.fromString("e1842c39-fcfa-4289-9b5e-61bf50386a72")),
                Language.Id.of(UUID.fromString("c83881b3-5aef-4819-9596-fdbbbedf2b0b")),
                Language.Id.of(UUID.fromString("7ddd9417-4cf1-4c08-8040-9380dc6889e2")),
                Language.Id.of(UUID.fromString("6b3f8a21-8ae9-4f73-81df-06aeaddbaf42"))
        ));

        githubHelper.createRepo("repo2", ProjectId.of(UUID.fromString("ade75c25-b39f-4fdf-a03a-e2391c1bc371")));
        projectHelper.addLanguages(ProjectId.of(UUID.fromString("ade75c25-b39f-4fdf-a03a-e2391c1bc371")), List.of(
                Language.Id.of(UUID.fromString("ca600cac-0f45-44e9-a6e8-25e21b0c6887")),
                Language.Id.of(UUID.fromString("f57d0866-89f3-4613-aaa2-32f4f4ecc972")),
                Language.Id.of(UUID.fromString("69eba92e-104c-4d3e-8721-ad6a5fa5ea5a")),
                Language.Id.of(UUID.fromString("c83881b3-5aef-4819-9596-fdbbbedf2b0b")),
                Language.Id.of(UUID.fromString("7ddd9417-4cf1-4c08-8040-9380dc6889e2")),
                Language.Id.of(UUID.fromString("6b3f8a21-8ae9-4f73-81df-06aeaddbaf42"))
        ));

        githubHelper.createRepo("repo3", ProjectId.of(UUID.fromString("166b6ed6-2d71-44f9-bbf1-89ce82ec3e43")));
        projectHelper.addLanguages(ProjectId.of(UUID.fromString("166b6ed6-2d71-44f9-bbf1-89ce82ec3e43")), List.of(
                Language.Id.of(UUID.fromString("ca600cac-0f45-44e9-a6e8-25e21b0c6887")),
                Language.Id.of(UUID.fromString("f57d0866-89f3-4613-aaa2-32f4f4ecc972")),
                Language.Id.of(UUID.fromString("e1842c39-fcfa-4289-9b5e-61bf50386a72")),
                Language.Id.of(UUID.fromString("c83881b3-5aef-4819-9596-fdbbbedf2b0b")),
                Language.Id.of(UUID.fromString("7ddd9417-4cf1-4c08-8040-9380dc6889e2"))
        ));

        githubHelper.createRepo("repo4", ProjectId.of(UUID.fromString("c6940f66-d64e-4b29-9a7f-07abf5c3e0ed")));
        projectHelper.addLanguages(ProjectId.of(UUID.fromString("c6940f66-d64e-4b29-9a7f-07abf5c3e0ed")), List.of(
                Language.Id.of(UUID.fromString("ca600cac-0f45-44e9-a6e8-25e21b0c6887")),
                Language.Id.of(UUID.fromString("f57d0866-89f3-4613-aaa2-32f4f4ecc972")),
                Language.Id.of(UUID.fromString("69eba92e-104c-4d3e-8721-ad6a5fa5ea5a")),
                Language.Id.of(UUID.fromString("e1842c39-fcfa-4289-9b5e-61bf50386a72")),
                Language.Id.of(UUID.fromString("7ddd9417-4cf1-4c08-8040-9380dc6889e2"))
        ));

        githubHelper.createRepo("repo5", ProjectId.of(UUID.fromString("ccf90dcf-a91b-42c6-b5ca-49d687b4401a")));
        projectHelper.addLanguages(ProjectId.of(UUID.fromString("ccf90dcf-a91b-42c6-b5ca-49d687b4401a")), List.of(
                Language.Id.of(UUID.fromString("ca600cac-0f45-44e9-a6e8-25e21b0c6887")),
                Language.Id.of(UUID.fromString("f57d0866-89f3-4613-aaa2-32f4f4ecc972"))
        ));

        githubHelper.createRepo("repo6", ProjectId.of(UUID.fromString("e4e9d711-5866-48b3-b2e0-14c48a2f9e12")));
        projectHelper.addLanguages(ProjectId.of(UUID.fromString("e4e9d711-5866-48b3-b2e0-14c48a2f9e12")), List.of(
                Language.Id.of(UUID.fromString("c83881b3-5aef-4819-9596-fdbbbedf2b0b")),
                Language.Id.of(UUID.fromString("7ddd9417-4cf1-4c08-8040-9380dc6889e2")),
                Language.Id.of(UUID.fromString("6b3f8a21-8ae9-4f73-81df-06aeaddbaf42"))
        ));

        githubHelper.createRepo("repo7", ProjectId.of(UUID.fromString("fd10776c-3e09-45f0-998b-8537992a3726")));
        projectHelper.addLanguages(ProjectId.of(UUID.fromString("fd10776c-3e09-45f0-998b-8537992a3726")), List.of(
                Language.Id.of(UUID.fromString("ca600cac-0f45-44e9-a6e8-25e21b0c6887")),
                Language.Id.of(UUID.fromString("f57d0866-89f3-4613-aaa2-32f4f4ecc972")),
                Language.Id.of(UUID.fromString("69eba92e-104c-4d3e-8721-ad6a5fa5ea5a")),
                Language.Id.of(UUID.fromString("1109d0a2-1143-4915-a9c1-69e8be6c1bea"))
        ));

        // Create 3 categories
        final var category1 = projectHelper.createCategory("AI");
        final var category2 = projectHelper.createCategory("Dev tools");
        final var category3 = projectHelper.createCategory("Finops");

        // Add categories to projects
        projectHelper.addCategory(ProjectId.of(UUID.fromString("45ca43d6-130e-4bf7-9776-2b1eb1dcb782")), category1.id());
        projectHelper.addCategory(ProjectId.of(UUID.fromString("ade75c25-b39f-4fdf-a03a-e2391c1bc371")), category2.id());
        projectHelper.addCategory(ProjectId.of(UUID.fromString("166b6ed6-2d71-44f9-bbf1-89ce82ec3e43")), category3.id());
        projectHelper.addCategory(ProjectId.of(UUID.fromString("c6940f66-d64e-4b29-9a7f-07abf5c3e0ed")), category1.id());
        projectHelper.addCategory(ProjectId.of(UUID.fromString("ccf90dcf-a91b-42c6-b5ca-49d687b4401a")), category2.id());
        projectHelper.addCategory(ProjectId.of(UUID.fromString("e4e9d711-5866-48b3-b2e0-14c48a2f9e12")), category3.id());
        projectHelper.addCategory(ProjectId.of(UUID.fromString("fd10776c-3e09-45f0-998b-8537992a3726")), category1.id());

    }
}
