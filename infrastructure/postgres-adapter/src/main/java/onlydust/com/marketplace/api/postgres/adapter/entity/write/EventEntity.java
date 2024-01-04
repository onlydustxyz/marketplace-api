package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import java.io.Serializable;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.domain.model.notification.Event;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;

@MappedSuperclass
@NoArgsConstructor
@EqualsAndHashCode
@Data
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@TypeDef(name = "outbox_event_status", typeClass = PostgreSQLEnumType.class)
public abstract class EventEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Type(type = "jsonb")
  @Column(columnDefinition = "jsonb", nullable = false)
  Payload payload;

  @Enumerated(EnumType.STRING)
  @Type(type = "outbox_event_status")
  Status status;

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
  }

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

  public enum Status {
    PENDING, PROCESSED, FAILED
  }
}
