package onlydust.com.marketplace.api.it.bo;

import onlydust.com.backoffice.api.contract.model.ProjectCategoryCreateRequest;
import onlydust.com.backoffice.api.contract.model.ProjectCategoryResponse;
import onlydust.com.backoffice.api.contract.model.ProjectCategoryUpdateRequest;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagBO;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectCategorySuggestionEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectCategorySuggestionRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.UUID;

@TagBO
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackofficeProjectCategoryApiIT extends AbstractMarketplaceBackOfficeApiIT {
    UserAuthHelper.AuthenticatedBackofficeUser emilie;
    static UUID projectCategoryId;
    static UUID projectCategoryFromSuggestionId;

    @Autowired
    ProjectCategorySuggestionRepository projectCategorySuggestionRepository;

    @BeforeEach
    void setUp() {
        emilie = userAuthHelper.authenticateEmilie();
    }

    @Test
    @Order(0)
    void should_forbid_non_admin_to_modify_categories() {
        // Given
        final var notAnAdmin = userAuthHelper.authenticateGregoire();

        // When
        client.post()
                .uri(PROJECT_CATEGORIES)
                .header("Authorization", "Bearer " + notAnAdmin.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Security",
                          "iconSlug": "lock"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isUnauthorized();

        // When
        client.put()
                .uri(PROJECT_CATEGORY.formatted(UUID.randomUUID()))
                .header("Authorization", "Bearer " + notAnAdmin.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Security",
                          "iconSlug": "lock"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isUnauthorized();


        // When
        client.delete()
                .uri(PROJECT_CATEGORY.formatted(UUID.randomUUID()))
                .header("Authorization", "Bearer " + notAnAdmin.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }


    @Test
    @Order(1)
    void should_create_project_category() {
        // When
        final var response = client.post()
                .uri(PROJECT_CATEGORIES)
                .header("Authorization", "Bearer " + emilie.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Security",
                          "iconSlug": "lock"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ProjectCategoryResponse.class)
                .returnResult()
                .getResponseBody();

        projectCategoryId = response.getId();

        // When
        client.get()
                .uri(PROJECT_CATEGORY.formatted(projectCategoryId))
                .header("Authorization", "Bearer " + emilie.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(projectCategoryId.toString())
                .json("""
                        {
                          "name": "Security",
                          "iconSlug": "lock"
                        }
                        """);
    }


    @Test
    @Order(1)
    void should_create_project_category_from_suggestion() {
        // Given
        final var suggestionId = UUID.randomUUID();
        final var projectId = UUID.fromString("02a533f5-6cbb-4cb6-90fe-f6bee220443c");

        projectCategorySuggestionRepository.save(new ProjectCategorySuggestionEntity(suggestionId, "ai", projectId));

        // When
        final var response = client.post()
                .uri(PROJECT_CATEGORIES)
                .header("Authorization", "Bearer " + emilie.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ProjectCategoryCreateRequest()
                        .name("AI")
                        .iconSlug("brain")
                        .suggestionId(suggestionId))
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ProjectCategoryResponse.class)
                .returnResult()
                .getResponseBody();

        projectCategoryFromSuggestionId = response.getId();

        // When
        client.get()
                .uri(PROJECT_CATEGORY.formatted(projectCategoryFromSuggestionId))
                .header("Authorization", "Bearer " + emilie.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(projectCategoryFromSuggestionId.toString())
                .json("""
                        {
                          "name": "AI",
                          "iconSlug": "brain"
                        }
                        """);

        // When
        client.get()
                .uri(PROJECT_CATEGORIES)
                .header("Authorization", "Bearer " + emilie.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.categories[?(@.name == 'AI')].projectCount").isEqualTo(1)
                .jsonPath("$.categories[?(@.status == 'PENDING')]").doesNotExist();
    }

    @Test
    @Order(2)
    void should_update_project_category() {
        // When
        client.put()
                .uri(PROJECT_CATEGORY.formatted(projectCategoryId))
                .header("Authorization", "Bearer " + emilie.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Another name",
                          "iconSlug": "something-else"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(projectCategoryId.toString())
                .json("""
                        {
                          "name": "Another name",
                          "iconSlug": "something-else"
                        }
                        """);

        // When
        client.get()
                .uri(PROJECT_CATEGORY.formatted(projectCategoryId))
                .header("Authorization", "Bearer " + emilie.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(projectCategoryId.toString())
                .json("""
                        {
                          "name": "Another name",
                          "iconSlug": "something-else"
                        }
                        """);
    }


    @Test
    @Order(2)
    void should_update_project_category_with_suggestion() {
        // Given
        final var suggestionId = UUID.randomUUID();
        final var projectId = UUID.fromString("3a1e0a11-634e-4bf1-a3ed-022ae68b6436");

        projectCategorySuggestionRepository.save(new ProjectCategorySuggestionEntity(suggestionId, "ai", projectId));

        // When
        final var response = client.put()
                .uri(PROJECT_CATEGORY.formatted(projectCategoryFromSuggestionId))
                .header("Authorization", "Bearer " + emilie.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ProjectCategoryUpdateRequest()
                        .name("AI")
                        .iconSlug("brain")
                        .suggestionId(suggestionId))
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ProjectCategoryResponse.class)
                .returnResult()
                .getResponseBody();

        // When
        client.get()
                .uri(PROJECT_CATEGORY.formatted(projectCategoryFromSuggestionId))
                .header("Authorization", "Bearer " + emilie.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(projectCategoryFromSuggestionId.toString())
                .json("""
                        {
                          "name": "AI",
                          "iconSlug": "brain"
                        }
                        """);

        // When
        client.get()
                .uri(PROJECT_CATEGORIES)
                .header("Authorization", "Bearer " + emilie.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.categories[?(@.name == 'AI')].projectCount").isEqualTo(2)
                .jsonPath("$.categories[?(@.status == 'PENDING')]").doesNotExist();
    }

    @Test
    @Order(3)
    void should_delete_project_category() {
        // When
        client.delete()
                .uri(PROJECT_CATEGORY.formatted(projectCategoryId))
                .header("Authorization", "Bearer " + emilie.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        // When
        client.get()
                .uri(PROJECT_CATEGORY.formatted(projectCategoryId))
                .header("Authorization", "Bearer " + emilie.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isNotFound();
    }
}
