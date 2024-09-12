package onlydust.com.marketplace.user.domain.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.model.notification.Notification;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.kernel.model.notification.NotificationData;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.user.domain.model.NotificationStatusUpdateRequest;
import onlydust.com.marketplace.user.domain.model.SendableNotification;
import onlydust.com.marketplace.user.domain.port.output.AppUserStoragePort;
import onlydust.com.marketplace.user.domain.port.output.NotificationSender;
import onlydust.com.marketplace.user.domain.port.output.NotificationSettingsStoragePort;
import onlydust.com.marketplace.user.domain.port.output.NotificationStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class NotificationService implements NotificationPort {
    private final NotificationSettingsStoragePort notificationSettingsStoragePort;
    private final NotificationStoragePort notificationStoragePort;
    private final AppUserStoragePort userStoragePort;
    private final NotificationSender asyncNotificationEmailProcessor;

    @Override
    public Notification push(UserId recipientId, NotificationData notificationData) {
        final var channels = notificationSettingsStoragePort.getNotificationChannels(recipientId, notificationData.category());
        final var notification = Notification.of(recipientId, notificationData, new HashSet<>(channels));
        notificationStoragePort.save(notification);

        if (channels.contains(NotificationChannel.EMAIL)) {
            sendEmail(recipientId, notification);
        }
        return notification;
    }

    private void sendEmail(UserId recipientId, Notification notification) {
        userStoragePort.findById(recipientId)
                .ifPresent(user -> asyncNotificationEmailProcessor.send(SendableNotification.of(user, notification)));
    }

    public void markAllInAppUnreadAsRead(UserId userId) {
        notificationStoragePort.markAllInAppUnreadAsRead(userId);
    }

    @Transactional
    public void updateInAppNotificationsStatus(UserId userId, List<NotificationStatusUpdateRequest> notificationStatusUpdateRequests) {
        final Map<NotificationStatusUpdateRequest.NotificationStatus, List<NotificationStatusUpdateRequest>> notificationStatusListMap =
                notificationStatusUpdateRequests.stream().collect(Collectors.groupingBy(NotificationStatusUpdateRequest::notificationStatus));
        final List<NotificationStatusUpdateRequest> unread = notificationStatusListMap.get(NotificationStatusUpdateRequest.NotificationStatus.UNREAD);
        if (unread != null && !unread.isEmpty()) {
            notificationStoragePort.markInAppNotificationsAsUnreadForUser(userId, unread.stream()
                    .map(NotificationStatusUpdateRequest::notificationId)
                    .toList());
        }
        final List<NotificationStatusUpdateRequest> read = notificationStatusListMap.get(NotificationStatusUpdateRequest.NotificationStatus.READ);
        if (read != null && !read.isEmpty()) {
            notificationStoragePort.markInAppNotificationsAsReadForUser(userId, read.stream()
                    .map(NotificationStatusUpdateRequest::notificationId)
                    .toList());
        }

    }
}
