package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectCategoryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectProjectCategoryEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectCategoryRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectCategorySuggestionRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class EcosystemReadApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    ProjectCategorySuggestionRepository projectCategorySuggestionRepository;
    @Autowired
    ProjectCategoryRepository projectCategoryRepository;
    @Autowired
    ProjectRepository projectRepository;

    @BeforeEach
    void setUp() {
        final var categoryAI = new ProjectCategoryEntity(UUID.fromString("b151c7e4-1493-4927-bb0f-8647ec98a9c5"), "AI", "brain");
        final var categorySecurity = new ProjectCategoryEntity(UUID.fromString("7a1c0dcb-2079-487c-adaa-88d425bf13ea"), "Security", "lock");
        projectCategoryRepository.saveAll(List.of(
                categorySecurity,
                categoryAI,
                new ProjectCategoryEntity(UUID.fromString("d847060c-490c-482b-a3be-e48f93506b5d"), "Foo", "bar")
        ));
        final var project = projectRepository.findById(UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56")).get();
        project.setCategories(Set.of(new ProjectProjectCategoryEntity(project.getId(), categoryAI.getId()),
                new ProjectProjectCategoryEntity(project.getId(), categorySecurity.getId())));
        projectRepository.save(project);
    }

    @Test
    void should_list_ecosystems() {
        // When
        client.get()
                .uri(getApiURI(V2_ECOSYSTEMS, "hidden", "false"))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 4,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "ecosystems": [
                            {
                              "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                              "slug": "starknet",
                              "name": "Starknet",
                              "description": "Starknet ecosystem",
                              "banners": {
                                "xl": {
                                  "url": "https://s3.amazonaws.com/onlydust/ecosystem_banners/starknet-xl.png",
                                  "fontColor": "DARK"
                                },
                                "md": {
                                  "url": "https://s3.amazonaws.com/onlydust/ecosystem_banners/starknet-md.png",
                                  "fontColor": "DARK"
                                }
                              },
                              "topProjects": [
                                {
                                  "id": "27ca7e18-9e71-468f-8825-c64fe6b79d66",
                                  "slug": "b-conseil",
                                  "name": "B Conseil",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11012050846615405488.png"
                                },
                                {
                                  "id": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                                  "slug": "mooooooonlight",
                                  "name": "Mooooooonlight",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1913921207486176664.jpg"
                                },
                                {
                                  "id": "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e",
                                  "slug": "calcom",
                                  "name": "Cal.com",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5271998260751715005.png"
                                }
                              ],
                              "projectCount": 4,
                              "topProjectCategories": [],
                              "projectCategoryCount": 0
                            },
                            {
                              "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                              "slug": "aztec",
                              "name": "Aztec",
                              "description": "Aztec ecosystem",
                              "banners": {
                                "xl": {
                                  "url": "https://s3.amazonaws.com/onlydust/ecosystem_banners/aztec-xl.png",
                                  "fontColor": "DARK"
                                },
                                "md": {
                                  "url": "https://s3.amazonaws.com/onlydust/ecosystem_banners/aztec-md.png",
                                  "fontColor": "DARK"
                                }
                              },
                              "topProjects": [
                                {
                                  "id": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                                  "slug": "mooooooonlight",
                                  "name": "Mooooooonlight",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1913921207486176664.jpg"
                                }
                              ],
                              "projectCount": 1,
                              "topProjectCategories": [],
                              "projectCategoryCount": 0
                            },
                            {
                              "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                              "slug": "ethereum",
                              "name": "Ethereum",
                              "description": "Ethereum ecosystem",
                              "banners": {
                                "xl": {
                                  "url": "https://s3.amazonaws.com/onlydust/ecosystem_banners/ethereum-xl.png",
                                  "fontColor": "LIGHT"
                                },
                                "md": {
                                  "url": "https://s3.amazonaws.com/onlydust/ecosystem_banners/ethereum-md.png",
                                  "fontColor": "LIGHT"
                                }
                              },
                              "topProjects": [
                                {
                                  "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                  "slug": "bretzel",
                                  "name": "Bretzel",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                                }
                              ],
                              "projectCount": 1,
                              "topProjectCategories": [
                                {
                                  "id": "7a1c0dcb-2079-487c-adaa-88d425bf13ea",
                                  "name": "Security",
                                  "iconSlug": "lock"
                                },
                                {
                                  "id": "b151c7e4-1493-4927-bb0f-8647ec98a9c5",
                                  "name": "AI",
                                  "iconSlug": "brain"
                                }
                              ],
                              "projectCategoryCount": 2
                            },
                            {
                              "id": "dd6f737e-2a9d-40b9-be62-8f64ec157989",
                              "slug": "optimism",
                              "name": "Optimism",
                              "description": "Optimism ecosystem",
                              "banners": {
                                "xl": {
                                  "url": "https://s3.amazonaws.com/onlydust/ecosystem_banners/optimism-xl.png",
                                  "fontColor": "DARK"
                                },
                                "md": {
                                  "url": "https://s3.amazonaws.com/onlydust/ecosystem_banners/optimism-md.png",
                                  "fontColor": "DARK"
                                }
                              },
                              "topProjects": [
                                {
                                  "id": "27ca7e18-9e71-468f-8825-c64fe6b79d66",
                                  "slug": "b-conseil",
                                  "name": "B Conseil",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11012050846615405488.png"
                                }
                              ],
                              "projectCount": 1,
                              "topProjectCategories": [],
                              "projectCategoryCount": 0
                            }
                          ]
                        }
                        """, true);
    }

    @Test
    void should_list_featured_ecosystems() {
        // When
        client.get()
                .uri(getApiURI(V2_ECOSYSTEMS, Map.of("featured", "true", "hidden", "false")))
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "ecosystems": [
                            {
                              "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                              "slug": "starknet",
                              "name": "Starknet",
                              "description": "Starknet ecosystem",
                              "banners": {
                                "xl": {
                                  "url": "https://s3.amazonaws.com/onlydust/ecosystem_banners/starknet-xl.png",
                                  "fontColor": "DARK"
                                },
                                "md": {
                                  "url": "https://s3.amazonaws.com/onlydust/ecosystem_banners/starknet-md.png",
                                  "fontColor": "DARK"
                                }
                              },
                              "topProjects": [
                                {
                                  "id": "27ca7e18-9e71-468f-8825-c64fe6b79d66",
                                  "slug": "b-conseil",
                                  "name": "B Conseil",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11012050846615405488.png"
                                },
                                {
                                  "id": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                                  "slug": "mooooooonlight",
                                  "name": "Mooooooonlight",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1913921207486176664.jpg"
                                },
                                {
                                  "id": "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e",
                                  "slug": "calcom",
                                  "name": "Cal.com",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5271998260751715005.png"
                                }
                              ],
                              "projectCount": 4,
                              "topProjectCategories": [],
                              "projectCategoryCount": 0
                            },
                            {
                              "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                              "slug": "ethereum",
                              "name": "Ethereum",
                              "description": "Ethereum ecosystem",
                              "banners": {
                                "xl": {
                                  "url": "https://s3.amazonaws.com/onlydust/ecosystem_banners/ethereum-xl.png",
                                  "fontColor": "LIGHT"
                                },
                                "md": {
                                  "url": "https://s3.amazonaws.com/onlydust/ecosystem_banners/ethereum-md.png",
                                  "fontColor": "LIGHT"
                                }
                              },
                              "topProjects": [
                                {
                                  "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                  "slug": "bretzel",
                                  "name": "Bretzel",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                                }
                              ],
                              "projectCount": 1,
                              "topProjectCategories": [
                                {
                                  "id": "7a1c0dcb-2079-487c-adaa-88d425bf13ea",
                                  "name": "Security",
                                  "iconSlug": "lock"
                                },
                                {
                                  "id": "b151c7e4-1493-4927-bb0f-8647ec98a9c5",
                                  "name": "AI",
                                  "iconSlug": "brain"
                                }
                              ],
                              "projectCategoryCount": 2
                            }
                          ]
                        }
                        """, true);
    }

    @Test
    void should_get_ecosystem_by_slug() {
        // When
        client.get()
                .uri(getApiURI(ECOSYSTEM_BY_SLUG.formatted("ethereum")))
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                          "slug": "ethereum",
                          "name": "Ethereum",
                          "description": "Ethereum ecosystem",
                          "banners": {
                            "xl": {
                              "url": "https://s3.amazonaws.com/onlydust/ecosystem_banners/ethereum-xl.png",
                              "fontColor": "LIGHT"
                            },
                            "md": {
                              "url": "https://s3.amazonaws.com/onlydust/ecosystem_banners/ethereum-md.png",
                              "fontColor": "LIGHT"
                            }
                          },
                          "relatedArticles": [
                            {
                              "title": "Ethereum article 1",
                              "url": "https://ethereum.org/",
                              "imageUrl": "https://s3.amazonaws.com/onlydust/ecosystem_articles/ethereum-article-1.png",
                              "description": "Ethereum article 1 description"
                            },
                            {
                              "title": "Ethereum article 2",
                              "url": "https://ethereum.org/",
                              "imageUrl": "https://s3.amazonaws.com/onlydust/ecosystem_articles/ethereum-article-2.png",
                              "description": "Ethereum article 2 description"
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_get_ecosystem_languages() {
        // When
        client.get()
                .uri(getApiURI(ECOSYSTEM_LANGUAGES.formatted("ethereum")))
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                           "totalPageNumber": 1,
                           "totalItemNumber": 1,
                           "hasMore": false,
                           "nextPageIndex": 0,
                           "languages": [
                             {
                               "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                               "name": "Typescript",
                               "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                               "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                             }
                           ]
                         }
                        """);
    }

    @Test
    void should_get_ecosystem_project_categories() {
        // When
        client.get()
                .uri(getApiURI(ECOSYSTEM_PROJECT_CATEGORIES.formatted("ethereum")))
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "projectCategories": [
                            {
                              "id": "7a1c0dcb-2079-487c-adaa-88d425bf13ea",
                              "name": "Security",
                              "iconSlug": "lock"
                            },
                            {
                              "id": "b151c7e4-1493-4927-bb0f-8647ec98a9c5",
                              "name": "AI",
                              "iconSlug": "brain"
                            }
                          ]
                        }
                        """);
    }
}
