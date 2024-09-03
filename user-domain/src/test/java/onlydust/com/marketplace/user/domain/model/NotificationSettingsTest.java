package onlydust.com.marketplace.user.domain.model;

import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class NotificationSettingsTest {

    @Test
    void should_return_has_subscribed_to_marketing_notifications_given_default_settings() {
        // Given
        final NotificationSettings notificationSettings = NotificationSettings.defaultSettings();

        // When
        final boolean hasSubscribedToMarketingEmailNotifications = notificationSettings.hasSubscribedToMarketingEmailNotifications();

        // Then
        Assertions.assertTrue(hasSubscribedToMarketingEmailNotifications);
    }

    @Test
    void should_return_has_not_subscribed_to_marketing_notifications() {
        // Given
        final NotificationSettings notificationSettings1 = NotificationSettings.builder()
                .channelsPerCategory(Map.of(NotificationCategory.CONTRIBUTOR_REWARD, List.of(NotificationChannel.EMAIL)))
                .build();
        final NotificationSettings notificationSettings2 = NotificationSettings.builder()
                .channelsPerCategory(Map.of(NotificationCategory.GLOBAL_MARKETING, List.of(NotificationChannel.EMAIL)))
                .build();


        // When
        final boolean hasSubscribedToMarketingEmailNotifications1 = notificationSettings1.hasSubscribedToMarketingEmailNotifications();
        final boolean hasSubscribedToMarketingEmailNotifications2 = notificationSettings2.hasSubscribedToMarketingEmailNotifications();

        // Then
        Assertions.assertFalse(hasSubscribedToMarketingEmailNotifications1);
        Assertions.assertTrue(hasSubscribedToMarketingEmailNotifications2);
    }


}
