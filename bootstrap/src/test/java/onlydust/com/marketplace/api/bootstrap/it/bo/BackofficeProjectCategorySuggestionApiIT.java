package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectCategoryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectCategorySuggestionEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectProjectCategoryEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectCategoryRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectCategorySuggestionRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BackofficeProjectCategorySuggestionApiIT extends AbstractMarketplaceBackOfficeApiIT {

    @Autowired
    ProjectCategorySuggestionRepository projectCategorySuggestionRepository;
    @Autowired
    ProjectCategoryRepository projectCategoryRepository;
    @Autowired
    ProjectRepository projectRepository;

    @BeforeEach
    void setUp() {
        projectCategorySuggestionRepository.saveAll(List.of(
                new ProjectCategorySuggestionEntity(UUID.fromString("fbb36293-1a5b-49c5-9cd0-6e33922d22ba"), "Gaming"),
                new ProjectCategorySuggestionEntity(UUID.fromString("d3af3bfc-5689-412a-8191-1466aa269830"), "DeFi"),
                new ProjectCategorySuggestionEntity(UUID.fromString("d3df4dbf-850e-42a5-af16-ca8a0278489c"), "Art")
        ));
        final var categoryAI = new ProjectCategoryEntity(UUID.fromString("b151c7e4-1493-4927-bb0f-8647ec98a9c5"), "ai", "AI", "brain");
        projectCategoryRepository.saveAll(List.of(
                new ProjectCategoryEntity(UUID.fromString("7a1c0dcb-2079-487c-adaa-88d425bf13ea"), "security", "Security", "lock"),
                new ProjectCategoryEntity(UUID.fromString("b1d059b7-f70e-4a9c-b522-28076bc59938"), "nft", "NFT", "paint"),
                categoryAI
        ));
        final var project = projectRepository.findById(UUID.fromString("7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54")).get();
        project.setCategories(Set.of(new ProjectProjectCategoryEntity(project.getId(), categoryAI.getId())));
        projectRepository.save(project);
    }

    @Test
    void should_get_project_category_suggestions() {
        // Given
        final var emilie = userAuthHelper.authenticateEmilie();

        // When
        client.get()
                .uri(PROJECT_CATEGORIES)
                .header("Authorization", "Bearer " + emilie.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 2,
                          "totalItemNumber": 6,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "categories": [
                            {
                              "id": "d3df4dbf-850e-42a5-af16-ca8a0278489c",
                              "slug": null,
                              "name": "Art",
                              "status": "PENDING",
                              "iconSlug": null,
                              "projectCount": null
                            },
                            {
                              "id": "d3af3bfc-5689-412a-8191-1466aa269830",
                              "slug": null,
                              "name": "DeFi",
                              "status": "PENDING",
                              "iconSlug": null,
                              "projectCount": null
                            },
                            {
                              "id": "fbb36293-1a5b-49c5-9cd0-6e33922d22ba",
                              "slug": null,
                              "name": "Gaming",
                              "status": "PENDING",
                              "iconSlug": null,
                              "projectCount": null
                            },
                            {
                              "id": "b151c7e4-1493-4927-bb0f-8647ec98a9c5",
                              "slug": "ai",
                              "name": "AI",
                              "status": "APPROVED",
                              "iconSlug": "brain",
                              "projectCount": 1
                            },
                            {
                              "id": "b1d059b7-f70e-4a9c-b522-28076bc59938",
                              "slug": "nft",
                              "name": "NFT",
                              "status": "APPROVED",
                              "iconSlug": "paint",
                              "projectCount": 0
                            }
                          ]
                        }
                        """, true);
    }


    @Test
    void should_delete_project_category_suggestion() {
        // Given
        final var emilie = userAuthHelper.authenticateEmilie();

        // When
        client.delete()
                .uri(PROJECT_CATEGORY_SUGGESTION.formatted("d3af3bfc-5689-412a-8191-1466aa269830"))
                .header("Authorization", "Bearer " + emilie.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        // When
        client.get()
                .uri(PROJECT_CATEGORIES)
                .header("Authorization", "Bearer " + emilie.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.totalItemNumber").isEqualTo(5)
                .jsonPath("$.categories.length()").isEqualTo(5)
                .jsonPath("$.categories[?(@.name == 'DeFi')]").doesNotExist();
    }
}
