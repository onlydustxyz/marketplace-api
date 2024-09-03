package onlydust.com.marketplace.user.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.user.domain.model.NotificationRecipient;
import onlydust.com.marketplace.user.domain.model.NotificationSettings;
import onlydust.com.marketplace.user.domain.port.input.NotificationSettingsPort;
import onlydust.com.marketplace.user.domain.port.input.UserObserverPort;
import onlydust.com.marketplace.user.domain.port.output.AppUserStoragePort;
import onlydust.com.marketplace.user.domain.port.output.MarketingNotificationSettingsStoragePort;
import onlydust.com.marketplace.user.domain.port.output.NotificationSettingsStoragePort;

@AllArgsConstructor
public class NotificationSettingsService implements NotificationSettingsPort, UserObserverPort {

    private final NotificationSettingsStoragePort notificationSettingsStoragePort;
    private final MarketingNotificationSettingsStoragePort marketingNotificationSettingsStoragePort;
    private final AppUserStoragePort appUserStoragePort;

    @Override
    public void updateNotificationSettings(NotificationRecipient.Id userId, NotificationSettings settings) {
        notificationSettingsStoragePort.update(userId, settings);
        updateMarketingNotificationSettings(userId, settings);

    }

    private void updateMarketingNotificationSettings(NotificationRecipient.Id userId, NotificationSettings settings) {
        final NotificationRecipient notificationRecipient = appUserStoragePort.findById(userId)
                .orElseThrow(() -> OnlyDustException.internalServerError("User %s not found"));
        marketingNotificationSettingsStoragePort.update(notificationRecipient.email(), settings);
    }


    @Override
    public void patchNotificationSettingsForProject(NotificationRecipient.Id userId, NotificationSettings.Project settings) {
        final var currentSettings = notificationSettingsStoragePort.getNotificationSettingsForProject(userId, settings.projectId())
                .orElse(settings);
        notificationSettingsStoragePort.saveNotificationSettingsForProject(userId, currentSettings.patchWith(settings));
    }

    @Override
    public void onUserSignedUp(AuthenticatedUser user) {
        notificationSettingsStoragePort.create(NotificationRecipient.Id.of(user.id()), NotificationSettings.defaultSettings());
    }
}
