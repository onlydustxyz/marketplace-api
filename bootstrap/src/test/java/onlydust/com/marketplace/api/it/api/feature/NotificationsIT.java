package onlydust.com.marketplace.api.it.api.feature;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.it.api.AbstractMarketplaceApiIT;
import onlydust.com.marketplace.api.suites.tags.TagUser;
import onlydust.com.marketplace.kernel.model.notification.*;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.user.domain.model.NotificationSettings;
import onlydust.com.marketplace.user.domain.model.UserId;
import onlydust.com.marketplace.user.domain.port.input.NotificationSettingsPort;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TagUser
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NotificationsIT extends AbstractMarketplaceApiIT {
    @Autowired
    NotificationSettingsPort notificationSettingsPort;
    @Autowired
    NotificationPort notificationPort;

    private UserAuthHelper.AuthenticatedUser olivier;
    private UUID olivierId;
    private NotificationRecipient olivierRecipient;

    private final TestNotification rewardNotification1 = new TestNotification(1, NotificationCategory.REWARD_AS_CONTRIBUTOR);
    private final TestNotification rewardNotification2 = new TestNotification(2, NotificationCategory.REWARD_AS_CONTRIBUTOR);
    private final TestNotification rewardNotification3 = new TestNotification(3, NotificationCategory.REWARD_AS_CONTRIBUTOR);
    private final TestNotification gfiNotification1 = new TestNotification(100, NotificationCategory.PROJECT_GOOD_FIRST_ISSUE_AS_CONTRIBUTOR);
    private final TestNotification gfiNotification2 = new TestNotification(101, NotificationCategory.PROJECT_GOOD_FIRST_ISSUE_AS_CONTRIBUTOR);

    @BeforeEach
    void setUp() {
        olivier = userAuthHelper.authenticateOlivier();
        olivierId = olivier.user().getId();
        olivierRecipient = new NotificationRecipient(olivierId, olivier.user().getGithubEmail(), olivier.user().getGithubLogin());
    }

    @Test
    @Order(1)
    void should_return_nothing_when_no_notification() {
        assertNoPendingNotification(NotificationChannel.values());
    }

    @Test
    @Order(2)
    void should_return_nothing_when_notification_has_no_channel() {
        // When
        notificationPort.push(olivierId, new TestNotification(faker.random().nextInt(1000, Integer.MAX_VALUE), NotificationCategory.REWARD_AS_CONTRIBUTOR));

        // Then
        assertNoPendingNotification(NotificationChannel.values());
    }

    @Test
    @Order(3)
    void should_return_notification_for_appropriate_channels() {
        // Given
        notificationSettingsPort.updateNotificationSettings(UserId.of(olivierId), NotificationSettings.builder()
                .channelsPerCategory(Map.of(
                        NotificationCategory.REWARD_AS_CONTRIBUTOR, List.of(NotificationChannel.DAILY_EMAIL, NotificationChannel.IN_APP)
                ))
                .build());

        // When
        notificationPort.push(olivierId, rewardNotification1);

        // Then
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(rewardNotification1)
        ), NotificationChannel.DAILY_EMAIL, NotificationChannel.IN_APP);

        assertNoPendingNotification(NotificationChannel.EMAIL);
    }

    @Test
    @Order(4)
    void should_return_multiple_notifications_for_appropriate_channels() {
        // Given
        notificationSettingsPort.updateNotificationSettings(UserId.of(olivierId), NotificationSettings.builder()
                .channelsPerCategory(Map.of(
                        NotificationCategory.REWARD_AS_CONTRIBUTOR, List.of(NotificationChannel.DAILY_EMAIL, NotificationChannel.IN_APP),
                        NotificationCategory.PROJECT_GOOD_FIRST_ISSUE_AS_CONTRIBUTOR, List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP)
                ))
                .build());

        // When
        notificationPort.push(olivierId, rewardNotification2);
        notificationPort.push(olivierId, gfiNotification1);

        // Then
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(rewardNotification1, rewardNotification2)
        ), NotificationChannel.DAILY_EMAIL);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(rewardNotification1, rewardNotification2, gfiNotification1)
        ), NotificationChannel.IN_APP);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(gfiNotification1)
        ), NotificationChannel.EMAIL);
    }

    @Test
    @Order(10)
    void should_not_impact_old_notifications_when_channel_is_added_for_category() {
        // When
        notificationSettingsPort.updateNotificationSettings(UserId.of(olivierId), NotificationSettings.builder()
                .channelsPerCategory(Map.of(
                        NotificationCategory.REWARD_AS_CONTRIBUTOR, List.of(NotificationChannel.DAILY_EMAIL, NotificationChannel.IN_APP),
                        NotificationCategory.PROJECT_GOOD_FIRST_ISSUE_AS_CONTRIBUTOR, List.of(NotificationChannel.DAILY_EMAIL, NotificationChannel.EMAIL,
                                NotificationChannel.IN_APP)
                ))
                .build());

        // Then
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(rewardNotification1, rewardNotification2)
        ), NotificationChannel.DAILY_EMAIL);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(rewardNotification1, rewardNotification2, gfiNotification1)
        ), NotificationChannel.IN_APP);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(gfiNotification1)
        ), NotificationChannel.EMAIL);
    }

    @Test
    @Order(11)
    void should_impact_new_notifications_when_channel_is_added_for_category() {
        // When
        notificationPort.push(olivierId, gfiNotification2);

        // Then
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(rewardNotification1, rewardNotification2, gfiNotification2)
        ), NotificationChannel.DAILY_EMAIL);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(rewardNotification1, rewardNotification2, gfiNotification1, gfiNotification2)
        ), NotificationChannel.IN_APP);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(gfiNotification1, gfiNotification2)
        ), NotificationChannel.EMAIL);
    }

    @Test
    @Order(20)
    void should_not_impact_old_notifications_when_channel_is_removed_for_category() {
        // When
        notificationSettingsPort.updateNotificationSettings(UserId.of(olivierId), NotificationSettings.builder()
                .channelsPerCategory(Map.of(
                        NotificationCategory.REWARD_AS_CONTRIBUTOR, List.of(NotificationChannel.IN_APP),
                        NotificationCategory.PROJECT_GOOD_FIRST_ISSUE_AS_CONTRIBUTOR, List.of(NotificationChannel.DAILY_EMAIL, NotificationChannel.EMAIL,
                                NotificationChannel.IN_APP)
                ))
                .build());

        // Then
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(rewardNotification1, rewardNotification2, gfiNotification2)
        ), NotificationChannel.DAILY_EMAIL);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(rewardNotification1, rewardNotification2, gfiNotification1, gfiNotification2)
        ), NotificationChannel.IN_APP);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(gfiNotification1, gfiNotification2)
        ), NotificationChannel.EMAIL);
    }

    @Test
    @Order(21)
    void should_impact_new_notifications_when_channel_is_removed_for_category() {
        // When
        notificationPort.push(olivierId, rewardNotification3);

        // Then
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(rewardNotification1, rewardNotification2, gfiNotification2)
        ), NotificationChannel.DAILY_EMAIL);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(rewardNotification1, rewardNotification2, gfiNotification1, gfiNotification2, rewardNotification3)
        ), NotificationChannel.IN_APP);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(gfiNotification1, gfiNotification2)
        ), NotificationChannel.EMAIL);
    }

    private void assertNoPendingNotification(NotificationChannel... channels) {
        for (var channel : channels) {
            final var pendingNotificationsPerRecipient = notificationPort.getPendingNotificationsPerRecipient(channel);
            assertThat(pendingNotificationsPerRecipient).isEmpty();
        }
    }

    private void assertPendingNotifications(Map<NotificationRecipient, List<Notification>> expectedNotifications, NotificationChannel... channels) {
        for (var channel : channels) {
            final var pendingNotificationsPerRecipient = notificationPort.getPendingNotificationsPerRecipient(channel);
            assertThat(pendingNotificationsPerRecipient).containsAllEntriesOf(expectedNotifications);
            assertThat(expectedNotifications).containsAllEntriesOf(pendingNotificationsPerRecipient);
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
    @NotificationType("TestNotification")
    static class TestNotification extends Notification {
        @EqualsAndHashCode.Include
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
