package onlydust.com.marketplace.api.read.entities.user;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.accounting.domain.notification.RewardCanceled;
import onlydust.com.marketplace.accounting.domain.notification.RewardReceived;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NotificationEntity;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationData;
import onlydust.com.marketplace.kernel.model.notification.NotificationTypeIdResolver;
import onlydust.com.marketplace.project.domain.model.notification.CommitteeApplicationCreated;
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

    public NotificationPageItemResponse toNotificationPageItemResponse() {
        final NotificationPageItemResponseData notificationPageItemResponseData = new NotificationPageItemResponseData();
        NotificationType notificationType = null;
        if (data.notification() instanceof CommitteeApplicationCreated committeeApplicationCreated) {
            notificationPageItemResponseData.setMaintainerCommitteeApplicationCreated(new NotificationMaintainerCommitteeApplicationCreated()
                    .committeeName(committeeApplicationCreated.getCommitteeName())
            );
            notificationType = NotificationType.COMMITTEE_APPLICATION_CREATED;
        } else if (data.notification() instanceof RewardReceived) {

        } else if (data.notification() instanceof RewardCanceled) {

        } else {
            throw OnlyDustException.internalServerError("Unknown notification data type %s".formatted(data.notification().getClass().getSimpleName()));
        }

        return new NotificationPageItemResponse()
                .id(id)
                .type(notificationType)
                .status(Boolean.TRUE.equals(isRead) ? NotificationStatus.READ : NotificationStatus.UNREAD)
                .timestamp(createdAt)
                .data(notificationPageItemResponseData);
    }
}
