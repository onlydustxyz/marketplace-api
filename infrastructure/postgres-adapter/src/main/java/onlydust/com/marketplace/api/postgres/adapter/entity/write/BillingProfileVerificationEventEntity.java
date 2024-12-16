package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.kernel.infrastructure.postgres.EventEntity;
import onlydust.com.marketplace.kernel.model.Event;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "billing_profile_verification_outbox_events", schema = "accounting")
@EntityListeners(AuditingEntityListener.class)
public class BillingProfileVerificationEventEntity extends EventEntity {

    public BillingProfileVerificationEventEntity(Event event) {
        super(event);
    }
}
