package onlydust.com.marketplace.project.domain.service;

import com.github.javafaker.Faker;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.JuryAssignment;
import onlydust.com.marketplace.project.domain.model.JuryCriteria;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;
import onlydust.com.marketplace.project.domain.port.input.CommitteeObserverPort;
import onlydust.com.marketplace.project.domain.port.output.CommitteeStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        committeeService.createCommittee(name, applicationStartDate, applicationEndDate);

        // Then
        final ArgumentCaptor<Committee> committeeArgumentCaptor = ArgumentCaptor.forClass(Committee.class);
        verify(committeeStoragePort).save(committeeArgumentCaptor.capture());
        assertEquals(name, committeeArgumentCaptor.getValue().name());
        assertEquals(applicationEndDate, committeeArgumentCaptor.getValue().applicationEndDate());
        assertEquals(applicationStartDate, committeeArgumentCaptor.getValue().applicationStartDate());
        assertEquals(Committee.Status.DRAFT, committeeArgumentCaptor.getValue().status());
        assertNotNull(committeeArgumentCaptor.getValue().id());
    }

    @Nested
    public class ShouldCreateUpdateApplication {

        @Test
        void given_a_committee_not_existing() {
            // Given
            final var committeeId = Committee.Id.random();

            // When
            when(committeeStoragePort.findById(committeeId)).thenReturn(Optional.empty());

            // Then
            assertThatThrownBy(() -> committeeService.createUpdateApplicationForCommittee(committeeId, new Committee.Application(UserId.random(),
                    ProjectId.random(), List.of())))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Committee %s was not found".formatted(committeeId.value().toString()));
        }

        @Test
        void given_a_user_not_project_lead() {
            // Given
            final var committeeId = Committee.Id.random();
            final var userId = UserId.random();
            final var projectId = ProjectId.random();

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
            final var committeeId = Committee.Id.random();
            final var userId = UserId.random();
            final var projectId = ProjectId.random();

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
            final var committeeId = Committee.Id.random();
            final var userId = UserId.random();
            final var projectId = ProjectId.random();

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
            final var committeeId = Committee.Id.random();
            final var userId = UserId.random();
            final var projectId = ProjectId.random();

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
            final var committeeId = Committee.Id.random();
            final var userId = UserId.random();
            final var projectId = ProjectId.random();
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
            final var committeeId = Committee.Id.random();
            final var userId = UserId.random();
            final var projectId = ProjectId.random();
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

    @ParameterizedTest
    @EnumSource(Committee.Status.class)
    void should_update_committee(Committee.Status status) {
        // Given
        final var projectQuestions = List.of(
                new ProjectQuestion("Q1", false),
                new ProjectQuestion("Q2", true));

        final var existingCommittee = Committee.builder()
                .id(Committee.Id.random())
                .name(faker.rickAndMorty().location())
                .status(status)
                .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .projectQuestions(projectQuestions)
                .votePerJury(1)
                .projectApplications(Map.of(
                        ProjectId.random(), new Committee.Application(UserId.random(), ProjectId.random(), List.of()),
                        ProjectId.random(), new Committee.Application(UserId.random(), ProjectId.random(), List.of())
                ))
                .build();

        final var committee = Committee.builder()
                .id(existingCommittee.id())
                .name(faker.rickAndMorty().location())
                .status(status)
                .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .projectQuestions(projectQuestions)
                .votePerJury(2)
                .build();

        when(committeeStoragePort.findById(existingCommittee.id())).thenReturn(Optional.of(existingCommittee));

        // When
        committeeService.update(committee);

        // Then
        final var committeeCaptor = ArgumentCaptor.forClass(Committee.class);
        verify(committeeStoragePort).save(committeeCaptor.capture());
        final var updatedCommittee = committeeCaptor.getValue();
        assertThat(updatedCommittee.id()).isEqualTo(existingCommittee.id());
        assertThat(updatedCommittee.name()).isEqualTo(committee.name());
        assertThat(updatedCommittee.status()).isEqualTo(existingCommittee.status());
        assertThat(updatedCommittee.applicationStartDate()).isEqualTo(committee.applicationStartDate());
        assertThat(updatedCommittee.applicationEndDate()).isEqualTo(committee.applicationEndDate());
        assertThat(updatedCommittee.juryIds()).isEqualTo(committee.juryIds());
        assertThat(updatedCommittee.votePerJury()).isEqualTo(committee.votePerJury());
        assertThat(updatedCommittee.projectApplications()).isEqualTo(existingCommittee.projectApplications());
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
                .hasMessage("Project questions cannot be updated");

        // Then
        verify(committeeStoragePort, never()).save(committee);
    }


    @ParameterizedTest
    @EnumSource(value = Committee.Status.class, names = {"DRAFT", "OPEN_TO_APPLICATIONS"}, mode = EnumSource.Mode.EXCLUDE)
    void should_prevent_jury_update_if_not_draft_or_open_to_application(Committee.Status status) {
        // Given
        final var existingCommittee = Committee.builder()
                .id(Committee.Id.random())
                .name(faker.rickAndMorty().location())
                .status(status)
                .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .juryIds(List.of(UserId.random(), UserId.random()))
                .build();

        final var committee = existingCommittee.toBuilder()
                .juryIds(List.of(UserId.random(), UserId.random()))
                .build();

        when(committeeStoragePort.findById(existingCommittee.id())).thenReturn(Optional.of(existingCommittee));

        // When
        assertThatThrownBy(() -> committeeService.update(committee))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Juries cannot be updated");

        // Then
        verify(committeeStoragePort, never()).save(committee);
    }

    @ParameterizedTest
    @EnumSource(value = Committee.Status.class, names = {"DRAFT", "OPEN_TO_APPLICATIONS"}, mode = EnumSource.Mode.EXCLUDE)
    void should_prevent_jury_criteria_update_if_not_draft_or_open_to_application(Committee.Status status) {
        // Given
        final var existingCommittee = Committee.builder()
                .id(Committee.Id.random())
                .name(faker.rickAndMorty().location())
                .status(status)
                .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                .juryCriteria(List.of(new JuryCriteria(JuryCriteria.Id.random(), faker.pokemon().name())))
                .build();

        final var committee = existingCommittee.toBuilder()
                .juryCriteria(List.of(new JuryCriteria(JuryCriteria.Id.random(), faker.pokemon().name())))
                .build();

        when(committeeStoragePort.findById(existingCommittee.id())).thenReturn(Optional.of(existingCommittee));

        // When
        assertThatThrownBy(() -> committeeService.update(committee))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Jury criteria cannot be updated");

        // Then
        verify(committeeStoragePort, never()).save(committee);
    }

    @Nested
    public class ShouldAssignProjectsToJuries {

        @Test
        void given_no_jury() {
            // Given
            final var committeeId = Committee.Id.random();
            final var committee = Committee.builder()
                    .id(committeeId)
                    .name(faker.rickAndMorty().character())
                    .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .status(Committee.Status.OPEN_TO_VOTES)
                    .votePerJury(1)
                    .projectApplications(Map.of(ProjectId.random(), new Committee.Application(UserId.random(), ProjectId.random(), List.of())))
                    .build();

            // When
            when(committeeStoragePort.findById(committeeId)).thenReturn(Optional.of(committee));

            // Then
            assertThatThrownBy(() -> committeeService.updateStatus(committeeId, Committee.Status.OPEN_TO_VOTES))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Committee %s must have some juries to assign them to project".formatted(committeeId));
        }

        @Test
        void given_not_enough_juries_compared_to_projects() {
            // Given
            final var committeeId = Committee.Id.random();
            final var committee = Committee.builder()
                    .id(committeeId)
                    .name(faker.rickAndMorty().character())
                    .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .status(Committee.Status.OPEN_TO_VOTES)
                    .votePerJury(1)
                    .juryIds(List.of(UserId.random()))
                    .juryCriteria(List.of(new JuryCriteria(JuryCriteria.Id.random(), faker.pokemon().name())))
                    .projectApplications(Map.of(ProjectId.random(), new Committee.Application(UserId.random(), ProjectId.random(), List.of()),
                            ProjectId.random(), new Committee.Application(UserId.random(), ProjectId.random(), List.of()),
                            ProjectId.random(), new Committee.Application(UserId.random(), ProjectId.random(), List.of()),
                            ProjectId.random(), new Committee.Application(UserId.random(), ProjectId.random(), List.of())))
                    .build();

            // When
            when(committeeStoragePort.findById(committeeId)).thenReturn(Optional.of(committee));

            // Then
            assertThatThrownBy(() -> committeeService.updateStatus(committeeId, Committee.Status.OPEN_TO_VOTES))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Not enough juries or vote per jury to cover all projects");
        }

        @Test
        void given_no_jury_criteria() {
            final var committeeId = Committee.Id.random();
            final var committee = Committee.builder()
                    .id(committeeId)
                    .name(faker.rickAndMorty().character())
                    .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .status(Committee.Status.OPEN_TO_VOTES)
                    .votePerJury(2)
                    .juryIds(List.of(UserId.random()))
                    .projectApplications(Map.of(ProjectId.random(), new Committee.Application(UserId.random(), ProjectId.random(), List.of())))
                    .build();

            // When
            when(committeeStoragePort.findById(committeeId)).thenReturn(Optional.of(committee));

            // Then
            assertThatThrownBy(() -> committeeService.updateStatus(committeeId, Committee.Status.OPEN_TO_VOTES))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Cannot assign juries to project given empty jury criteria");
        }

        @Test
        void given_no_application() {
            final var committeeId = Committee.Id.random();
            final var jury1 = UserId.random();
            final var committee = Committee.builder()
                    .id(committeeId)
                    .name(faker.rickAndMorty().character())
                    .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .status(Committee.Status.OPEN_TO_APPLICATIONS)
                    .votePerJury(2)
                    .juryIds(List.of(jury1))
                    .juryCriteria(List.of(new JuryCriteria(JuryCriteria.Id.random(), faker.pokemon().name())))
                    .build();

            // When
            when(committeeStoragePort.findById(committeeId)).thenReturn(Optional.of(committee));
            when(projectStoragePort.getProjectLedIdsForUser(jury1)).thenReturn(List.of());
            when(projectStoragePort.getProjectContributedOnIdsForUser(jury1)).thenReturn(List.of());
            assertThatThrownBy(() -> committeeService.updateStatus(committeeId, Committee.Status.OPEN_TO_VOTES))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Committee %s must have some project applications to assign juries to them".formatted(committeeId));
        }

        @Test
        void given_1_jury_with_2_votes_for_2_projects() {
            final var committeeId = Committee.Id.random();
            final var jury1 = UserId.random();
            final var committee = Committee.builder()
                    .id(committeeId)
                    .name(faker.rickAndMorty().character())
                    .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .status(Committee.Status.OPEN_TO_APPLICATIONS)
                    .votePerJury(2)
                    .juryIds(List.of(jury1))
                    .juryCriteria(List.of(new JuryCriteria(JuryCriteria.Id.random(), faker.pokemon().name())))
                    .projectApplications(Map.of(ProjectId.random(), new Committee.Application(UserId.random(), ProjectId.random(), List.of())))
                    .build();

            // When
            when(committeeStoragePort.findById(committeeId)).thenReturn(Optional.of(committee));
            when(projectStoragePort.getProjectLedIdsForUser(jury1)).thenReturn(List.of());
            when(projectStoragePort.getProjectContributedOnIdsForUser(jury1)).thenReturn(List.of());
            committeeService.updateStatus(committeeId, Committee.Status.OPEN_TO_VOTES);

            // Then
            final ArgumentCaptor<List<JuryAssignment>> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
            verify(committeeStoragePort).saveJuryAssignments(listArgumentCaptor.capture());
        }

        @Test
        void given_juries_as_project_lead_and_contributor() {
            final var committeeId = Committee.Id.random();
            final var projectIds = IntStream.range(0, 10).mapToObj(i -> ProjectId.random()).toList();
            final var jury1 = UserId.random();
            final var jury2 = UserId.random();

            final var committee = Committee.builder()
                    .id(committeeId)
                    .name(faker.rickAndMorty().character())
                    .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .status(Committee.Status.OPEN_TO_VOTES)
                    .votePerJury(5)
                    .juryIds(List.of(jury1, jury2))
                    .juryCriteria(List.of(new JuryCriteria(JuryCriteria.Id.random(), faker.pokemon().name())))
                    .projectApplications(projectIds.stream().collect(toMap(identity(), p -> new Committee.Application(UserId.random(), p, List.of()))))
                    .build();

            // When
            when(committeeStoragePort.findById(committeeId)).thenReturn(Optional.of(committee));

            when(projectStoragePort.getProjectLedIdsForUser(jury1)).thenReturn(List.of(projectIds.get(0)));
            when(projectStoragePort.getProjectContributedOnIdsForUser(jury1)).thenReturn(List.of(projectIds.get(1), projectIds.get(2)));

            when(projectStoragePort.getProjectLedIdsForUser(jury2)).thenReturn(List.of(projectIds.get(3)));
            when(projectStoragePort.getProjectContributedOnIdsForUser(jury2)).thenReturn(List.of(projectIds.get(4), projectIds.get(0)));

            // Then
            assertThatThrownBy(() -> committeeService.updateStatus(committeeId, Committee.Status.OPEN_TO_VOTES))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Not enough juries or vote per jury to cover all projects given some juries are project lead or contributor on application " +
                                "project");
        }


        @Test
        void given_enough_juries() {
            final var committeeId = Committee.Id.random();
            final var projectIds = IntStream.range(0, 10).mapToObj(i -> ProjectId.random()).toList();
            final int votePerJury = 5;
            final var jury1 = UserId.random();
            final var jury2 = UserId.random();
            final var jury3 = UserId.random();
            final var juries = List.of(jury1, jury2, jury3);
            final var juryCriteria = List.of(new JuryCriteria(JuryCriteria.Id.random(), faker.pokemon().name()));

            final var committee = Committee.builder()
                    .id(committeeId)
                    .name(faker.rickAndMorty().character())
                    .applicationStartDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .applicationEndDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()))
                    .status(Committee.Status.OPEN_TO_VOTES)
                    .votePerJury(votePerJury)
                    .juryIds(juries)
                    .juryCriteria(juryCriteria)
                    .projectApplications(projectIds.stream().collect(toMap(identity(), p -> new Committee.Application(UserId.random(), p, List.of()))))
                    .build();

            // When
            when(committeeStoragePort.findById(committeeId)).thenReturn(Optional.of(committee));

            when(projectStoragePort.getProjectLedIdsForUser(jury1)).thenReturn(List.of(projectIds.get(0)));
            when(projectStoragePort.getProjectContributedOnIdsForUser(jury1)).thenReturn(List.of(projectIds.get(1), projectIds.get(2)));

            when(projectStoragePort.getProjectLedIdsForUser(jury2)).thenReturn(List.of(projectIds.get(3)));
            when(projectStoragePort.getProjectContributedOnIdsForUser(jury2)).thenReturn(List.of(projectIds.get(4), projectIds.get(0)));

            when(projectStoragePort.getProjectLedIdsForUser(jury2)).thenReturn(List.of(projectIds.get(5)));
            when(projectStoragePort.getProjectContributedOnIdsForUser(jury2)).thenReturn(List.of(projectIds.get(6), projectIds.get(7)));
            committeeService.updateStatus(committeeId, Committee.Status.OPEN_TO_VOTES);

            // Then
            final ArgumentCaptor<List<JuryAssignment>> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
            verify(committeeStoragePort).saveJuryAssignments(listArgumentCaptor.capture());
            final List<JuryAssignment> assignments = listArgumentCaptor.getValue();
            assertEquals(votePerJury * juries.size() * juryCriteria.size(), assignments.size());
        }
    }

    @Nested
    public class ShouldAllocateBudgets {
        private final Committee committee = Committee.builder()
                .id(Committee.Id.random())
                .name(faker.rickAndMorty().character())
                .applicationStartDate(faker.date().past(5, 2, TimeUnit.DAYS).toInstant().atZone(ZoneOffset.UTC))
                .applicationEndDate(faker.date().past(2, TimeUnit.DAYS).toInstant().atZone(ZoneOffset.UTC))
                .status(Committee.Status.CLOSED)
                .build();
        private final UUID STRK = UUID.randomUUID();
        private final UUID ETH = UUID.randomUUID();

        @Test
        void given_a_non_existing_committee() {
            // Given
            when(committeeStoragePort.findById(committee.id())).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> committeeService.allocate(committee.id(), STRK, BigDecimal.TEN, 2))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Committee %s was not found".formatted(committee.id()));
        }

        @ParameterizedTest
        @EnumSource(value = Committee.Status.class, names = {"CLOSED"}, mode = EnumSource.Mode.EXCLUDE)
        void given_an_open_committee(Committee.Status status) {
            // Given
            when(committeeStoragePort.findById(committee.id())).thenReturn(Optional.of(committee.toBuilder().status(status).build()));

            // When
            assertThatThrownBy(() -> committeeService.allocate(committee.id(), STRK, BigDecimal.TEN, 2))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Committee %s must be closed to allocate budgets".formatted(committee.id()));
        }

        @Test
        void given_an_open_committee_with_missing_scores() {
            // Given
            when(committeeStoragePort.findById(committee.id())).thenReturn(Optional.of(committee.toBuilder().status(Committee.Status.OPEN_TO_VOTES).build()));
            when(committeeStoragePort.findJuryAssignments(committee.id())).thenReturn(List.of(
                    JuryAssignment.virgin(UserId.random(), committee.id(), ProjectId.random(), List.of(fakeCriteria(), fakeCriteria(), fakeCriteria())),
                    JuryAssignment.virgin(UserId.random(), committee.id(), ProjectId.random(), List.of(fakeCriteria(), fakeCriteria(), fakeCriteria())),
                    JuryAssignment.virgin(UserId.random(), committee.id(), ProjectId.random(), List.of(fakeCriteria(), fakeCriteria(), fakeCriteria()))
            ));

            // When
            assertThatThrownBy(() -> committeeService.updateStatus(committee.id(), Committee.Status.CLOSED))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Cannot close committee if a project score is missing");
        }

        @Test
        void a_STRK_budget() {
            // Given
            final var project1 = ProjectId.of("00000000-0000-0000-0000-000000000001");
            final var project2 = ProjectId.of("00000000-0000-0000-0000-000000000002");
            final var project3 = ProjectId.of("00000000-0000-0000-0000-000000000003");
            final var project4 = ProjectId.of("00000000-0000-0000-0000-000000000004");
            final var project5 = ProjectId.of("00000000-0000-0000-0000-000000000005");
            final var budget = BigDecimal.valueOf(15);

            when(committeeStoragePort.findById(committee.id())).thenReturn(Optional.of(committee));
            when(committeeStoragePort.findJuryAssignments(committee.id())).thenReturn(List.of(
                    JuryAssignment.withVotes(UserId.random(), committee.id(), project1, Map.of(JuryCriteria.Id.random(), 1)),
                    JuryAssignment.withVotes(UserId.random(), committee.id(), project2, Map.of(JuryCriteria.Id.random(), 2)),
                    JuryAssignment.withVotes(UserId.random(), committee.id(), project3, Map.of(JuryCriteria.Id.random(), 3)),
                    JuryAssignment.withVotes(UserId.random(), committee.id(), project4, Map.of(JuryCriteria.Id.random(), 4)),
                    JuryAssignment.withVotes(UserId.random(), committee.id(), project5, Map.of(JuryCriteria.Id.random(), 5))
            ));

            // When
            committeeService.allocate(committee.id(), STRK, budget, 2);

            // Then
            final var allocationsCaptor = ArgumentCaptor.forClass(Map.class);
            verify(committeeStoragePort).saveAllocations(eq(committee.id()), eq(STRK), allocationsCaptor.capture());
            final Map<ProjectId, BigDecimal> allocations = allocationsCaptor.getValue();
            assertThat(allocations).hasSize(3);
            assertThat(allocations.get(project3)).isEqualTo(BigDecimal.valueOf(1.67));
            assertThat(allocations.get(project4)).isEqualTo(BigDecimal.valueOf(5).setScale(2));
            assertThat(allocations.get(project5)).isEqualTo(BigDecimal.valueOf(8.33));
            assertThat(allocations.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add)).isEqualByComparingTo(budget);
        }

        @Test
        void an_ETH_budget() {
            // Given
            final var project1 = ProjectId.of("00000000-0000-0000-0000-000000000001");
            final var project2 = ProjectId.of("00000000-0000-0000-0000-000000000002");
            final var project3 = ProjectId.of("00000000-0000-0000-0000-000000000003");
            final var project4 = ProjectId.of("00000000-0000-0000-0000-000000000004");
            final var project5 = ProjectId.of("00000000-0000-0000-0000-000000000005");
            final var budget = BigDecimal.valueOf(0.1);

            when(committeeStoragePort.findById(committee.id())).thenReturn(Optional.of(committee));
            when(committeeStoragePort.findJuryAssignments(committee.id())).thenReturn(List.of(
                    JuryAssignment.withVotes(UserId.random(), committee.id(), project1, Map.of(JuryCriteria.Id.random(), 1)),
                    JuryAssignment.withVotes(UserId.random(), committee.id(), project2, Map.of(JuryCriteria.Id.random(), 2)),
                    JuryAssignment.withVotes(UserId.random(), committee.id(), project3, Map.of(JuryCriteria.Id.random(), 2)),
                    JuryAssignment.withVotes(UserId.random(), committee.id(), project4, Map.of(JuryCriteria.Id.random(), 3)),
                    JuryAssignment.withVotes(UserId.random(), committee.id(), project5, Map.of(JuryCriteria.Id.random(), 4))
            ));

            // When
            committeeService.allocate(committee.id(), ETH, budget, 6);

            // Then
            final var allocationsCaptor = ArgumentCaptor.forClass(Map.class);
            verify(committeeStoragePort).saveAllocations(eq(committee.id()), eq(ETH), allocationsCaptor.capture());
            final Map<ProjectId, BigDecimal> allocations = allocationsCaptor.getValue();
            assertThat(allocations).hasSize(2);
            assertThat(allocations.get(project4)).isEqualByComparingTo(BigDecimal.valueOf(0.025000));
            assertThat(allocations.get(project5)).isEqualByComparingTo(BigDecimal.valueOf(0.075000));
            assertThat(allocations.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add)).isEqualByComparingTo(budget);
        }

        @NonNull
        private JuryCriteria fakeCriteria() {
            return new JuryCriteria(JuryCriteria.Id.random(),
                    faker.pokemon().name());
        }
    }
}
