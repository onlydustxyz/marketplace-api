package onlydust.com.marketplace.user.domain.port.input;

import onlydust.com.marketplace.user.domain.model.NotificationSettings;
import onlydust.com.marketplace.user.domain.model.SmallUser;

public interface NotificationSettingsPort {
    void updateNotificationSettings(SmallUser.Id userId, NotificationSettings settings);

    void patchNotificationSettingsForProject(SmallUser.Id userId, NotificationSettings.Project settings);
}
