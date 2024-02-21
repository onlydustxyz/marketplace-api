package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.kernel.model.Event;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "tracking_outbox_events", schema = "public")
@EntityListeners(AuditingEntityListener.class)
public class TrackingEventEntity extends EventEntity {

    public TrackingEventEntity(Event event) {
        super(event);
    }
}
