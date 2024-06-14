package onlydust.com.marketplace.bff.read.entities.user.rsql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Entity
@Immutable
@Accessors(fluent = true)
@Table(name = "received_rewards_stats_per_user", schema = "public")
public class ReceivedRewardStatsRSQLEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    Long recipientId;

    @NonNull
    @Column(name = "rewardCount")
    Integer count;
    @NonNull
    @Column(name = "usdTotal")
    BigDecimal totalUsdAmount;
    @NonNull
    @Column(name = "maxUsd")
    BigDecimal maxUsdAmount;
    @NonNull
    @Column(name = "pendingRequestRewardCount")
    Integer pendingCount;
}
