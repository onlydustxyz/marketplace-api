package onlydust.com.marketplace.api.bootstrap.it;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProjectVisibilityEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public class ProjectsApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    ProjectRepository projectRepository;


    @Test
    void should_get_projects() {
        client.get()
                .uri(getApiURI(PROJECTS_GET))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();
    }

    @Order(1)
    @Test
    public void should_get_a_project_by_slug() {
        // Given
        final ProjectEntity projectEntity = projectRepository.save(ProjectEntity.builder()
                .id(UUID.randomUUID())
                .name(faker.name().name())
                .longDescription(faker.name().fullName())
                .shortDescription(faker.name().lastName())
                .visibility(ProjectVisibilityEnumEntity.PUBLIC)
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
                .jsonPath("$.slug").isEqualTo(slug)
                .jsonPath("$.name").isEqualTo(projectEntity.getName())
                .jsonPath("$.shortDescription").isEqualTo(projectEntity.getShortDescription())
                .jsonPath("$.longDescription").isEqualTo(projectEntity.getLongDescription())
                .jsonPath("$.logoUrl").isEqualTo(projectEntity.getLogoUrl())
                .jsonPath("$.moreInfoUrl").isEqualTo(projectEntity.getTelegramLink())
                .jsonPath("$.hiring").isEqualTo(projectEntity.getHiring())
                .jsonPath("$.visibility").isEqualTo(projectEntity.getVisibility())
                .jsonPath("$.contributorCount").isEqualTo("")
                .jsonPath("$.topContributors").isEqualTo("")
                .jsonPath("$.repos").isEqualTo("")
                .jsonPath("$.leaders").isEqualTo("")
                .jsonPath("$.sponsors").isEqualTo("")
                .jsonPath("$.technologies").isEqualTo("");
    }
}
