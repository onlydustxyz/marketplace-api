package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.project.domain.model.ProjectCategory;
import onlydust.com.marketplace.project.domain.port.output.ProjectCategoryStoragePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ProjectCategoriesApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    private ProjectCategoryStoragePort projectCategoryStoragePort;

    @Test
    void should_create_project_category() {
        // Given
        projectCategoryStoragePort.save(ProjectCategory.of("Game", "game"));
        projectCategoryStoragePort.save(ProjectCategory.of("Tutorial", "tuto"));

        // When
        client.get()
                .uri(PROJECT_CATEGORIES)
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "categories": [
                            {
                              "name": "Game",
                              "iconSlug": "game"
                            },
                            {
                              "name": "Tutorial",
                              "iconSlug": "tuto"
                            }
                          ]
                        }
                        """);
    }
}