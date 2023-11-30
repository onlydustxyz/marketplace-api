package onlydust.com.marketplace.api.postgres.adapter.it.repository;

import onlydust.com.marketplace.api.domain.model.notification.Notification;
import onlydust.com.marketplace.api.domain.model.notification.ProjectLeaderAssigned;
import onlydust.com.marketplace.api.postgres.adapter.PostgresNotificationAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationEntity;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresNotificationAdapterIT extends AbstractPostgresIT {

    @Autowired
    PostgresNotificationAdapter postgresNotificationAdapter;
    @Autowired
    NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
    }

    @Test
    void should_save_and_get_notification() {
        // Given
        final Notification notification = new ProjectLeaderAssigned(UUID.randomUUID(), UUID.randomUUID(),
                faker.date().birthday());

        // When
        postgresNotificationAdapter.push(notification);
        final var notificationPeeked = postgresNotificationAdapter.peek();

        // Then
        assertThat(notificationPeeked).isPresent();
        assertThat(notificationPeeked.get()).isEqualTo(notification);
    }

    @Test
    void should_peek_the_first_pending_notification() {
        // Given
        final Notification notification1 = new ProjectLeaderAssigned(UUID.randomUUID(), UUID.randomUUID(),
                faker.date().birthday());
        final Notification notification2 = new ProjectLeaderAssigned(UUID.randomUUID(), UUID.randomUUID(),
                faker.date().birthday());
        final Notification notification3 = new ProjectLeaderAssigned(UUID.randomUUID(), UUID.randomUUID(),
                faker.date().birthday());

        // When
        postgresNotificationAdapter.push(notification1);
        postgresNotificationAdapter.push(notification2);
        postgresNotificationAdapter.push(notification3);
        var notificationPeeked = postgresNotificationAdapter.peek();

        // Then
        assertThat(notificationPeeked).isPresent();
        assertThat(notificationPeeked.get()).isEqualTo(notification1);

        // When
        postgresNotificationAdapter.ack();
        notificationPeeked = postgresNotificationAdapter.peek();

        // Then
        assertThat(notificationPeeked).isPresent();
        assertThat(notificationPeeked.get()).isEqualTo(notification2);

        // When
        postgresNotificationAdapter.nack("Some error");
        notificationPeeked = postgresNotificationAdapter.peek();

        // Then
        assertThat(notificationPeeked).isPresent();
        assertThat(notificationPeeked.get()).isEqualTo(notification2);

        // When
        postgresNotificationAdapter.ack();
        notificationPeeked = postgresNotificationAdapter.peek();

        // Then
        assertThat(notificationPeeked).isPresent();
        assertThat(notificationPeeked.get()).isEqualTo(notification3);

        // When
        postgresNotificationAdapter.ack();
        notificationPeeked = postgresNotificationAdapter.peek();

        // Then
        assertThat(notificationPeeked).isNotPresent();
    }

    @Test
    void should_get_nothing_when_there_is_no_notifications() {
        // When
        final var notificationPeeked = postgresNotificationAdapter.peek();

        // Then
        assertThat(notificationPeeked).isNotPresent();
    }

    @Test
    void should_ack() {
        // Given
        final Notification notification = new ProjectLeaderAssigned(UUID.randomUUID(), UUID.randomUUID(),
                faker.date().birthday());

        // When
        postgresNotificationAdapter.push(notification);
        final var notificationPeeked = postgresNotificationAdapter.peek();

        // Then
        assertThat(notificationPeeked).isPresent();
        assertThat(notificationPeeked.get()).isEqualTo(notification);

        // And when
        postgresNotificationAdapter.ack();
        assertThat(postgresNotificationAdapter.peek()).isNotPresent();

        final var entity = notificationRepository.findAll().get(0);
        assertThat(entity.getStatus()).isEqualTo(NotificationEntity.Status.PROCESSED);
        assertThat(entity.getError()).isNull();
    }

    @Test
    void should_nack() {
        // Given
        final Notification notification = new ProjectLeaderAssigned(UUID.randomUUID(), UUID.randomUUID(),
                faker.date().birthday());

        // When
        postgresNotificationAdapter.push(notification);
        final var notificationPeeked = postgresNotificationAdapter.peek();

        // Then
        assertThat(notificationPeeked).isPresent();
        assertThat(notificationPeeked.get()).isEqualTo(notification);

        // And when
        postgresNotificationAdapter.nack("Some error");
        assertThat(notificationPeeked).isPresent();
        assertThat(notificationPeeked.get()).isEqualTo(notification);

        final var entity = notificationRepository.findAll().get(0);
        assertThat(entity.getStatus()).isEqualTo(NotificationEntity.Status.FAILED);
        assertThat(entity.getError()).isEqualTo("Some error");
    }

}