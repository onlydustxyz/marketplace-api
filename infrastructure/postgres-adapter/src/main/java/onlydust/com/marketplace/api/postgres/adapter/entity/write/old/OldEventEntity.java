package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import onlydust.com.marketplace.api.domain.model.OldEvent;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

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
