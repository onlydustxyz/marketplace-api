package onlydust.com.marketplace.project.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;
import onlydust.com.marketplace.project.domain.port.output.CommitteeStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.view.CommitteeApplicationView;
import onlydust.com.marketplace.project.domain.view.CommitteeView;
import onlydust.com.marketplace.project.domain.view.ProjectAnswerView;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.net.URI;
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
                    .status(Committee.Status.OPEN_TO_APPLICATIONS)
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
                    .status(Committee.Status.OPEN_TO_APPLICATIONS)
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

    @Test
    void should_get_applications_given_creation() {
        // Given
        final Committee.Id committeeId = Committee.Id.random();
        final UUID userId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();
        final CommitteeApplicationView.ProjectInfosView projectInfosView = new CommitteeApplicationView.ProjectInfosView(
                UUID.randomUUID(), faker.lordOfTheRings().character(), faker.internet().slug(), URI.create(faker.internet().url()),
                faker.rickAndMorty().character(),
                faker.rickAndMorty().location(), List.of(), 1,
                BigDecimal.ONE, 2, 4, 6, 8);
        final ProjectQuestion q1 = new ProjectQuestion("Q1", false);
        final ProjectQuestion q2 = new ProjectQuestion("Q2", true);
        final CommitteeView committeeView = CommitteeView.builder()
                .name(faker.rickAndMorty().location())
                .id(committeeId)
                .status(Committee.Status.OPEN_TO_APPLICATIONS)
                .startDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .endDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .projectQuestions(List.of(
                        q1,
                        q2
                ))
                .build();

        // When
        when(committeeStoragePort.findById(committeeId)).thenReturn(Optional.of(committeeView));
        when(permissionService.isUserProjectLead(projectId, userId)).thenReturn(true);
        when(projectStoragePort.exists(projectId)).thenReturn(true);
        when(committeeStoragePort.getApplicationAnswers(committeeId, projectId))
                .thenReturn(List.of());
        when(projectStoragePort.getProjectInfos(projectId))
                .thenReturn(projectInfosView);
        final CommitteeApplicationView committeeApplication = committeeService.getCommitteeApplication(committeeId, Optional.of(projectId), userId);

        // Then
        assertEquals(projectInfosView, committeeApplication.projectInfosView());
        assertEquals(committeeView.status(), committeeApplication.status());
        assertEquals(2, committeeApplication.answers().size());
        assertEquals("Q1", committeeApplication.answers().get(0).question());
        assertEquals(q1.id(), committeeApplication.answers().get(0).questionId());
        assertEquals(false, committeeApplication.answers().get(0).required());
        assertNull(committeeApplication.answers().get(0).answer());
        assertEquals("Q2", committeeApplication.answers().get(1).question());
        assertEquals(q2.id(), committeeApplication.answers().get(1).questionId());
        assertEquals(true, committeeApplication.answers().get(1).required());
        assertNull(committeeApplication.answers().get(1).answer());
    }

    @Test
    void should_get_applications_given_update() {
        // Given
        final Committee.Id committeeId = Committee.Id.random();
        final UUID userId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();
        final CommitteeApplicationView.ProjectInfosView projectInfosView = new CommitteeApplicationView.ProjectInfosView(
                UUID.randomUUID(), faker.lordOfTheRings().character(), faker.internet().slug(), URI.create(faker.internet().url()),
                faker.rickAndMorty().character(),
                faker.rickAndMorty().location(), List.of(), 1,
                BigDecimal.ONE, 2, 4, 6, 8);
        final CommitteeView committeeView = CommitteeView.builder()
                .name(faker.rickAndMorty().location())
                .id(committeeId)
                .status(Committee.Status.OPEN_TO_APPLICATIONS)
                .startDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .endDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .projectQuestions(List.of(
                        new ProjectQuestion("Q1", false),
                        new ProjectQuestion("Q2", true)
                ))
                .build();

        // When
        when(committeeStoragePort.findById(committeeId)).thenReturn(Optional.of(committeeView));
        when(permissionService.isUserProjectLead(projectId, userId)).thenReturn(true);
        when(projectStoragePort.exists(projectId)).thenReturn(true);
        final ProjectAnswerView a3 = new ProjectAnswerView(ProjectQuestion.Id.random(), "Q3", true, "A3");
        final ProjectAnswerView a4 = new ProjectAnswerView(ProjectQuestion.Id.random(), "Q4", false, "A4");
        when(committeeStoragePort.getApplicationAnswers(committeeId, projectId))
                .thenReturn(List.of(
                        a3,
                        a4
                ));
        when(projectStoragePort.getProjectInfos(projectId))
                .thenReturn(projectInfosView);
        final CommitteeApplicationView committeeApplication = committeeService.getCommitteeApplication(committeeId, Optional.of(projectId), userId);

        // Then
        assertEquals(projectInfosView, committeeApplication.projectInfosView());
        assertEquals(committeeView.status(), committeeApplication.status());
        assertEquals(2, committeeApplication.answers().size());
        assertEquals("Q3", committeeApplication.answers().get(0).question());
        assertEquals(true, committeeApplication.answers().get(0).required());
        assertEquals("A3", committeeApplication.answers().get(0).answer());
        assertEquals(a3.questionId(), committeeApplication.answers().get(0).questionId());
        assertEquals("Q4", committeeApplication.answers().get(1).question());
        assertEquals(false, committeeApplication.answers().get(1).required());
        assertEquals("A4", committeeApplication.answers().get(1).answer());
        assertEquals(a4.questionId(), committeeApplication.answers().get(1).questionId());
    }


    @Test
    void should_get_application_project_questions() {
        // Given
        final Committee.Id committeeId = Committee.Id.random();
        final UUID userId = UUID.randomUUID();
        final ProjectQuestion q1 = new ProjectQuestion("Q1", false);
        final ProjectQuestion q2 = new ProjectQuestion("Q2", true);
        final CommitteeView committeeView = CommitteeView.builder()
                .name(faker.rickAndMorty().location())
                .id(committeeId)
                .status(Committee.Status.OPEN_TO_APPLICATIONS)
                .startDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .endDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .projectQuestions(List.of(
                        q1,
                        q2
                ))
                .build();

        // When
        when(committeeStoragePort.findById(committeeId)).thenReturn(Optional.of(committeeView));
        final CommitteeApplicationView committeeApplication = committeeService.getCommitteeApplication(committeeId, Optional.empty(), userId);

        // Then
        assertNull(committeeApplication.projectInfosView());
        assertEquals(committeeView.status(), committeeApplication.status());
        assertEquals(2, committeeApplication.answers().size());
        assertEquals("Q1", committeeApplication.answers().get(0).question());
        assertEquals(false, committeeApplication.answers().get(0).required());
        assertEquals(q1.id(), committeeApplication.answers().get(0).questionId());
        assertNull(committeeApplication.answers().get(0).answer());
        assertEquals("Q2", committeeApplication.answers().get(1).question());
        assertEquals(true, committeeApplication.answers().get(1).required());
        assertEquals(q2.id(), committeeApplication.answers().get(1).questionId());
        assertNull(committeeApplication.answers().get(1).answer());
        verifyNoInteractions(projectStoragePort);
    }

    @Test
    void should_update_given_a_draft_committee() {
        // Given
        final Committee.Id committeeId = Committee.Id.random();
        final ProjectQuestion q1 = new ProjectQuestion("Q1", false);
        final ProjectQuestion q2 = new ProjectQuestion("Q2", true);
        final Committee committee = Committee.builder()
                .name(faker.rickAndMorty().location())
                .id(committeeId)
                .status(Committee.Status.DRAFT)
                .startDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .endDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .build();
        committee.projectQuestions().addAll(List.of(q1, q2));

        // When
        committeeService.update(committee);

        // Then
        verify(committeeStoragePort).save(committee);
        verify(committeeStoragePort).deleteAllProjectQuestions(committeeId);
        verify(committeeStoragePort).saveProjectQuestions(committeeId, List.of(q1, q2));
    }

    @Test
    void should_update_given_a_not_draft_committee() {
        // Given
        final Committee.Id committeeId = Committee.Id.random();
        final ProjectQuestion q1 = new ProjectQuestion("Q1", false);
        final ProjectQuestion q2 = new ProjectQuestion("Q2", true);
        final Committee committee = Committee.builder()
                .name(faker.rickAndMorty().location())
                .id(committeeId)
                .status(Committee.Status.OPEN_TO_APPLICATIONS)
                .startDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .endDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .build();
        committee.projectQuestions().addAll(List.of(q1, q2));

        // When
        committeeService.update(committee);

        // Then
        verify(committeeStoragePort).save(committee);
        verify(committeeStoragePort, times(0)).saveProjectQuestions(committeeId, List.of(q1, q2));
        verify(committeeStoragePort, times(0)).deleteAllProjectQuestions(committeeId);
    }


}
