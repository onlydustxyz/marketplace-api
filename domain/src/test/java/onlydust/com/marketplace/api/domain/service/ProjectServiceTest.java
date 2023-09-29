package onlydust.com.marketplace.api.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProjectServiceTest {

    private final Faker faker = new Faker();

    @Test
    void should_get_a_project_by_slug() {
        // Given
        final String slug = faker.pokemon().name();
        final ProjectStoragePort projectStoragePort = Mockito.mock(ProjectStoragePort.class);
        final ProjectService projectService = new ProjectService(projectStoragePort);

        // When
        final Project expectedProject = Project.builder()
                .id(UUID.randomUUID())
                .slug(slug)
                .build();
        Mockito.when(projectStoragePort.getBySlug(slug))
                .thenReturn(expectedProject);
        final Project project = projectService.getBySlug(slug);

        // Then
        assertEquals(project, expectedProject);
    }
}
