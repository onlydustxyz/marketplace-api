package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.BatchPayment;
import onlydust.com.marketplace.accounting.domain.model.PayableReward;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Table(name = "batch_payments", schema = "accounting")
@EntityListeners(AuditingEntityListener.class)
@TypeDef(name = "batch_payment_status", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "network", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class BatchPaymentEntity {

    @Id
    UUID id;
    String csv;
    String transactionHash;
    @Enumerated(EnumType.STRING)
    @Type(type = "network")
    NetworkEnumEntity network;
    @Type(type = "batch_payment_status")
    @Enumerated(EnumType.STRING)
    Status status;
    @OneToMany(mappedBy = "batchPayment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    List<BatchPaymentRewardEntity> rewards;
    @ManyToMany
    @JoinTable(
            name = "batch_payment_invoices",
            schema = "accounting",
            joinColumns = @JoinColumn(name = "batch_payment_id"),
            inverseJoinColumns = @JoinColumn(name = "invoice_id")
    )
    List<InvoiceEntity> invoices;
    @CreationTimestamp
    @Column(name = "tech_created_at", nullable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "tech_updated_at", nullable = false)
    @EqualsAndHashCode.Exclude
    private Date updatedAt;

    public static BatchPaymentEntity fromDomain(final BatchPayment batchPayment) {
        return BatchPaymentEntity.builder()
                .id(batchPayment.id().value())
                .csv(batchPayment.csv())
                .transactionHash(batchPayment.transactionHash())
                .network(NetworkEnumEntity.of(batchPayment.network()))
                .status(switch (batchPayment.status()) {
                    case PAID -> Status.PAID;
                    case TO_PAY -> Status.TO_PAY;
                })
                .invoices(batchPayment.invoices().stream().map(InvoiceEntity::fromDomain).toList())
                .rewards(batchPayment.rewards().stream().map(r -> BatchPaymentRewardEntity.from(batchPayment.id(), r)).toList())
                .build();
    }

    public enum Status {
        TO_PAY, PAID;
    }

    public BatchPayment toDomain() {
        return BatchPayment.builder()
                .network(this.network.toNetwork())
                .csv(this.csv)
                .id(BatchPayment.Id.of(this.id))
                .transactionHash(this.transactionHash)
                .rewards(this.rewards.stream().map(r ->
                        new PayableReward(
                                RewardId.of(r.rewardId()),
                                r.reward().currency().toDomain().forNetwork(this.network.toNetwork()),
                                PositiveAmount.of(r.amount())
                        )).toList())
                .invoices(this.invoices.stream().map(InvoiceEntity::toDomain).toList())
                .status(switch (this.status) {
                    case PAID -> BatchPayment.Status.PAID;
                    case TO_PAY -> BatchPayment.Status.TO_PAY;
                })
                .createdAt(this.createdAt)
                .build();
    }

}
