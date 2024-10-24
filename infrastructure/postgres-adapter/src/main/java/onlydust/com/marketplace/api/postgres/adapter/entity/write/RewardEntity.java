package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Reward;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
    @NonNull
    UUID id;
    @NonNull
    UUID projectId;
    @NonNull
    UUID requestorId;
    @NonNull
    Long recipientId;
    @NonNull
    UUID currencyId;
    @NonNull
    BigDecimal amount;
    @NonNull
    Date requestedAt;

    public RewardEntity(@NonNull UUID id,
                        @NonNull UUID projectId,
                        @NonNull UUID requestorId,
                        @NonNull Long recipientId,
                        @NonNull UUID currencyId,
                        @NonNull BigDecimal amount,
                        @NonNull Date requestedAt,
                        @NonNull Set<RewardItemEntity> rewardItems,
                        UUID billingProfileId) {
        this.id = id;
        this.projectId = projectId;
        this.requestorId = requestorId;
        this.recipientId = recipientId;
        this.currencyId = currencyId;
        this.amount = amount;
        this.requestedAt = requestedAt;
        this.rewardItems = rewardItems;
        this.billingProfileId = billingProfileId;
    }

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "invoiceId")
    InvoiceEntity invoice;

    @OneToMany(mappedBy = "rewardId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @NonNull
    Set<RewardItemEntity> rewardItems;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currencyId", insertable = false, updatable = false)
    CurrencyEntity currency;

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
                .id(reward.id().value())
                .projectId(reward.projectId().value())
                .requestorId(reward.requestorId().value())
                .recipientId(reward.recipientId())
                .currencyId(reward.currencyId().value())
                .amount(reward.amount())
                .requestedAt(reward.requestedAt())
                .rewardItems(RewardItemEntity.of(reward))
                .build();
    }

    public Reward toReward() {
        return new Reward(
                RewardId.of(id),
                ProjectId.of(projectId),
                UserId.of(requestorId),
                recipientId,
                amount,
                CurrencyView.Id.of(currencyId),
                requestedAt,
                rewardItems.stream().map(RewardItemEntity::toRewardItem).toList());
    }
}
