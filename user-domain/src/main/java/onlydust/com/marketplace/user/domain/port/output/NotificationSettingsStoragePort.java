package onlydust.com.marketplace.user.domain.port.output;

import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.user.domain.model.NotificationSettings;
import onlydust.com.marketplace.user.domain.model.ProjectId;
import onlydust.com.marketplace.user.domain.model.SmallUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationSettingsStoragePort {
    Optional<NotificationSettings.Project> getNotificationSettingsForProject(SmallUser.Id userId, ProjectId projectId);

    void saveNotificationSettingsForProject(SmallUser.Id userId, NotificationSettings.Project settings);

    List<NotificationChannel> getNotificationChannels(UUID recipientId, NotificationCategory category);

    void save(SmallUser.Id userId, NotificationSettings settings);
}
