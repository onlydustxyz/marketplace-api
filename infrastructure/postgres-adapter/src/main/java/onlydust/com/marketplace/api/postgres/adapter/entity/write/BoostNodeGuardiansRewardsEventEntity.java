package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.kernel.model.Event;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "node_guardians_rewards_boost_outbox_events", schema = "public")
@EntityListeners(AuditingEntityListener.class)
public class BoostNodeGuardiansRewardsEventEntity extends EventEntity {

    public BoostNodeGuardiansRewardsEventEntity(Event event) {
        super(event);
    }
}
