package onlydust.com.marketplace.api.read.entities.user.rsql;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.read.entities.LanguageReadEntity;
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
@Table(name = "received_rewards_stats_per_language_per_user", schema = "public")
@IdClass(ReceivedRewardStatsPerLanguageRSQLEntity.PrimaryKey.class)
public class ReceivedRewardStatsPerLanguageRSQLEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    Long recipientId;

    @Id
    @NonNull
    @EqualsAndHashCode.Include
    UUID languageId;

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
    @JoinColumn(name = "languageId", referencedColumnName = "id", insertable = false, updatable = false)
    LanguageReadEntity language;


    @EqualsAndHashCode
    @NoArgsConstructor
    public static class PrimaryKey implements Serializable {
        Long recipientId;
        UUID languageId;
    }
}
