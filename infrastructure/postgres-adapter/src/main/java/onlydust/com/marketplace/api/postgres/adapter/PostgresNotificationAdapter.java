package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.NotificationRepository;
import onlydust.com.marketplace.kernel.model.notification.Notification;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.user.domain.model.SendableNotification;
import onlydust.com.marketplace.user.domain.port.output.NotificationStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public List<SendableNotification> getPendingNotifications(@NonNull NotificationChannel notificationChannel) {
        return notificationRepository.findAllPendingByChannel(notificationChannel).stream()
                .map(NotificationEntity::toDomain)
                .sorted(comparing(SendableNotification::createdAt))
                .toList();
    }
}
