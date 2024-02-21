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
@Table(name = "notification_outbox_events", schema = "public")
@EntityListeners(AuditingEntityListener.class)
public class NotificationEventEntity extends EventEntity {

    public NotificationEventEntity(Event event) {
        super(event);
    }
}
