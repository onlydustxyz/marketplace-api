package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.Reward;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Value
@Table(name = "rewards", schema = "public")
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@TypeDef(name = "currency", typeClass = PostgreSQLEnumType.class)
public class RewardEntity {
    @Id
    @NonNull UUID id;
    @NonNull UUID projectId;
    @NonNull UUID requestorId;
    @NonNull Long recipientId;

    @Enumerated(EnumType.STRING)
    @Type(type = "currency")
    @NonNull CurrencyEnumEntity currency;

    @NonNull BigDecimal amount;
    @NonNull Date requestedAt;

    @OneToMany(mappedBy = "rewardId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @NonNull List<RewardItemEntity> rewardItems;

    public static RewardEntity of(Reward reward) {
        return RewardEntity.builder()
                .id(reward.id())
                .projectId(reward.projectId())
                .requestorId(reward.requestorId())
                .recipientId(reward.recipientId())
                .currency(CurrencyEnumEntity.of(reward.currency()))
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
                currency.toDomain(),
                requestedAt,
                rewardItems.stream().map(RewardItemEntity::toRewardItem).toList());
    }
}
