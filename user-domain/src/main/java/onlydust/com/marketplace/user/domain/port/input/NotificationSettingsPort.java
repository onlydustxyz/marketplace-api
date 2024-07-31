package onlydust.com.marketplace.user.domain.port.input;

import onlydust.com.marketplace.user.domain.model.NotificationRecipient;
import onlydust.com.marketplace.user.domain.model.NotificationSettings;

public interface NotificationSettingsPort {
    void updateNotificationSettings(NotificationRecipient.Id userId, NotificationSettings settings);

    void patchNotificationSettingsForProject(NotificationRecipient.Id userId, NotificationSettings.Project settings);
}
