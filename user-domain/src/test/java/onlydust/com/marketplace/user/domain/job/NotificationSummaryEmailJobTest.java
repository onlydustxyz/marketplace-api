package onlydust.com.marketplace.user.domain.job;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.model.notification.Notification;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.kernel.model.notification.NotificationData;
import onlydust.com.marketplace.user.domain.model.NotificationRecipient;
import onlydust.com.marketplace.user.domain.model.SendableNotification;
import onlydust.com.marketplace.user.domain.port.output.NotificationSender;
import onlydust.com.marketplace.user.domain.port.output.NotificationStoragePort;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

public class NotificationSummaryEmailJobTest {
    private final Faker faker = new Faker();

    @Test
    void should_send_notifications_grouped_by_recipient() {
        // Given
        final NotificationSender notificationEmailSender = mock(NotificationSender.class);
        final NotificationStoragePort notificationStoragePort = mock(NotificationStoragePort.class);
        final NotificationSummaryEmailJob notificationSummaryEmailJob = new NotificationSummaryEmailJob(notificationStoragePort, notificationEmailSender);
        final NotificationRecipient recipient1 = new NotificationRecipient(NotificationRecipient.Id.random(), faker.internet().emailAddress(),
                faker.name().username());
        final NotificationRecipient recipient2 = new NotificationRecipient(NotificationRecipient.Id.random(), faker.internet().emailAddress(),
                faker.name().username());
        final SendableNotification n11 = SendableNotification.of(recipient1,
                Notification.builder()
                        .channels(Set.of(NotificationChannel.SUMMARY_EMAIL))
                        .recipientId(recipient1.id().value())
                        .data(new DummyNotification())
                        .createdAt(ZonedDateTime.now())
                        .id(Notification.Id.random())
                        .build()
        );
        final SendableNotification n12 = SendableNotification.of(recipient1,
                Notification.builder()
                        .channels(Set.of(NotificationChannel.SUMMARY_EMAIL))
                        .recipientId(recipient1.id().value())
                        .data(new DummyNotification())
                        .createdAt(ZonedDateTime.now())
                        .id(Notification.Id.random())
                        .build()
        );
        final SendableNotification n2 = SendableNotification.of(recipient2,
                Notification.builder()
                        .channels(Set.of(NotificationChannel.SUMMARY_EMAIL))
                        .recipientId(recipient2.id().value())
                        .data(new DummyNotification())
                        .createdAt(ZonedDateTime.now())
                        .id(Notification.Id.random())
                        .build()
        );


        // When
        when(notificationStoragePort.getPendingNotifications(NotificationChannel.SUMMARY_EMAIL))
                .thenReturn(List.of(
                        n11,
                        n12,
                        n2
                ));
        notificationSummaryEmailJob.run();

        // Then
        verify(notificationStoragePort).markAsSent(NotificationChannel.SUMMARY_EMAIL, List.of(n11.id(), n12.id()));
        verify(notificationStoragePort).markAsSent(NotificationChannel.SUMMARY_EMAIL, List.of(n2.id()));
        verify(notificationEmailSender).sendAllForRecipient(recipient1, List.of(n11, n12));
        verify(notificationEmailSender).sendAllForRecipient(recipient2, List.of(n2));
    }

    public static class DummyNotification extends NotificationData {
        @Override
        public NotificationCategory category() {
            return null;
        }
    }
}
