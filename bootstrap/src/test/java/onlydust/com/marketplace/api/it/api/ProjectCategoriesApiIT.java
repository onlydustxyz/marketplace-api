package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.project.domain.model.ProjectCategory;
import onlydust.com.marketplace.project.domain.port.output.ProjectCategoryStoragePort;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@TagProject
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectCategoriesApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    private ProjectCategoryStoragePort projectCategoryStoragePort;

    @Test
    @Order(1)
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

    @Test
    @Order(2)
    void should_search_project_categories_by_name() {
        // Given
        projectCategoryStoragePort.save(ProjectCategory.of("DeFi", "Web3 is the best", "defi"));
        projectCategoryStoragePort.save(ProjectCategory.of("Finance", "Web2 forever", "finance"));
        projectCategoryStoragePort.save(ProjectCategory.of("AI", "Can't do it myself", "ai"));

        // When
        client.get()
                .uri(getApiURI(PROJECT_CATEGORIES, Map.of("search", "fi")))
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "categories": [
                            {
                              "name": "DeFi"
                            },
                            {
                              "name": "Finance"
                            }
                          ]
                        }
                        """);
    }
}
