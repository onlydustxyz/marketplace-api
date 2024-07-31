package onlydust.com.marketplace.user.domain.port.input;

import onlydust.com.marketplace.user.domain.model.NotificationSettings;
import onlydust.com.marketplace.user.domain.model.User;

public interface NotificationSettingsPort {
    void updateNotificationSettings(User.Id userId, NotificationSettings settings);

    void patchNotificationSettingsForProject(User.Id userId, NotificationSettings.Project settings);
}
