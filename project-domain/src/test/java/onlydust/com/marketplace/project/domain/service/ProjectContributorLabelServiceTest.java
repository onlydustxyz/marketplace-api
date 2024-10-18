package onlydust.com.marketplace.project.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.port.input.ProjectObserverPort;
import onlydust.com.marketplace.project.domain.port.output.ProjectContributorLabelStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ProjectContributorLabelServiceTest {
    private final PermissionService permissionService = mock(PermissionService.class);
    private final ProjectContributorLabelStoragePort projectContributorLabelStoragePort = mock(ProjectContributorLabelStoragePort.class);
    private final ProjectObserverPort projectObserverPort = mock(ProjectObserverPort.class);

    private final static Faker faker = new Faker();

    private final ProjectContributorLabelService projectContributorLabelService = new ProjectContributorLabelService(
            permissionService,
            projectContributorLabelStoragePort,
            projectObserverPort
    );

    final UserId projectLeadId = UserId.random();
    final ProjectId projectId = ProjectId.random();

    @BeforeEach
    void setUp() {
        reset(permissionService, projectContributorLabelStoragePort, projectObserverPort);

        when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(true);
    }

    @Test
    void should_create_label() {
        // Given
        final var name = faker.lorem().word();

        // When
        final var label = projectContributorLabelService.createLabel(projectLeadId, projectId, name);

        // Then
        verify(projectContributorLabelStoragePort).save(label);
    }


    @Test
    void should_prevent_anyone_to_create_label() {
        // Given
        final var name = faker.lorem().word();

        // When
        assertThatThrownBy(() -> projectContributorLabelService.createLabel(UserId.random(), projectId, name))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Only project leaders can create labels");

        // Then
        verifyNoInteractions(projectContributorLabelStoragePort);
    }

    @Test
    void should_assign_labels_to_contributors() {
        // Given
        final var contributor1Id = faker.number().randomNumber();
        final var contributor2Id = faker.number().randomNumber();
        final var label = projectContributorLabelService.createLabel(projectLeadId, projectId, faker.lorem().word());
        final var labelsPerContributor = Map.of(contributor1Id, List.of(label.id()),
                contributor2Id, List.of(label.id()));

        // When
        when(projectContributorLabelStoragePort.get(label.id())).thenReturn(Optional.of(label));
        projectContributorLabelService.updateLabelsOfContributors(projectLeadId, projectId, labelsPerContributor);

        // Then
        verify(projectContributorLabelStoragePort).saveLabelOfContributor(label.id(), contributor1Id);
        verify(projectContributorLabelStoragePort).saveLabelOfContributor(label.id(), contributor2Id);
        verify(projectObserverPort).onLabelsModified(projectId, Set.of(contributor1Id, contributor2Id));
    }
}