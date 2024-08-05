package onlydust.com.marketplace.api.it.api.feature;

import com.onlydust.customer.io.adapter.CustomerIOAdapter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import onlydust.com.marketplace.api.it.api.AbstractMarketplaceApiIT;
import onlydust.com.marketplace.api.suites.tags.TagUser;
import onlydust.com.marketplace.kernel.model.notification.*;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.user.domain.model.NotificationRecipient;
import onlydust.com.marketplace.user.domain.model.NotificationSettings;
import onlydust.com.marketplace.user.domain.model.SendableNotification;
import onlydust.com.marketplace.user.domain.port.input.NotificationSettingsPort;
import onlydust.com.marketplace.user.domain.port.output.NotificationStoragePort;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TagUser
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NotificationsIT extends AbstractMarketplaceApiIT {
    @Autowired
    NotificationSettingsPort notificationSettingsPort;
    @Autowired
    NotificationPort notificationPort;
    @Autowired
    NotificationStoragePort notificationStoragePort;
    @Autowired
    CustomerIOAdapter notificationInstantEmailSender;

    private UUID olivierId;
    private NotificationRecipient olivierRecipient;
    private UUID pierreId;
    private NotificationRecipient pierreRecipient;

    private final TestNotification rewardNotification1Data = new TestNotification(1, NotificationCategory.CONTRIBUTOR_REWARD);
    private final TestNotification rewardNotification2Data = new TestNotification(2, NotificationCategory.CONTRIBUTOR_REWARD);
    private final TestNotification rewardNotification3Data = new TestNotification(3, NotificationCategory.CONTRIBUTOR_REWARD);
    private final TestNotification rewardNotification4Data = new TestNotification(4, NotificationCategory.CONTRIBUTOR_REWARD);
    private final TestNotification kycNotification1Data = new TestNotification(100, NotificationCategory.KYC_KYB_BILLING_PROFILE);
    private final TestNotification kycNotification2Data = new TestNotification(101, NotificationCategory.KYC_KYB_BILLING_PROFILE);

    private static Notification olivierRewardNotification1;
    private static Notification olivierRewardNotification2;
    private static Notification olivierRewardNotification3;
    private static Notification olivierRewardNotification4;
    private static Notification olivierKycNotification1;
    private static Notification olivierKycNotification2;


    private static Notification pierreRewardNotification1;
    private static Notification pierreRewardNotification4;
    private static Notification pierreKycNotification1;

    @BeforeEach
    void setUp() {
        reset(notificationInstantEmailSender);
        
        final var olivier = userAuthHelper.authenticateOlivier();
        olivierId = olivier.user().getId();
        olivierRecipient = new NotificationRecipient(NotificationRecipient.Id.of(olivierId), olivier.user().getEmail(), olivier.user().getGithubLogin());

        final var pierre = userAuthHelper.authenticatePierre();
        pierreId = pierre.user().getId();
        pierreRecipient = new NotificationRecipient(NotificationRecipient.Id.of(pierreId), pierre.user().getEmail(), pierre.user().getGithubLogin());
    }

    @Test
    @Order(1)
    void should_return_nothing_when_no_notification() {
        assertNoPendingNotification(NotificationChannel.values());
    }

    @Test
    @Order(2)
    void should_return_notification_for_default_channels_for_old_user() {
        // When
        final var notification = notificationPort.push(olivierId, new TestNotification(faker.random().nextInt(1000, Integer.MAX_VALUE),
                NotificationCategory.CONTRIBUTOR_REWARD));

        // Then
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(notification)
        ), NotificationChannel.IN_APP);
        assertNoPendingNotification(NotificationChannel.DAILY_EMAIL);
        assertEmailsSent(List.of(notification));

        // Cleanup
        notificationStoragePort.markAsSent(NotificationChannel.IN_APP, List.of(notification.id()));
    }

    @Test
    @Order(3)
    void should_return_notification_for_default_channels_for_new_user() {
        // Given
        final var newUser = userAuthHelper.signUpUser(
                faker.number().randomNumber(10, true),
                faker.internet().slug(),
                faker.internet().avatar(),
                false);
        final var newUserRecipient = new NotificationRecipient(NotificationRecipient.Id.of(newUser.user().getId()), newUser.user().getEmail(),
                newUser.user().getGithubLogin());

        // When
        final var notification = notificationPort.push(newUser.user().getId(), new TestNotification(faker.random().nextInt(1000, Integer.MAX_VALUE),
                NotificationCategory.MAINTAINER_PROJECT_PROGRAM));

        // Then
        assertPendingNotifications(Map.of(
                newUserRecipient, List.of(notification)
        ), NotificationChannel.IN_APP);
        assertNoPendingNotification(NotificationChannel.DAILY_EMAIL);
        assertEmailsSent(List.of(notification));

        // Cleanup
        notificationStoragePort.markAsSent(NotificationChannel.IN_APP, List.of(notification.id()));
    }

    @Test
    @Order(4)
    void should_return_notification_for_appropriate_channels() {
        // Given
        notificationSettingsPort.updateNotificationSettings(NotificationRecipient.Id.of(olivierId),
                NotificationSettings.builder()
                        .channelsPerCategory(Map.of(
                                NotificationCategory.CONTRIBUTOR_REWARD, List.of(NotificationChannel.DAILY_EMAIL, NotificationChannel.IN_APP)
                        ))
                        .build());

        // When
        olivierRewardNotification1 = notificationPort.push(olivierId, rewardNotification1Data);

        // Then
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1)
        ), NotificationChannel.DAILY_EMAIL, NotificationChannel.IN_APP);

        assertNoEmailsSent();
    }

    @Test
    @Order(5)
    void should_return_multiple_notifications_for_appropriate_channels() {
        // Given
        notificationSettingsPort.updateNotificationSettings(NotificationRecipient.Id.of(olivierId),
                NotificationSettings.builder()
                        .channelsPerCategory(Map.of(
                                NotificationCategory.CONTRIBUTOR_REWARD, List.of(NotificationChannel.DAILY_EMAIL, NotificationChannel.IN_APP),
                                NotificationCategory.KYC_KYB_BILLING_PROFILE, List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP)
                        ))
                        .build());

        // When
        olivierRewardNotification2 = notificationPort.push(olivierId, rewardNotification2Data);
        olivierKycNotification1 = notificationPort.push(olivierId, kycNotification1Data);

        // Then
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2)
        ), NotificationChannel.DAILY_EMAIL);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2, olivierKycNotification1)
        ), NotificationChannel.IN_APP);
        assertEmailsSent(List.of(olivierKycNotification1));
    }

    @Test
    @Order(10)
    void should_not_impact_old_notifications_when_channel_is_added_for_category() {
        // When
        notificationSettingsPort.updateNotificationSettings(NotificationRecipient.Id.of(olivierId),
                NotificationSettings.builder()
                        .channelsPerCategory(Map.of(
                                NotificationCategory.CONTRIBUTOR_REWARD, List.of(NotificationChannel.DAILY_EMAIL, NotificationChannel.IN_APP),
                                NotificationCategory.KYC_KYB_BILLING_PROFILE, List.of(NotificationChannel.DAILY_EMAIL, NotificationChannel.EMAIL,
                                        NotificationChannel.IN_APP)
                        ))
                        .build());

        // Then
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2)
        ), NotificationChannel.DAILY_EMAIL);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2, olivierKycNotification1)
        ), NotificationChannel.IN_APP);
        assertNoEmailsSent();
    }

    @Test
    @Order(11)
    void should_impact_new_notifications_when_channel_is_added_for_category() {
        // When
        olivierKycNotification2 = notificationPort.push(olivierId, kycNotification2Data);

        // Then
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2, olivierKycNotification2)
        ), NotificationChannel.DAILY_EMAIL);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2, olivierKycNotification1, olivierKycNotification2)
        ), NotificationChannel.IN_APP);
        assertEmailsSent(List.of(olivierKycNotification2));
    }

    @Test
    @Order(20)
    void should_not_impact_old_notifications_when_channel_is_removed_for_category() {
        // When
        notificationSettingsPort.updateNotificationSettings(NotificationRecipient.Id.of(olivierId),
                NotificationSettings.builder()
                        .channelsPerCategory(Map.of(
                                NotificationCategory.CONTRIBUTOR_REWARD, List.of(NotificationChannel.IN_APP),
                                NotificationCategory.KYC_KYB_BILLING_PROFILE, List.of(NotificationChannel.DAILY_EMAIL, NotificationChannel.EMAIL,
                                        NotificationChannel.IN_APP)
                        ))
                        .build());

        // Then
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2, olivierKycNotification2)
        ), NotificationChannel.DAILY_EMAIL);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2, olivierKycNotification1, olivierKycNotification2)
        ), NotificationChannel.IN_APP);
        assertNoEmailsSent();
    }

    @Test
    @Order(21)
    void should_impact_new_notifications_when_channel_is_removed_for_category() {
        // When
        olivierRewardNotification3 = notificationPort.push(olivierId, rewardNotification3Data);

        // Then
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2, olivierKycNotification2)
        ), NotificationChannel.DAILY_EMAIL);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2, olivierKycNotification1, olivierKycNotification2,
                        olivierRewardNotification3)
        ), NotificationChannel.IN_APP);
        assertNoEmailsSent();
    }

    @Test
    @Order(30)
    void should_not_mix_notifications_between_users() {
        // Given
        notificationSettingsPort.updateNotificationSettings(NotificationRecipient.Id.of(pierreId),
                NotificationSettings.builder()
                        .channelsPerCategory(Map.of(
                                NotificationCategory.CONTRIBUTOR_REWARD, List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP),
                                NotificationCategory.KYC_KYB_BILLING_PROFILE, List.of(NotificationChannel.IN_APP)
                        ))
                        .build());

        // When
        pierreRewardNotification1 = notificationPort.push(pierreId, rewardNotification1Data);
        pierreRewardNotification4 = notificationPort.push(pierreId, rewardNotification4Data);
        pierreKycNotification1 = notificationPort.push(pierreId, kycNotification1Data);
        olivierRewardNotification4 = notificationPort.push(olivierId, rewardNotification4Data);

        // Then
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2, olivierKycNotification2)
        ), NotificationChannel.DAILY_EMAIL);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2, olivierKycNotification1, olivierKycNotification2,
                        olivierRewardNotification3,
                        olivierRewardNotification4),
                pierreRecipient, List.of(pierreRewardNotification1, pierreRewardNotification4, pierreKycNotification1)
        ), NotificationChannel.IN_APP);
        assertEmailsSent(List.of(pierreRewardNotification1, pierreRewardNotification4));
    }

    @Test
    @Order(40)
    void should_mark_notifications_as_sent() {
        // When
        notificationStoragePort.markAsSent(NotificationChannel.DAILY_EMAIL, List.of(olivierRewardNotification1.id()));

        // Then
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification2, olivierKycNotification2)
        ), NotificationChannel.DAILY_EMAIL);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2, olivierKycNotification1, olivierKycNotification2,
                        olivierRewardNotification3,
                        olivierRewardNotification4),
                pierreRecipient, List.of(pierreRewardNotification1, pierreRewardNotification4, pierreKycNotification1)
        ), NotificationChannel.IN_APP);
        assertNoEmailsSent();

        // When
        notificationStoragePort.markAsSent(NotificationChannel.EMAIL, List.of(olivierKycNotification1.id(), olivierKycNotification2.id(),
                pierreRewardNotification1.id(), pierreRewardNotification4.id()));

        // Then
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification2, olivierKycNotification2)
        ), NotificationChannel.DAILY_EMAIL);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2, olivierKycNotification1, olivierKycNotification2,
                        olivierRewardNotification3,
                        olivierRewardNotification4),
                pierreRecipient, List.of(pierreRewardNotification1, pierreRewardNotification4, pierreKycNotification1)
        ), NotificationChannel.IN_APP);
        assertNoEmailsSent();
    }

    @Test
    @Order(40)
    void should_be_ok_to_mark_notifications_as_sent_twice() {
        // When
        notificationStoragePort.markAsSent(NotificationChannel.DAILY_EMAIL, List.of(olivierRewardNotification1.id()));

        // Then
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification2, olivierKycNotification2)
        ), NotificationChannel.DAILY_EMAIL);
    }

    private void assertNoPendingNotification(NotificationChannel... channels) {
        try {
            for (int test = 0; test < 10 && hasPendingNotification(channels); test++) {
                // Let some time for email to be sent
                Thread.sleep(100);
            }
            assertThat(hasPendingNotification(channels)).isFalse();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean hasPendingNotification(NotificationChannel... channels) {
        for (var channel : channels) {
            if (!notificationStoragePort.getPendingNotifications(channel).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void assertPendingNotifications(Map<NotificationRecipient, List<Notification>> expectedNotifications, NotificationChannel... channels) {
        for (var channel : channels) {
            final List<SendableNotification> pendingNotificationsPerRecipient = notificationStoragePort.getPendingNotifications(channel).stream()
                    .map(NotificationsIT::comparable)
                    .toList();

            // Build SendableNotification out of expected notifications for comparison purposes
            final List<SendableNotification> sendableExpectedNotifications = expectedNotifications.entrySet().stream()
                    .map(entry -> entry.getValue().stream()
                            .map(notification -> comparable(SendableNotification.of(entry.getKey(), notification)))
                            .toList())
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            assertThat(pendingNotificationsPerRecipient).containsAll(sendableExpectedNotifications);
            assertThat(sendableExpectedNotifications).containsAll(pendingNotificationsPerRecipient);
        }
    }

    // Truncate createdAt to milliseconds, and use ordered TreeSet for channels to avoid comparison issues
    private static SendableNotification comparable(SendableNotification notification) {
        return notification.toBuilder()
                .createdAt(notification.createdAt().truncatedTo(ChronoUnit.MILLIS))
                .channels(notification.channels().stream().sorted().collect(Collectors.toCollection(TreeSet::new)))
                .build();
    }

    private void assertEmailsSent(List<Notification> notifications) {
        assertNoPendingNotification(NotificationChannel.EMAIL);
        final var captor = ArgumentCaptor.forClass(SendableNotification.class);
        verify(notificationInstantEmailSender, times(notifications.size())).send(captor.capture());
        final var sentNotificationIds = captor.getAllValues().stream().map(SendableNotification::id).toList();
        final var notificationIds = notifications.stream().map(Notification::id).toList();
        assertThat(notificationIds).containsAll(sentNotificationIds);
        assertThat(sentNotificationIds).containsAll(notificationIds);
        reset(notificationInstantEmailSender);
    }

    private void assertNoEmailsSent() {
        assertNoPendingNotification(NotificationChannel.EMAIL);
        verify(notificationInstantEmailSender, never()).send(any(SendableNotification.class));
        reset(notificationInstantEmailSender);
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
