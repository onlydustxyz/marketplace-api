package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.notification.Notification;
import onlydust.com.marketplace.api.domain.port.output.NotificationPort;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.NotificationRepository;

import java.util.Optional;

@AllArgsConstructor
public class PostgresNotificationAdapter implements NotificationPort {

    private final NotificationRepository notificationRepository;

    @Override
    public void push(Notification notification) {
        notificationRepository.save(new NotificationEntity(notification));
    }

    @Override
    public Optional<Notification> peek() {
        return notificationRepository.findNextToProcess().map(entity -> entity.getPayload().getNotification());
    }

    @Override
    public void ack() {
        notificationRepository.findNextToProcess().ifPresent(entity -> {
            entity.setStatus(NotificationEntity.Status.PROCESSED);
            entity.setError(null);
            notificationRepository.save(entity);
        });
    }

    @Override
    public void nack(String message) {
        notificationRepository.findNextToProcess().ifPresent(entity -> {
            entity.setStatus(NotificationEntity.Status.FAILED);
            entity.setError(message);
            notificationRepository.save(entity);
        });
    }
}
