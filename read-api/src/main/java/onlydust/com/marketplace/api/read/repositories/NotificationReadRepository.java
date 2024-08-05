package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.user.NotificationReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface NotificationReadRepository extends JpaRepository<NotificationReadEntity, UUID> {

    @Query(nativeQuery = true, value = """
                select n.*,
                       nc.read_at is not null is_read
                from iam.notifications n
                join iam.notification_channels nc on nc.notification_id = n.id
                where n.recipient_id = :userId 
                and nc.channel = 'IN_APP'
                and (coalesce(:isRead) is null or (nc.read_at is not null) = :isRead)
            """)
    Page<NotificationReadEntity> findAllInAppByStatusAndUserId(Boolean isRead, UUID userId, Pageable pageable);

    @Query(nativeQuery = true, value = """
                select count(n.id)
                from iam.notifications n
                join iam.notification_channels nc on nc.notification_id = n.id
                where n.recipient_id = :userId
                and nc.channel = 'IN_APP'
                and (coalesce(:isRead) is null or (nc.read_at is not null) = :isRead)
            """)
    int countAllInAppByStatusForUserId(Boolean isRead, UUID userId);
}
