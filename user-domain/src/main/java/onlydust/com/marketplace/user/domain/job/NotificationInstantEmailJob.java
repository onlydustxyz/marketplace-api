package onlydust.com.marketplace.user.domain.job;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.user.domain.port.output.NotificationSender;
import onlydust.com.marketplace.user.domain.port.output.NotificationStoragePort;

@AllArgsConstructor
public class NotificationInstantEmailJob {
    private final NotificationStoragePort notificationStoragePort;
    private final NotificationSender notificationInstantEmailSender;

    public void run() {
        final var pendingNotifications = notificationStoragePort.getPendingNotifications(NotificationChannel.EMAIL);
        notificationInstantEmailSender.send(pendingNotifications);
    }
}
