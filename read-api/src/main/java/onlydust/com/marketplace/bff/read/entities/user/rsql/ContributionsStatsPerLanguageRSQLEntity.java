package onlydust.com.marketplace.bff.read.entities.user.rsql;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.bff.read.entities.LanguageReadEntity;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Entity
@Immutable
@Accessors(fluent = true)
@Table(name = "contributions_stats_per_language_per_user", schema = "public")
@IdClass(ContributionsStatsPerLanguageRSQLEntity.PrimaryKey.class)
public class ContributionsStatsPerLanguageRSQLEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    Long contributorId;

    @Id
    @NonNull
    @EqualsAndHashCode.Include
    UUID languageId;

    @NonNull
    @Column(name = "contributionCount")
    Integer count;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contributorId", referencedColumnName = "githubUserId", insertable = false, updatable = false)
    AllUserRSQLEntity contributor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "languageId", referencedColumnName = "id", insertable = false, updatable = false)
    LanguageReadEntity language;

    @EqualsAndHashCode
    @NoArgsConstructor
    public static class PrimaryKey implements Serializable {
        Long contributorId;
        UUID languageId;
    }
}
