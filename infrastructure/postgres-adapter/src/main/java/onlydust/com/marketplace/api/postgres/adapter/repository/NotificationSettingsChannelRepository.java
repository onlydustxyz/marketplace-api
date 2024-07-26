package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationSettingsChannelEntity;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface NotificationSettingsChannelRepository extends Repository<NotificationSettingsChannelEntity, NotificationSettingsChannelEntity.PrimaryKey> {

    List<NotificationSettingsChannelEntity> findAllByUserIdAndCategory(UUID userId, NotificationCategory category);

}
