package onlydust.com.marketplace.api.read.entities.billing_profile;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.BatchPaymentDetailsResponse;
import onlydust.com.backoffice.api.contract.model.BatchPaymentResponse;
import onlydust.com.backoffice.api.contract.model.BatchPaymentStatus;
import onlydust.com.backoffice.api.contract.model.TotalMoneyWithUsdEquivalentResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import onlydust.com.marketplace.api.read.entities.reward.RewardReadEntity;
import onlydust.com.marketplace.api.read.utils.Arithmetic;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.*;
import static onlydust.com.marketplace.api.read.mapper.NetworkMapper.map;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
@Table(name = "batch_payments", schema = "accounting")
@Immutable
public class BatchPaymentReadEntity {
    @Id
    @EqualsAndHashCode.Include
    UUID id;

    String csv;
    String transactionHash;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "network")
    @NonNull
    NetworkEnumEntity network;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "batch_payment_status")
    BatchPaymentStatus status;

    @Column(name = "tech_created_at", nullable = false, updatable = false)
    ZonedDateTime createdAt;

    @ManyToMany
    @JoinTable(
            name = "batch_payment_rewards",
            schema = "accounting",
            joinColumns = @JoinColumn(name = "batchPaymentId"),
            inverseJoinColumns = @JoinColumn(name = "rewardId")
    )
    @NonNull
    Set<RewardReadEntity> rewards;

    public List<TotalMoneyWithUsdEquivalentResponse> totalsPerCurrency() {
        return rewards.stream()
                .collect(groupingBy(r -> r.currency().id(),
                        mapping(RewardReadEntity::toTotalMoneyWithUsdEquivalentResponse,
                                reducing(null, Arithmetic::sum))))
                .values().stream().toList();
    }

    public BigDecimal totalUsdEquivalentAfterTax() {
        return rewards.stream()
                .map(RewardReadEntity::usdEquivalentAfterTax)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalUsdEquivalent() {
        return rewards.stream()
                .map(RewardReadEntity::usdEquivalent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BatchPaymentResponse toResponse() {
        return new BatchPaymentResponse()
                .id(id)
                .createdAt(createdAt)
                .status(status)
                .network(map(network))
                .rewardCount((long) rewards.size())
                .totalsPerCurrency(totalsPerCurrency())
                .totalUsdEquivalent(totalUsdEquivalent());
    }

    public BatchPaymentDetailsResponse toDetailsResponse() {
        return new BatchPaymentDetailsResponse()
                .id(id)
                .createdAt(createdAt)
                .status(status)
                .csv(csv)
                .network(map(network))
                .transactionHash(transactionHash)
                .rewardCount((long) rewards.size())
                .rewards(rewards.stream().map(RewardReadEntity::toShortResponse).toList())
                .totalsPerCurrency(totalsPerCurrency())
                .totalUsdEquivalent(totalUsdEquivalentAfterTax());
    }
}
