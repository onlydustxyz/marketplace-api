package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectCategorySuggestionEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectCategorySuggestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

public class BackofficeProjectCategorySuggestionApiIT extends AbstractMarketplaceBackOfficeApiIT {

    @Autowired
    ProjectCategorySuggestionRepository projectCategorySuggestionRepository;

    @BeforeEach
    void setUp() {
        projectCategorySuggestionRepository.saveAll(List.of(
                new ProjectCategorySuggestionEntity(UUID.fromString("fbb36293-1a5b-49c5-9cd0-6e33922d22ba"), "Gaming"),
                new ProjectCategorySuggestionEntity(UUID.fromString("d3af3bfc-5689-412a-8191-1466aa269830"), "DeFi"),
                new ProjectCategorySuggestionEntity(UUID.fromString("d3df4dbf-850e-42a5-af16-ca8a0278489c"), "Art")
        ));
    }

    @Test
    void should_get_project_category_suggestions() {
        // Given
        final var camille = userAuthHelper.authenticateCamille();

        // When
        client.get()
                .uri(PROJECT_CATEGORIES)
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 3,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "categories": [
                            {
                              "id": "d3df4dbf-850e-42a5-af16-ca8a0278489c",
                              "name": "Art",
                              "status": "PENDING",
                              "iconSlug": null,
                              "projectCount": null
                            },
                            {
                              "id": "d3af3bfc-5689-412a-8191-1466aa269830",
                              "name": "DeFi",
                              "status": "PENDING",
                              "iconSlug": null,
                              "projectCount": null
                            },
                            {
                              "id": "fbb36293-1a5b-49c5-9cd0-6e33922d22ba",
                              "name": "Gaming",
                              "status": "PENDING",
                              "iconSlug": null,
                              "projectCount": null
                            }
                          ]
                        }
                        """, true);
    }
}
