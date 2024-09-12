package onlydust.com.marketplace.user.domain.port.output;

import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.user.domain.model.NotificationSettings;

import java.util.List;
import java.util.Optional;

public interface NotificationSettingsStoragePort {
    Optional<NotificationSettings.Project> getNotificationSettingsForProject(UserId userId, ProjectId projectId);

    void saveNotificationSettingsForProject(UserId userId, NotificationSettings.Project settings);

    List<NotificationChannel> getNotificationChannels(UserId recipientId, NotificationCategory category);

    void create(UserId userId, NotificationSettings settings);

    void update(UserId userId, NotificationSettings settings);
}
