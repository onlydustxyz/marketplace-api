package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.notification.Notification;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.kernel.model.notification.NotificationIdResolver;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;
import java.util.List;
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

    public static NotificationEntity of(@NonNull UUID recipientId, @NonNull Notification notification, @NonNull List<NotificationChannel> channels) {
        final var notificationId = UUID.randomUUID();
        return NotificationEntity.builder()
                .id(notificationId)
                .recipientId(recipientId)
                .category(notification.category())
                .data(new Data(notification))
                .createdAt(notification.createdAt())
                .channels(channels.stream().map(channel -> NotificationChannelEntity.of(notificationId, channel)).collect(Collectors.toSet()))
                .build();
    }

    public Notification toDomain() {
        return data.notification;
    }

    @lombok.Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Data {

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
        @JsonTypeIdResolver(NotificationIdResolver.class)
        private Notification notification;
    }
}
