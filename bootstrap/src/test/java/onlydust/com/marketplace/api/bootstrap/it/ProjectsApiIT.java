package onlydust.com.marketplace.api.bootstrap.it;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public class ProjectsApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    ProjectRepository projectRepository;

    @Order(1)
    @Test
    public void should_get_a_project_by_slug() {
        // Given
        final ProjectEntity projectEntity = projectRepository.save(ProjectEntity.builder()
                .id(UUID.randomUUID())
                .name(faker.name().name())
                .longDescription(faker.name().fullName())
                .shortDescription(faker.name().lastName())
                .visibility(ProjectEntity.Visibility.PUBLIC)
                .hiring(Boolean.FALSE)
                .logoUrl("https://logo-url-test/" + faker.pokemon().name())
                .rank(10)
                .build());

        // When
        final String slug = projectEntity.getName().replace(" ", "-").toLowerCase();
        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_SLUG + "/" + slug))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(projectEntity.getId().toString())
                .jsonPath("$.prettyId").isEqualTo(slug)
                .jsonPath("$.name").isEqualTo(projectEntity.getName())
                .jsonPath("$.shortDescription").isEqualTo(projectEntity.getShortDescription())
                .jsonPath("$.logoUrl").isEqualTo(projectEntity.getLogoUrl());
    }
}
