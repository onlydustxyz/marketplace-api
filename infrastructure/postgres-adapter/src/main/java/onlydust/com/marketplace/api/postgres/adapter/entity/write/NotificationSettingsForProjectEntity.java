package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.user.domain.model.NotificationSettings;
import onlydust.com.marketplace.user.domain.model.ProjectId;
import onlydust.com.marketplace.user.domain.model.UserId;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "user_projects_notification_settings", schema = "iam")
@IdClass(NotificationSettingsForProjectEntity.PrimaryKey.class)
public class NotificationSettingsForProjectEntity {
    @Id
    @Column(nullable = false, updatable = false)
    @EqualsAndHashCode.Include
    UUID userId;

    @Id
    @Column(nullable = false, updatable = false)
    @EqualsAndHashCode.Include
    UUID projectId;

    Boolean onGoodFirstIssueAdded;

    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrimaryKey implements Serializable {
        UUID userId;
        UUID projectId;
    }

    public static NotificationSettingsForProjectEntity of(UserId userId, NotificationSettings.Project settings) {
        return NotificationSettingsForProjectEntity.builder()
                .userId(userId.value())
                .projectId(settings.projectId().value())
                .onGoodFirstIssueAdded(settings.onGoodFirstIssueAdded().orElse(null))
                .build();
    }

    public NotificationSettings.Project toDomain() {
        return new NotificationSettings.Project(
                ProjectId.of(projectId),
                Optional.ofNullable(onGoodFirstIssueAdded)
        );
    }
}
