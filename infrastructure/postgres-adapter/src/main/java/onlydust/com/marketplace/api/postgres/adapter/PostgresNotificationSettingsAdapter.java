package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationSettingsChannelEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationSettingsForProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.NotificationSettingsChannelRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.NotificationSettingsForProjectRepository;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.user.domain.model.NotificationRecipient;
import onlydust.com.marketplace.user.domain.model.NotificationSettings;
import onlydust.com.marketplace.user.domain.model.ProjectId;
import onlydust.com.marketplace.user.domain.port.output.NotificationSettingsStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class PostgresNotificationSettingsAdapter implements NotificationSettingsStoragePort {
    private final NotificationSettingsForProjectRepository notificationSettingsForProjectRepository;
    private final NotificationSettingsChannelRepository notificationSettingsChannelRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationSettings.Project> getNotificationSettingsForProject(NotificationRecipient.Id userId, ProjectId projectId) {
        return notificationSettingsForProjectRepository.findById(new NotificationSettingsForProjectEntity.PrimaryKey(userId.value(), projectId.value()))
                .map(NotificationSettingsForProjectEntity::toDomain);
    }

    @Override
    @Transactional
    public void saveNotificationSettingsForProject(NotificationRecipient.Id userId, NotificationSettings.Project settings) {
        notificationSettingsForProjectRepository.save(NotificationSettingsForProjectEntity.of(userId, settings));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationChannel> getNotificationChannels(UUID recipientId, NotificationCategory category) {
        return notificationSettingsChannelRepository.findAllByUserIdAndCategory(recipientId, category).stream()
                .map(NotificationSettingsChannelEntity::channel)
                .toList();
    }

    @Override
    @Transactional
    public void save(NotificationRecipient.Id userId, NotificationSettings settings) {
        final List<NotificationSettingsChannelEntity> entities = settings.channelsPerCategory().entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(channel -> new NotificationSettingsChannelEntity(userId.value(), entry.getKey(), channel)))
                .toList();

        notificationSettingsChannelRepository.deleteAllByUserId(userId.value());
        notificationSettingsChannelRepository.saveAll(entities);
    }
}
