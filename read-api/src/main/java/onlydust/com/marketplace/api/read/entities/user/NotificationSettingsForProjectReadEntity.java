package onlydust.com.marketplace.api.read.entities.user;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.NotificationSettingsForProjectResponse;
import onlydust.com.marketplace.api.read.entities.project.ProjectLinkReadEntity;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Entity
@Immutable
@Accessors(fluent = true)
@Table(name = "user_projects_notification_settings", schema = "iam")
@IdClass(NotificationSettingsForProjectReadEntity.PrimaryKey.class)
public class NotificationSettingsForProjectReadEntity {
    @Id
    @Column(nullable = false, updatable = false)
    UUID userId;

    @Id
    @Column(nullable = false, updatable = false)
    UUID projectId;

    Boolean onGoodFirstIssueAdded;

    @ManyToOne
    @JoinColumn(name = "projectId", referencedColumnName = "id", insertable = false, updatable = false)
    @NonNull
    ProjectLinkReadEntity project;

    public NotificationSettingsForProjectResponse toDto() {
        return new NotificationSettingsForProjectResponse()
                .id(project.id())
                .slug(project.slug())
                .name(project.name())
                .logoUrl(project.logoUrl())
                .onGoodFirstIssueAdded(onGoodFirstIssueAdded);
    }

    public static NotificationSettingsForProjectResponse defaultDto(ProjectLinkReadEntity project) {
        return new NotificationSettingsForProjectResponse()
                .id(project.id())
                .slug(project.slug())
                .name(project.name())
                .logoUrl(project.logoUrl())
                .onGoodFirstIssueAdded(false);
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrimaryKey implements Serializable {
        UUID userId;
        UUID projectId;
    }
}
