package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.CurrencyView;
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
    @NonNull UUID currencyId;
    @NonNull BigDecimal amount;
    @NonNull Date requestedAt;
    UUID invoiceId;

    @OneToMany(mappedBy = "rewardId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @NonNull List<RewardItemEntity> rewardItems;

    @Column(name = "billingProfileId", insertable = false, updatable = false)
    UUID billingProfileId;
    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "reward_id", insertable = false, updatable = false)
    RewardStatusEntity status;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "rewards_receipts",
            schema = "accounting",
            joinColumns = @JoinColumn(name = "reward_id"),
            inverseJoinColumns = @JoinColumn(name = "receipt_id"))
    @Builder.Default
    Set<ReceiptEntity> receipts = new HashSet<>();

    public static RewardEntity of(Reward reward) {
        return RewardEntity.builder()
                .id(reward.id())
                .projectId(reward.projectId())
                .requestorId(reward.requestorId())
                .recipientId(reward.recipientId())
                .currencyId(reward.currencyId().value())
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
                CurrencyView.Id.of(currencyId),
                requestedAt,
                rewardItems.stream().map(RewardItemEntity::toRewardItem).toList(),
                invoiceId != null);
    }
}
