package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.suites.tags.TagProject;
import onlydust.com.marketplace.api.contract.model.SuggestProjectCategoryRequest;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectCategorySuggestionRepository;
import onlydust.com.marketplace.api.slack.SlackApiAdapter;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@TagProject
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectCategorySuggestionApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    ProjectCategorySuggestionRepository projectCategorySuggestionRepository;
    @Autowired
    SlackApiAdapter slackApiAdapter;

    @Test
    @Order(1)
    void should_suggest_project_category() {
        // Given
        final var pierre = userAuthHelper.authenticatePierre();
        final var categoryName = faker.rickAndMorty().character();
        final var projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        // When
        client.post()
                .uri(PROJECTS_CATEGORIES_SUGGEST)
                .header("Authorization", "Bearer " + pierre.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new SuggestProjectCategoryRequest()
                        .name(categoryName)
                        .projectId(projectId))
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        final var projectCategories = projectCategorySuggestionRepository.findAll();
        assertThat(projectCategories).hasSize(1);
        final var projectCategorySuggestionEntity = projectCategories.get(0);
        assertThat(projectCategorySuggestionEntity.getName()).isEqualTo(categoryName);
        assertThat(projectCategorySuggestionEntity.getProjectId()).isEqualTo(projectId);

        verify(slackApiAdapter).onProjectCategorySuggested(categoryName, pierre.user().getId());
    }
}
