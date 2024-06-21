package onlydust.com.marketplace.project.domain.job;

import com.github.javafaker.Faker;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.event.OnGithubIssueAssigned;
import onlydust.com.marketplace.kernel.model.event.OnPullRequestCreated;
import onlydust.com.marketplace.kernel.model.event.OnPullRequestMerged;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubComment;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.User;
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
import java.util.*;
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
            when(projectStoragePort.isLinkedToAProject(any())).thenReturn(false);
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
        @BeforeEach
        void setUp() {
            when(userStoragePort.getUserByGithubId(githubUserId)).thenReturn(Optional.empty());
            when(projectStoragePort.isLinkedToAProject(any())).thenReturn(false);
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
    class GivenARegisteredUser {
        final User user = User.builder()
                .githubUserId(githubUserId)
                .id(UUID.randomUUID())
                .build();

        @BeforeEach
        void setUp() {
            when(userStoragePort.getUserByGithubId(githubUserId)).thenReturn(Optional.of(user));
            when(projectStoragePort.isLinkedToAProject(githubRepoId)).thenReturn(true);
        }

        @Test
        void should_publish_github_issue_assigned_events() {
            // Given
            final var event = (OnGithubIssueAssigned) fakeEvent(OnGithubIssueAssigned.class);
            when(userStoragePort.findScoredApplications(user.getGithubUserId(), GithubIssue.Id.of(event.id()))).thenReturn(List.of());

            // When
            trackingEventPublisherOutboxConsumer.process(event);

            // Then
            final var trackingEventCaptor = ArgumentCaptor.forClass(OnGithubIssueAssignedTrackingEvent.class);
            verify(trackingEventPublisher).publish(trackingEventCaptor.capture());
            final var capturedTrackingEvent = trackingEventCaptor.getValue();
            assertThat(capturedTrackingEvent.issueId()).isEqualTo(event.id());
            assertThat(capturedTrackingEvent.assigneeGithubId()).isEqualTo(event.assigneeId());
            assertThat(capturedTrackingEvent.assigneeUserId()).isEqualTo(user.getId());
            assertThat(capturedTrackingEvent.createdAt()).isEqualTo(event.createdAt());
            assertThat(capturedTrackingEvent.assignedAt()).isEqualTo(event.assignedAt());
            assertThat(capturedTrackingEvent.isGoodFirstIssue()).isFalse();
            assertThat(capturedTrackingEvent.availabilityScore()).isNull();
            assertThat(capturedTrackingEvent.bestProjectsSimilarityScore()).isNull();
            assertThat(capturedTrackingEvent.mainRepoLanguageUserScore()).isNull();
            assertThat(capturedTrackingEvent.projectFidelityScore()).isNull();
            assertThat(capturedTrackingEvent.recommendationScore()).isNull();
        }

        @Test
        void should_include_application_Score_in_github_issue_assigned_events() {
            // Given
            final var event = (OnGithubIssueAssigned) fakeEvent(OnGithubIssueAssigned.class);
            final var issueId = GithubIssue.Id.of(event.id());

            when(userStoragePort.findScoredApplications(user.getGithubUserId(), issueId))
                    .thenReturn(List.of(
                            new Application(Application.Id.random(),
                                    UUID.randomUUID(),
                                    user.getGithubUserId(),
                                    Application.Origin.MARKETPLACE,
                                    faker.date().past(3, TimeUnit.DAYS).toInstant().atZone(ZoneOffset.UTC),
                                    issueId,
                                    GithubComment.Id.random(),
                                    faker.lorem().sentence(),
                                    faker.lorem().sentence())
                                    .scored(70, 12, 34, 56, 89),
                            new Application(Application.Id.random(),
                                    UUID.randomUUID(),
                                    user.getGithubUserId(),
                                    Application.Origin.GITHUB,
                                    faker.date().past(3, TimeUnit.DAYS).toInstant().atZone(ZoneOffset.UTC),
                                    issueId,
                                    GithubComment.Id.random(),
                                    faker.lorem().sentence(),
                                    null)
                                    .scored(60, 23, 45, 67, 78)
                    ));

            // When
            trackingEventPublisherOutboxConsumer.process(event);

            // Then
            final var trackingEventCaptor = ArgumentCaptor.forClass(OnGithubIssueAssignedTrackingEvent.class);
            verify(trackingEventPublisher).publish(trackingEventCaptor.capture());
            final var capturedTrackingEvent = trackingEventCaptor.getValue();
            assertThat(capturedTrackingEvent.availabilityScore()).isEqualTo(70);
            assertThat(capturedTrackingEvent.bestProjectsSimilarityScore()).isEqualTo(12);
            assertThat(capturedTrackingEvent.mainRepoLanguageUserScore()).isEqualTo(34);
            assertThat(capturedTrackingEvent.projectFidelityScore()).isEqualTo(56);
            assertThat(capturedTrackingEvent.recommendationScore()).isEqualTo(89);
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
            assertThat(capturedTrackingEvent.authorUserId()).isEqualTo(user.getId());
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
            assertThat(capturedTrackingEvent.authorUserId()).isEqualTo(user.getId());
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
                    .labels(Set.of(faker.lorem().word(), faker.lorem().word(), goodFirstIssueLabel, faker.lorem().word()))
                    .createdAt(faker.date().birthday().toInstant().atZone(ZoneOffset.UTC))
                    .assignedAt(faker.date().birthday().toInstant().atZone(ZoneOffset.UTC))
                    .build();

            // When
            trackingEventPublisherOutboxConsumer.process(event);

            // Then
            final var trackingEventCaptor = ArgumentCaptor.forClass(OnGithubIssueAssignedTrackingEvent.class);
            verify(trackingEventPublisher).publish(trackingEventCaptor.capture());
            final var capturedTrackingEvent = trackingEventCaptor.getValue();
            assertThat(capturedTrackingEvent.isGoodFirstIssue()).isTrue();
        }
    }

    @Test
    void should_publish_on_application_created() {
        // Given
        final var application = new Application(Application.Id.random(),
                UUID.randomUUID(),
                githubUserId,
                Application.Origin.MARKETPLACE,
                faker.date().past(3, TimeUnit.DAYS).toInstant().atZone(ZoneOffset.UTC),
                GithubIssue.Id.random(),
                GithubComment.Id.random(),
                faker.lorem().sentence(),
                faker.lorem().sentence())
                .scored(70, 12, 34, 56, 89);

        when(userStoragePort.findScoredApplication(application.id())).thenReturn(Optional.of(application));

        // When
        trackingEventPublisherOutboxConsumer.process(OnApplicationCreated.of(application));

        // Then
        final var trackingEventCaptor = ArgumentCaptor.forClass(OnApplicationCreatedTrackingEvent.class);
        verify(trackingEventPublisher).publish(trackingEventCaptor.capture());
        final var capturedTrackingEvent = trackingEventCaptor.getValue();
        assertThat(capturedTrackingEvent.applicationId()).isEqualTo(application.id());
        assertThat(capturedTrackingEvent.projectId()).isEqualTo(application.projectId());
        assertThat(capturedTrackingEvent.applicantGithubId()).isEqualTo(application.applicantId());
        assertThat(capturedTrackingEvent.origin()).isEqualTo(application.origin());
        assertThat(capturedTrackingEvent.appliedAt()).isEqualTo(application.appliedAt());
        assertThat(capturedTrackingEvent.issueId()).isEqualTo(application.issueId());
        assertThat(capturedTrackingEvent.availabilityScore()).isEqualTo(application.availabilityScore());
        assertThat(capturedTrackingEvent.bestProjectsSimilarityScore()).isEqualTo(application.bestProjectsSimilarityScore());
        assertThat(capturedTrackingEvent.mainRepoLanguageUserScore()).isEqualTo(application.mainRepoLanguageUserScore());
        assertThat(capturedTrackingEvent.projectFidelityScore()).isEqualTo(application.projectFidelityScore());
        assertThat(capturedTrackingEvent.recommendationScore()).isEqualTo(application.recommendationScore());
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