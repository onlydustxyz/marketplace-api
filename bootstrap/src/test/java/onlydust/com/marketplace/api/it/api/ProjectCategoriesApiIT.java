package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.project.domain.model.ProjectCategory;
import onlydust.com.marketplace.project.domain.port.output.ProjectCategoryStoragePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@TagProject
public class ProjectCategoriesApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    private ProjectCategoryStoragePort projectCategoryStoragePort;

    @Test
    void should_create_project_category() {
        // Given
        projectCategoryStoragePort.save(ProjectCategory.of("Game", "Games are fun", "game"));
        projectCategoryStoragePort.save(ProjectCategory.of("Tutorial", "I love learning", "tuto"));

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
                              "description": "Games are fun",
                              "slug": "game",
                              "iconSlug": "game"
                            },
                            {
                              "name": "Tutorial",
                              "description": "I love learning",
                              "slug": "tutorial",
                              "iconSlug": "tuto"
                            }
                          ]
                        }
                        """);
    }
}
