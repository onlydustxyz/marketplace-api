package onlydust.com.marketplace.api.read.entities.user.rsql;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.UserSearchPageItemResponseLanguageInner;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
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
    @Column(name = "languageId")
    UUID id;

    @NonNull
    @Column(name = "languageName")
    String name;
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

    public UserSearchPageItemResponseLanguageInner toDto() {
        return new UserSearchPageItemResponseLanguageInner()
                .id(id)
                .name(name)
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
        UUID id;
    }
}
