package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.api.domain.model.notification.Event;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@Data
@Table(name = "notification_outbox_events", schema = "public")
@EntityListeners(AuditingEntityListener.class)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@TypeDef(name = "outbox_event_status", typeClass = PostgreSQLEnumType.class)
public class NotificationEntity implements EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", nullable = false)
    private Payload payload;

    @Enumerated(EnumType.STRING)
    @Type(type = "outbox_event_status")
    private EventEntity.Status status;

    private String error;

    @EqualsAndHashCode.Exclude
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @EqualsAndHashCode.Exclude
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    public NotificationEntity(Event event) {
        this.payload = new Payload(event);
        this.status = Status.PENDING;
    }

    @Override
    public Event getEvent() {
        return payload.getEvent();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Payload implements Serializable {

        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "className")
        private Event event;
    }
}
