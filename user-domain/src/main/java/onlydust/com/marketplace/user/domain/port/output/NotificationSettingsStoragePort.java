package onlydust.com.marketplace.user.domain.port.output;

import onlydust.com.marketplace.user.domain.model.NotificationSettings;
import onlydust.com.marketplace.user.domain.model.ProjectId;
import onlydust.com.marketplace.user.domain.model.UserId;

import java.util.Optional;

public interface NotificationSettingsStoragePort {
    Optional<NotificationSettings.Project> getNotificationSettingsForProject(UserId userId, ProjectId projectId);

    void saveNotificationSettingsForProject(UserId userId, NotificationSettings.Project settings);
}
