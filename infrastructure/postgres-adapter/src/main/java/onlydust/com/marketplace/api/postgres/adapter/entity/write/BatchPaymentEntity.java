package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.PayableReward;
import onlydust.com.marketplace.accounting.domain.model.Payment;
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

    @CreationTimestamp
    @Column(name = "tech_created_at", nullable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "tech_updated_at", nullable = false)
    @EqualsAndHashCode.Exclude
    private Date updatedAt;

    public static BatchPaymentEntity fromDomain(final Payment payment) {
        return BatchPaymentEntity.builder()
                .id(payment.id().value())
                .csv(payment.csv())
                .transactionHash(payment.transactionHash())
                .network(NetworkEnumEntity.of(payment.network()))
                .status(switch (payment.status()) {
                    case PAID -> Status.PAID;
                    case TO_PAY -> Status.TO_PAY;
                })
                .rewards(payment.rewards().stream().map(r -> BatchPaymentRewardEntity.from(payment.id(), r)).toList())
                .build();
    }

    public enum Status {
        TO_PAY, PAID;

        public static Status of(Payment.Status status) {
            return switch (status) {
                case TO_PAY -> TO_PAY;
                case PAID -> PAID;
            };
        }

        public Payment.Status toDomain() {
            return switch (this) {
                case TO_PAY -> Payment.Status.TO_PAY;
                case PAID -> Payment.Status.PAID;
            };
        }
    }

    public Payment toDomain() {
        return Payment.builder()
                .network(this.network.toNetwork())
                .csv(this.csv)
                .id(Payment.Id.of(this.id))
                .transactionHash(this.transactionHash)
                .rewards(this.rewards.stream().map(r ->
                        PayableReward.of(
                                RewardId.of(r.rewardId()),
                                r.reward().currency().toDomain().forNetwork(this.network.toNetwork()),
                                PositiveAmount.of(r.amount()),
                                r.reward().invoice().toDomain()
                        )).toList())
                .status(this.status.toDomain())
                .createdAt(this.createdAt)
                .build();
    }

}
