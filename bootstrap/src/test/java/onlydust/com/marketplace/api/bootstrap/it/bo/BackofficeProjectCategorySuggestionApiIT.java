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
                new ProjectCategorySuggestionEntity(UUID.randomUUID(), "Gaming"),
                new ProjectCategorySuggestionEntity(UUID.randomUUID(), "DeFi"),
                new ProjectCategorySuggestionEntity(UUID.randomUUID(), "Art")
        ));
    }

    @Test
    void should_get_project_category_suggestions() {
        // Given
        final var camille = userAuthHelper.authenticateCamille();

        // When
        client.get()
                .uri(PROJECT_CATEGORY_SUGGESTIONS)
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(System.out::println);
    }
}
