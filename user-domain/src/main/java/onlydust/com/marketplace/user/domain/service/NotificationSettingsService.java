package onlydust.com.marketplace.user.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.user.domain.model.NotificationRecipient;
import onlydust.com.marketplace.user.domain.model.NotificationSettings;
import onlydust.com.marketplace.user.domain.port.input.NotificationSettingsPort;
import onlydust.com.marketplace.user.domain.port.input.UserObserverPort;
import onlydust.com.marketplace.user.domain.port.output.NotificationSettingsStoragePort;

@AllArgsConstructor
public class NotificationSettingsService implements NotificationSettingsPort, UserObserverPort {

    private final NotificationSettingsStoragePort notificationSettingsStoragePort;

    @Override
    public void updateNotificationSettings(NotificationRecipient.Id userId, NotificationSettings settings) {
        notificationSettingsStoragePort.save(userId, settings);
    }

    @Override
    public void patchNotificationSettingsForProject(NotificationRecipient.Id userId, NotificationSettings.Project settings) {
        final var currentSettings = notificationSettingsStoragePort.getNotificationSettingsForProject(userId, settings.projectId())
                .orElse(settings);
        notificationSettingsStoragePort.saveNotificationSettingsForProject(userId, currentSettings.patchWith(settings));
    }

    @Override
    public void onUserSignedUp(AuthenticatedUser user) {
        notificationSettingsStoragePort.save(NotificationRecipient.Id.of(user.id()), NotificationSettings.defaultSettings());
    }
}
