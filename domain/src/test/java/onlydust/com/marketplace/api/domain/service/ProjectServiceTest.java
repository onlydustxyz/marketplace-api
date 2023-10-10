package onlydust.com.marketplace.api.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.domain.model.CreateProjectCommand;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.domain.port.output.UUIDGeneratorPort;
import onlydust.com.marketplace.api.domain.view.ProjectDetailsView;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class ProjectServiceTest {

    private final Faker faker = new Faker();

    @Test
    void should_get_a_project_by_slug() {
        // Given
        final String slug = faker.pokemon().name();
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final ProjectService projectService = new ProjectService(projectStoragePort, mock(UUIDGeneratorPort.class));

        // When
        final var expectedProject = ProjectDetailsView.builder()
                .id(UUID.randomUUID())
                .slug(slug)
                .build();
        Mockito.when(projectStoragePort.getBySlug(slug))
                .thenReturn(expectedProject);
        final var project = projectService.getBySlug(slug);

        // Then
        assertEquals(project, expectedProject);
    }

    @Test
    void should_create_project() {
        // Given
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final UUIDGeneratorPort uuidGeneratorPort = mock(UUIDGeneratorPort.class);
        final ProjectService projectService = new ProjectService(projectStoragePort, uuidGeneratorPort);
        final InputStream imageInputStream = mock(InputStream.class);
        final CreateProjectCommand createProjectCommand = CreateProjectCommand.builder()
                .name(faker.pokemon().name())
                .shortDescription(faker.lorem().sentence())
                .longDescription(faker.lorem().paragraph())
                .isLookingForContributors(false)
                .moreInfos(List.of(CreateProjectCommand.MoreInfo.builder().value(faker.lorem().sentence()).url(faker.internet().url()).build()))
                .githubUserIdsAsProjectLeads(List.of(faker.number().randomNumber()))
                .githubRepoIds(List.of(faker.number().randomNumber()))
                .image(imageInputStream)
                .build();
        final UUID expectedProjectId = UUID.randomUUID();
        final String imageUrl = faker.internet().url();

        // When
        when(uuidGeneratorPort.generate()).thenReturn(expectedProjectId);
        final UUID projectId = projectService.createProject(createProjectCommand);

        // Then
        assertNotNull(projectId);
        verify(projectStoragePort, times(1)).createProject(expectedProjectId, createProjectCommand.getName(),
                createProjectCommand.getShortDescription(),
                createProjectCommand.getLongDescription(), createProjectCommand.getIsLookingForContributors(),
                createProjectCommand.getMoreInfos(), createProjectCommand.getGithubRepoIds(),
                createProjectCommand.getGithubUserIdsAsProjectLeads(),
                imageUrl
        );

    }
}
