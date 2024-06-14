package onlydust.com.marketplace.bff.read.entities.user.rsql;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.bff.read.entities.project.ProjectReadEntity;
import onlydust.com.marketplace.bff.read.entities.user.UserReadEntity;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Set;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Entity
@Immutable
@Accessors(fluent = true)
@Table(name = "all_users", schema = "iam")
public class AllUserRSQLEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    Long githubUserId;
    UUID userId;
    @NonNull
    String login;
    @NonNull
    String avatarUrl;
    String email;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    UserReadEntity registered;

    @ManyToMany
    @JoinTable(
            name = "projects_contributors",
            schema = "public",
            joinColumns = @JoinColumn(name = "githubUserId", referencedColumnName = "githubUserId"),
            inverseJoinColumns = @JoinColumn(name = "projectId", referencedColumnName = "id")
    )
    Set<ProjectReadEntity> contributedProjects;

    // Contributions ===========================================================================
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "githubUserId", insertable = false, updatable = false)
    ContributionsStatsRSQLEntity contributionsStats;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "contributor")
    Set<ContributionsStatsPerLanguageRSQLEntity> contributionsStatsPerLanguages;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "contributor")
    Set<ContributionsStatsPerEcosystemRSQLEntity> contributionsStatsPerEcosystems;
    // =========================================================================================

    // Rewards =================================================================================
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "githubUserId", insertable = false, updatable = false)
    ReceivedRewardStatsRSQLEntity receivedRewardsStats;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "recipient")
    Set<ReceivedRewardStatsPerLanguageRSQLEntity> receivedRewardsStatsPerLanguages;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "recipient")
    Set<ReceivedRewardStatsPerEcosystemRSQLEntity> receivedRewardsStatsPerEcosystems;
    // =========================================================================================

    // Ranking =================================================================================
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "githubUserId", insertable = false, updatable = false)
    GlobalUsersRanksRSQLEntity globalUsersRank;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "contributor")
    Set<PerLanguageUsersRanksRSQLEntity> perLanguageUsersRanks;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "contributor")
    Set<PerEcosystemUsersRanksRSQLEntity> perEcosystemUsersRanks;
    // =========================================================================================


    public UserSearchPageItemResponse toBoPageItemResponse() {
        return new UserSearchPageItemResponse()
                .id(userId)
                .githubUserId(githubUserId)
                .login(login)
                .avatarUrl(avatarUrl)
                .email(email)
                .lastSeenAt(ofNullable(registered).map(u -> u.lastSeenAt().toInstant().atZone(ZoneId.systemDefault())).orElse(null))
                .signedUpAt(ofNullable(registered).map(u -> u.createdAt().toInstant().atZone(ZoneId.systemDefault())).orElse(null))
                .globalContributions(new UserSearchContributions()
                        .count(ofNullable(contributionsStats).map(ContributionsStatsRSQLEntity::count).orElse(0))
                )
                .perLanguageContributions(contributionsStatsPerLanguages.stream()
                        .map(c -> new UserSearchPageItemResponsePerLanguageContributionsInner()
                                .name(c.language().name())
                                .count(c.count()))
                        .toList()
                )
                .perEcosystemContributions(contributionsStatsPerEcosystems.stream()
                        .map(c -> new UserSearchPageItemResponsePerEcosystemContributionsInner()
                                .name(c.ecosystem().name())
                                .count(c.count()))
                        .toList()
                )
                .globalReceivedRewards(new UserSearchReceivedRewards()
                        .count(ofNullable(receivedRewardsStats).map(ReceivedRewardStatsRSQLEntity::count).orElse(0))
                        .pendingCount(ofNullable(receivedRewardsStats).map(ReceivedRewardStatsRSQLEntity::pendingCount).orElse(0))
                        .totalUsdAmount(ofNullable(receivedRewardsStats).map(ReceivedRewardStatsRSQLEntity::totalUsdAmount).orElse(BigDecimal.ZERO))
                        .maxUsdAmount(ofNullable(receivedRewardsStats).map(ReceivedRewardStatsRSQLEntity::maxUsdAmount).orElse(BigDecimal.ZERO))
                )
                .perLanguageReceivedRewards(receivedRewardsStatsPerLanguages.stream()
                        .map(r -> new UserSearchPageItemResponsePerLanguageReceivedRewardsInner()
                                .name(r.language().name())
                                .count(r.count())
                                .pendingCount(r.pendingCount())
                                .totalUsdAmount(r.totalUsdAmount())
                                .maxUsdAmount(r.maxUsdAmount())
                        )
                        .toList()
                )
                .perEcosystemReceivedRewards(receivedRewardsStatsPerEcosystems.stream()
                        .map(r -> new UserSearchPageItemResponsePerEcosystemReceivedRewardsInner()
                                .name(r.ecosystem().name())
                                .count(r.count())
                                .pendingCount(r.pendingCount())
                                .totalUsdAmount(r.totalUsdAmount())
                                .maxUsdAmount(r.maxUsdAmount())
                        )
                        .toList()
                )
                .globalRank(new UserSearchRanking()
                        .value(ofNullable(globalUsersRank).map(GlobalUsersRanksRSQLEntity::value).map(v -> v.intValue()).orElse(null))
                )
                .perLanguageRank(perLanguageUsersRanks.stream()
                        .map(r -> new UserSearchPageItemResponsePerLanguageRankInner()
                                .name(r.language().name())
                                .value(ofNullable(r.value()).map(v -> v.intValue()).orElse(null))
                        )
                        .toList()
                )
                .perEcosystemRank(perEcosystemUsersRanks.stream()
                        .map(r -> new UserSearchPageItemResponsePerEcosystemRankInner()
                                .name(r.ecosystem().name())
                                .value(ofNullable(r.value()).map(v -> v.intValue()).orElse(null))
                        )
                        .toList()
                )
                ;
    }
}
