package onlydust.com.marketplace.project.domain.job;

import com.github.javafaker.Faker;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.event.OnGithubIssueAssigned;
import onlydust.com.marketplace.kernel.model.event.OnPullRequestCreated;
import onlydust.com.marketplace.kernel.model.event.OnPullRequestMerged;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.model.event.OnGithubIssueAssignedTrackingEvent;
import onlydust.com.marketplace.project.domain.model.event.OnPullRequestCreatedTrackingEvent;
import onlydust.com.marketplace.project.domain.model.event.OnPullRequestMergedTrackingEvent;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TrackingEventPublisherOutboxConsumerTest {
    private final TrackingEventPublisher trackingEventPublisher = mock(TrackingEventPublisher.class);
    private final UserStoragePort userStoragePort = mock(UserStoragePort.class);
    private final TrackingEventPublisherOutboxConsumer trackingEventPublisherOutboxConsumer = new TrackingEventPublisherOutboxConsumer(
            trackingEventPublisher, userStoragePort
    );

    private final Faker faker = new Faker();

    private final Long githubUserId = faker.number().randomNumber();

    @BeforeEach
    void setUp() {
        reset(trackingEventPublisher, userStoragePort);
    }

    @Nested
    class GivenNonRegisteredUser {
        @BeforeEach
        void setUp() {
            when(userStoragePort.getUserByGithubId(githubUserId)).thenReturn(Optional.empty());
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

        @NoArgsConstructor
        private static class TestEvent extends Event {
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
            assertThat(capturedTrackingEvent.assigneeUserId()).isEqualTo(user.getId());
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
                    .id(faker.number().randomNumber())
                    .assigneeId(githubUserId)
                    .labels(Set.of(faker.lorem().word(), faker.lorem().word(), goodFirstIssueLabel, faker.lorem().word()))
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

    @SneakyThrows
    private Event fakeEvent(Class<? extends Event> eventClass) {
        if (eventClass.equals(OnGithubIssueAssigned.class))
            return OnGithubIssueAssigned.builder()
                    .id(faker.number().randomNumber())
                    .assigneeId(githubUserId)
                    .labels(new HashSet<>(faker.lorem().words()))
                    .assignedAt(faker.date().birthday().toInstant().atZone(ZoneOffset.UTC))
                    .build();

        if (eventClass.equals(OnPullRequestCreated.class))
            return OnPullRequestCreated.builder()
                    .id(faker.number().randomNumber())
                    .authorId(githubUserId)
                    .createdAt(faker.date().birthday().toInstant().atZone(ZoneOffset.UTC))
                    .build();

        if (eventClass.equals(OnPullRequestMerged.class))
            return OnPullRequestMerged.builder()
                    .id(faker.number().randomNumber())
                    .authorId(githubUserId)
                    .createdAt(faker.date().birthday().toInstant().atZone(ZoneOffset.UTC))
                    .mergedAt(faker.date().birthday().toInstant().atZone(ZoneOffset.UTC))
                    .build();

        return eventClass.getConstructor().newInstance();
    }
}