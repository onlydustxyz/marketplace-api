package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationSettingsChannelEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationSettingsForProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.NotificationSettingsChannelRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.NotificationSettingsForProjectRepository;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.user.domain.model.NotificationSettings;
import onlydust.com.marketplace.user.domain.port.output.NotificationSettingsStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class PostgresNotificationSettingsAdapter implements NotificationSettingsStoragePort {
    private final NotificationSettingsForProjectRepository notificationSettingsForProjectRepository;
    private final NotificationSettingsChannelRepository notificationSettingsChannelRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationSettings.Project> getNotificationSettingsForProject(UserId userId, ProjectId projectId) {
        return notificationSettingsForProjectRepository.findById(new NotificationSettingsForProjectEntity.PrimaryKey(userId.value(), projectId.value()))
                .map(NotificationSettingsForProjectEntity::toDomain);
    }

    @Override
    @Transactional
    public void saveNotificationSettingsForProject(UserId userId, NotificationSettings.Project settings) {
        final var entity = notificationSettingsForProjectRepository
                .findById(new NotificationSettingsForProjectEntity.PrimaryKey(userId.value(), settings.projectId().value()))
                .map(e -> {
                    e.setOnGoodFirstIssueAdded(settings.onGoodFirstIssueAdded().orElse(null));
                    return e;
                })
                .orElseGet(() -> NotificationSettingsForProjectEntity.of(userId, settings));
        notificationSettingsForProjectRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationChannel> getNotificationChannels(UserId recipientId, NotificationCategory category) {
        return notificationSettingsChannelRepository.findAllByUserIdAndCategory(recipientId.value(), category).stream()
                .map(NotificationSettingsChannelEntity::channel)
                .toList();
    }

    @Override
    @Transactional
    public void create(UserId userId, NotificationSettings settings) {
        final List<NotificationSettingsChannelEntity> entities = settings.channelsPerCategory().entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(channel -> new NotificationSettingsChannelEntity(userId.value(), entry.getKey(), channel)))
                .toList();
        notificationSettingsChannelRepository.saveAll(entities);
    }

    @Override
    @Transactional
    public void update(UserId userId, NotificationSettings settings) {
        final List<NotificationSettingsChannelEntity> entities = settings.channelsPerCategory().entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(channel -> new NotificationSettingsChannelEntity(userId.value(), entry.getKey(), channel)))
                .toList();
        notificationSettingsChannelRepository.deleteAllByUserId(userId.value());
        notificationSettingsChannelRepository.saveAll(entities);
    }
}
