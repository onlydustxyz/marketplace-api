package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.PayableReward;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Table(name = "batch_payments", schema = "accounting")
@EntityListeners(AuditingEntityListener.class)
public class BatchPaymentEntity {
    @Id
    @EqualsAndHashCode.Include
    UUID id;
    String csv;
    String transactionHash;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "network")
    NetworkEnumEntity network;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "batch_payment_status")
    Payment.Status status;
    @OneToMany(mappedBy = "batchPayment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    List<BatchPaymentRewardEntity> rewards;

    @CreationTimestamp
    @Column(name = "tech_created_at", nullable = false, updatable = false)
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "tech_updated_at", nullable = false)
    private Date updatedAt;

    public static BatchPaymentEntity fromDomain(final Payment payment) {
        return BatchPaymentEntity.builder()
                .id(payment.id().value())
                .csv(payment.csv())
                .transactionHash(payment.transactionHash())
                .network(NetworkEnumEntity.of(payment.network()))
                .status(payment.status())
                .rewards(payment.rewards().stream().map(r -> BatchPaymentRewardEntity.from(payment.id(), r)).toList())
                .build();
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
                                r.reward().invoice().toDomain().billingProfileSnapshot()
                        )).toList())
                .status(this.status)
                .createdAt(this.createdAt)
                .build();
    }

}
