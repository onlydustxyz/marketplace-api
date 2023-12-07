package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.domain.model.notification.Event;
import onlydust.com.marketplace.api.postgres.adapter.OutboxRepository;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationEntity;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface NotificationRepository extends OutboxRepository<NotificationEntity> {

    @Override
    default void saveEvent(Event event) {
        save(new NotificationEntity(event));
    }

    @Override
    @Query(value = """
            SELECT next_notif
            FROM NotificationEntity next_notif
            WHERE next_notif.id = (SELECT min(n.id) FROM NotificationEntity n WHERE n.status = 'PENDING' OR n.status = 'FAILED')
            """)
    Optional<NotificationEntity> findNextToProcess();
}
