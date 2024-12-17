package onlydust.com.marketplace.api.it.api;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import onlydust.com.marketplace.api.helper.DatabaseHelper;
import onlydust.com.marketplace.api.helper.EcosystemHelper;
import onlydust.com.marketplace.api.helper.ProjectHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectCategoryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectProjectCategoryEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectCategoryRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.bi.BiProjectGlobalDataRepository;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.kernel.model.EcosystemId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.Ecosystem;


@TagProject
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectsV2PageApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    ProjectCategoryRepository projectCategoryRepository;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    BiProjectGlobalDataRepository biProjectGlobalDataRepository;
    @Autowired
    DatabaseHelper databaseHelper;
    @Autowired
    ProjectHelper projectHelper;
    @Autowired
    EcosystemHelper ecosystemHelper;
    static Ecosystem chipotle;

    @BeforeEach
    void setUp() {
        final var categoryAI = new ProjectCategoryEntity(UUID.fromString("b151c7e4-1493-4927-bb0f-8647ec98a9c5"), "ai", "AI", "AI is cool", "brain", Set.of());
        projectCategoryRepository.saveAll(List.of(
                new ProjectCategoryEntity(UUID.fromString("7a1c0dcb-2079-487c-adaa-88d425bf13ea"), "security", "Security", "Security is important", "lock",
                        Set.of()),
                categoryAI
        ));
        final var project = projectRepository.findById(UUID.fromString("6239cb20-eece-466a-80a0-742c1071dd3c")).get();
        project.setCategories(Set.of(new ProjectProjectCategoryEntity(project.getId(), categoryAI.getId())));
        projectRepository.saveAndFlush(project);
        chipotle = ecosystemHelper.create("Chipotle");
        final var project2 = projectRepository.findById(UUID.fromString("1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e")).get();
        projectHelper.addEcosystem(ProjectId.of(project2.getId()), EcosystemId.of(chipotle.id()));
        databaseHelper.executeInTransaction(() -> biProjectGlobalDataRepository.refreshByProject(ProjectId.of(project.getId())));
        databaseHelper.executeInTransaction(() -> biProjectGlobalDataRepository.refreshByProject(ProjectId.of(project2.getId())));
    }


    @Test
    void should_get_projects_given_filters() {
        // When
        client.get()
                .uri(getApiURI(PROJECTS_V2_GET, Map.of("languageIds", "75ce6b37-8610-4600-8d2d-753b50aeda1e", "pageSize", "2", "pageIndex", "0")))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                // Then
                .json("""
                        {
                          "totalPageNumber": 5,
                          "totalItemNumber": 9,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "projects": [
                            {
                              "id": "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e",
                              "slug": null,
                              "name": "Cal.com",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5271998260751715005.png",
                              "shortDescription": "Scheduling infrastructure for everyone.",
                              "contributorCount": 43,
                              "starCount": 25285,
                              "forkCount": 5406,
                              "availableIssueCount": 28,
                              "goodFirstIssueCount": 4,
                              "categories": [],
                              "languages": [
                                {
                                  "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                                  "slug": "typescript",
                                  "name": "TypeScript",
                                  "percentage": 100.00,
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png",
                                  "color": "#3178C6",
                                  "transparentLogoUrl": "https://od-languages-develop.s3.eu-west-1.amazonaws.com/transparent/typescript.png"
                                }
                              ]
                            },
                            {
                              "id": "f39b827f-df73-498c-8853-99bc3f562723",
                              "slug": null,
                              "name": "QA new contributions",
                              "logoUrl": null,
                              "shortDescription": "QA new contributions",
                              "contributorCount": 0,
                              "starCount": 17,
                              "forkCount": 10,
                              "availableIssueCount": 0,
                              "goodFirstIssueCount": 0,
                              "categories": [],
                              "languages": [
                                {
                                  "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                                  "slug": "rust",
                                  "name": "Rust",
                                  "percentage": 0.00,
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png",
                                  "color": "#F74B00",
                                  "transparentLogoUrl": "https://od-languages-develop.s3.eu-west-1.amazonaws.com/transparent/rust.png"
                                },
                                {
                                  "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                                  "slug": "javascript",
                                  "name": "JavaScript",
                                  "percentage": 0.75,
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png",
                                  "color": "#F7DF1E",
                                  "transparentLogoUrl": "https://od-languages-develop.s3.eu-west-1.amazonaws.com/transparent/javascript.png"
                                },
                                {
                                  "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                                  "slug": "typescript",
                                  "name": "TypeScript",
                                  "percentage": 99.25,
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png",
                                  "color": "#3178C6",
                                  "transparentLogoUrl": "https://od-languages-develop.s3.eu-west-1.amazonaws.com/transparent/typescript.png"
                                }
                              ]
                            }
                          ]
                        }
                        
                        """, true);

        // When
        client.get()
                .uri(getApiURI(PROJECTS_V2_GET, Map.of("categoryIds", "b151c7e4-1493-4927-bb0f-8647ec98a9c5", "pageSize", "2", "pageIndex", "0")))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                // Then
                .json("""
                        {
                            "totalPageNumber": 1,
                            "totalItemNumber": 1,
                            "hasMore": false,
                            "nextPageIndex": 0,
                            "projects": [
                              {
                                "id": "6239cb20-eece-466a-80a0-742c1071dd3c",
                                "slug": null,
                                "name": "Starklings",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13746458086965388437.jpg",
                                "shortDescription": "Stop tuto",
                                "contributorCount": 0,
                                "starCount": 0,
                                "forkCount": 0,
                                "availableIssueCount": 0,
                                "goodFirstIssueCount": 0,
                                "categories": [
                                  {
                                    "id": "b151c7e4-1493-4927-bb0f-8647ec98a9c5",
                                    "slug": "ai",
                                    "name": "AI",
                                    "description": "AI is cool",
                                    "iconSlug": "brain",
                                    "projectCount": null
                                  }
                                ],
                                "languages": []
                              }
                            ]
                          }
                        """, true);

        // When
        client.get()
                .uri(getApiURI(PROJECTS_V2_GET, Map.of("ecosystemIds", chipotle.id().toString(), "pageSize", "2", "pageIndex", "0")))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                // Then
                .json("""
                        {
                           "totalPageNumber": 1,
                           "totalItemNumber": 1,
                           "hasMore": false,
                           "nextPageIndex": 0,
                           "projects": [
                             {
                               "id": "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e",
                               "slug": null,
                               "name": "Cal.com",
                               "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5271998260751715005.png",
                               "shortDescription": "Scheduling infrastructure for everyone.",
                               "contributorCount": 43,
                               "starCount": 25285,
                               "forkCount": 5406,
                               "availableIssueCount": 28,
                               "goodFirstIssueCount": 4,
                               "categories": [],
                               "languages": [
                                 {
                                   "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                                   "slug": "typescript",
                                   "name": "TypeScript",
                                   "percentage": 100.00,
                                   "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                                   "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png",
                                   "color": "#3178C6",
                                   "transparentLogoUrl": "https://od-languages-develop.s3.eu-west-1.amazonaws.com/transparent/typescript.png"
                                 }
                               ]
                             }
                           ]
                         }
                        """, true);


    }
}
