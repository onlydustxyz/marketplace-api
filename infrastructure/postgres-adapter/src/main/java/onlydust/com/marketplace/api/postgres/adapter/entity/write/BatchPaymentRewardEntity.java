package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.model.PayableReward;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Accessors(fluent = true)
@Table(name = "batch_payment_rewards", schema = "accounting")
@IdClass(BatchPaymentRewardEntity.PrimaryKey.class)
@EntityListeners(AuditingEntityListener.class)
public class BatchPaymentRewardEntity {

    @Id
    UUID batchPaymentId;
    @Id
    UUID rewardId;
    BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batchPaymentId", referencedColumnName = "id", insertable = false, updatable = false)
    BatchPaymentEntity batchPayment;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rewardId", referencedColumnName = "id", insertable = false, updatable = false)
    RewardEntity reward;

    @EqualsAndHashCode
    @AllArgsConstructor
    @Data
    @NoArgsConstructor(force = true)
    public static class PrimaryKey implements Serializable {
        UUID batchPaymentId;
        UUID rewardId;
    }

    public static BatchPaymentRewardEntity from(@NonNull Payment.Id batchPaymentId, @NonNull PayableReward reward) {
        return BatchPaymentRewardEntity.builder()
                .batchPaymentId(batchPaymentId.value())
                .rewardId(reward.id().value())
                .amount(reward.amount().getValue())
                .build();
    }
}
