package onlydust.com.marketplace.api.it.api.feature;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
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

    private UUID olivierId;
    private NotificationRecipient olivierRecipient;
    private UUID pierreId;
    private NotificationRecipient pierreRecipient;

    private final TestNotification rewardNotification1Data = new TestNotification(1, NotificationCategory.REWARD_AS_CONTRIBUTOR);
    private final TestNotification rewardNotification2Data = new TestNotification(2, NotificationCategory.REWARD_AS_CONTRIBUTOR);
    private final TestNotification rewardNotification3Data = new TestNotification(3, NotificationCategory.REWARD_AS_CONTRIBUTOR);
    private final TestNotification rewardNotification4Data = new TestNotification(4, NotificationCategory.REWARD_AS_CONTRIBUTOR);
    private final TestNotification gfiNotification1Data = new TestNotification(100, NotificationCategory.PROJECT_GOOD_FIRST_ISSUE_AS_CONTRIBUTOR);
    private final TestNotification gfiNotification2Data = new TestNotification(101, NotificationCategory.PROJECT_GOOD_FIRST_ISSUE_AS_CONTRIBUTOR);

    private static Notification olivierRewardNotification1;
    private static Notification olivierRewardNotification2;
    private static Notification olivierRewardNotification3;
    private static Notification olivierRewardNotification4;
    private static Notification olivierGfiNotification1;
    private static Notification olivierGfiNotification2;


    private static Notification pierreRewardNotification1;
    private static Notification pierreRewardNotification2;
    private static Notification pierreRewardNotification3;
    private static Notification pierreRewardNotification4;
    private static Notification pierreGfiNotification1;
    private static Notification pierreGfiNotification2;

    @BeforeEach
    void setUp() {
        final var olivier = userAuthHelper.authenticateOlivier();
        olivierId = olivier.user().getId();
        olivierRecipient = new NotificationRecipient(olivierId, olivier.user().getGithubEmail(), olivier.user().getGithubLogin());

        final var pierre = userAuthHelper.authenticatePierre();
        pierreId = pierre.user().getId();
        pierreRecipient = new NotificationRecipient(pierreId, pierre.user().getGithubEmail(), pierre.user().getGithubLogin());
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
        olivierRewardNotification1 = notificationPort.push(olivierId, rewardNotification1Data);

        // Then
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1)
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
        olivierRewardNotification2 = notificationPort.push(olivierId, rewardNotification2Data);
        olivierGfiNotification1 = notificationPort.push(olivierId, gfiNotification1Data);

        // Then
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2)
        ), NotificationChannel.DAILY_EMAIL);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2, olivierGfiNotification1)
        ), NotificationChannel.IN_APP);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierGfiNotification1)
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
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2)
        ), NotificationChannel.DAILY_EMAIL);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2, olivierGfiNotification1)
        ), NotificationChannel.IN_APP);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierGfiNotification1)
        ), NotificationChannel.EMAIL);
    }

    @Test
    @Order(11)
    void should_impact_new_notifications_when_channel_is_added_for_category() {
        // When
        olivierGfiNotification2 = notificationPort.push(olivierId, gfiNotification2Data);

        // Then
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2, olivierGfiNotification2)
        ), NotificationChannel.DAILY_EMAIL);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2, olivierGfiNotification1, olivierGfiNotification2)
        ), NotificationChannel.IN_APP);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierGfiNotification1, olivierGfiNotification2)
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
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2, olivierGfiNotification2)
        ), NotificationChannel.DAILY_EMAIL);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2, olivierGfiNotification1, olivierGfiNotification2)
        ), NotificationChannel.IN_APP);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierGfiNotification1, olivierGfiNotification2)
        ), NotificationChannel.EMAIL);
    }

    @Test
    @Order(21)
    void should_impact_new_notifications_when_channel_is_removed_for_category() {
        // When
        olivierRewardNotification3 = notificationPort.push(olivierId, rewardNotification3Data);

        // Then
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2, olivierGfiNotification2)
        ), NotificationChannel.DAILY_EMAIL);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2, olivierGfiNotification1, olivierGfiNotification2,
                        olivierRewardNotification3)
        ), NotificationChannel.IN_APP);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierGfiNotification1, olivierGfiNotification2)
        ), NotificationChannel.EMAIL);
    }

    @Test
    @Order(30)
    void should_not_mix_notifications_between_users() {
        // Given
        notificationSettingsPort.updateNotificationSettings(UserId.of(pierreId), NotificationSettings.builder()
                .channelsPerCategory(Map.of(
                        NotificationCategory.REWARD_AS_CONTRIBUTOR, List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP),
                        NotificationCategory.PROJECT_GOOD_FIRST_ISSUE_AS_CONTRIBUTOR, List.of()
                ))
                .build());

        // When
        pierreRewardNotification1 = notificationPort.push(pierreId, rewardNotification1Data);
        pierreRewardNotification4 = notificationPort.push(pierreId, rewardNotification4Data);
        pierreGfiNotification1 = notificationPort.push(pierreId, gfiNotification1Data);
        olivierRewardNotification4 = notificationPort.push(olivierId, rewardNotification4Data);

        // Then
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2, olivierGfiNotification2)
        ), NotificationChannel.DAILY_EMAIL);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierRewardNotification1, olivierRewardNotification2, olivierGfiNotification1, olivierGfiNotification2,
                        olivierRewardNotification3,
                        olivierRewardNotification4),
                pierreRecipient, List.of(pierreRewardNotification1, pierreRewardNotification4)
        ), NotificationChannel.IN_APP);
        assertPendingNotifications(Map.of(
                olivierRecipient, List.of(olivierGfiNotification1, olivierGfiNotification2),
                pierreRecipient, List.of(pierreRewardNotification1, pierreRewardNotification4)
        ), NotificationChannel.EMAIL);
    }

    @Test
    @Order(40)
    void should_mark_notifications_as_sent() {
        // When
        //TODO notificationPort.markAsSent(rewardNotification1);

        // Then
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

//            final Map<NotificationRecipient, List<NotificationData>> pendingNotificationsDataPerRecipient = pendingNotificationsPerRecipient.entrySet()
//            .stream()
//                    .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(),
//                                    entry.getValue().stream().map(Notification::data).toList()),
//                            Map::putAll);
            assertThat(pendingNotificationsPerRecipient).containsAllEntriesOf(expectedNotifications);
            assertThat(expectedNotifications).containsAllEntriesOf(pendingNotificationsPerRecipient);
        }
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
