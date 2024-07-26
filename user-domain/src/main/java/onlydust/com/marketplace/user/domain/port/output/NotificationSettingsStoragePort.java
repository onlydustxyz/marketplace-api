package onlydust.com.marketplace.user.domain.port.output;

import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.user.domain.model.NotificationSettings;
import onlydust.com.marketplace.user.domain.model.ProjectId;
import onlydust.com.marketplace.user.domain.model.UserId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationSettingsStoragePort {
    Optional<NotificationSettings.Project> getNotificationSettingsForProject(UserId userId, ProjectId projectId);

    void saveNotificationSettingsForProject(UserId userId, NotificationSettings.Project settings);

    List<NotificationChannel> getNotificationChannels(UUID recipientId, NotificationCategory category);
}
