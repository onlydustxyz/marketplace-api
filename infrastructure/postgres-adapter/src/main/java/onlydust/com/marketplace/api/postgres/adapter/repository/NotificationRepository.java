package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long>,
        JpaSpecificationExecutor<NotificationEntity> {

    @Query(value = """
            SELECT n FROM NotificationEntity n WHERE n.status = 'PENDING' OR n.status = 'FAILED' ORDER BY n.id ASC
            """)
    Optional<NotificationEntity> findNextToProcess();
}
