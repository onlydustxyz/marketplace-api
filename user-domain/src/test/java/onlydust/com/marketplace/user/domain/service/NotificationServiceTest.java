package onlydust.com.marketplace.user.domain.service;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.kernel.model.notification.NotificationData;
import onlydust.com.marketplace.kernel.model.notification.NotificationType;
import onlydust.com.marketplace.user.domain.model.SendableNotification;
import onlydust.com.marketplace.user.domain.model.SmallUser;
import onlydust.com.marketplace.user.domain.port.output.AppUserStoragePort;
import onlydust.com.marketplace.user.domain.port.output.NotificationSender;
import onlydust.com.marketplace.user.domain.port.output.NotificationSettingsStoragePort;
import onlydust.com.marketplace.user.domain.port.output.NotificationStoragePort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

class NotificationServiceTest {

    private final NotificationSettingsStoragePort notificationSettingsStoragePort = mock(NotificationSettingsStoragePort.class);
    private final NotificationStoragePort notificationStoragePort = mock(NotificationStoragePort.class);
    private final AppUserStoragePort userStoragePort = mock(AppUserStoragePort.class);
    private final NotificationSender notificationEmailSender = mock(NotificationSender.class);

    NotificationService notificationService = new NotificationService(notificationSettingsStoragePort, notificationStoragePort, userStoragePort,
            new AsyncNotificationEmailProcessor(notificationEmailSender, notificationStoragePort));

    @Test
    void push() {
        // Given
        final var recipientId = UUID.randomUUID();

        // When
        when(notificationSettingsStoragePort.getNotificationChannels(recipientId, NotificationCategory.CONTRIBUTOR_REWARD))
                .thenReturn(List.of(NotificationChannel.EMAIL));
        when(userStoragePort.findById(SmallUser.Id.of(recipientId)))
                .thenReturn(java.util.Optional.of(new SmallUser(SmallUser.Id.of(recipientId), "foo@bar.baz", "foo")));
        final var notification = notificationService.push(recipientId, new TestNotification(1, NotificationCategory.CONTRIBUTOR_REWARD));

        // Then
        verify(notificationStoragePort).save(eq(notification));
        verify(notificationEmailSender).send(any(SendableNotification.class));
        verify(notificationStoragePort).markAsSent(NotificationChannel.EMAIL, List.of(notification.id()));
    }

    @Test
    void should_push_even_when_email_fails() {
        // Given
        final var recipientId = UUID.randomUUID();

        // When
        when(notificationSettingsStoragePort.getNotificationChannels(recipientId, NotificationCategory.CONTRIBUTOR_REWARD))
                .thenReturn(List.of(NotificationChannel.EMAIL));
        when(userStoragePort.findById(SmallUser.Id.of(recipientId)))
                .thenReturn(java.util.Optional.of(new SmallUser(SmallUser.Id.of(recipientId), "foo@bar.baz", "foo")));
        doThrow(new RuntimeException("Email sending failed")).when(notificationEmailSender).send(any(SendableNotification.class));
        final var notification = notificationService.push(recipientId, new TestNotification(1, NotificationCategory.CONTRIBUTOR_REWARD));

        // Then
        verify(notificationStoragePort).save(eq(notification));
        verify(notificationEmailSender).send(any(SendableNotification.class));
        verify(notificationStoragePort, never()).markAsSent(NotificationChannel.EMAIL, List.of(notification.id()));
    }

    @Data
    @ToString(callSuper = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @NotificationType("TestNotification")
    static class TestNotification extends NotificationData {
        int id;
        NotificationCategory category;

        public TestNotification(int id, NotificationCategory category) {
            this.id = id;
            this.category = category;
        }

        @Override
        public NotificationCategory category() {
            return category;
        }
    }
}