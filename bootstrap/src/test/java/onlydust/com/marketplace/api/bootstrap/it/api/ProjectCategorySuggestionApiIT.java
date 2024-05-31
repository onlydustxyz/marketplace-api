package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectCategorySuggestionEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectCategorySuggestionRepository;
import onlydust.com.marketplace.api.slack.SlackApiAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

public class ProjectCategorySuggestionApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    ProjectCategorySuggestionRepository projectCategorySuggestionRepository;
    @Autowired
    SlackApiAdapter slackApiAdapter;

    @Test
    void should_suggest_project_category() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        final String categoryName = faker.rickAndMorty().character();

        // When
        client.post()
                .uri(PROJECTS_CATEGORIES_SUGGEST)
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
