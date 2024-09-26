package onlydust.com.marketplace.project.domain.job;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.event.*;
import onlydust.com.marketplace.kernel.port.output.IndexerPort;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubComment;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.port.output.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ApplicationsUpdaterTest {
    final ProjectApplicationStoragePort projectApplicationStoragePort = mock(ProjectApplicationStoragePort.class);
    final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
    final LLMPort llmPort = mock(LLMPort.class);
    final IndexerPort indexerPort = mock(IndexerPort.class);
    final GithubStoragePort githubStoragePort = mock(GithubStoragePort.class);
    final ApplicationObserverPort applicationObserverPort = mock(ApplicationObserverPort.class);
    final ApplicationsUpdater applicationsUpdater = new ApplicationsUpdater(projectStoragePort, projectApplicationStoragePort, llmPort,
            indexerPort, githubStoragePort,
            applicationObserverPort);

    final Faker faker = new Faker();

    final ProjectId projectId1 = ProjectId.random();
    final ProjectId projectId2 = ProjectId.random();
    final GithubIssue issue = new GithubIssue(GithubIssue.Id.random(),
            faker.number().randomNumber(),
            faker.number().randomNumber(),
            faker.rickAndMorty().character(),
            faker.rickAndMorty().quote(),
            faker.internet().url(),
            faker.rickAndMorty().character(),
            0, faker.pokemon().name(), faker.pokemon().name(), List.of());

    @BeforeEach
    void setup() {
        reset(projectStoragePort, llmPort, indexerPort, githubStoragePort, applicationObserverPort);
    }

    @Nested
    class OnGithubCommentCreatedProcessing {
        final OnGithubCommentCreated event = OnGithubCommentCreated.builder()
                .id(GithubComment.Id.random().value())
                .repoId(issue.repoId())
                .issueId(issue.id().value())
                .authorId(faker.number().randomNumber())
                .createdAt(faker.date().birthday().toInstant().atZone(ZoneOffset.UTC))
                .body(faker.lorem().sentence())
                .build();

        @BeforeEach
        void setup() {
            when(projectStoragePort.findProjectIdsByRepoId(event.repoId())).thenReturn(List.of(projectId1, projectId2));
            when(projectApplicationStoragePort.findApplications(event.authorId(), GithubIssue.Id.of(event.issueId()))).thenReturn(List.of());
            when(githubStoragePort.findIssueById(issue.id())).thenReturn(Optional.of(issue));
        }

        @Test
        void should_not_create_applications_without_project() {
            // Given
            when(projectStoragePort.findProjectIdsByRepoId(event.repoId())).thenReturn(List.of());

            // When
            applicationsUpdater.process(event);

            // Then
            verify(projectApplicationStoragePort, never()).save(any(Application[].class));
            verifyNoInteractions(llmPort);
            verifyNoInteractions(applicationObserverPort);
        }

        @Test
        void should_not_create_application_if_already_applied() {
            // Given
            final var existingApplication = new Application(Application.Id.random(),
                    projectId1,
                    event.authorId(),
                    Application.Origin.MARKETPLACE,
                    faker.date().past(3, TimeUnit.DAYS).toInstant().atZone(ZoneOffset.UTC),
                    GithubIssue.Id.of(event.issueId()),
                    GithubComment.Id.of(event.id()),
                    faker.lorem().sentence(),
                    faker.lorem().sentence());

            when(projectApplicationStoragePort.findApplications(event.authorId(), GithubIssue.Id.of(event.issueId()))).thenReturn(List.of(existingApplication));

            // When
            applicationsUpdater.process(event);

            // Then
            verify(projectApplicationStoragePort, never()).save(any(Application[].class));
            verifyNoInteractions(llmPort);
            verifyNoInteractions(applicationObserverPort);
            verifyNoInteractions(indexerPort);
        }

        @Test
        void should_not_create_application_if_it_does_not_express_interest() {
            // Given
            when(llmPort.isCommentShowingInterestToContribute(event.body())).thenReturn(false);

            // When
            applicationsUpdater.process(event);

            // Then
            verify(projectApplicationStoragePort, never()).save(any(Application[].class));
            verifyNoInteractions(indexerPort);
            verifyNoInteractions(applicationObserverPort);
        }

        @Test
        void should_throw_if_issue_is_not_indexed() {
            // Given
            when(githubStoragePort.findIssueById(any())).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> applicationsUpdater.process(event))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Issue %s not found".formatted(event.issueId()));

            // Then
            verify(projectApplicationStoragePort, never()).save(any(Application[].class));
            verifyNoInteractions(applicationObserverPort);
            verifyNoInteractions(indexerPort);
            verifyNoInteractions(llmPort);
        }

        @Test
        void should_not_create_application_if_issue_is_assigned() {
            // Given
            when(githubStoragePort.findIssueById(any())).thenReturn(Optional.of(new GithubIssue(GithubIssue.Id.random(),
                    faker.number().randomNumber(),
                    faker.number().randomNumber(),
                    faker.rickAndMorty().character(),
                    faker.rickAndMorty().quote(),
                    faker.internet().url(),
                    faker.rickAndMorty().character(),
                    1, faker.pokemon().name(), faker.pokemon().name(), List.of())));

            // When
            applicationsUpdater.process(event);

            // Then
            verify(projectApplicationStoragePort, never()).save(any(Application[].class));
            verifyNoInteractions(applicationObserverPort);
            verifyNoInteractions(indexerPort);
            verifyNoInteractions(llmPort);
        }

        @Test
        void should_create_application() {
            // Given
            when(llmPort.isCommentShowingInterestToContribute(event.body())).thenReturn(true);

            // When
            applicationsUpdater.process(event);

            // Then
            final var applicationsCaptor = ArgumentCaptor.forClass(Application[].class);
            verify(projectApplicationStoragePort).save(applicationsCaptor.capture());
            final var applications = applicationsCaptor.getValue();
            assertThat(applications).hasSize(2);

            assertThat(Arrays.stream(applications).map(Application::projectId)).containsExactlyInAnyOrder(projectId1, projectId2);
            assertThat(applications).allMatch(application -> application.issueId().value().equals(event.issueId()));
            assertThat(applications).allMatch(application -> application.applicantId().equals(event.authorId()));
            assertThat(applications).allMatch(application -> application.origin().equals(Application.Origin.GITHUB));
            assertThat(applications).allMatch(application -> application.commentId().value().equals(event.id()));
            assertThat(applications).allMatch(application -> application.appliedAt().equals(event.createdAt()));
            assertThat(applications).allMatch(application -> application.motivations().equals(event.body()));
            assertThat(applications).allMatch(application -> application.problemSolvingApproach() == null);

            verify(indexerPort).indexUser(event.authorId());
            Arrays.stream(applications).forEach(application -> verify(applicationObserverPort).onApplicationCreated(application));
        }

        @Test
        void should_remove_quotes_when_analyzing_comments() {
            // Given
            when(llmPort.isCommentShowingInterestToContribute(any())).thenReturn(false);

            // When
            applicationsUpdater.process(OnGithubCommentCreated.builder()
                    .id(GithubComment.Id.random().value())
                    .repoId(issue.repoId())
                    .issueId(issue.id().value())
                    .authorId(faker.number().randomNumber())
                    .createdAt(faker.date().birthday().toInstant().atZone(ZoneOffset.UTC))
                    .body("""
                            > Can I work on this ?
                            
                            Yeah sure!
                            """)
                    .build());

            // Then
            verify(llmPort).isCommentShowingInterestToContribute("Yeah sure!");
        }
    }

    @Nested
    class OnGithubCommentEditedProcessing {
        final OnGithubCommentEdited event = OnGithubCommentEdited.builder()
                .id(GithubComment.Id.random().value())
                .issueId(issue.id().value())
                .repoId(issue.repoId())
                .updatedAt(faker.date().birthday().toInstant().atZone(ZoneOffset.UTC))
                .authorId(faker.number().randomNumber())
                .body(faker.lorem().sentence())
                .build();

        @BeforeEach
        void setup() {
            when(githubStoragePort.findIssueById(issue.id())).thenReturn(Optional.of(issue));
        }

        @Test
        void should_delete_github_applications_if_new_comment_does_not_express_interest() {
            // Given
            final var commentId = GithubComment.Id.of(event.id());
            final var existingApplications = List.of(
                    new Application(Application.Id.random(),
                            projectId1,
                            event.authorId(),
                            Application.Origin.MARKETPLACE,
                            faker.date().past(3, TimeUnit.DAYS).toInstant().atZone(ZoneOffset.UTC),
                            GithubIssue.Id.of(event.issueId()),
                            commentId,
                            faker.lorem().sentence(),
                            faker.lorem().sentence()),
                    new Application(Application.Id.random(),
                            projectId2,
                            event.authorId(),
                            Application.Origin.GITHUB,
                            faker.date().past(3, TimeUnit.DAYS).toInstant().atZone(ZoneOffset.UTC),
                            GithubIssue.Id.of(event.issueId()),
                            commentId,
                            faker.lorem().sentence(),
                            null)
            );

            when(projectApplicationStoragePort.findApplications(event.authorId(), GithubIssue.Id.of(event.issueId()))).thenReturn(existingApplications);
            when(projectApplicationStoragePort.findApplications(commentId)).thenReturn(existingApplications);
            when(llmPort.isCommentShowingInterestToContribute(event.body())).thenReturn(false);

            // When
            applicationsUpdater.process(event);

            // Then
            verify(projectApplicationStoragePort).findApplications(commentId);
            verify(projectApplicationStoragePort).deleteApplications(existingApplications.get(1).id());
            verifyNoInteractions(indexerPort);
        }

        @Test
        void should_create_applications_if_none_yet_and_new_comment_expresses_interest() {
            // Given
            when(projectApplicationStoragePort.findApplications(event.authorId(), GithubIssue.Id.of(event.issueId()))).thenReturn(List.of());
            when(projectStoragePort.findProjectIdsByRepoId(event.repoId())).thenReturn(List.of(projectId1, projectId2));
            when(llmPort.isCommentShowingInterestToContribute(event.body())).thenReturn(true);

            // When
            applicationsUpdater.process(event);

            // Then
            final var applicationsCaptor = ArgumentCaptor.forClass(Application[].class);
            verify(projectApplicationStoragePort).save(applicationsCaptor.capture());
            final var applications = applicationsCaptor.getValue();
            assertThat(applications).hasSize(2);

            assertThat(Arrays.stream(applications).map(Application::projectId)).containsExactlyInAnyOrder(projectId1, projectId2);
            assertThat(applications).allMatch(application -> application.issueId().value().equals(event.issueId()));
            assertThat(applications).allMatch(application -> application.applicantId().equals(event.authorId()));
            assertThat(applications).allMatch(application -> application.origin().equals(Application.Origin.GITHUB));
            assertThat(applications).allMatch(application -> application.commentId().value().equals(event.id()));
            assertThat(applications).allMatch(application -> application.appliedAt().equals(event.updatedAt()));
            assertThat(applications).allMatch(application -> application.motivations().equals(event.body()));
            assertThat(applications).allMatch(application -> application.problemSolvingApproach() == null);

            verify(indexerPort).indexUser(event.authorId());

            Arrays.stream(applications).forEach(application -> verify(applicationObserverPort).onApplicationCreated(application));
        }

        @Test
        void should_not_call_llm_if_not_needed() {
            // Given
            final var existingApplications = List.of(
                    new Application(Application.Id.random(),
                            projectId1,
                            event.authorId(),
                            Application.Origin.MARKETPLACE,
                            faker.date().past(3, TimeUnit.DAYS).toInstant().atZone(ZoneOffset.UTC),
                            GithubIssue.Id.of(event.issueId()),
                            GithubComment.Id.of(event.id()),
                            faker.lorem().sentence(),
                            faker.lorem().sentence())
            );
            when(projectApplicationStoragePort.findApplications(event.authorId(), GithubIssue.Id.of(event.issueId()))).thenReturn(existingApplications);

            // When
            applicationsUpdater.process(event);

            // Then
            verifyNoInteractions(llmPort);
            verify(projectApplicationStoragePort, never()).deleteApplications(any(Application.Id[].class));
            verify(projectApplicationStoragePort, never()).save(any(Application[].class));
            verifyNoInteractions(indexerPort);
        }
    }

    @Nested
    class OnGithubCommentDeletedProcessing {
        final OnGithubCommentDeleted event = OnGithubCommentDeleted.builder()
                .id(GithubComment.Id.random().value())
                .build();

        @Test
        void should_delete_github_applications() {
            // Given
            final var authorId = faker.number().randomNumber();
            final var issueId = GithubIssue.Id.random();

            final var existingApplications = List.of(
                    new Application(Application.Id.random(),
                            projectId1,
                            authorId,
                            Application.Origin.MARKETPLACE,
                            faker.date().past(3, TimeUnit.DAYS).toInstant().atZone(ZoneOffset.UTC),
                            issueId,
                            GithubComment.Id.of(event.id()),
                            faker.lorem().sentence(),
                            faker.lorem().sentence()),
                    new Application(Application.Id.random(),
                            projectId2,
                            authorId,
                            Application.Origin.GITHUB,
                            faker.date().past(3, TimeUnit.DAYS).toInstant().atZone(ZoneOffset.UTC),
                            issueId,
                            GithubComment.Id.of(event.id()),
                            faker.lorem().sentence(),
                            null)
            );

            when(projectApplicationStoragePort.findApplications(GithubComment.Id.of(event.id()))).thenReturn(existingApplications);

            // When
            applicationsUpdater.process(event);

            // Then
            verifyNoInteractions(llmPort);
            verify(projectApplicationStoragePort).deleteApplications(existingApplications.get(1).id());
        }
    }

    @Nested
    class OnGithubIssueDeletedProcessing {
        final OnGithubIssueDeleted event = OnGithubIssueDeleted.builder()
                .id(GithubIssue.Id.random().value())
                .build();

        @Test
        void should_delete_github_applications() {
            // When
            applicationsUpdater.process(event);

            // Then
            verifyNoInteractions(llmPort);
            verify(projectApplicationStoragePort).deleteApplicationsByIssueId(GithubIssue.Id.of(event.id()));
        }
    }

    @Nested
    class OnGithubIssueTransferredProcessing {
        final OnGithubIssueTransferred event = OnGithubIssueTransferred.builder()
                .id(GithubIssue.Id.random().value())
                .build();

        @Test
        void should_delete_github_applications() {
            // When
            applicationsUpdater.process(event);

            // Then
            verifyNoInteractions(llmPort);
            verify(projectApplicationStoragePort).deleteApplicationsByIssueId(GithubIssue.Id.of(event.id()));
        }
    }
}
