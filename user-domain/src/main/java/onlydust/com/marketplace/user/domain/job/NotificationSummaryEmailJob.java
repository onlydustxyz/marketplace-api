package onlydust.com.marketplace.user.domain.job;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.model.notification.Notification;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.user.domain.model.NotificationRecipient;
import onlydust.com.marketplace.user.domain.model.SendableNotification;
import onlydust.com.marketplace.user.domain.port.output.NotificationSender;
import onlydust.com.marketplace.user.domain.port.output.NotificationStoragePort;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class NotificationSummaryEmailJob {
    private final NotificationStoragePort notificationStoragePort;
    private final NotificationSender notificationEmailSender;

    public void run() {
        final Map<NotificationRecipient, List<SendableNotification>> notificationsMappedByRecipient =
                notificationStoragePort.getPendingNotifications(NotificationChannel.SUMMARY_EMAIL)
                        .stream()
                        .collect(Collectors.groupingBy(SendableNotification::recipient));
        for (Map.Entry<NotificationRecipient, List<SendableNotification>> notificationRecipientListEntry : notificationsMappedByRecipient.entrySet()) {
            notificationEmailSender.sendAllForRecipient(notificationRecipientListEntry.getKey(), notificationRecipientListEntry.getValue());
            notificationStoragePort.markAsSent(NotificationChannel.SUMMARY_EMAIL,
                    notificationRecipientListEntry.getValue().stream().map(Notification::id).toList());
        }
    }
}
