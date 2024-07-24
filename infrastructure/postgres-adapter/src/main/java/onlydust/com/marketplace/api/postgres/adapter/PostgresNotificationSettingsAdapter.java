package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationSettingsForProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.NotificationSettingsForProjectRepository;
import onlydust.com.marketplace.user.domain.model.NotificationSettings;
import onlydust.com.marketplace.user.domain.model.ProjectId;
import onlydust.com.marketplace.user.domain.model.UserId;
import onlydust.com.marketplace.user.domain.port.output.NotificationSettingsStoragePort;

import java.util.Optional;

@AllArgsConstructor
public class PostgresNotificationSettingsAdapter implements NotificationSettingsStoragePort {
    private final NotificationSettingsForProjectRepository notificationSettingsForProjectRepository;

    @Override
    public Optional<NotificationSettings.Project> getNotificationSettingsForProject(UserId userId, ProjectId projectId) {
        return notificationSettingsForProjectRepository.findById(new NotificationSettingsForProjectEntity.PrimaryKey(userId.value(), projectId.value()))
                .map(NotificationSettingsForProjectEntity::toDomain);
    }

    @Override
    public void saveNotificationSettingsForProject(UserId userId, NotificationSettings.Project settings) {
        notificationSettingsForProjectRepository.save(NotificationSettingsForProjectEntity.of(userId, settings));
    }
}
