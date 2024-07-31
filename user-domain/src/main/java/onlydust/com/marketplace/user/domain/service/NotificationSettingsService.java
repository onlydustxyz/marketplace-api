package onlydust.com.marketplace.user.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.user.domain.model.NotificationSettings;
import onlydust.com.marketplace.user.domain.model.SmallUser;
import onlydust.com.marketplace.user.domain.port.input.NotificationSettingsPort;
import onlydust.com.marketplace.user.domain.port.output.NotificationSettingsStoragePort;

@AllArgsConstructor
public class NotificationSettingsService implements NotificationSettingsPort {

    private final NotificationSettingsStoragePort notificationSettingsStoragePort;

    @Override
    public void updateNotificationSettings(SmallUser.Id userId, NotificationSettings settings) {
        notificationSettingsStoragePort.save(userId, settings);
    }

    @Override
    public void patchNotificationSettingsForProject(SmallUser.Id userId, NotificationSettings.Project settings) {
        final var currentSettings = notificationSettingsStoragePort.getNotificationSettingsForProject(userId, settings.projectId())
                .orElse(settings);
        notificationSettingsStoragePort.saveNotificationSettingsForProject(userId, currentSettings.patchWith(settings));
    }
}
