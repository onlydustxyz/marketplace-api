package onlydust.com.marketplace.project.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.port.input.CommitteeObserverPort;
import onlydust.com.marketplace.project.domain.port.output.CommitteeStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.view.*;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeApplicationLinkView;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeApplicationView;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CommitteeServiceTest {
    private final Faker faker = new Faker();
    private CommitteeService committeeService;
    private CommitteeStoragePort committeeStoragePort;
    private ProjectStoragePort projectStoragePort;
    private PermissionService permissionService;
    private CommitteeObserverPort committeeObserverPort;

    @BeforeEach
    void setUp() {
        committeeStoragePort = mock(CommitteeStoragePort.class);
        projectStoragePort = mock(ProjectStoragePort.class);
        permissionService = mock(PermissionService.class);
        committeeObserverPort = mock(CommitteeObserverPort.class);
        committeeService = new CommitteeService(committeeStoragePort, permissionService, projectStoragePort, committeeObserverPort);
    }

    @Test
    void should_create_a_committee() {
        // Given
        final String name = faker.rickAndMorty().character();
        final ZonedDateTime applicationStartDate = faker.date().birthday().toInstant().atZone(ZoneId.systemDefault());
        final ZonedDateTime applicationEndDate = faker.date().birthday().toInstant().atZone(ZoneId.systemDefault());

        // When
        committeeService.createCommittee(name, applicationStartDate,
                applicationEndDate);

        // Then
        final ArgumentCaptor<Committee> committeeArgumentCaptor = ArgumentCaptor.forClass(Committee.class);
        verify(committeeStoragePort).save(committeeArgumentCaptor.capture());
        assertEquals(name, committeeArgumentCaptor.getValue().name());
        assertEquals(applicationEndDate, committeeArgumentCaptor.getValue().applicationEndDate());
        assertEquals(applicationStartDate, committeeArgumentCaptor.getValue().applicationStartDate());
        assertEquals(Committee.Status.DRAFT, committeeArgumentCaptor.getValue().status());
        assertNotNull(committeeArgumentCaptor.getValue().id());
    }

    @Test
    void should_get_committee_by_id() {
        // Given
        final Committee.Id committeeId = Committee.Id.random();
        final String name = faker.rickAndMorty().character();
        final ZonedDateTime applicationStartDate = faker.date().birthday().toInstant().atZone(ZoneId.systemDefault());
        final ZonedDateTime applicationEndDate = faker.date().birthday().toInstant().atZone(ZoneId.systemDefault());
        final CommitteeView committeeView = CommitteeView.builder()
                .id(Committee.Id.random())
                .applicationEndDate(applicationEndDate)
                .applicationStartDate(applicationStartDate)
                .name(name)
                .status(Committee.Status.OPEN_TO_APPLICATIONS)
                .build();

        // When
        when(committeeStoragePort.findViewById(committeeId)).thenReturn(Optional.of(committeeView));
        final CommitteeView committeeById = committeeService.getCommitteeById(committeeId);

        // Then
        assertEquals(committeeView, committeeById);
    }

    @Test
    void should_throw_not_found_given_a_committee_not_found_by_id() {
        // When
        when(committeeStoragePort.findViewById(any())).thenReturn(Optional.empty());

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
            when(committeeStoragePort.findViewById(committeeId)).thenReturn(Optional.empty());

            // Then
            assertThatThrownBy(() -> committeeService.createUpdateApplicationForCommittee(committeeId, new Committee.Application(UUID.randomUUID(),
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
            when(committeeStoragePort.findById(committeeId)).thenReturn(Optional.of(Committee.builder()
                    .name(faker.rickAndMorty().location())
                    .id(committeeId)
                    .status(Committee.Status.OPEN_TO_APPLICATIONS)
                    .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .projectQuestions(List.of())
                    .build()));
            when(permissionService.isUserProjectLead(projectId, userId)).thenReturn(false);

            // Then
            assertThatThrownBy(() -> committeeService.createUpdateApplicationForCommittee(committeeId, new Committee.Application(userId,
                    projectId, List.of())))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Only project lead can send new application to committee");
        }

        @Test
        void given_a_project_not_existing() {
            // Given
            final Committee.Id committeeId = Committee.Id.random();
            final UUID userId = UUID.randomUUID();
            final UUID projectId = UUID.randomUUID();

            // When
            when(committeeStoragePort.findById(committeeId)).thenReturn(Optional.of(Committee.builder()
                    .name(faker.rickAndMorty().location())
                    .id(committeeId)
                    .status(Committee.Status.OPEN_TO_APPLICATIONS)
                    .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .projectQuestions(List.of())
                    .build()));
            when(permissionService.isUserProjectLead(projectId, userId)).thenReturn(true);
            when(projectStoragePort.exists(projectId)).thenReturn(false);

            // Then
            assertThatThrownBy(() -> committeeService.createUpdateApplicationForCommittee(committeeId, new Committee.Application(userId,
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
            when(committeeStoragePort.findById(committeeId)).thenReturn(Optional.of(Committee.builder()
                    .name(faker.rickAndMorty().location())
                    .id(committeeId)
                    .status(Committee.Status.DRAFT)
                    .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .build()));
            when(permissionService.isUserProjectLead(projectId, userId)).thenReturn(true);
            when(projectStoragePort.exists(projectId)).thenReturn(true);

            // Then
            assertThatThrownBy(() -> committeeService.createUpdateApplicationForCommittee(committeeId, new Committee.Application(userId,
                    projectId, List.of())))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Applications are not opened or are closed for committee %s".formatted(committeeId.value()));
        }

        @Test
        void given_a_question_not_linked_to_committee() {
            final Committee.Id committeeId = Committee.Id.random();
            final UUID userId = UUID.randomUUID();
            final UUID projectId = UUID.randomUUID();

            // When
            when(committeeStoragePort.findById(committeeId)).thenReturn(Optional.of(Committee.builder()
                    .name(faker.rickAndMorty().location())
                    .id(committeeId)
                    .status(Committee.Status.OPEN_TO_APPLICATIONS)
                    .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .projectQuestions(List.of(new ProjectQuestion(ProjectQuestion.Id.random(), "Q1", false)))
                    .build()));
            when(permissionService.isUserProjectLead(projectId, userId)).thenReturn(true);
            when(projectStoragePort.exists(projectId)).thenReturn(true);

            // Then
            assertThatThrownBy(() -> committeeService.createUpdateApplicationForCommittee(committeeId, new Committee.Application(userId,
                    projectId, List.of(new Committee.ProjectAnswer(ProjectQuestion.Id.random(), "a1")))))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("A project question is not linked to committee %s".formatted(committeeId.value()));
        }

        @Test
        void given_new_application() {
            // Given
            final Committee.Id committeeId = Committee.Id.random();
            final UUID userId = UUID.randomUUID();
            final UUID projectId = UUID.randomUUID();
            final ProjectQuestion.Id projectQuestion = ProjectQuestion.Id.random();
            final Committee.Application application = new Committee.Application(userId,
                    projectId, List.of(new Committee.ProjectAnswer(projectQuestion, "A1")));

            // When
            when(committeeStoragePort.findById(committeeId)).thenReturn(Optional.of(Committee.builder()
                    .name(faker.rickAndMorty().location())
                    .id(committeeId)
                    .status(Committee.Status.OPEN_TO_APPLICATIONS)
                    .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .projectQuestions(List.of(new ProjectQuestion(projectQuestion, "Q1", false)))
                    .build()));
            when(permissionService.isUserProjectLead(projectId, userId)).thenReturn(true);
            when(projectStoragePort.exists(projectId)).thenReturn(true);
            committeeService.createUpdateApplicationForCommittee(committeeId, application);

            // Then
            final var committeeCaptor = ArgumentCaptor.forClass(Committee.class);
            verify(committeeStoragePort).save(committeeCaptor.capture());
            final var committee = committeeCaptor.getValue();
            assertThat(committee.projectApplications()).containsKey(projectId);

            verify(committeeObserverPort).onNewApplication(committeeId, application.projectId(), application.userId());
        }

        @Test
        void given_application_update() {
            // Given
            final Committee.Id committeeId = Committee.Id.random();
            final UUID userId = UUID.randomUUID();
            final UUID projectId = UUID.randomUUID();
            final Committee.Application application = new Committee.Application(userId,
                    projectId, List.of());
            final Committee.Application oldApplication = new Committee.Application(userId,
                    projectId, List.of());
            final var existingCommittee = Committee.builder()
                    .id(committeeId)
                    .name(faker.rickAndMorty().location())
                    .status(Committee.Status.OPEN_TO_APPLICATIONS)
                    .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .projectQuestions(List.of())
                    .build();
            existingCommittee.projectApplications().put(projectId, oldApplication);

            // When
            when(committeeStoragePort.findById(committeeId)).thenReturn(Optional.of(existingCommittee));
            when(permissionService.isUserProjectLead(projectId, userId)).thenReturn(true);
            when(projectStoragePort.exists(projectId)).thenReturn(true);
            committeeService.createUpdateApplicationForCommittee(committeeId, application);

            // Then
            final var committeeCaptor = ArgumentCaptor.forClass(Committee.class);
            verify(committeeStoragePort).save(committeeCaptor.capture());
            final var committee = committeeCaptor.getValue();
            assertThat(committee.projectApplications()).containsKey(projectId);

            verifyNoInteractions(committeeObserverPort);
        }
    }

    @Test
    void should_get_applications_given_creation() {
        // Given
        final Committee.Id committeeId = Committee.Id.random();
        final UUID userId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();
        final ProjectInfosView projectInfosView = new ProjectInfosView(
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
                .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .projectQuestions(List.of(
                        q1,
                        q2
                ))
                .build();

        // When
        when(committeeStoragePort.findViewById(committeeId)).thenReturn(Optional.of(committeeView));
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
        assertFalse(committeeApplication.hasStartedApplication());
    }

    @Test
    void should_get_applications_given_update() {
        // Given
        final Committee.Id committeeId = Committee.Id.random();
        final UUID userId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();
        final ProjectInfosView projectInfosView = new ProjectInfosView(
                UUID.randomUUID(), faker.lordOfTheRings().character(), faker.internet().slug(), URI.create(faker.internet().url()),
                faker.rickAndMorty().character(),
                faker.rickAndMorty().location(), List.of(), 1,
                BigDecimal.ONE, 2, 4, 6, 8);
        final CommitteeView committeeView = CommitteeView.builder()
                .name(faker.rickAndMorty().location())
                .id(committeeId)
                .status(Committee.Status.OPEN_TO_APPLICATIONS)
                .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .projectQuestions(List.of(
                        new ProjectQuestion("Q1", false),
                        new ProjectQuestion("Q2", true)
                ))
                .build();

        // When
        when(committeeStoragePort.findViewById(committeeId)).thenReturn(Optional.of(committeeView));
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
        assertTrue(committeeApplication.hasStartedApplication());
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
                .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .projectQuestions(List.of(
                        q1,
                        q2
                ))
                .build();

        // When
        when(committeeStoragePort.findViewById(committeeId)).thenReturn(Optional.of(committeeView));
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

    @ParameterizedTest
    @EnumSource(Committee.Status.class)
    void should_update_committee(Committee.Status status) {
        // Given
        final var existingCommittee = Committee.builder()
                .id(Committee.Id.random())
                .name(faker.rickAndMorty().location())
                .status(status)
                .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .projectQuestions(List.of(
                        new ProjectQuestion("Q1", false),
                        new ProjectQuestion("Q2", true)))
                .build();

        final var committee = existingCommittee.toBuilder()
                .name(faker.rickAndMorty().location())
                .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .build();

        when(committeeStoragePort.findById(existingCommittee.id())).thenReturn(Optional.of(existingCommittee));

        // When
        committeeService.update(committee);

        // Then
        verify(committeeStoragePort).save(committee);
    }

    @Test
    void should_update_project_questions() {
        // Given
        final var existingCommittee = Committee.builder()
                .id(Committee.Id.random())
                .name(faker.rickAndMorty().location())
                .status(Committee.Status.DRAFT)
                .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .projectQuestions(List.of(
                        new ProjectQuestion("Q1", false),
                        new ProjectQuestion("Q2", true)))
                .build();

        final var committee = existingCommittee.toBuilder()
                .projectQuestions(List.of(
                        new ProjectQuestion("Q2", false),
                        new ProjectQuestion("Q3", false)))
                .build();

        when(committeeStoragePort.findById(existingCommittee.id())).thenReturn(Optional.of(existingCommittee));

        // When
        committeeService.update(committee);

        // Then
        verify(committeeStoragePort).save(committee);
    }

    @Test
    void should_prevent_committee_update_status() {
        // Given
        final var existingCommittee = Committee.builder()
                .id(Committee.Id.random())
                .name(faker.rickAndMorty().location())
                .status(Committee.Status.OPEN_TO_APPLICATIONS)
                .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .projectQuestions(List.of(
                        new ProjectQuestion("Q1", false),
                        new ProjectQuestion("Q2", true)))
                .build();

        final var committee = existingCommittee.toBuilder()
                .status(Committee.Status.DRAFT)
                .build();

        when(committeeStoragePort.findById(existingCommittee.id())).thenReturn(Optional.of(existingCommittee));

        // When
        assertThatThrownBy(() -> committeeService.update(committee))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Status cannot be updated");

        // Then
        verify(committeeStoragePort, never()).save(committee);
    }

    @Test
    void should_prevent_project_question_update_if_not_draft() {
        // Given
        final var existingCommittee = Committee.builder()
                .id(Committee.Id.random())
                .name(faker.rickAndMorty().location())
                .status(Committee.Status.OPEN_TO_APPLICATIONS)
                .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .projectQuestions(List.of(
                        new ProjectQuestion("Q1", false),
                        new ProjectQuestion("Q2", true)))
                .build();

        final var committee = existingCommittee.toBuilder()
                .projectQuestions(List.of(
                        new ProjectQuestion("Q2", true),
                        new ProjectQuestion("Q1", false)))
                .build();

        when(committeeStoragePort.findById(existingCommittee.id())).thenReturn(Optional.of(existingCommittee));

        // When
        assertThatThrownBy(() -> committeeService.update(committee))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Project questions can only be updated for draft committees");

        // Then
        verify(committeeStoragePort, never()).save(committee);
    }

    @Nested
    public class ShouldAssignProjectsToJuries {

        @Test
        void given_no_jury() {
            // Given
            final Committee.Id committeeId = Committee.Id.random();
            final List<CommitteeApplicationLinkView> committeeApplicationLinks = List.of(
                    CommitteeApplicationLinkView.builder()
                            .applicant(ProjectLeaderLinkView.builder().build())
                            .projectShortView(projectStub())
                            .build(),
                    CommitteeApplicationLinkView.builder()
                            .applicant(ProjectLeaderLinkView.builder().build())
                            .projectShortView(projectStub())
                            .build()
            );
            final int votePerJury = 1;
            final List<RegisteredContributorLinkView> juries = List.of();
            final List<JuryCriteria> juryCriteria = List.of();
            final CommitteeView committeeView = new CommitteeView(
                    committeeId, faker.rickAndMorty().character(), faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()),
                    faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()), Committee.Status.OPEN_TO_VOTES, null, List.of(),
                    committeeApplicationLinks, juries, juryCriteria, votePerJury, null
            );

            // When
            when(committeeStoragePort.findById(committeeId))
                    .thenReturn(Optional.of(
                            committeeView
                    ));


            // Then
            assertThrowsExactly(OnlyDustException.class,
                    () -> committeeService.updateStatus(committeeId, Committee.Status.OPEN_TO_VOTES),
                    "Not enough juries or vote per jury to cover all projects");
        }

        @Test
        void given_not_enough_juries_compared_to_projects() {
            // Given
            final Committee.Id committeeId = Committee.Id.random();
            final List<CommitteeApplicationLinkView> committeeApplicationLinks = List.of(
                    CommitteeApplicationLinkView.builder()
                            .applicant(ProjectLeaderLinkView.builder().build())
                            .projectShortView(projectStub())
                            .build(),
                    CommitteeApplicationLinkView.builder()
                            .applicant(ProjectLeaderLinkView.builder().build())
                            .projectShortView(projectStub())
                            .build()
            );
            final int votePerJury = 1;
            final List<RegisteredContributorLinkView> juries = List.of(
                    userStub());
            final List<JuryCriteria> juryCriteria = List.of();
            final CommitteeView committeeView = new CommitteeView(
                    committeeId, faker.rickAndMorty().character(), faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()),
                    faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()), Committee.Status.OPEN_TO_VOTES, null, List.of(),
                    committeeApplicationLinks, juries, juryCriteria, votePerJury, null
            );

            // When
            when(committeeStoragePort.findById(committeeId))
                    .thenReturn(Optional.of(
                            committeeView
                    ));


            // Then
            assertThrowsExactly(OnlyDustException.class,
                    () -> committeeService.updateStatus(committeeId, Committee.Status.OPEN_TO_VOTES),
                    "Not enough juries or vote per jury to cover all projects");
        }

        @Test
        void given_no_jury_criteria() {
            final Committee.Id committeeId = Committee.Id.random();
            final List<CommitteeApplicationLinkView> committeeApplicationLinks = List.of(
                    CommitteeApplicationLinkView.builder()
                            .applicant(ProjectLeaderLinkView.builder().build())
                            .projectShortView(projectStub())
                            .build(),
                    CommitteeApplicationLinkView.builder()
                            .applicant(ProjectLeaderLinkView.builder().build())
                            .projectShortView(projectStub())
                            .build()
            );
            final int votePerJury = 2;
            final RegisteredContributorLinkView jury1 = userStub();
            final List<RegisteredContributorLinkView> juries = List.of(
                    jury1);
            final List<JuryCriteria> juryCriteria = List.of();
            final CommitteeView committeeView = new CommitteeView(
                    committeeId, faker.rickAndMorty().character(), faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()),
                    faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()), Committee.Status.OPEN_TO_VOTES, null, List.of(),
                    committeeApplicationLinks, juries, juryCriteria, votePerJury, null
            );

            // When
            when(committeeStoragePort.findById(committeeId))
                    .thenReturn(Optional.of(
                            committeeView
                    ));

            // Then
            assertThrowsExactly(OnlyDustException.class,
                    () -> committeeService.updateStatus(committeeId, Committee.Status.OPEN_TO_VOTES),
                    "Cannot assign juries to project given empty jury criteria");
        }

        @Test
        void given_1_jury_with_2_votes_for_2_projects() {
            final Committee.Id committeeId = Committee.Id.random();
            final List<CommitteeApplicationLinkView> committeeApplicationLinks = List.of(
                    CommitteeApplicationLinkView.builder()
                            .applicant(ProjectLeaderLinkView.builder().build())
                            .projectShortView(projectStub())
                            .build(),
                    CommitteeApplicationLinkView.builder()
                            .applicant(ProjectLeaderLinkView.builder().build())
                            .projectShortView(projectStub())
                            .build()
            );
            final int votePerJury = 2;
            final RegisteredContributorLinkView jury1 = userStub();
            final List<RegisteredContributorLinkView> juries = List.of(
                    jury1);
            final List<JuryCriteria> juryCriteria = List.of(
                    new JuryCriteria(JuryCriteria.Id.random(), faker.pokemon().name())
            );
            final CommitteeView committeeView = new CommitteeView(
                    committeeId, faker.rickAndMorty().character(), faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()),
                    faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()), Committee.Status.OPEN_TO_VOTES, null, List.of(),
                    committeeApplicationLinks, juries, juryCriteria, votePerJury, null
            );

            // When
            when(committeeStoragePort.findById(committeeId))
                    .thenReturn(Optional.of(
                            committeeView
                    ));
            when(projectStoragePort.getProjectLedIdsForUser(jury1.getId())).thenReturn(List.of());
            when(projectStoragePort.getProjectContributedOnIdsForUser(jury1.getId())).thenReturn(List.of());
            committeeService.updateStatus(committeeId, Committee.Status.OPEN_TO_VOTES);

            // Then
            final ArgumentCaptor<List<JuryAssignment>> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
            verify(committeeStoragePort).saveJuryAssignments(listArgumentCaptor.capture());
        }

        @Test
        void given_juries_as_project_lead_and_contributor() {
            final Committee.Id committeeId = Committee.Id.random();
            final List<CommitteeApplicationLinkView> committeeApplicationLinks = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                committeeApplicationLinks.add(CommitteeApplicationLinkView.builder()
                        .applicant(ProjectLeaderLinkView.builder().build())
                        .projectShortView(projectStub())
                        .build());
            }
            final List<UUID> projectIds =
                    committeeApplicationLinks.stream().map(committeeApplicationLinkView -> committeeApplicationLinkView.projectShortView().id())
                            .toList();
            final int votePerJury = 5;
            final RegisteredContributorLinkView jury1 = userStub();
            final RegisteredContributorLinkView jury2 = userStub();
            final List<RegisteredContributorLinkView> juries = List.of(
                    jury1, jury2);
            final List<JuryCriteria> juryCriteria = List.of(
                    new JuryCriteria(JuryCriteria.Id.random(), faker.pokemon().name())
            );
            final CommitteeView committeeView = new CommitteeView(
                    committeeId, faker.rickAndMorty().character(), faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()),
                    faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()), Committee.Status.OPEN_TO_VOTES, null, List.of(),
                    committeeApplicationLinks, juries, juryCriteria, votePerJury, null
            );

            // When
            when(committeeStoragePort.findById(committeeId))
                    .thenReturn(Optional.of(
                            committeeView
                    ));

            when(projectStoragePort.getProjectLedIdsForUser(jury1.getId())).thenReturn(List.of(projectIds.get(0)));
            when(projectStoragePort.getProjectContributedOnIdsForUser(jury1.getId())).thenReturn(List.of(projectIds.get(1), projectIds.get(2)));

            when(projectStoragePort.getProjectLedIdsForUser(jury2.getId())).thenReturn(List.of(projectIds.get(3)));
            when(projectStoragePort.getProjectContributedOnIdsForUser(jury2.getId())).thenReturn(List.of(projectIds.get(4), projectIds.get(0)));

            // Then
            assertThrowsExactly(OnlyDustException.class,
                    () -> committeeService.updateStatus(committeeId, Committee.Status.OPEN_TO_VOTES),
                    "Not enough juries or vote per jury to cover all projects given some juries are project lead or contributor on application project");
        }


        @Test
        void given_enough_juries() {
            final Committee.Id committeeId = Committee.Id.random();
            final List<CommitteeApplicationLinkView> committeeApplicationLinks = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                committeeApplicationLinks.add(CommitteeApplicationLinkView.builder()
                        .applicant(ProjectLeaderLinkView.builder().build())
                        .projectShortView(projectStub())
                        .build());
            }
            final List<UUID> projectIds = committeeApplicationLinks.stream()
                    .map(committeeApplicationLinkView -> committeeApplicationLinkView.projectShortView().id())
                    .toList();
            final int votePerJury = 5;
            final RegisteredContributorLinkView jury1 = userStub();
            final RegisteredContributorLinkView jury2 = userStub();
            final RegisteredContributorLinkView jury3 = userStub();
            final List<RegisteredContributorLinkView> juries = List.of(
                    jury1, jury2, jury3);
            final List<JuryCriteria> juryCriteria = List.of(
                    new JuryCriteria(JuryCriteria.Id.random(), faker.pokemon().name())
            );
            final CommitteeView committeeView = new CommitteeView(
                    committeeId, faker.rickAndMorty().character(), faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()),
                    faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()), Committee.Status.OPEN_TO_VOTES, null, List.of(),
                    committeeApplicationLinks, juries, juryCriteria, votePerJury, null
            );

            // When
            when(committeeStoragePort.findById(committeeId))
                    .thenReturn(Optional.of(
                            committeeView
                    ));

            when(projectStoragePort.getProjectLedIdsForUser(jury1.getId())).thenReturn(List.of(projectIds.get(0)));
            when(projectStoragePort.getProjectContributedOnIdsForUser(jury1.getId())).thenReturn(List.of(projectIds.get(1), projectIds.get(2)));

            when(projectStoragePort.getProjectLedIdsForUser(jury2.getId())).thenReturn(List.of(projectIds.get(3)));
            when(projectStoragePort.getProjectContributedOnIdsForUser(jury2.getId())).thenReturn(List.of(projectIds.get(4), projectIds.get(0)));

            when(projectStoragePort.getProjectLedIdsForUser(jury2.getId())).thenReturn(List.of(projectIds.get(5)));
            when(projectStoragePort.getProjectContributedOnIdsForUser(jury2.getId())).thenReturn(List.of(projectIds.get(6), projectIds.get(7)));
            committeeService.updateStatus(committeeId, Committee.Status.OPEN_TO_VOTES);

            // Then
            final ArgumentCaptor<List<JuryAssignment>> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
            verify(committeeStoragePort).saveJuryAssignments(listArgumentCaptor.capture());
            final List<JuryAssignment> assignments = listArgumentCaptor.getValue();
            assertEquals(votePerJury * juries.size() * committeeView.juryCriteria().size(), assignments.size());
        }

        private RegisteredContributorLinkView userStub() {
            return RegisteredContributorLinkView.builder()
                    .id(UUID.randomUUID())
                    .login(faker.rickAndMorty().character())
                    .avatarUrl(faker.internet().url())
                    .githubUserId(faker.number().randomNumber())
                    .build();
        }

        private ProjectShortView projectStub() {
            return ProjectShortView.builder()
                    .id(UUID.randomUUID())
                    .logoUrl(faker.internet().url())
                    .shortDescription(faker.rickAndMorty().location())
                    .name(faker.lordOfTheRings().character())
                    .visibility(ProjectVisibility.PUBLIC)
                    .slug(faker.gameOfThrones().character())
                    .build();
        }
    }
}
