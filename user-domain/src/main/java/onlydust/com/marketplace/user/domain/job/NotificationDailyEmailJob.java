package onlydust.com.marketplace.user.domain.job;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.model.notification.Notification;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.user.domain.port.output.NotificationSender;
import onlydust.com.marketplace.user.domain.port.output.NotificationStoragePort;

@AllArgsConstructor
public class NotificationDailyEmailJob {
    private final NotificationStoragePort notificationStoragePort;
    private final NotificationSender notificationEmailSender;
    private final NotificationPort notificationPort;

    public void run() {
        final var pendingNotifications = notificationStoragePort.getPendingNotifications(NotificationChannel.DAILY_EMAIL);
        notificationEmailSender.send(pendingNotifications);
        notificationPort.markAsSent(NotificationChannel.DAILY_EMAIL, pendingNotifications.stream().map(Notification::id).toList());
    }
}
