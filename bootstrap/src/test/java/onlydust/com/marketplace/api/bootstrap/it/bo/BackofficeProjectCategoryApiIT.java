package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.backoffice.api.contract.model.ProjectCategoryResponse;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackofficeProjectCategoryApiIT extends AbstractMarketplaceBackOfficeApiIT {
    UserAuthHelper.AuthenticatedBackofficeUser emilie;
    static UUID projectCategoryId;

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
