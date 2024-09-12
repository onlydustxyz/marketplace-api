package onlydust.com.marketplace.user.domain.port.input;

import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.user.domain.model.NotificationSettings;

public interface NotificationSettingsPort {
    void updateNotificationSettings(UserId userId, NotificationSettings settings);

    void patchNotificationSettingsForProject(UserId userId, NotificationSettings.Project settings);
}
