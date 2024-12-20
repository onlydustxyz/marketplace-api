package onlydust.com.marketplace.project.domain.job;

import com.github.javafaker.Faker;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.model.event.OnGithubIssueAssigned;
import onlydust.com.marketplace.kernel.model.event.OnPullRequestCreated;
import onlydust.com.marketplace.kernel.model.event.OnPullRequestMerged;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubComment;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.event.*;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.port.output.TrackingEventPublisher;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TrackingEventPublisherOutboxConsumerTest {
    private final TrackingEventPublisher trackingEventPublisher = mock(TrackingEventPublisher.class);
    private final UserStoragePort userStoragePort = mock(UserStoragePort.class);
    private final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
    private final TrackingEventPublisherOutboxConsumer trackingEventPublisherOutboxConsumer = new TrackingEventPublisherOutboxConsumer(
            trackingEventPublisher, userStoragePort, projectStoragePort
    );

    private final Faker faker = new Faker();

    private final Long githubUserId = faker.number().randomNumber();
    private final Long githubRepoId = faker.number().randomNumber();

    @BeforeEach
    void setUp() {
        reset(trackingEventPublisher, userStoragePort, projectStoragePort);
    }

    @Nested
    class GivenARepoNotLinkedToAProject {
        @BeforeEach
        void setUp() {
            when(projectStoragePort.findProjectIdsByRepoId(any())).thenReturn(List.of());
        }

        @ParameterizedTest
        @ValueSource(classes = {OnGithubIssueAssigned.class, OnPullRequestCreated.class, OnPullRequestMerged.class})
        void should_not_publish_github_events(Class<? extends Event> eventClass) {
            // Given
            final var event = fakeEvent(eventClass);

            // When
            trackingEventPublisherOutboxConsumer.process(event);

            // Then
            verify(trackingEventPublisher, never()).publish(any());
        }


        @Test
        void should_forward_unknown_events() {
            // Given
            final var event = fakeEvent(TestEvent.class);

            // When
            trackingEventPublisherOutboxConsumer.process(event);

            // Then
            verify(trackingEventPublisher).publish(event);
        }
    }

    @Nested
    class GivenNonRegisteredUser {

        protected ProjectId projectId;

        @BeforeEach
        void setUp() {
            projectId = ProjectId.random();
            when(userStoragePort.getRegisteredUserByGithubId(githubUserId)).thenReturn(Optional.empty());
            when(projectStoragePort.findProjectIdsByRepoId(anyLong())).thenReturn(List.of(projectId, ProjectId.random()));
        }

        @ParameterizedTest
        @ValueSource(classes = {OnPullRequestCreated.class, OnPullRequestMerged.class})
        void should_not_publish_github_events(Class<? extends Event> eventClass) {
            // Given
            final var event = fakeEvent(eventClass);

            // When
            trackingEventPublisherOutboxConsumer.process(event);

            // Then
            verifyNoInteractions(trackingEventPublisher);
        }

        @Test
        void should_publish_github_issue_assigned_events() {
            // Given
            final var event = (OnGithubIssueAssigned) fakeEvent(OnGithubIssueAssigned.class);

            // When
            trackingEventPublisherOutboxConsumer.process(event);

            // Then
            final var trackingEventCaptor = ArgumentCaptor.forClass(OnGithubIssueAssignedTrackingEvent.class);
            verify(trackingEventPublisher).publish(trackingEventCaptor.capture());
            final var capturedTrackingEvent = trackingEventCaptor.getValue();
            assertThat(capturedTrackingEvent.issueId()).isEqualTo(event.id());
            assertThat(capturedTrackingEvent.assigneeGithubId()).isEqualTo(event.assigneeId());
            assertThat(capturedTrackingEvent.assigneeUserId()).isNull();
            assertThat(capturedTrackingEvent.createdAt()).isEqualTo(event.createdAt());
            assertThat(capturedTrackingEvent.assignedAt()).isEqualTo(event.assignedAt());
            assertThat(capturedTrackingEvent.isGoodFirstIssue()).isFalse();
        }

        @Test
        void should_publish_on_application_created() {
            // Given
            final var application = new Application(Application.Id.random(),
                    ProjectId.random(),
                    githubUserId,
                    Application.Origin.MARKETPLACE,
                    faker.date().past(3, TimeUnit.DAYS).toInstant().atZone(ZoneOffset.UTC),
                    GithubIssue.Id.random(),
                    GithubComment.Id.random(),
                    faker.lorem().sentence());

            // When
            trackingEventPublisherOutboxConsumer.process(OnApplicationCreated.of(application));

            // Then
            final var trackingEventCaptor = ArgumentCaptor.forClass(OnApplicationCreatedTrackingEvent.class);
            verify(trackingEventPublisher).publish(trackingEventCaptor.capture());
            final var capturedTrackingEvent = trackingEventCaptor.getValue();
            assertThat(capturedTrackingEvent.applicationId()).isEqualTo(application.id());
            assertThat(capturedTrackingEvent.projectId()).isEqualTo(application.projectId());
            assertThat(capturedTrackingEvent.applicantGithubId()).isEqualTo(application.applicantId());
            assertThat(capturedTrackingEvent.applicantUserId()).isNull();
            assertThat(capturedTrackingEvent.origin()).isEqualTo(application.origin());
            assertThat(capturedTrackingEvent.appliedAt()).isEqualTo(application.appliedAt());
            assertThat(capturedTrackingEvent.issueId()).isEqualTo(application.issueId());
        }

        @Test
        void should_forward_unknown_events() {
            // Given
            final var event = fakeEvent(TestEvent.class);

            // When
            trackingEventPublisherOutboxConsumer.process(event);

            // Then
            verify(trackingEventPublisher).publish(event);
        }
    }

    @Nested
    class GivenARegisteredUser {
        final AuthenticatedUser user = AuthenticatedUser.builder()
                .githubUserId(githubUserId)
                .id(UserId.random())
                .build();

        protected ProjectId projectId;

        @BeforeEach
        void setUp() {
            projectId = ProjectId.random();
            when(userStoragePort.getRegisteredUserByGithubId(githubUserId)).thenReturn(Optional.of(user));
            when(projectStoragePort.findProjectIdsByRepoId(githubRepoId)).thenReturn(List.of(projectId, ProjectId.random()));
        }

        @Test
        void should_publish_github_issue_assigned_events() {
            // Given
            final var event = (OnGithubIssueAssigned) fakeEvent(OnGithubIssueAssigned.class);

            // When
            trackingEventPublisherOutboxConsumer.process(event);

            // Then
            final var trackingEventCaptor = ArgumentCaptor.forClass(OnGithubIssueAssignedTrackingEvent.class);
            verify(trackingEventPublisher).publish(trackingEventCaptor.capture());
            final var capturedTrackingEvent = trackingEventCaptor.getValue();
            assertThat(capturedTrackingEvent.issueId()).isEqualTo(event.id());
            assertThat(capturedTrackingEvent.assigneeGithubId()).isEqualTo(event.assigneeId());
            assertThat(capturedTrackingEvent.assigneeUserId()).isEqualTo(user.id());
            assertThat(capturedTrackingEvent.createdAt()).isEqualTo(event.createdAt());
            assertThat(capturedTrackingEvent.assignedAt()).isEqualTo(event.assignedAt());
            assertThat(capturedTrackingEvent.isGoodFirstIssue()).isFalse();
        }

        @Test
        void should_publish_pull_request_created_events() {
            // Given
            final var event = (OnPullRequestCreated) fakeEvent(OnPullRequestCreated.class);

            // When
            trackingEventPublisherOutboxConsumer.process(event);

            // Then
            final var trackingEventCaptor = ArgumentCaptor.forClass(OnPullRequestCreatedTrackingEvent.class);
            verify(trackingEventPublisher).publish(trackingEventCaptor.capture());
            final var capturedTrackingEvent = trackingEventCaptor.getValue();
            assertThat(capturedTrackingEvent.pullRequestId()).isEqualTo(event.id());
            assertThat(capturedTrackingEvent.authorGithubId()).isEqualTo(event.authorId());
            assertThat(capturedTrackingEvent.authorUserId()).isEqualTo(user.id());
            assertThat(capturedTrackingEvent.createdAt()).isEqualTo(event.createdAt());
        }

        @Test
        void should_publish_pull_request_merged_events() {
            // Given
            final var event = (OnPullRequestMerged) fakeEvent(OnPullRequestMerged.class);

            // When
            trackingEventPublisherOutboxConsumer.process(event);

            // Then
            final var trackingEventCaptor = ArgumentCaptor.forClass(OnPullRequestMergedTrackingEvent.class);
            verify(trackingEventPublisher).publish(trackingEventCaptor.capture());
            final var capturedTrackingEvent = trackingEventCaptor.getValue();
            assertThat(capturedTrackingEvent.pullRequestId()).isEqualTo(event.id());
            assertThat(capturedTrackingEvent.authorGithubId()).isEqualTo(event.authorId());
            assertThat(capturedTrackingEvent.authorUserId()).isEqualTo(user.id());
            assertThat(capturedTrackingEvent.createdAt()).isEqualTo(event.createdAt());
            assertThat(capturedTrackingEvent.mergedAt()).isEqualTo(event.mergedAt());
        }

        @ParameterizedTest
        @ValueSource(strings = {"good first issue", "good-first-issue", "GoodFirstIssue", "Good first issue for beginners"})
        void should_detect_good_first_issues(String goodFirstIssueLabel) {
            // Given
            final var event = OnGithubIssueAssigned.builder()
                    .id(faker.random().nextLong())
                    .repoId(githubRepoId)
                    .assigneeId(githubUserId)
                    .labels(Set.of("foo", "bar", goodFirstIssueLabel, "baz"))
                    .createdAt(faker.date().birthday().toInstant().atZone(ZoneOffset.UTC))
                    .assignedAt(faker.date().birthday().toInstant().atZone(ZoneOffset.UTC))
                    .assignedById(faker.number().randomNumber())
                    .build();

            // When
            trackingEventPublisherOutboxConsumer.process(event);

            // Then
            final var trackingEventCaptor = ArgumentCaptor.forClass(OnGithubIssueAssignedTrackingEvent.class);
            verify(trackingEventPublisher).publish(trackingEventCaptor.capture());
            final var capturedTrackingEvent = trackingEventCaptor.getValue();
            assertThat(capturedTrackingEvent.isGoodFirstIssue()).isTrue();
        }

        @Test
        void should_publish_on_application_created() {
            // Given
            final var application = new Application(Application.Id.random(),
                    ProjectId.random(),
                    githubUserId,
                    Application.Origin.MARKETPLACE,
                    faker.date().past(3, TimeUnit.DAYS).toInstant().atZone(ZoneOffset.UTC),
                    GithubIssue.Id.random(),
                    GithubComment.Id.random(),
                    faker.lorem().sentence());

            // When
            trackingEventPublisherOutboxConsumer.process(OnApplicationCreated.of(application));

            // Then
            final var trackingEventCaptor = ArgumentCaptor.forClass(OnApplicationCreatedTrackingEvent.class);
            verify(trackingEventPublisher).publish(trackingEventCaptor.capture());
            final var capturedTrackingEvent = trackingEventCaptor.getValue();
            assertThat(capturedTrackingEvent.applicationId()).isEqualTo(application.id());
            assertThat(capturedTrackingEvent.projectId()).isEqualTo(application.projectId());
            assertThat(capturedTrackingEvent.applicantGithubId()).isEqualTo(application.applicantId());
            assertThat(capturedTrackingEvent.applicantUserId()).isEqualTo(user.id());
            assertThat(capturedTrackingEvent.origin()).isEqualTo(application.origin());
            assertThat(capturedTrackingEvent.appliedAt()).isEqualTo(application.appliedAt());
            assertThat(capturedTrackingEvent.issueId()).isEqualTo(application.issueId());
        }
    }

    @NoArgsConstructor
    private static class TestEvent extends Event {
    }

    @SneakyThrows
    private Event fakeEvent(Class<? extends Event> eventClass) {
        if (eventClass.equals(OnGithubIssueAssigned.class))
            return OnGithubIssueAssigned.builder()
                    .id(faker.number().randomNumber())
                    .repoId(githubRepoId)
                    .assigneeId(githubUserId)
                    .labels(new HashSet<>(faker.lorem().words()))
                    .createdAt(faker.date().birthday().toInstant().atZone(ZoneOffset.UTC))
                    .assignedAt(faker.date().birthday().toInstant().atZone(ZoneOffset.UTC))
                    .assignedById(faker.number().randomNumber())
                    .build();

        if (eventClass.equals(OnPullRequestCreated.class))
            return OnPullRequestCreated.builder()
                    .id(faker.number().randomNumber())
                    .repoId(githubRepoId)
                    .authorId(githubUserId)
                    .createdAt(faker.date().birthday().toInstant().atZone(ZoneOffset.UTC))
                    .build();

        if (eventClass.equals(OnPullRequestMerged.class))
            return OnPullRequestMerged.builder()
                    .id(faker.number().randomNumber())
                    .repoId(githubRepoId)
                    .authorId(githubUserId)
                    .createdAt(faker.date().birthday().toInstant().atZone(ZoneOffset.UTC))
                    .mergedAt(faker.date().birthday().toInstant().atZone(ZoneOffset.UTC))
                    .build();

        return eventClass.getConstructor().newInstance();
    }
}