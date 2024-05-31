package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectCategorySuggestionEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectCategorySuggestionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

public class BackofficeProjectCategorySuggestionApiIT extends AbstractMarketplaceBackOfficeApiIT {

    @Autowired
    ProjectCategorySuggestionRepository projectCategorySuggestionRepository;


    @Test
    void should_get_project_category_suggestions() {
        // Given
        final var camille = userAuthHelper.authenticateCamille();

        // When
        client.get()
                .uri(PROJECT_CATEGORY_SUGGESTIONS)
                .header("Authorization", "Bearer " + pierre.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "name": "%s"
                        }
                        """.formatted(categoryName))
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        final List<ProjectCategorySuggestionEntity> projectCategories = projectCategorySuggestionRepository.findAll();
        assertEquals(1, projectCategories.size());
        final ProjectCategorySuggestionEntity projectCategorySuggestionEntity = projectCategories.get(0);
        assertEquals(categoryName, projectCategorySuggestionEntity.getName());

        verify(slackApiAdapter).onProjectCategorySuggested(categoryName, pierre.user().getId());
    }
}
