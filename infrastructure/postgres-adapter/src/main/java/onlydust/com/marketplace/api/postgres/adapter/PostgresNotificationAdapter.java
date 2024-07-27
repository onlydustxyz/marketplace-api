package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.NotificationRepository;
import onlydust.com.marketplace.kernel.model.notification.Notification;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.kernel.model.notification.NotificationRecipient;
import onlydust.com.marketplace.user.domain.port.output.NotificationStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;

@AllArgsConstructor
public class PostgresNotificationAdapter implements NotificationStoragePort {
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void save(@NonNull Notification notification) {
        notificationRepository.save(NotificationEntity.of(notification));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<NotificationRecipient, List<Notification>> getPendingNotificationsPerRecipient(@NonNull NotificationChannel notificationChannel) {
        final Map<NotificationRecipient, List<Notification>> pendingNotificationsPerRecipient =
                notificationRepository.findAllPendingByChannel(notificationChannel).stream()
                        .collect(HashMap::new,
                                (map, entity) -> map.computeIfAbsent(entity.recipient().toNotificationRecipient(), k -> new ArrayList<>())
                                        .add(entity.toDomain()),
                                Map::putAll);

        pendingNotificationsPerRecipient.forEach((recipient, notifications) -> notifications.sort(comparing(Notification::createdAt)));
        return pendingNotificationsPerRecipient;
    }
}
