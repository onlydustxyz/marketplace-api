package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationSettingsForProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationSettingsForProjectRepository extends JpaRepository<NotificationSettingsForProjectEntity,
        NotificationSettingsForProjectEntity.PrimaryKey> {

    List<NotificationSettingsForProjectEntity> findAllByProjectIdAndOnGoodFirstIssueAdded(UUID projectId, boolean onGoodFirstIssueAdded);
}
