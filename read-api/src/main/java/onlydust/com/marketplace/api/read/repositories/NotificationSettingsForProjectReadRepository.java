package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.user.NotificationSettingsForProjectReadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSettingsForProjectReadRepository extends JpaRepository<NotificationSettingsForProjectReadEntity,
        NotificationSettingsForProjectReadEntity.PrimaryKey> {
}
