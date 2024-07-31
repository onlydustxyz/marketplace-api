package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.notification.Notification;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationData;
import onlydust.com.marketplace.kernel.model.notification.NotificationTypeIdResolver;
import onlydust.com.marketplace.user.domain.model.SendableNotification;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Accessors(fluent = true)
@Data
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter(AccessLevel.PUBLIC)
@Table(name = "notifications", schema = "iam")
@EntityListeners(AuditingEntityListener.class)
public class NotificationEntity {

    @Id
    @NonNull
    @EqualsAndHashCode.Include
    UUID id;

    @Column(nullable = false)
    @NonNull
    UUID recipientId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @NonNull
    NotificationCategory category;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    @NonNull
    Data data;

    ZonedDateTime createdAt;

    @OneToMany(mappedBy = "notificationId", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @NonNull
    Set<NotificationChannelEntity> channels;

    @ManyToOne
    @JoinColumn(name = "recipientId", insertable = false, updatable = false)
    UserEntity recipient;

    public static NotificationEntity of(@NonNull Notification notification) {
        return NotificationEntity.builder()
                .id(notification.id().value())
                .recipientId(notification.recipientId())
                .category(notification.data().category())
                .data(new Data(notification.data()))
                .createdAt(notification.createdAt())
                .channels(notification.channels().stream().map(channel -> NotificationChannelEntity.of(notification.id().value(), channel)).collect(Collectors.toSet()))
                .build();
    }

    public SendableNotification toDomain() {
        return SendableNotification.builder()
                .id(Notification.Id.of(id))
                .recipientId(recipientId)
                .data(data.notification)
                .createdAt(createdAt)
                .channels(channels.stream().map(NotificationChannelEntity::channel).collect(Collectors.toSet()))
                .recipient(recipient.toUser())
                .build();
    }

    @lombok.Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Data {

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
        @JsonTypeIdResolver(NotificationTypeIdResolver.class)
        private NotificationData notification;
    }
}
