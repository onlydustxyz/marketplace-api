package onlydust.com.marketplace.bff.read.entities.user.rsql;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.bff.read.entities.ecosystem.EcosystemReadEntity;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Entity
@Immutable
@Accessors(fluent = true)
@Table(name = "received_rewards_stats_per_ecosystem_per_user", schema = "public")
@IdClass(ReceivedRewardStatsPerEcosystemRSQLEntity.PrimaryKey.class)
public class ReceivedRewardStatsPerEcosystemRSQLEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    Long recipientId;

    @Id
    @NonNull
    @EqualsAndHashCode.Include
    UUID ecosystemId;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipientId", referencedColumnName = "githubUserId", insertable = false, updatable = false)
    AllUserRSQLEntity recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ecosystemId", referencedColumnName = "id", insertable = false, updatable = false)
    EcosystemReadEntity ecosystem;


    @EqualsAndHashCode
    @NoArgsConstructor
    public static class PrimaryKey implements Serializable {
        Long recipientId;
        UUID ecosystemId;
    }
}