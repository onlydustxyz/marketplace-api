package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.EventIdResolver;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

@MappedSuperclass
@NoArgsConstructor
@EqualsAndHashCode
@Data
public abstract class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    Payload payload;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "outbox_event_status")
    Status status;

    String error;

    public EventEntity(Event event) {
        this.payload = new Payload(event);
        this.status = Status.PENDING;
    }

    public OutboxPort.IdentifiableEvent toIdentifiableEvent() {
        return new OutboxPort.IdentifiableEvent(id, payload.getEvent());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Payload {

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
        @JsonTypeIdResolver(EventIdResolver.class)
        private Event event;
    }

    public enum Status {
        PENDING, PROCESSED, FAILED, SKIPPED
    }
}
