package onlydust.com.marketplace.user.domain.job;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.model.notification.Notification;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.user.domain.port.output.NotificationSender;
import onlydust.com.marketplace.user.domain.port.output.NotificationStoragePort;

@AllArgsConstructor
public class NotificationDailyEmailJob {
    private final NotificationStoragePort notificationStoragePort;
    private final NotificationSender notificationEmailSender;

    public void run() {
        final var pendingNotifications = notificationStoragePort.getPendingNotifications(NotificationChannel.SUMMARY_EMAIL);
        notificationEmailSender.send(pendingNotifications);
        notificationStoragePort.markAsSent(NotificationChannel.SUMMARY_EMAIL, pendingNotifications.stream().map(Notification::id).toList());
    }
}
