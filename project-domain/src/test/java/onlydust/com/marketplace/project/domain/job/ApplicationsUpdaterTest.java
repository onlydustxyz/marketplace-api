package onlydust.com.marketplace.project.domain.job;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.model.event.OnGithubCommentCreated;
import onlydust.com.marketplace.kernel.model.event.OnGithubCommentEdited;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubComment;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ApplicationsUpdaterTest {
    final UserStoragePort userStoragePort = mock(UserStoragePort.class);
    final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
    final ApplicationsUpdater applicationsUpdater = new ApplicationsUpdater(projectStoragePort, userStoragePort);

    final Faker faker = new Faker();

    final Project project1 = Project.builder()
            .id(UUID.randomUUID())
            .name(faker.app().name())
            .build();

    final Project project2 = Project.builder()
            .id(UUID.randomUUID())
            .name(faker.app().name())
            .build();

    @BeforeEach
    void setup() {
        reset(userStoragePort, projectStoragePort);
    }

    @Nested
    class OnGithubCommentCreatedProcessing {
        final OnGithubCommentCreated event = OnGithubCommentCreated.builder()
                .id(GithubComment.Id.random().value())
                .repoId(faker.number().randomNumber())
                .issueId(GithubIssue.Id.random().value())
                .authorId(faker.number().randomNumber())
                .createdAt(faker.date().birthday().toInstant().atZone(ZoneOffset.UTC))
                .body(faker.lorem().sentence())
                .build();

        @BeforeEach
        void setup() {
            when(projectStoragePort.findProjectsByRepoId(event.repoId())).thenReturn(List.of(project1, project2));
            when(userStoragePort.findApplications(event.authorId(), GithubIssue.Id.of(event.issueId()))).thenReturn(List.of());
        }

        @Test
        void should_not_create_applications_without_project() {
            // Given
            when(projectStoragePort.findProjectsByRepoId(event.repoId())).thenReturn(List.of());

            // When
            applicationsUpdater.process(event);

            // Then
            verify(userStoragePort, never()).save(any(Application[].class));
        }

        @Test
        void should_not_create_application_if_already_applied() {
            // Given
            final var existingApplication = new Application(Application.Id.random(),
                    project1.getId(),
                    event.authorId(),
                    Application.Origin.MARKETPLACE,
                    faker.date().past(3, TimeUnit.DAYS).toInstant().atZone(ZoneOffset.UTC),
                    GithubIssue.Id.of(event.issueId()),
                    GithubComment.Id.of(event.id()),
                    faker.lorem().sentence(),
                    faker.lorem().sentence());

            when(userStoragePort.findApplications(event.authorId(), GithubIssue.Id.of(event.issueId()))).thenReturn(List.of(existingApplication));

            // When
            applicationsUpdater.process(event);

            // Then
            verify(userStoragePort, never()).save(any(Application[].class));
        }

        @Test
        void should_create_application() {
            // When
            applicationsUpdater.process(event);

            // Then
            final var applicationsCaptor = ArgumentCaptor.forClass(Application[].class);
            verify(userStoragePort).save(applicationsCaptor.capture());
            final var applications = applicationsCaptor.getValue();
            assertThat(applications).hasSize(2);

            assertThat(Arrays.stream(applications).map(Application::projectId)).containsExactlyInAnyOrder(project1.getId(), project2.getId());
            assertThat(applications).allMatch(application -> application.issueId().value().equals(event.issueId()));
            assertThat(applications).allMatch(application -> application.applicantId().equals(event.authorId()));
            assertThat(applications).allMatch(application -> application.origin().equals(Application.Origin.GITHUB));
            assertThat(applications).allMatch(application -> application.commentId().value().equals(event.id()));
            assertThat(applications).allMatch(application -> application.appliedAt().equals(event.createdAt()));
        }
    }

    @Nested
    class OnGithubCommentEditedProcessing {
        final OnGithubCommentEdited event = OnGithubCommentEdited.builder()
                .id(GithubComment.Id.random().value())
                .issueId(GithubIssue.Id.random().value())
                .authorId(faker.number().randomNumber())
                .build();

        @BeforeEach
        void setup() {
        }

        @Test
        void should_not_update_applications_modified_on_the_marketplace() {
            
        }

    }
}
