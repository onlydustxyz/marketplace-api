package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.EventIdResolver;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.Instant;

@MappedSuperclass
@NoArgsConstructor
@EqualsAndHashCode
@Data
public abstract class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    Payload payload;

    @Enumerated(EnumType.STRING)
    @Type(PostgreSQLEnumType.class)
    @Column(columnDefinition = "outbox_event_status")
    Status status;

    @Column(name = "group_key")
    String group;

    String error;

    @EqualsAndHashCode.Exclude
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @EqualsAndHashCode.Exclude
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    public EventEntity(Event event) {
        this.payload = new Payload(event);
        this.status = Status.PENDING;
        this.group = event.group().orElse(null);
    }

    public OutboxPort.IdentifiableEvent toIdentifiableEvent() {
        return new OutboxPort.IdentifiableEvent(id, payload.getEvent());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Payload implements Serializable {

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
        @JsonTypeIdResolver(EventIdResolver.class)
        private Event event;
    }

    public enum Status {
        PENDING, PROCESSED, FAILED, SKIPPED
    }
}
