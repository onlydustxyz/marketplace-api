package onlydust.com.marketplace.user.domain.port.output;

import onlydust.com.marketplace.user.domain.model.NotificationSettings;

public interface MarketingNotificationSettingsStoragePort {
    void update(String email, NotificationSettings settings);
}
