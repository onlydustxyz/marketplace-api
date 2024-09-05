package onlydust.com.marketplace.api.read.entities.user;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.NotificationStatus;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationEntity;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationData;
import onlydust.com.marketplace.kernel.model.notification.NotificationTypeIdResolver;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.UUID;

import static java.util.Objects.isNull;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "notifications", schema = "iam")
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
public class NotificationReadEntity {

    @Id
    UUID id;
    @Column(nullable = false)
    UUID recipientId;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @NonNull
    NotificationCategory category;
    @Column(nullable = false)
    ZonedDateTime createdAt;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    @NonNull
    NotificationEntity.Data data;
    boolean isRead;


    @lombok.Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Data {

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
        @JsonTypeIdResolver(NotificationTypeIdResolver.class)
        private NotificationData notification;
    }
    
    public static Boolean isReadFromContract(final NotificationStatus status) {
        return isNull(status) ? null : switch (status) {
            case READ -> true;
            case UNREAD -> false;
        };
    }

}
