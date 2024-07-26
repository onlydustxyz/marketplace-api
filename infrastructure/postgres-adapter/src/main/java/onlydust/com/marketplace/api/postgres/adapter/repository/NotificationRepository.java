package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationEntity;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends Repository<NotificationEntity, UUID> {

    void save(NotificationEntity notificationEntity);

    @Query("""
            SELECT n
            FROM NotificationEntity n
            JOIN n.channels c
            WHERE c.channel = :channel
              AND c.sentAt IS NULL
            """)
    List<NotificationEntity> findAllPendingByChannel(NotificationChannel channel);
}
