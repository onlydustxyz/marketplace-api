package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationSettingsForProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSettingsForProjectRepository extends JpaRepository<NotificationSettingsForProjectEntity,
        NotificationSettingsForProjectEntity.PrimaryKey> {
}
