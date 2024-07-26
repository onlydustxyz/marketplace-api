package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Accessors(fluent = true, chain = true)
@Table(name = "notification_channels", schema = "iam")
@EntityListeners(AuditingEntityListener.class)
@IdClass(NotificationChannelEntity.PrimaryKey.class)
public class NotificationChannelEntity {

    @Id
    @EqualsAndHashCode.Include
    UUID notificationId;

    @Id
    @EqualsAndHashCode.Include
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    NotificationChannel channel;

    ZonedDateTime sentAt;

    public static NotificationChannelEntity of(UUID notificationId, NotificationChannel channel) {
        return new NotificationChannelEntity()
                .notificationId(notificationId)
                .channel(channel);
    }

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        UUID notificationId;
        NotificationChannel channel;
    }
}
