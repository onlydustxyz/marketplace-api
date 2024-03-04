package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.BatchPayment;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.kernel.model.UuidWrapper;
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
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "reward_to_batch_payment", schema = "public",
            joinColumns = @JoinColumn(name = "batch_payment_id")
    )
    @Column(name = "reward_id")
    List<UUID> rewardIds;
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
                .network(NetworkEnumEntity.of(batchPayment.blockchain()))
                .status(switch (batchPayment.status()) {
                    case PAID -> Status.PAID;
                    case TO_PAY -> Status.TO_PAY;
                })
                .rewardIds(batchPayment.rewardIds().stream().map(UuidWrapper::value).toList())
                .build();
    }

    public enum Status {
        TO_PAY, PAID;
    }

    public BatchPayment toDomain() {
        return BatchPayment.builder()
                .moneys(List.of())
                .blockchain(this.network.toBlockchain())
                .csv(this.csv)
                .id(BatchPayment.Id.of(this.id))
                .transactionHash(this.transactionHash)
                .rewardIds(this.rewardIds.stream().map(RewardId::of).toList())
                .status(switch (this.status) {
                    case PAID -> BatchPayment.Status.PAID;
                    case TO_PAY -> BatchPayment.Status.TO_PAY;
                })
                .build();
    }

}
