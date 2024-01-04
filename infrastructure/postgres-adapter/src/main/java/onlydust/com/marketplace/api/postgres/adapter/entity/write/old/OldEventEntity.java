package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.domain.model.OldEvent;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder
@Table(name = "events", schema = "public")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class OldEventEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long index;
  @Column(nullable = false)
  Date timestamp;
  @Column(nullable = false)
  String aggregateName;
  @Column(nullable = false)
  UUID aggregateId;
  @Type(type = "jsonb")
  @Column(columnDefinition = "jsonb", nullable = false)
  String payload;

  public static OldEventEntity of(OldEvent oldEvent) {
    return OldEventEntity.builder()
        .timestamp(new Date())
        .aggregateName(oldEvent.aggregateName)
        .aggregateId(oldEvent.aggregateId)
        .payload(oldEvent.payload)
        .build();
  }
}
