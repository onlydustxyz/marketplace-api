package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;


import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder(toBuilder = true)
@Table(name = "payment_requests", schema = "public")
@TypeDef(name = "currency", typeClass = PostgreSQLEnumType.class)
public class PaymentRequestEntity {

    @Id
    @Column(name = "id", nullable = false)
    UUID id;
    @Column(name = "requestor_id", nullable = false)
    UUID requestorId;
    @Column(name = "recipient_id", nullable = false)
    Long recipientId;
    @Column(name = "requested_at", nullable = false)
    Date requestedAt;
    @Column(name = "amount", nullable = false)
    BigDecimal amount;
    @Column(name = "invoice_received_at")
    Date invoiceReceivedAt;
    @Column(name = "hours_worked", nullable = false)
    Integer hoursWorked;
    @Column(name = "project_id", nullable = false)
    UUID projectId;
    @Enumerated(EnumType.STRING)
    @Type(type = "currency")
    @Column(name = "currency", nullable = false)
    CurrencyEnumEntity currency;
}
