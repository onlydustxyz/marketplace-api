package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectCategoryEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectCategoryRepository;
import onlydust.com.marketplace.api.slack.SlackApiAdapter;
import onlydust.com.marketplace.project.domain.model.ProjectCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

public class ProjectCategoryApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    ProjectCategoryRepository projectCategoryRepository;
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

        final List<ProjectCategoryEntity> projectCategories = projectCategoryRepository.findAll();
        assertEquals(1, projectCategories.size());
        final ProjectCategoryEntity projectCategoryEntity = projectCategories.get(0);
        assertEquals(categoryName, projectCategoryEntity.getName());
        assertEquals(ProjectCategory.Status.SUGGESTED.name(), projectCategoryEntity.getStatus().name());

        verify(slackApiAdapter).onProjectCategorySuggested(categoryName, pierre.user().getId());
    }
}