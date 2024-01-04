package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.domain.model.notification.Event;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "indexer_outbox_events", schema = "public")
@EntityListeners(AuditingEntityListener.class)
public class IndexerEventEntity extends EventEntity {

  public IndexerEventEntity(Event event) {
    super(event);
  }
}
