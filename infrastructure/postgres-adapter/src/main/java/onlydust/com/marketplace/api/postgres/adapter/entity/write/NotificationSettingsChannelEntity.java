package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder
@Accessors(fluent = true)
@Table(name = "user_notification_settings_channels", schema = "iam")
@IdClass(NotificationSettingsChannelEntity.PrimaryKey.class)
public class NotificationSettingsChannelEntity {
    @Id
    @Column(nullable = false, updatable = false)
    @NonNull
    UUID userId;

    @Id
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false, updatable = false)
    @NonNull
    NotificationCategory category;

    @Id
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false, updatable = false)
    @NonNull
    NotificationChannel channel;

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        UUID userId;
        @Enumerated(EnumType.STRING)
        NotificationCategory category;
        @Enumerated(EnumType.STRING)
        NotificationChannel channel;
    }
}
