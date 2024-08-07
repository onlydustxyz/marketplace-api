package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationSettingsChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationSettingsChannelReadRepository extends JpaRepository<NotificationSettingsChannelEntity,
        NotificationSettingsChannelEntity.PrimaryKey> {

    List<NotificationSettingsChannelEntity> findAllByUserId(UUID userId);
}
