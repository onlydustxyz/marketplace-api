package onlydust.com.marketplace.user.domain.port.input;

import onlydust.com.marketplace.user.domain.model.NotificationSettings;
import onlydust.com.marketplace.user.domain.model.UserId;

public interface NotificationSettingsPort {
    void patchNotificationSettingsForProject(UserId userId, NotificationSettings.Project settings);
}
