package onlydust.com.marketplace.api.postgres.adapter.it.adapters;

import onlydust.com.marketplace.api.domain.model.notification.Event;
import onlydust.com.marketplace.api.domain.model.notification.ProjectLeaderAssigned;
import onlydust.com.marketplace.api.postgres.adapter.PostgresOutboxAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationEventEntity;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.NotificationEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresNotificationOutboxAdapterIT extends AbstractPostgresIT {

    @Autowired
    PostgresOutboxAdapter<NotificationEventEntity> postgresOutboxAdapter;
    @Autowired
    NotificationEventRepository notificationEventRepository;

    @BeforeEach
    void setUp() {
        notificationEventRepository.deleteAll();
    }

    @Test
    void should_save_and_get_notification() {
        // Given
        final Event event = new ProjectLeaderAssigned(UUID.randomUUID(), UUID.randomUUID(),
                faker.date().birthday());

        // When
        postgresOutboxAdapter.push(event);
        final var notificationPeeked = postgresOutboxAdapter.peek();

        // Then
        assertThat(notificationPeeked).isPresent();
        assertThat(notificationPeeked.get()).isEqualTo(event);
    }

    @Test
    void should_peek_the_first_pending_notification() {
        // Given
        final Event event1 = new ProjectLeaderAssigned(UUID.randomUUID(), UUID.randomUUID(),
                faker.date().birthday());
        final Event event2 = new ProjectLeaderAssigned(UUID.randomUUID(), UUID.randomUUID(),
                faker.date().birthday());
        final Event event3 = new ProjectLeaderAssigned(UUID.randomUUID(), UUID.randomUUID(),
                faker.date().birthday());

        // When
        postgresOutboxAdapter.push(event1);
        postgresOutboxAdapter.push(event2);
        postgresOutboxAdapter.push(event3);
        var notificationPeeked = postgresOutboxAdapter.peek();

        // Then
        assertThat(notificationPeeked).isPresent();
        assertThat(notificationPeeked.get()).isEqualTo(event1);

        // When
        postgresOutboxAdapter.ack();
        notificationPeeked = postgresOutboxAdapter.peek();

        // Then
        assertThat(notificationPeeked).isPresent();
        assertThat(notificationPeeked.get()).isEqualTo(event2);

        // When
        postgresOutboxAdapter.nack("Some error");
        notificationPeeked = postgresOutboxAdapter.peek();

        // Then
        assertThat(notificationPeeked).isPresent();
        assertThat(notificationPeeked.get()).isEqualTo(event2);

        // When
        postgresOutboxAdapter.ack();
        notificationPeeked = postgresOutboxAdapter.peek();

        // Then
        assertThat(notificationPeeked).isPresent();
        assertThat(notificationPeeked.get()).isEqualTo(event3);

        // When
        postgresOutboxAdapter.ack();
        notificationPeeked = postgresOutboxAdapter.peek();

        // Then
        assertThat(notificationPeeked).isNotPresent();
    }

    @Test
    void should_get_nothing_when_there_is_no_notifications() {
        // When
        final var notificationPeeked = postgresOutboxAdapter.peek();

        // Then
        assertThat(notificationPeeked).isNotPresent();
    }

    @Test
    void should_ack() {
        // Given
        final Event event = new ProjectLeaderAssigned(UUID.randomUUID(), UUID.randomUUID(),
                faker.date().birthday());

        // When
        postgresOutboxAdapter.push(event);
        final var notificationPeeked = postgresOutboxAdapter.peek();

        // Then
        assertThat(notificationPeeked).isPresent();
        assertThat(notificationPeeked.get()).isEqualTo(event);

        // And when
        postgresOutboxAdapter.ack();
        assertThat(postgresOutboxAdapter.peek()).isNotPresent();

        final var entity = notificationEventRepository.findAll().get(0);
        assertThat(entity.getStatus()).isEqualTo(NotificationEventEntity.Status.PROCESSED);
        assertThat(entity.getError()).isNull();
    }

    @Test
    void should_nack() {
        // Given
        final Event event = new ProjectLeaderAssigned(UUID.randomUUID(), UUID.randomUUID(),
                faker.date().birthday());

        // When
        postgresOutboxAdapter.push(event);
        final var notificationPeeked = postgresOutboxAdapter.peek();

        // Then
        assertThat(notificationPeeked).isPresent();
        assertThat(notificationPeeked.get()).isEqualTo(event);

        // And when
        postgresOutboxAdapter.nack("Some error");
        assertThat(notificationPeeked).isPresent();
        assertThat(notificationPeeked.get()).isEqualTo(event);

        final var entity = notificationEventRepository.findAll().get(0);
        assertThat(entity.getStatus()).isEqualTo(NotificationEventEntity.Status.FAILED);
        assertThat(entity.getError()).isEqualTo("Some error");
    }

}