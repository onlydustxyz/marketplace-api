package onlydust.com.marketplace.bff.read.entities.user.rsql;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.backoffice.api.contract.model.UserSearchPageItemResponseLanguageInner;
import onlydust.com.marketplace.bff.read.entities.LanguageReadEntity;
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
@Table(name = "united_stats_per_language_per_user", schema = "public")
@IdClass(UnitedStatsPerLanguagePerUserRSQLEntity.PrimaryKey.class)
public class UnitedStatsPerLanguagePerUserRSQLEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    Long githubUserId;
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    UUID languageId;

    @NonNull
    Integer contributionCount;
    @NonNull
    @Column(name = "rewardCount")
    Integer rewardCount;
    @NonNull
    @Column(name = "pendingRequestRewardCount")
    Integer pendingRewardCount;
    @NonNull
    @Column(name = "usdTotal")
    BigDecimal totalUsdAmount;
    @NonNull
    @Column(name = "maxUsd")
    BigDecimal maxUsdAmount;
    @NonNull
    Integer rank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "githubUserId", referencedColumnName = "githubUserId", insertable = false, updatable = false)
    AllUserRSQLEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "languageId", referencedColumnName = "id", insertable = false, updatable = false)
    LanguageReadEntity language;

    public UserSearchPageItemResponseLanguageInner toDto() {
        return new UserSearchPageItemResponseLanguageInner()
                .id(languageId)
                .name(language.name())
                .contributionCount(contributionCount)
                .rewardCount(rewardCount)
                .pendingRewardCount(pendingRewardCount)
                .totalUsdAmount(totalUsdAmount)
                .maxUsdAmount(maxUsdAmount)
                .rank(rank);
    }

    @EqualsAndHashCode
    @NoArgsConstructor
    public static class PrimaryKey implements Serializable {
        Long githubUserId;
        UUID languageId;
    }
}
