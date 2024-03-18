package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.project.domain.model.Reward;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Data
@Table(name = "rewards", schema = "public")
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@Accessors(fluent = true, chain = true)
public class RewardEntity {
    @Id
    @NonNull UUID id;
    @NonNull UUID projectId;
    @NonNull UUID requestorId;
    @NonNull Long recipientId;

    @ManyToOne
    @NonNull CurrencyEntity currency;

    @NonNull BigDecimal amount;
    @NonNull Date requestedAt;

    @OneToMany(mappedBy = "rewardId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @NonNull List<RewardItemEntity> rewardItems;

    @Column(name = "invoice_id", insertable = false, updatable = false)
    UUID invoiceId;
    @ManyToOne
    InvoiceEntity invoice;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "rewards_receipts",
            schema = "accounting",
            joinColumns = @JoinColumn(name = "reward_id"),
            inverseJoinColumns = @JoinColumn(name = "receipt_id"))
    @Builder.Default
    Set<ReceiptEntity> receipts = new HashSet<>();

    public static RewardEntity of(Reward reward, Currency currency) {
        return RewardEntity.builder()
                .id(reward.id())
                .projectId(reward.projectId())
                .requestorId(reward.requestorId())
                .recipientId(reward.recipientId())
                .currency(CurrencyEntity.of(currency))
                .amount(reward.amount())
                .requestedAt(reward.requestedAt())
                .rewardItems(RewardItemEntity.of(reward))
                .build();
    }

    public Reward toReward() {
        return new Reward(
                id,
                projectId,
                requestorId,
                recipientId,
                amount,
                currency.toOldDomain(),
                requestedAt,
                rewardItems.stream().map(RewardItemEntity::toRewardItem).toList(),
                invoiceId != null);
    }
}
