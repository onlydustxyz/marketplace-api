package onlydust.com.marketplace.api.read.entities.user.rsql;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.read.entities.ecosystem.EcosystemReadEntity;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Entity
@Immutable
@Accessors(fluent = true)
@Table(name = "contributions_stats_per_ecosystem_per_user", schema = "public")
@IdClass(ContributionsStatsPerEcosystemRSQLEntity.PrimaryKey.class)
public class ContributionsStatsPerEcosystemRSQLEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    Long contributorId;

    @Id
    @NonNull
    @EqualsAndHashCode.Include
    UUID ecosystemId;

    @NonNull
    @Column(name = "contributionCount")
    Integer count;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contributorId", referencedColumnName = "githubUserId", insertable = false, updatable = false)
    AllUserRSQLEntity contributor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ecosystemId", referencedColumnName = "id", insertable = false, updatable = false)
    EcosystemReadEntity ecosystem;

    @EqualsAndHashCode
    @NoArgsConstructor
    public static class PrimaryKey implements Serializable {
        Long contributorId;
        UUID ecosystemId;
    }
}
