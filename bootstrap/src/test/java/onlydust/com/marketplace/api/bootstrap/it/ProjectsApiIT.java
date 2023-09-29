package onlydust.com.marketplace.api.bootstrap.it;

import onlydust.com.marketplace.api.postgres.adapter.entity.ProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public class ProjectsApiIT extends AbstractMarketplaceApiIT{

    @Autowired
    ProjectRepository projectRepository;

    @Order(1)
    @Test
    public void should_get_a_project_by_slug(){
        // Given
        final ProjectEntity projectEntity = projectRepository.save(ProjectEntity.builder()
                .id(UUID.randomUUID())
                .name(FAKER.name().name())
                .longDescription(FAKER.name().fullName())
                .shortDescription(FAKER.name().lastName())
                .visibility(ProjectEntity.Visibility.PUBLIC)
                .hiring(Boolean.FALSE)
                .rank(10)
                .build());

        // When
        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_SLUG +"/"+projectEntity.getName().replace(" ","-").toLowerCase()))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id",projectEntity.getId());
    }
}
