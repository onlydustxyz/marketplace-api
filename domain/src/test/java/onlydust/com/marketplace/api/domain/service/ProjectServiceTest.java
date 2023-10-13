package onlydust.com.marketplace.api.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.domain.model.CreateProjectCommand;
import onlydust.com.marketplace.api.domain.port.output.ImageStoragePort;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.domain.port.output.UUIDGeneratorPort;
import onlydust.com.marketplace.api.domain.view.ProjectDetailsView;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
        final ImageStoragePort imageStoragePort = mock(ImageStoragePort.class);
        final ProjectService projectService = new ProjectService(projectStoragePort, imageStoragePort, mock(UUIDGeneratorPort.class));

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
    void should_create_project() throws MalformedURLException {
        // Given
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final ImageStoragePort imageStoragePort = mock(ImageStoragePort.class);
        final UUIDGeneratorPort uuidGeneratorPort = mock(UUIDGeneratorPort.class);
        final ProjectService projectService = new ProjectService(projectStoragePort, imageStoragePort, uuidGeneratorPort);
        final InputStream imageInputStream = mock(InputStream.class);
        final String imageUrl = faker.internet().image();
        final CreateProjectCommand command = CreateProjectCommand.builder()
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

        // When
        when(imageStoragePort.storeImage(imageInputStream)).thenReturn(new URL(imageUrl));
        when(uuidGeneratorPort.generate()).thenReturn(expectedProjectId);
        final UUID projectId = projectService.createProject(command);

        // Then
        assertNotNull(projectId);
        verify(projectStoragePort, times(1)).createProject(expectedProjectId, command.getName(),
                command.getShortDescription(),
                command.getLongDescription(), command.getIsLookingForContributors(),
                command.getMoreInfos(), command.getGithubRepoIds(),
                command.getGithubUserIdsAsProjectLeads(),
                imageUrl
        );

    }
}
