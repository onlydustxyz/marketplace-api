package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.NotificationRepository;
import onlydust.com.marketplace.kernel.model.notification.Notification;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.kernel.model.notification.NotificationRecipient;
import onlydust.com.marketplace.user.domain.port.output.NotificationStoragePort;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Comparator.comparing;

@AllArgsConstructor
public class PostgresNotificationAdapter implements NotificationStoragePort {
    private final NotificationRepository notificationRepository;

    @Override
    public void save(@NonNull UUID recipientId, @NonNull Notification notification, @NonNull List<NotificationChannel> channels) {
        notificationRepository.save(NotificationEntity.of(recipientId, notification, channels));
    }

    @Override
    public Map<NotificationRecipient, List<Notification>> getPendingNotificationsPerRecipient(@NonNull NotificationChannel notificationChannel) {
        final Map<NotificationRecipient, List<Notification>> pendingNotificationsPerRecipient =
                notificationRepository.findAllPendingByChannel(notificationChannel).stream()
                        .collect(HashMap::new,
                                (map, notification) -> map.computeIfAbsent(notification.recipient().toNotificationRecipient(), k -> List.of())
                                        .add(notification.toDomain()),
                                Map::putAll);

        pendingNotificationsPerRecipient.forEach((recipient, notifications) -> notifications.sort(comparing(Notification::createdAt)));
        return pendingNotificationsPerRecipient;
    }
}