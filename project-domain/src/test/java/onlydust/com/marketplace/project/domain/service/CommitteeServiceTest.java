package onlydust.com.marketplace.project.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.port.output.CommitteeStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.view.CommitteeView;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CommitteeServiceTest {

    private final Faker faker = new Faker();
    private CommitteeService committeeService;
    private CommitteeStoragePort committeeStoragePort;
    private ProjectStoragePort projectStoragePort;
    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        committeeStoragePort = mock(CommitteeStoragePort.class);
        projectStoragePort = mock(ProjectStoragePort.class);
        permissionService = mock(PermissionService.class);
        committeeService = new CommitteeService(committeeStoragePort, permissionService, projectStoragePort);
    }

    @Test
    void should_create_a_committee() {
        // Given
        final String name = faker.rickAndMorty().character();
        final ZonedDateTime startDate = faker.date().birthday().toInstant().atZone(ZoneId.systemDefault());
        final ZonedDateTime endDate = faker.date().birthday().toInstant().atZone(ZoneId.systemDefault());

        // When
        committeeService.createCommittee(name, startDate,
                endDate);

        // Then
        final ArgumentCaptor<Committee> committeeArgumentCaptor = ArgumentCaptor.forClass(Committee.class);
        verify(committeeStoragePort).save(committeeArgumentCaptor.capture());
        assertEquals(name, committeeArgumentCaptor.getValue().name());
        assertEquals(endDate, committeeArgumentCaptor.getValue().endDate());
        assertEquals(startDate, committeeArgumentCaptor.getValue().startDate());
        assertEquals(Committee.Status.DRAFT, committeeArgumentCaptor.getValue().status());
        assertNotNull(committeeArgumentCaptor.getValue().id());
    }

    @Test
    void should_get_committee_by_id() {
        // Given
        final Committee.Id committeeId = Committee.Id.random();
        final String name = faker.rickAndMorty().character();
        final ZonedDateTime startDate = faker.date().birthday().toInstant().atZone(ZoneId.systemDefault());
        final ZonedDateTime endDate = faker.date().birthday().toInstant().atZone(ZoneId.systemDefault());
        final CommitteeView committeeView = CommitteeView.builder()
                .id(Committee.Id.random())
                .endDate(endDate)
                .startDate(startDate)
                .name(name)
                .status(Committee.Status.OPEN_TO_APPLICATIONS)
                .build();

        // When
        when(committeeStoragePort.findById(committeeId)).thenReturn(Optional.of(committeeView));
        final CommitteeView committeeById = committeeService.getCommitteeById(committeeId);

        // Then
        assertEquals(committeeView, committeeById);
    }

    @Test
    void should_throw_not_found_given_a_committee_not_found_by_id() {
        // When
        when(committeeStoragePort.findById(any())).thenReturn(Optional.empty());

        // Then
        assertThrows(OnlyDustException.class, () -> committeeService.getCommitteeById(Committee.Id.random()));
    }

    @Nested
    public class ShouldCreateUpdateApplication {

        @Test
        void given_a_committee_not_existing() {
            // Given
            final Committee.Id committeeId = Committee.Id.random();

            // When
            when(committeeStoragePort.findById(committeeId)).thenReturn(Optional.empty());

            // Then
            Assertions.assertThatThrownBy(() -> committeeService.createUpdateApplicationForCommittee(committeeId, new Committee.Application(UUID.randomUUID(),
                            UUID.randomUUID(), List.of())))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Committee %s was not found".formatted(committeeId.value().toString()));
        }

        @Test
        void given_a_user_not_project_lead() {
            // Given
            final Committee.Id committeeId = Committee.Id.random();
            final UUID userId = UUID.randomUUID();
            final UUID projectId = UUID.randomUUID();

            // When
            when(committeeStoragePort.findById(committeeId)).thenReturn(Optional.of(CommitteeView.builder()
                    .name(faker.rickAndMorty().location())
                    .id(committeeId)
                    .status(Committee.Status.DRAFT)
                    .startDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .endDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .build()));
            when(permissionService.isUserProjectLead(projectId, userId)).thenReturn(false);

            // Then
            Assertions.assertThatThrownBy(() -> committeeService.createUpdateApplicationForCommittee(committeeId, new Committee.Application(userId,
                            projectId, List.of())))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Only project leads send new application to committee");
        }

        @Test
        void given_a_project_not_existing() {
            // Given
            final Committee.Id committeeId = Committee.Id.random();
            final UUID userId = UUID.randomUUID();
            final UUID projectId = UUID.randomUUID();

            // When
            when(committeeStoragePort.findById(committeeId)).thenReturn(Optional.of(CommitteeView.builder()
                    .name(faker.rickAndMorty().location())
                    .id(committeeId)
                    .status(Committee.Status.DRAFT)
                    .startDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .endDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .build()));
            when(permissionService.isUserProjectLead(projectId, userId)).thenReturn(true);
            when(projectStoragePort.exists(projectId)).thenReturn(false);

            // Then
            Assertions.assertThatThrownBy(() -> committeeService.createUpdateApplicationForCommittee(committeeId, new Committee.Application(userId,
                            projectId, List.of())))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Project %s was not found".formatted(projectId));
        }

        @Test
        void given_a_committee_not_open_for_applications() {
            // Given
            final Committee.Id committeeId = Committee.Id.random();
            final UUID userId = UUID.randomUUID();
            final UUID projectId = UUID.randomUUID();

            // When
            when(committeeStoragePort.findById(committeeId)).thenReturn(Optional.of(CommitteeView.builder()
                    .name(faker.rickAndMorty().location())
                    .id(committeeId)
                    .status(Committee.Status.DRAFT)
                    .startDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .endDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .build()));
            when(permissionService.isUserProjectLead(projectId, userId)).thenReturn(true);
            when(projectStoragePort.exists(projectId)).thenReturn(true);

            // Then
            Assertions.assertThatThrownBy(() -> committeeService.createUpdateApplicationForCommittee(committeeId, new Committee.Application(userId,
                            projectId, List.of())))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Applications are not opened or are closed for committee %s".formatted(committeeId.value()));
        }

        @Test
        void given_everything_ok() {
            // Given
            final Committee.Id committeeId = Committee.Id.random();
            final UUID userId = UUID.randomUUID();
            final UUID projectId = UUID.randomUUID();
            final Committee.Application application = new Committee.Application(userId,
                    projectId, List.of());

            // When
            when(committeeStoragePort.findById(committeeId)).thenReturn(Optional.of(CommitteeView.builder()
                    .name(faker.rickAndMorty().location())
                    .id(committeeId)
                    .status(Committee.Status.OPEN_TO_APPLICATIONS)
                    .startDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .endDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .build()));
            when(permissionService.isUserProjectLead(projectId, userId)).thenReturn(true);
            when(projectStoragePort.exists(projectId)).thenReturn(true);
            committeeService.createUpdateApplicationForCommittee(committeeId, application);

            // Then
            verify(committeeStoragePort).saveApplication(committeeId, application);
        }


    }
}
