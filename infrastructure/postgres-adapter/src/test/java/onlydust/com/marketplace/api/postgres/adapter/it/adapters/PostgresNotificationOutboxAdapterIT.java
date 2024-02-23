package onlydust.com.marketplace.api.postgres.adapter.it.adapters;

import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.PostgresOutboxAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationEventEntity;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.NotificationEventRepository;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresNotificationOutboxAdapterIT extends AbstractPostgresIT {

    @Autowired
    PostgresOutboxAdapter<NotificationEventEntity> postgresOutboxAdapter;
    @Autowired
    NotificationEventRepository notificationEventRepository;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @EventType("TestEvent")
    public static class TestEvent extends Event {
        UUID id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @EventType("GroupedTestEvent")
    public static class GroupedTestEvent extends Event {
        @NonNull String group;
        UUID id;

        @Override
        public Optional<String> group() {
            return Optional.of(group);
        }
    }

    @BeforeEach
    void setUp() {
        notificationEventRepository.deleteAll();
    }

    @Test
    void should_save_and_get_notification() {
        // Given
        final Event event = new TestEvent(UUID.randomUUID());

        // When
        postgresOutboxAdapter.push(event);
        final var notificationPeeked = postgresOutboxAdapter.peek();

        // Then
        assertThat(notificationPeeked).hasSize(1);
        assertThat(notificationPeeked.get(0).id()).isNotNull();
        assertThat(notificationPeeked.get(0).event()).isEqualTo(event);
    }

    @Test
    void should_peek_the_first_pending_event() {
        // Given
        final Event event1 = new TestEvent(UUID.randomUUID());
        final Event event2 = new TestEvent(UUID.randomUUID());
        final Event event3 = new TestEvent(UUID.randomUUID());

        // When
        postgresOutboxAdapter.push(event1);
        postgresOutboxAdapter.push(event2);
        postgresOutboxAdapter.push(event3);
        var notificationPeeked = postgresOutboxAdapter.peek();

        // Then
        assertThat(notificationPeeked).hasSize(1);
        assertThat(notificationPeeked.get(0).id()).isNotNull();
        assertThat(notificationPeeked.get(0).event()).isEqualTo(event1);

        // When
        postgresOutboxAdapter.ack(notificationPeeked.get(0).id());
        notificationPeeked = postgresOutboxAdapter.peek();

        // Then
        assertThat(notificationPeeked).hasSize(1);
        assertThat(notificationPeeked.get(0).id()).isNotNull();
        assertThat(notificationPeeked.get(0).event()).isEqualTo(event2);

        // When
        postgresOutboxAdapter.nack(notificationPeeked.get(0).id(), "Some error");
        notificationPeeked = postgresOutboxAdapter.peek();

        // Then
        assertThat(notificationPeeked).hasSize(1);
        assertThat(notificationPeeked.get(0).id()).isNotNull();
        assertThat(notificationPeeked.get(0).event()).isEqualTo(event2);

        // When
        postgresOutboxAdapter.ack(notificationPeeked.get(0).id());
        notificationPeeked = postgresOutboxAdapter.peek();

        // Then
        assertThat(notificationPeeked).hasSize(1);
        assertThat(notificationPeeked.get(0).id()).isNotNull();
        assertThat(notificationPeeked.get(0).event()).isEqualTo(event3);

        // When
        postgresOutboxAdapter.ack(notificationPeeked.get(0).id());
        notificationPeeked = postgresOutboxAdapter.peek();

        // Then
        assertThat(notificationPeeked).isEmpty();
    }

    @Test
    void should_peek_the_first_pending_event_per_group() {
        // Given
        final Event event1 = new GroupedTestEvent("a", UUID.randomUUID());
        final Event event2 = new GroupedTestEvent("a", UUID.randomUUID());
        final Event event3 = new GroupedTestEvent("b", UUID.randomUUID());

        // When
        postgresOutboxAdapter.push(event1);
        postgresOutboxAdapter.push(event2);
        postgresOutboxAdapter.push(event3);
        var notificationPeeked = postgresOutboxAdapter.peek();

        // Then
        assertThat(notificationPeeked).hasSize(2);
        assertThat(notificationPeeked.get(0).id()).isNotNull();
        assertThat(notificationPeeked.get(0).event()).isEqualTo(event1);
        assertThat(notificationPeeked.get(1).id()).isNotNull();
        assertThat(notificationPeeked.get(1).event()).isEqualTo(event3);

        // When
        postgresOutboxAdapter.ack(notificationPeeked.get(0).id());
        notificationPeeked = postgresOutboxAdapter.peek();

        // Then
        assertThat(notificationPeeked).hasSize(2);
        assertThat(notificationPeeked.get(0).id()).isNotNull();
        assertThat(notificationPeeked.get(0).event()).isEqualTo(event2);
        assertThat(notificationPeeked.get(1).id()).isNotNull();
        assertThat(notificationPeeked.get(1).event()).isEqualTo(event3);

        // When
        postgresOutboxAdapter.nack(notificationPeeked.get(0).id(), "Some error");
        notificationPeeked = postgresOutboxAdapter.peek();

        // Then
        assertThat(notificationPeeked).hasSize(2);
        assertThat(notificationPeeked.get(0).id()).isNotNull();
        assertThat(notificationPeeked.get(0).event()).isEqualTo(event2);
        assertThat(notificationPeeked.get(1).id()).isNotNull();
        assertThat(notificationPeeked.get(1).event()).isEqualTo(event3);

        // When
        postgresOutboxAdapter.ack(notificationPeeked.get(0).id());
        notificationPeeked = postgresOutboxAdapter.peek();

        // Then
        assertThat(notificationPeeked).hasSize(1);
        assertThat(notificationPeeked.get(0).id()).isNotNull();
        assertThat(notificationPeeked.get(0).event()).isEqualTo(event3);

        // When
        postgresOutboxAdapter.ack(notificationPeeked.get(0).id());
        notificationPeeked = postgresOutboxAdapter.peek();

        // Then
        assertThat(notificationPeeked).isEmpty();
    }

    @Test
    void should_get_nothing_when_there_is_no_notifications() {
        // When
        final var notificationPeeked = postgresOutboxAdapter.peek();

        // Then
        assertThat(notificationPeeked).isEmpty();
    }

    @Test
    void should_ack() {
        // Given
        final Event event = new TestEvent(UUID.randomUUID());

        // When
        postgresOutboxAdapter.push(event);
        final var notificationPeeked = postgresOutboxAdapter.peek();

        // Then
        assertThat(notificationPeeked).hasSize(1);
        assertThat(notificationPeeked.get(0).id()).isNotNull();
        assertThat(notificationPeeked.get(0).event()).isEqualTo(event);

        // And when
        postgresOutboxAdapter.ack(notificationPeeked.get(0).id());
        assertThat(postgresOutboxAdapter.peek()).isEmpty();

        final var entity = notificationEventRepository.findAll().get(0);
        assertThat(entity.getStatus()).isEqualTo(NotificationEventEntity.Status.PROCESSED);
        assertThat(entity.getError()).isNull();
    }

    @Test
    void should_nack() {
        // Given
        final Event event = new TestEvent(UUID.randomUUID());

        // When
        postgresOutboxAdapter.push(event);
        final var notificationPeeked = postgresOutboxAdapter.peek();

        // Then
        assertThat(notificationPeeked).hasSize(1);
        assertThat(notificationPeeked.get(0).id()).isNotNull();
        assertThat(notificationPeeked.get(0).event()).isEqualTo(event);

        // And when
        postgresOutboxAdapter.nack(notificationPeeked.get(0).id(), "Some error");
        assertThat(notificationPeeked).hasSize(1);
        assertThat(notificationPeeked.get(0).id()).isNotNull();
        assertThat(notificationPeeked.get(0).event()).isEqualTo(event);

        final var entity = notificationEventRepository.findAll().get(0);
        assertThat(entity.getStatus()).isEqualTo(NotificationEventEntity.Status.FAILED);
        assertThat(entity.getError()).isEqualTo("Some error");
    }

    @Test
    void should_skip() {
        // Given
        final Event event = new TestEvent(UUID.randomUUID());

        // When
        postgresOutboxAdapter.push(event);
        final var notificationPeeked = postgresOutboxAdapter.peek();
        assertThat(notificationPeeked).hasSize(1);
        assertThat(notificationPeeked.get(0).id()).isNotNull();
        assertThat(notificationPeeked.get(0).event()).isEqualTo(event);

        postgresOutboxAdapter.skip(notificationPeeked.get(0).id(), "Some reason to skip");

        // Then
        final var entity = notificationEventRepository.findAll().get(0);
        assertThat(entity.getStatus()).isEqualTo(NotificationEventEntity.Status.SKIPPED);
        assertThat(entity.getError()).isEqualTo("Some reason to skip");
    }
}