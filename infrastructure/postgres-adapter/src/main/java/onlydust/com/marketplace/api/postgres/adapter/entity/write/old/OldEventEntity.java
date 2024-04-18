package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.OldEvent;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Date;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder
@Table(name = "events", schema = "public")
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
    @JdbcTypeCode(SqlTypes.JSON)
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
